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
                .status(WorkRequestStatus.PENDING)
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
}
