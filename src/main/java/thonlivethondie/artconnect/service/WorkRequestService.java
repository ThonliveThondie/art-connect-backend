package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import thonlivethondie.artconnect.common.UserType;
import thonlivethondie.artconnect.common.WorkRequestStatus;
import thonlivethondie.artconnect.common.exception.BadRequestException;
import thonlivethondie.artconnect.common.exception.ErrorCode;
import thonlivethondie.artconnect.dto.WorkRequestCreateRequestDto;
import thonlivethondie.artconnect.dto.WorkRequestResponseDto;
import thonlivethondie.artconnect.dto.WorkRequestSimpleDto;
import thonlivethondie.artconnect.dto.AcceptedProjectSimpleDto;
import thonlivethondie.artconnect.dto.ProjectSimpleDetailDto;
import thonlivethondie.artconnect.dto.CompletedProjectForBusinessOwnerDto;
import thonlivethondie.artconnect.dto.CompletedProjectForDesignerDto;
import thonlivethondie.artconnect.entity.Store;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.entity.WorkRequest;
import thonlivethondie.artconnect.entity.WorkRequestImage;
import thonlivethondie.artconnect.repository.StoreRepository;
import thonlivethondie.artconnect.repository.UserRepository;
import thonlivethondie.artconnect.repository.WorkRequestRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkRequestService {

    private final WorkRequestRepository workRequestRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final AwsS3Service awsS3Service;

    /**
     * 작업의뢰서 생성
     */
    @Transactional
    public WorkRequestResponseDto createWorkRequest(
            WorkRequestCreateRequestDto requestDto,
            List<MultipartFile> images,
            Long businessOwnerId,
            Long designerId) {

        // 1. 유효성 검증
        User businessOwner = validateBusinessOwner(businessOwnerId);
        User designer = validateDesigner(designerId);

        // 2. 소상공인의 매장 자동 조회
        Store store = getBusinessOwnerStore(businessOwnerId);

        // 3. WorkRequest 엔티티 생성
        WorkRequest workRequest = createWorkRequestEntity(requestDto, businessOwner, designer, store);

        // 4. 디자인 카테고리 설정
        workRequest.setDesignCategories(requestDto.designCategories());

        // 5. WorkRequest 저장
        WorkRequest savedWorkRequest = workRequestRepository.save(workRequest);

        // 6. 이미지 업로드 및 저장 (있는 경우)
        log.info("이미지 업로드 시작 - images: {}, isEmpty: {}",
                images != null ? images.size() : "null",
                images != null ? images.isEmpty() : "null");

        if (images != null && !images.isEmpty()) {
            uploadAndSaveImages(images, savedWorkRequest);
            log.info("이미지 업로드 완료 - 저장된 이미지 수: {}", savedWorkRequest.getWorkRequestImages().size());
        } else {
            log.info("업로드할 이미지가 없습니다.");
        }

        log.info("작업의뢰서가 생성되었습니다. ID: {}, 의뢰자: {}, 디자이너: {}",
                savedWorkRequest.getId(), businessOwner.getNickname(), designer.getNickname());

        return WorkRequestResponseDto.from(savedWorkRequest);
    }

    private User validateBusinessOwner(Long businessOwnerId) {
        User businessOwner = userRepository.findById(businessOwnerId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));

        if (businessOwner.getUserType() != UserType.BUSINESS_OWNER) {
            throw new BadRequestException(ErrorCode.INVALID_USER_TYPE);
        }

        return businessOwner;
    }

    private User validateDesigner(Long designerId) {
        User designer = userRepository.findById(designerId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));

        if (designer.getUserType() != UserType.DESIGNER) {
            throw new BadRequestException(ErrorCode.INVALID_USER_TYPE);
        }

        return designer;
    }

    private Store getBusinessOwnerStore(Long businessOwnerId) {
        return storeRepository.findByUserId(businessOwnerId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.STORE_NOT_FOUND));
    }

    private WorkRequest createWorkRequestEntity(
            WorkRequestCreateRequestDto requestDto,
            User businessOwner,
            User designer,
            Store store) {

        return WorkRequest.builder()
                .businessOwner(businessOwner)
                .designer(designer)
                .store(store)
                .projectTitle(requestDto.projectTitle())
                .endDate(requestDto.endDate())
                .budget(requestDto.budget())
                .productService(requestDto.productService())
                .targetCustomers(requestDto.targetCustomers())
                .nowStatus(requestDto.nowStatus())
                .goal(requestDto.goal())
                .additionalDescription(requestDto.additionalDescription())
                .additionalRequirement(requestDto.additionalRequirement())
                .status(WorkRequestStatus.PROPOSAL)
                .build();
    }

    /**
     * 이미지 업로드 및 저장
     */
    private void uploadAndSaveImages(List<MultipartFile> images, WorkRequest workRequest) {
        try {
            log.info("이미지 업로드 처리 시작 - 이미지 개수: {}", images.size());

            // 빈 파일 필터링
            List<MultipartFile> validImages = images.stream()
                    .filter(image -> !image.isEmpty())
                    .toList();

            log.info("유효한 이미지 개수: {}", validImages.size());

            if (validImages.isEmpty()) {
                log.warn("업로드할 유효한 이미지가 없습니다.");
                return;
            }

            // S3에 이미지들 일괄 업로드
            List<String> imageUrls = awsS3Service.uploadFile(validImages);
            log.info("S3 업로드 완료 - 업로드된 URL 개수: {}", imageUrls.size());

            // 업로드된 이미지 URL들과 원본 파일들을 매핑하여 WorkRequestImage 엔티티 생성
            for (int i = 0; i < validImages.size() && i < imageUrls.size(); i++) {
                MultipartFile image = validImages.get(i);
                String imageUrl = imageUrls.get(i);

                log.info("이미지 엔티티 생성 - 파일명: {}, URL: {}, 크기: {}",
                        image.getOriginalFilename(), imageUrl, image.getSize());

                WorkRequestImage workRequestImage = WorkRequestImage.builder()
                        .workRequest(workRequest)
                        .imageName(image.getOriginalFilename())
                        .imageUrl(imageUrl)
                        .imageSize(image.getSize())
                        .imageType(image.getContentType())
                        .build();

                workRequest.getWorkRequestImages().add(workRequestImage);
            }

            log.info("이미지 엔티티 생성 완료 - 총 생성된 엔티티 수: {}", workRequest.getWorkRequestImages().size());

        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            throw new BadRequestException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    /**
     * 디자이너가 받은 의뢰서 목록 조회
     */
    public List<WorkRequestResponseDto> getWorkRequestsForDesigner(Long designerId) {
        User designer = validateDesigner(designerId);

        List<WorkRequest> workRequests = workRequestRepository.findByDesignerOrderByCreateDateDesc(designer);

        return workRequests.stream()
                .map(WorkRequestResponseDto::from)
                .toList();
    }

    /**
     * 소상공인이 보낸 의뢰서 목록 조회
     */
    public List<WorkRequestResponseDto> getWorkRequestsForBusinessOwner(Long businessOwnerId) {
        User businessOwner = validateBusinessOwner(businessOwnerId);

        List<WorkRequest> workRequests = workRequestRepository.findByBusinessOwnerOrderByCreateDateDesc(businessOwner);

        return workRequests.stream()
                .map(WorkRequestResponseDto::from)
                .toList();
    }

    /**
     * 디자이너가 받은 의뢰서 간소화된 목록 조회
     * 목록 조회 시 필요한 핵심 정보만 반환 (프로젝트 제목, 매장명, 예산)
     */
    public List<WorkRequestSimpleDto> getSimpleWorkRequestsForDesigner(Long designerId) {
        User designer = validateDesigner(designerId);

        List<WorkRequest> workRequests = workRequestRepository.findByDesignerOrderByCreateDateDesc(designer);

        return workRequests.stream()
                .map(WorkRequestSimpleDto::from)
                .toList();
    }

    /**
     * 디자이너가 작업의뢰서 거절 (삭제)
     * 디자이너만 자신이 받은 의뢰서를 삭제할 수 있음
     */
    @Transactional
    public void deleteWorkRequestByDesigner(Long workRequestId, Long designerId) {
        // 1. 디자이너 유효성 검증
        User designer = validateDesigner(designerId);

        // 2. 작업의뢰서 존재 여부 확인
        WorkRequest workRequest = workRequestRepository.findById(workRequestId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.WORK_REQUEST_NOT_FOUND));

        // 3. 디자이너 권한 검증 (자신이 받은 의뢰서인지 확인)
        if (!workRequest.getDesigner().getId().equals(designerId)) {
            throw new BadRequestException(ErrorCode.WORK_REQUEST_ACCESS_DENIED);
        }

        // 4. 의뢰서 삭제 (연관된 이미지와 카테고리도 cascade로 함께 삭제됨)
        workRequestRepository.delete(workRequest);

        log.info("작업의뢰서가 삭제되었습니다. ID: {}, 디자이너: {}, 프로젝트: {}",
                workRequestId, designer.getNickname(), workRequest.getProjectTitle());
    }

    /**
     * 디자이너가 프로젝트 제안을 수락
     * PROPOSAL 상태의 작업의뢰서를 PENDING 상태로 변경
     */
    @Transactional
    public WorkRequestResponseDto acceptProposal(Long workRequestId, Long designerId) {
        // 1. 디자이너 유효성 검증
        User designer = validateDesigner(designerId);

        // 2. 작업의뢰서 존재 여부 확인
        WorkRequest workRequest = workRequestRepository.findById(workRequestId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.WORK_REQUEST_NOT_FOUND));

        // 3. 디자이너 권한 검증 (자신이 받은 의뢰서인지 확인)
        if (!workRequest.getDesigner().getId().equals(designerId)) {
            throw new BadRequestException(ErrorCode.WORK_REQUEST_ACCESS_DENIED);
        }

        // 4. 제안 수락 (상태 변경)
        workRequest.acceptProposal();

        log.info("프로젝트 제안이 수락되었습니다. ID: {}, 디자이너: {}, 프로젝트: {}",
                workRequestId, designer.getNickname(), workRequest.getProjectTitle());

        return WorkRequestResponseDto.from(workRequest);
    }

    /**
     * 디자이너가 수락한 프로젝트 목록 조회 (PENDING 상태 이상의 프로젝트들)
     */
    public List<WorkRequestResponseDto> getAcceptedProjectsForDesigner(Long designerId) {
        User designer = validateDesigner(designerId);

        // PENDING, FEEDBACK_WAITING, ACCEPTED 상태의 프로젝트들 조회
        List<WorkRequest> acceptedProjects = workRequestRepository.findByDesignerAndStatusInOrderByCreateDateDesc(
                designer,
                List.of(WorkRequestStatus.PENDING, WorkRequestStatus.FEEDBACK_WAITING,
                        WorkRequestStatus.ACCEPTED)
        );

        return acceptedProjects.stream()
                .map(WorkRequestResponseDto::from)
                .toList();
    }

    /**
     * 디자이너가 수락한 프로젝트 간소화된 목록 조회
     */
    public List<AcceptedProjectSimpleDto> getAcceptedProjectsSimpleForDesigner(Long designerId) {
        User designer = validateDesigner(designerId);

        // PENDING, FEEDBACK_WAITING, ACCEPTED 상태의 프로젝트들 조회
        List<WorkRequest> acceptedProjects = workRequestRepository.findByDesignerAndStatusInOrderByCreateDateDesc(
                designer,
                List.of(WorkRequestStatus.PENDING, WorkRequestStatus.FEEDBACK_WAITING,
                        WorkRequestStatus.ACCEPTED)
        );

        return acceptedProjects.stream()
                .map(AcceptedProjectSimpleDto::from)
                .toList();
    }

    /**
     * 소상공인이 의뢰한 프로젝트 중 디자이너가 수락한 프로젝트 목록 조회 (PENDING 상태 이상의 프로젝트들)
     */
    public List<WorkRequestResponseDto> getAcceptedProjectsForBusinessOwner(Long businessOwnerId) {
        User businessOwner = validateBusinessOwner(businessOwnerId);

        // PENDING, FEEDBACK_WAITING, ACCEPTED 상태의 프로젝트들 조회
        List<WorkRequest> acceptedProjects = workRequestRepository.findByBusinessOwnerAndStatusInOrderByCreateDateDesc(
                businessOwner,
                List.of(WorkRequestStatus.PENDING, WorkRequestStatus.FEEDBACK_WAITING,
                        WorkRequestStatus.ACCEPTED)
        );

        return acceptedProjects.stream()
                .map(WorkRequestResponseDto::from)
                .toList();
    }

    /**
     * 소상공인이 의뢰한 프로젝트 중 디자이너가 수락한 프로젝트 간소화된 목록 조회
     */
    public List<AcceptedProjectSimpleDto> getAcceptedProjectsSimpleForBusinessOwner(Long businessOwnerId) {
        User businessOwner = validateBusinessOwner(businessOwnerId);

        // PENDING, FEEDBACK_WAITING, ACCEPTED 상태의 프로젝트들 조회
        List<WorkRequest> acceptedProjects = workRequestRepository.findByBusinessOwnerAndStatusInOrderByCreateDateDesc(
                businessOwner,
                List.of(WorkRequestStatus.PENDING, WorkRequestStatus.FEEDBACK_WAITING,
                        WorkRequestStatus.ACCEPTED)
        );

        return acceptedProjects.stream()
                .map(AcceptedProjectSimpleDto::from)
                .toList();
    }

    /**
     * 프로젝트 간소화된 상세 정보 조회
     * 클릭한 프로젝트의 기본 정보만 반환 (storeName, designerName, endDate)
     */
    public ProjectSimpleDetailDto getProjectSimpleDetail(Long workRequestId, Long userId) {
        // 1. 사용자 유효성 검증
        userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));

        // 2. 작업의뢰서 조회
        WorkRequest workRequest = workRequestRepository.findById(workRequestId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.WORK_REQUEST_NOT_FOUND));

        // 3. 권한 확인 (디자이너 또는 소상공인만 접근 가능)
        if (!workRequest.getDesigner().getId().equals(userId) &&
            !workRequest.getBusinessOwner().getId().equals(userId)) {
            throw new BadRequestException(ErrorCode.WORK_REQUEST_ACCESS_DENIED);
        }

        return ProjectSimpleDetailDto.from(workRequest);
    }

    /**
     * 소상공인이 프로젝트를 완료 처리
     * 작업의뢰서를 COMPLETED 상태로 변경
     */
    @Transactional
    public WorkRequestResponseDto completeProject(Long workRequestId, Long businessOwnerId) {
        // 1. 소상공인 유효성 검증
        User businessOwner = validateBusinessOwner(businessOwnerId);

        // 2. 작업의뢰서 존재 여부 확인
        WorkRequest workRequest = workRequestRepository.findById(workRequestId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.WORK_REQUEST_NOT_FOUND));

        // 3. 소상공인 권한 검증 (자신이 의뢰한 프로젝트인지 확인)
        if (!workRequest.getBusinessOwner().getId().equals(businessOwnerId)) {
            throw new BadRequestException(ErrorCode.WORK_REQUEST_ACCESS_DENIED);
        }

        // 4. 프로젝트 완료 처리 (상태 변경)
        workRequest.updateStatus(WorkRequestStatus.COMPLETED);

        log.info("프로젝트가 완료되었습니다. ID: {}, 소상공인: {}, 프로젝트: {}",
                workRequestId, businessOwner.getNickname(), workRequest.getProjectTitle());

        return WorkRequestResponseDto.from(workRequest);
    }

    /**
     * 소상공인의 완료된 프로젝트 목록 조회 (COMPLETED 상태)
     */
    public List<CompletedProjectForBusinessOwnerDto> getCompletedProjectsForBusinessOwner(Long businessOwnerId) {
        User businessOwner = validateBusinessOwner(businessOwnerId);

        // COMPLETED 상태의 프로젝트들 조회
        List<WorkRequest> completedProjects = workRequestRepository.findByBusinessOwnerAndStatusOrderByCreateDateDesc(
                businessOwner, WorkRequestStatus.COMPLETED
        );

        return completedProjects.stream()
                .map(CompletedProjectForBusinessOwnerDto::from)
                .toList();
    }

    /**
     * 디자이너의 완료된 프로젝트 목록 조회 (COMPLETED 상태)
     */
    public List<CompletedProjectForDesignerDto> getCompletedProjectsForDesigner(Long designerId) {
        User designer = validateDesigner(designerId);

        // COMPLETED 상태의 프로젝트들 조회
        List<WorkRequest> completedProjects = workRequestRepository.findByDesignerAndStatusOrderByCreateDateDesc(
                designer, WorkRequestStatus.COMPLETED
        );

        return completedProjects.stream()
                .map(CompletedProjectForDesignerDto::from)
                .toList();
    }
}
