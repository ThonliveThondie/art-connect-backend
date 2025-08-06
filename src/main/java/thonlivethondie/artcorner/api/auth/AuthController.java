package thonlivethondie.artcorner.api.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import thonlivethondie.artcorner.api.auth.dto.TokenRefreshRequest;
import thonlivethondie.artcorner.api.auth.dto.TokenRefreshResponse;
import thonlivethondie.artcorner.api.auth.dto.LoginRequest;
import thonlivethondie.artcorner.api.auth.dto.LoginResponse;
import thonlivethondie.artcorner.entity.RefreshToken;
import thonlivethondie.artcorner.entity.User;
import thonlivethondie.artcorner.jwt.JwtHandler;
import thonlivethondie.artcorner.jwt.JwtUserClaim;
import thonlivethondie.artcorner.repository.RefreshTokenRepository;
import thonlivethondie.artcorner.repository.UserRepository;
import thonlivethondie.artcorner.entity.UserRole;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtHandler jwtHandler;

    // 일반 로그인 API
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1. 이메일 + 비밀번호로 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. DB에서 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // 3. 비밀번호 매칭 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 4. JWT 발급
        JwtUserClaim claim = new JwtUserClaim(user.getId(), user.getRole());
        var tokens = jwtHandler.createTokens(claim);

        // 5. RefreshToken DB에 저장 (기존 거 덮어씀)
        refreshTokenRepository.save(
                new RefreshToken(user.getId(), tokens.getRefreshToken())
        );

        // 6. 응답 반환
        return ResponseEntity.ok(new LoginResponse(tokens.getAccessToken(), tokens.getRefreshToken()));
    }

    // RefreshToken 으로 AccessToken 재발급 API
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1. refreshToken 파싱 (서명 확인 + 만료 여부)
        var claimsOpt = jwtHandler.getClaims(refreshToken);
        if (claimsOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        JwtUserClaim claims = claimsOpt.get();

        // 2. DB에서 해당 userId의 refreshToken 조회
        RefreshToken savedToken = refreshTokenRepository.findById(claims.userId())
                .orElseThrow(() -> new RuntimeException("저장된 리프레시 토큰이 없습니다."));

        // 3. 클라이언트가 보낸 refreshToken이 저장된 것과 일치하는지 확인
        if (!savedToken.getToken().equals(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        // 4. accessToken 재발급
        String newAccessToken = jwtHandler.createAccessToken(claims);

        return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@AssignUserId Long userId) {
        refreshTokenRepository.findById(userId)
                .ifPresent(refreshTokenRepository::delete);

        return ResponseEntity.noContent().build();
    }
}