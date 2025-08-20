package thonlivethondie.artconnect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import thonlivethondie.artconnect.dto.FeedbackCreateRequestDto;
import thonlivethondie.artconnect.dto.WorkSubmissionCreateRequestDto;
import thonlivethondie.artconnect.dto.WorkSubmissionListResponseDto;
import thonlivethondie.artconnect.dto.WorkSubmissionResponseDto;
import thonlivethondie.artconnect.service.WorkSubmissionService;

import java.util.List;

@RestController
@RequestMapping("/api/work-submissions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class WorkSubmissionController {

    private final WorkSubmissionService workSubmissionService;

    /**
     * 디자이너가 시안을 제출 (여러 번 제출 가능)
     * - 첫 번째 시안: PENDING -> FEEDBACK_WAITING
     * - 추가 시안: FEEDBACK_WAITING 또는 ACCEPTED -> FEEDBACK_WAITING
     */
    @PostMapping("/work-request/{workRequestId}")
    public ResponseEntity<WorkSubmissionResponseDto> submitDesign(
            @PathVariable Long workRequestId,
            @Valid @RequestPart("workSubmission") WorkSubmissionCreateRequestDto requestDto,
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("시안 제출 요청 - workRequestId: {}, designerId: {}", workRequestId, userId);
        log.info("Controller에서 받은 이미지 - images: {}, size: {}", 
                images != null ? "not null" : "null", 
                images != null ? images.size() : "null");
        
        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                log.info("이미지 [{}] - 파일명: {}, 크기: {}, 타입: {}, 비어있음: {}", 
                        i, image.getOriginalFilename(), image.getSize(), 
                        image.getContentType(), image.isEmpty());
            }
        }

        WorkSubmissionResponseDto response = workSubmissionService.submitDesign(
                workRequestId, requestDto, images, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 시안 및 피드백 내역 조회
     * 소상공인과 디자이너 모두 조회 가능
     */
    @GetMapping("/work-request/{workRequestId}")
    public ResponseEntity<WorkSubmissionListResponseDto> getSubmissionsWithFeedbacks(
            @PathVariable Long workRequestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("시안 및 피드백 내역 조회 요청 - workRequestId: {}, userId: {}", workRequestId, userId);

        WorkSubmissionListResponseDto response = workSubmissionService.getSubmissionsWithFeedbacks(
                workRequestId, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 피드백 작성
     * 소상공인 또는 디자이너가 시안에 대해 피드백 작성
     * 최초 피드백 시 WorkRequestStatus: FEEDBACK_WAITING -> ACCEPTED
     */
    @PostMapping("/{submissionId}/feedback")
    public ResponseEntity<WorkSubmissionResponseDto> createFeedback(
            @PathVariable Long submissionId,
            @Valid @RequestBody FeedbackCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("피드백 작성 요청 - submissionId: {}, userId: {}", submissionId, userId);

        WorkSubmissionResponseDto response = workSubmissionService.createFeedback(
                submissionId, requestDto, userId);

        return ResponseEntity.ok(response);
    }
}
