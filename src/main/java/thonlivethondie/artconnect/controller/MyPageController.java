package thonlivethondie.artconnect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import thonlivethondie.artconnect.dto.BusinessOwnerMyPageUpdateRequest;
import thonlivethondie.artconnect.dto.DesignerMyPageUpdateRequest;
import thonlivethondie.artconnect.service.MyPageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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

    @PostMapping("/profile-image")
    public ResponseEntity<Void> updateProfileImage(
            @AuthenticationPrincipal Long userId,
            @RequestParam String imageName,
            @RequestParam String imageUrl,
            @RequestParam Long imageSize,
            @RequestParam String imageType
    ) {
        myPageService.updateProfileImage(userId, imageName, imageUrl, imageSize, imageType);
        return ResponseEntity.noContent().build();
    }
}
