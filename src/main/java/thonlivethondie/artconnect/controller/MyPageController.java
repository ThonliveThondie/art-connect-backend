package thonlivethondie.artconnect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import thonlivethondie.artconnect.dto.BusinessOwnerMyPageUpdateRequest;
import thonlivethondie.artconnect.dto.DesignerMyPageUpdateRequest;
import thonlivethondie.artconnect.dto.ProfileImageResponseDto;
import thonlivethondie.artconnect.service.MyPageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

/**
 * 마이페이지 통합 컨트롤러
 * 사용자 타입(디자이너/소상공인)에 관계없이 동일한 엔드포인트 제공
 */
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyPage(@AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());

        Object response = myPageService.getMyPageByUserType(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/designer")
    public ResponseEntity<Void> updateDesignerMyPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DesignerMyPageUpdateRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());

        myPageService.updateDesignerMyPage(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/business-owner")
    public ResponseEntity<Void> updateBusinessOwnerMyPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BusinessOwnerMyPageUpdateRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());

        myPageService.updateBusinessOwnerMyPage(userId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 디자이너 프로필 이미지 업로드
     */
    @PostMapping("/designer/profile-image")
    public ResponseEntity<ProfileImageResponseDto> uploadDesignerProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("profileImage") MultipartFile profileImage
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        String imageUrl = myPageService.updateProfileImage(userId, profileImage); // URL 반환
        ProfileImageResponseDto dto = new ProfileImageResponseDto(imageUrl);
        return ResponseEntity.ok(dto);
    }

    /**
     * 소상공인 프로필 이미지 업로드
     */
    @PostMapping("/business-owner/profile-image")
    public ResponseEntity<ProfileImageResponseDto> uploadBusinessOwnerProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("profileImage") MultipartFile profileImage
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        String imageUrl = myPageService.updateProfileImage(userId, profileImage); // URL 반환
        ProfileImageResponseDto dto = new ProfileImageResponseDto(imageUrl);
        return ResponseEntity.ok(dto);
    }
}
