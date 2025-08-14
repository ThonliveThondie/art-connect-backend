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
}
