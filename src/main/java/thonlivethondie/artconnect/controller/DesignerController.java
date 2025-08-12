package thonlivethondie.artconnect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import thonlivethondie.artconnect.dto.DesignerInfoRequestDto;
import thonlivethondie.artconnect.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class DesignerController {

    private final UserService userService;

    @PostMapping("/designer/profiles")
    public ResponseEntity<Void> myProfile(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody DesignerInfoRequestDto requestBody) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        userService.updateDesignerInfo(userId, requestBody);
        return ResponseEntity.ok().build();
    }
}
