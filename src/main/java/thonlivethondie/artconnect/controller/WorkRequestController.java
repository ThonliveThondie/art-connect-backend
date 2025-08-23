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
import thonlivethondie.artconnect.dto.WorkRequestCreateRequestDto;
import thonlivethondie.artconnect.dto.WorkRequestResponseDto;
import thonlivethondie.artconnect.dto.WorkRequestSimpleDto;
import thonlivethondie.artconnect.dto.AcceptedProjectSimpleDto;
import thonlivethondie.artconnect.dto.ProjectSimpleDetailDto;
import thonlivethondie.artconnect.dto.CompletedProjectForBusinessOwnerDto;
import thonlivethondie.artconnect.dto.CompletedProjectForDesignerDto;
import thonlivethondie.artconnect.service.WorkRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/work-request")
@RequiredArgsConstructor
@Validated
@Slf4j
public class WorkRequestController {

    private final WorkRequestService workRequestService;

    @PostMapping("/to/{designerId}")
    public ResponseEntity<WorkRequestResponseDto> createWorkRequest(
            @PathVariable Long designerId,
            @Valid @RequestPart("workRequest") WorkRequestCreateRequestDto requestDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("작업의뢰서 생성 요청 - 의뢰자: {}, 디자이너: {}", userId, designerId);
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

        WorkRequestResponseDto response = workRequestService.createWorkRequest(requestDto, images, userId, designerId);

        return ResponseEntity.ok(response);
    }

    /**
     * 디자이너가 받은 의뢰서 목록 조회
     */
    @GetMapping("/designer")
    public ResponseEntity<List<WorkRequestResponseDto>> getWorkRequestsForDesigner(
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        List<WorkRequestResponseDto> workRequests = workRequestService.getWorkRequestsForDesigner(userId);

        return ResponseEntity.ok(workRequests);
    }

    @GetMapping("/designer/{requestId}")
    public ResponseEntity<WorkRequestResponseDto> getOneWorkRequestForDesigner(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());

        WorkRequestResponseDto workRequest = workRequestService.getOneWorkRequestForDesigner(requestId, userId);

        return ResponseEntity.ok(workRequest);
    }

    /**
     * 소상공인이 보낸 의뢰서 목록 조회
     */
    @GetMapping("/business-owner")
    public ResponseEntity<List<WorkRequestResponseDto>> getWorkRequestsForBusinessOwner(
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        List<WorkRequestResponseDto> workRequests = workRequestService.getWorkRequestsForBusinessOwner(userId);

        return ResponseEntity.ok(workRequests);
    }

    @GetMapping("/business-owner/{requestId}")
    public ResponseEntity<WorkRequestResponseDto> getOneWorkRequestForBusinessOwner(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());

        WorkRequestResponseDto workRequest = workRequestService.getOneWorkRequestForBusinessOwner(requestId, userId);

        return ResponseEntity.ok(workRequest);
    }

    /**
     * 디자이너가 받은 의뢰서 간소화된 목록 조회
     * 프로젝트 제목, 매장명, 예산 정보만 반환
     */
    @GetMapping("/designer/simple")
    public ResponseEntity<List<WorkRequestSimpleDto>> getSimpleWorkRequestsForDesigner(
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        List<WorkRequestSimpleDto> workRequests = workRequestService.getSimpleWorkRequestsForDesigner(userId);

        return ResponseEntity.ok(workRequests);
    }

    /**
     * 디자이너가 작업의뢰서 거절 (삭제)
     * 디자이너만 자신이 받은 의뢰서를 삭제할 수 있음
     */
    @DeleteMapping("/{workRequestId}/reject")
    public ResponseEntity<Void> rejectWorkRequest(
            @PathVariable Long workRequestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("작업의뢰서 거절 요청 - 의뢰서 ID: {}, 디자이너 ID: {}", workRequestId, userId);

        workRequestService.deleteWorkRequestByDesigner(workRequestId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 디자이너가 프로젝트 제안을 수락
     */
    @PostMapping("/{workRequestId}/accept")
    public ResponseEntity<WorkRequestResponseDto> acceptProposal(
            @PathVariable Long workRequestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("프로젝트 제안 수락 요청 - 의뢰서 ID: {}, 디자이너 ID: {}", workRequestId, userId);

        WorkRequestResponseDto response = workRequestService.acceptProposal(workRequestId, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 디자이너가 수락한 프로젝트 목록 조회
     */
    @GetMapping("/designer/accepted")
    public ResponseEntity<List<WorkRequestResponseDto>> getAcceptedProjectsForDesigner(
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        List<WorkRequestResponseDto> acceptedProjects = workRequestService.getAcceptedProjectsForDesigner(userId);

        return ResponseEntity.ok(acceptedProjects);
    }

    /**
     * 디자이너가 수락한 프로젝트 간소화된 목록 조회
     */
    @GetMapping("/designer/accepted/simple")
    public ResponseEntity<List<AcceptedProjectSimpleDto>> getAcceptedProjectsSimpleForDesigner(
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        List<AcceptedProjectSimpleDto> acceptedProjects = workRequestService.getAcceptedProjectsSimpleForDesigner(userId);

        return ResponseEntity.ok(acceptedProjects);
    }

    /**
     * 소상공인이 의뢰한 프로젝트 중 디자이너가 수락한 프로젝트 목록 조회
     */
    @GetMapping("/business-owner/accepted")
    public ResponseEntity<List<WorkRequestResponseDto>> getAcceptedProjectsForBusinessOwner(
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        List<WorkRequestResponseDto> acceptedProjects = workRequestService.getAcceptedProjectsForBusinessOwner(userId);

        return ResponseEntity.ok(acceptedProjects);
    }

    /**
     * 소상공인이 의뢰한 프로젝트 중 디자이너가 수락한 프로젝트 간소화된 목록 조회
     */
    @GetMapping("/business-owner/accepted/simple")
    public ResponseEntity<List<AcceptedProjectSimpleDto>> getAcceptedProjectsSimpleForBusinessOwner(
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        List<AcceptedProjectSimpleDto> acceptedProjects = workRequestService.getAcceptedProjectsSimpleForBusinessOwner(userId);

        return ResponseEntity.ok(acceptedProjects);
    }

    /**
     * 프로젝트 간소화된 상세 정보 조회
     * 클릭한 프로젝트의 기본 정보만 반환 (storeName, designerName, endDate)
     */
    @GetMapping("/{workRequestId}/simple-detail")
    public ResponseEntity<ProjectSimpleDetailDto> getProjectSimpleDetail(
            @PathVariable Long workRequestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        ProjectSimpleDetailDto projectDetail = workRequestService.getProjectSimpleDetail(workRequestId, userId);

        return ResponseEntity.ok(projectDetail);
    }

    /**
     * 소상공인이 프로젝트를 완료 처리
     * WorkRequestStatus: ACCEPTED -> COMPLETED
     */
    @PostMapping("/{workRequestId}/complete")
    public ResponseEntity<WorkRequestResponseDto> completeProject(
            @PathVariable Long workRequestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("프로젝트 완료 요청 - workRequestId: {}, businessOwnerId: {}", workRequestId, userId);

        WorkRequestResponseDto response = workRequestService.completeProject(workRequestId, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 소상공인의 완료된 프로젝트 목록 조회
     * COMPLETED 상태인 프로젝트만 반환
     */
    @GetMapping("/business-owner/completed")
    public ResponseEntity<List<CompletedProjectForBusinessOwnerDto>> getCompletedProjectsForBusinessOwner(
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("완료된 프로젝트 목록 조회 요청 - businessOwnerId: {}", userId);

        List<CompletedProjectForBusinessOwnerDto> completedProjects = workRequestService.getCompletedProjectsForBusinessOwner(userId);

        return ResponseEntity.ok(completedProjects);
    }

    /**
     * 디자이너의 완료된 프로젝트 목록 조회
     * COMPLETED 상태인 프로젝트만 반환
     */
    @GetMapping("/designer/completed")
    public ResponseEntity<List<CompletedProjectForDesignerDto>> getCompletedProjectsForDesigner(
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("디자이너 완료된 프로젝트 목록 조회 요청 - designerId: {}", userId);

        List<CompletedProjectForDesignerDto> completedProjects = workRequestService.getCompletedProjectsForDesigner(userId);

        return ResponseEntity.ok(completedProjects);
    }
}
