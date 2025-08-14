package thonlivethondie.artconnect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import thonlivethondie.artconnect.dto.MyPageResponse;
import thonlivethondie.artconnect.dto.MyPageUpdateRequest;
import thonlivethondie.artconnect.service.MyPageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<MyPageResponse> getMyPage(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(myPageService.getMyPage(userId));
    }

    @PutMapping
    public ResponseEntity<Void> updateMyPage(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody MyPageUpdateRequest request
    ) {
        myPageService.updateMyPage(userId, request);
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
