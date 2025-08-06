package thonlivethondie.artconnect.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import thonlivethondie.artconnect.oauth2.CustomOAuth2User;
import thonlivethondie.artconnect.repository.UserRepository;
import thonlivethondie.artconnect.service.JwtService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final String FRONTEND_URL = "http://localhost:3000";

    @Value("${jwt.access.expiration}")
    private String accessTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");

        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getEmail();

            // AccessToken과 RefreshToken 생성
            String accessToken = jwtService.createAccessToken(email);
            String refreshToken = jwtService.createRefreshToken();

            // 응답 헤더에 토큰 추가
            response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
            response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

            // JwtService를 통해 토큰 전송
            jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

            // RefreshToken을 데이터베이스에 저장
            userRepository.findByEmail(email)
                    .ifPresent(user -> {
                        user.updateRefreshToken(refreshToken);
                        userRepository.saveAndFlush(user);
                    });

            // 모든 소셜 로그인 사용자에게 토큰을 URL 파라미터로 전달
            String redirectUrl = FRONTEND_URL +
                    "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8) +
                    "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

            response.sendRedirect(redirectUrl);

            log.info("OAuth2 로그인에 성공하였습니다. 이메일 : {}", email);
            log.info("OAuth2 로그인에 성공하였습니다. AccessToken : {}", accessToken);
            log.info("발급된 AccessToken 만료 기간 : {}", accessTokenExpiration);

        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 오류 발생: {}", e.getMessage());
            response.sendRedirect(FRONTEND_URL + "/login?error=true");
        }
    }
}
