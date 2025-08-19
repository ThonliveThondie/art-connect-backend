package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import thonlivethondie.artconnect.common.WorkRequestStatus;
import thonlivethondie.artconnect.common.exception.BadRequestException;
import thonlivethondie.artconnect.common.exception.ErrorCode;
import thonlivethondie.artconnect.dto.*;
import thonlivethondie.artconnect.entity.*;
import thonlivethondie.artconnect.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkSubmissionService {

    private final WorkSubmissionRepository workSubmissionRepository;
    private final WorkSubmissionImageRepository workSubmissionImageRepository;
    private final FeedbackRepository feedbackRepository;
    private final WorkRequestRepository workRequestRepository;
    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;

    /**
     * 디자이너가 시안을 제출 (여러 번 제출 가능)
     * 상태 변경 로직:
     * - PENDING -> FEEDBACK_WAITING (첫 번째 시안)
     * - ACCEPTED -> FEEDBACK_WAITING (피드백 후 추가 시안)
     * - FEEDBACK_WAITING -> FEEDBACK_WAITING (기존 상태 유지)
     */
    @Transactional
    public WorkSubmissionResponseDto submitDesign(Long workRequestId, 
                                                  WorkSubmissionCreateRequestDto requestDto, 
                                                  List<MultipartFile> images, 
                                                  Long designerId) {
        
        log.info("시안 제출 요청 - workRequestId: {}, designerId: {}", workRequestId, designerId);
        
        // WorkRequest 조회 및 권한 확인
        WorkRequest workRequest = workRequestRepository.findById(workRequestId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.WORK_REQUEST_NOT_FOUND));
        
        // 디자이너 권한 확인
        if (!workRequest.getDesigner().getId().equals(designerId)) {
            throw new BadRequestException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        
        // 상태 확인 (PENDING, FEEDBACK_WAITING, ACCEPTED 상태에서 시안 제출 가능)
        if (workRequest.getStatus() != WorkRequestStatus.PENDING && 
            workRequest.getStatus() != WorkRequestStatus.FEEDBACK_WAITING &&
            workRequest.getStatus() != WorkRequestStatus.ACCEPTED) {
            throw new BadRequestException(ErrorCode.INVALID_WORK_REQUEST_STATUS);
        }
        
        // 이미지 필수 확인
        if (images == null || images.isEmpty()) {
            throw new BadRequestException(ErrorCode.IMAGE_REQUIRED);
        }
        
        // WorkSubmission 생성
        WorkSubmission workSubmission = WorkSubmission.builder()
                .comment(requestDto.comment())
                .workRequest(workRequest)
                .build();
        
        WorkSubmission savedSubmission = workSubmissionRepository.save(workSubmission);
        
        // 이미지 업로드 및 저장
        List<WorkSubmissionImageDto> uploadedImages = uploadImages(savedSubmission, images);
        
        // WorkRequest 상태 변경 로직
        WorkRequestStatus currentStatus = workRequest.getStatus();
        if (currentStatus == WorkRequestStatus.PENDING) {
            // 첫 번째 시안 제출
            workRequest.updateStatus(WorkRequestStatus.FEEDBACK_WAITING);
            log.info("첫 번째 시안 제출 - 상태 변경: PENDING -> FEEDBACK_WAITING");
        } else if (currentStatus == WorkRequestStatus.ACCEPTED) {
            // 피드백 후 추가 시안 제출
            workRequest.updateStatus(WorkRequestStatus.FEEDBACK_WAITING);
            log.info("추가 시안 제출 - 상태 변경: ACCEPTED -> FEEDBACK_WAITING");
        } else if (currentStatus == WorkRequestStatus.FEEDBACK_WAITING) {
            // 이미 FEEDBACK_WAITING 상태인 경우 상태 유지
            log.info("추가 시안 제출 - 상태 유지: FEEDBACK_WAITING");
        }
        
        log.info("시안 제출 완료 - submissionId: {}, 이미지 개수: {}", savedSubmission.getId(), uploadedImages.size());
        
        return convertToResponseDto(savedSubmission, uploadedImages, List.of());
    }

    /**
     * 시안 및 피드백 내역 조회
     */
    public WorkSubmissionListResponseDto getSubmissionsWithFeedbacks(Long workRequestId, Long userId) {
        
        log.info("시안 및 피드백 내역 조회 - workRequestId: {}, userId: {}", workRequestId, userId);
        
        // WorkRequest 조회 및 권한 확인
        WorkRequest workRequest = workRequestRepository.findById(workRequestId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.WORK_REQUEST_NOT_FOUND));
        
        // 권한 확인 (소상공인 또는 디자이너)
        if (!workRequest.getBusinessOwner().getId().equals(userId) && 
            !workRequest.getDesigner().getId().equals(userId)) {
            throw new BadRequestException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        
        // WorkSubmission 목록 조회 (이미지와 피드백 포함)
        List<WorkSubmission> submissions = workSubmissionRepository
                .findByWorkRequestIdWithImagesAndFeedbacks(workRequestId);
        
        List<WorkSubmissionResponseDto> submissionDtos = submissions.stream()
                .map(submission -> {
                    // 각 submission에 대해 피드백을 별도로 조회
                    List<Feedback> feedbacks = feedbackRepository.findByWorkSubmissionIdWithAuthor(submission.getId());
                    return convertToResponseDtoWithFeedbacks(submission, feedbacks);
                })
                .collect(Collectors.toList());
        
        return new WorkSubmissionListResponseDto(
                workRequest.getId(),
                workRequest.getProjectTitle(),
                workRequest.getStore().getStoreName(),
                workRequest.getDesigner().getNickname(),
                workRequest.getEndDate(),
                submissionDtos
        );
    }

    /**
     * 피드백 작성
     * 최초 피드백 시 WorkRequestStatus: FEEDBACK_WAITING -> ACCEPTED
     */
    @Transactional
    public WorkSubmissionResponseDto createFeedback(Long submissionId, 
                                                    FeedbackCreateRequestDto requestDto, 
                                                    Long userId) {
        
        log.info("피드백 작성 요청 - submissionId: {}, userId: {}", submissionId, userId);
        
        // WorkSubmission 조회
        WorkSubmission workSubmission = workSubmissionRepository.findByIdWithImagesAndFeedbacks(submissionId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.WORK_SUBMISSION_NOT_FOUND));
        
        // 사용자 조회
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));
        
        // 권한 확인 (소상공인 또는 디자이너)
        WorkRequest workRequest = workSubmission.getWorkRequest();
        if (!workRequest.getBusinessOwner().getId().equals(userId) && 
            !workRequest.getDesigner().getId().equals(userId)) {
            throw new BadRequestException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        
        // 피드백 생성
        Feedback feedback = Feedback.builder()
                .workSubmission(workSubmission)
                .author(author)
                .content(requestDto.content())
                .build();
        
        feedbackRepository.save(feedback);
        workSubmission.addFeedback(feedback);
        
        // 최초 피드백인지 확인하고 상태 변경
        long feedbackCount = feedbackRepository.countByWorkSubmissionId(submissionId);
        if (feedbackCount == 1 && workRequest.getStatus() == WorkRequestStatus.FEEDBACK_WAITING) {
            workRequest.updateStatus(WorkRequestStatus.ACCEPTED);
            log.info("최초 피드백으로 인한 상태 변경 - workRequestId: {}, status: ACCEPTED", workRequest.getId());
        }
        
        log.info("피드백 작성 완료 - feedbackId: {}", feedback.getId());
        
        // 업데이트된 피드백 목록을 다시 조회
        List<Feedback> updatedFeedbacks = feedbackRepository.findByWorkSubmissionIdWithAuthor(submissionId);
        return convertToResponseDtoWithFeedbacks(workSubmission, updatedFeedbacks);
    }

    /**
     * 이미지 업로드 처리
     */
    private List<WorkSubmissionImageDto> uploadImages(WorkSubmission workSubmission, List<MultipartFile> images) {
        return images.stream()
                .map(image -> {
                    try {
                        String imageUrl = awsS3Service.uploadFile(image);
                        
                        WorkSubmissionImage workSubmissionImage = WorkSubmissionImage.builder()
                                .workSubmission(workSubmission)
                                .imageName(image.getOriginalFilename())
                                .imageUrl(imageUrl)
                                .imageSize(image.getSize())
                                .imageType(image.getContentType())
                                .build();
                        
                        WorkSubmissionImage savedImage = workSubmissionImageRepository.save(workSubmissionImage);
                        workSubmission.addWorkSubmissionImage(savedImage);
                        
                        return new WorkSubmissionImageDto(
                                savedImage.getId(),
                                savedImage.getImageUrl(),
                                savedImage.getImageName(),
                                savedImage.getImageSize(),
                                savedImage.getImageType()
                        );
                    } catch (Exception e) {
                        log.error("이미지 업로드 실패", e);
                        throw new BadRequestException(ErrorCode.IMAGE_UPLOAD_FAILED);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * WorkSubmission -> WorkSubmissionResponseDto 변환
     */
    private WorkSubmissionResponseDto convertToResponseDto(WorkSubmission workSubmission) {
        List<WorkSubmissionImageDto> imageDtos = workSubmission.getWorkSubmissionImages().stream()
                .map(image -> new WorkSubmissionImageDto(
                        image.getId(),
                        image.getImageUrl(),
                        image.getImageName(),
                        image.getImageSize(),
                        image.getImageType()
                ))
                .collect(Collectors.toList());

        List<FeedbackDto> feedbackDtos = workSubmission.getFeedbacks().stream()
                .map(feedback -> new FeedbackDto(
                        feedback.getId(),
                        feedback.getContent(),
                        feedback.getAuthor().getNickname(),
                        feedback.getAuthor().getUserType().name(),
                        feedback.getCreateDate()
                ))
                .collect(Collectors.toList());

        return new WorkSubmissionResponseDto(
                workSubmission.getId(),
                workSubmission.getComment(),
                workSubmission.getStoreName(),
                workSubmission.getDesignerName(),
                workSubmission.getEndDate(),
                workSubmission.getCreateDate(),
                imageDtos,
                feedbackDtos
        );
    }

    /**
     * WorkSubmission -> WorkSubmissionResponseDto 변환 (이미지와 피드백 별도 제공)
     */
    private WorkSubmissionResponseDto convertToResponseDto(WorkSubmission workSubmission, 
                                                           List<WorkSubmissionImageDto> images, 
                                                           List<FeedbackDto> feedbacks) {
        return new WorkSubmissionResponseDto(
                workSubmission.getId(),
                workSubmission.getComment(),
                workSubmission.getStoreName(),
                workSubmission.getDesignerName(),
                workSubmission.getEndDate(),
                workSubmission.getCreateDate(),
                images,
                feedbacks
        );
    }

    /**
     * WorkSubmission과 Feedback 리스트를 받아서 ResponseDto로 변환
     */
    private WorkSubmissionResponseDto convertToResponseDtoWithFeedbacks(WorkSubmission workSubmission, 
                                                                        List<Feedback> feedbacks) {
        List<WorkSubmissionImageDto> imageDtos = workSubmission.getWorkSubmissionImages().stream()
                .map(image -> new WorkSubmissionImageDto(
                        image.getId(),
                        image.getImageUrl(),
                        image.getImageName(),
                        image.getImageSize(),
                        image.getImageType()
                ))
                .collect(Collectors.toList());

        List<FeedbackDto> feedbackDtos = feedbacks.stream()
                .map(feedback -> new FeedbackDto(
                        feedback.getId(),
                        feedback.getContent(),
                        feedback.getAuthor().getNickname(),
                        feedback.getAuthor().getUserType().name(),
                        feedback.getCreateDate()
                ))
                .collect(Collectors.toList());

        return new WorkSubmissionResponseDto(
                workSubmission.getId(),
                workSubmission.getComment(),
                workSubmission.getStoreName(),
                workSubmission.getDesignerName(),
                workSubmission.getEndDate(),
                workSubmission.getCreateDate(),
                imageDtos,
                feedbackDtos
        );
    }
}
