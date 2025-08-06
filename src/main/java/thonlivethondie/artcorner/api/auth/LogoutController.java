package thonlivethondie.artcorner.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import thonlivethondie.artcorner.jwt.JwtHandler;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LogoutController {

    private final JwtHandler jwtHandler;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId, HttpServletRequest request, HttpServletResponse response) {
        // TODO: RefreshToken DB 또는 Redis에서 삭제 로직 구현 필요 (여기서는 단순 응답 처리)
        // 프론트가 AccessToken, RefreshToken 클라이언트에서 삭제해야 함

        return ResponseEntity.ok().build();
    }
}