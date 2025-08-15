package thonlivethondie.artconnect.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import thonlivethondie.artconnect.dto.LoginSuccessResponse;
import thonlivethondie.artconnect.repository.UserRepository;
import thonlivethondie.artconnect.service.JwtService;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${jwt.access.expiration}")
    private String accessTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        String username = extractUsername(authentication); // 인증 정보에서 Username(userId) 추출
        Long userId = Long.parseLong(username);

        String accessToken = jwtService.createAccessToken(userId); // JwtService의 createAccessToken을 사용하여 AccessToken 발급
        String refreshToken = jwtService.createRefreshToken(); // JwtService의 createRefreshToken을 사용하여 RefreshToken 발급

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken); // 응답 헤더에 AccessToken, RefreshToken 실어서 응답

        userRepository.findById(userId)
                .ifPresent(user -> {
                    user.updateRefreshToken(refreshToken);
                    userRepository.saveAndFlush(user);

                    // JSON 응답 추가
                    try {
                        LoginSuccessResponse loginSuccessResponse = new LoginSuccessResponse(
                                user.getId(),
                                user.getUserType(),
                                "로그인 성공"
                        );

                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(objectMapper.writeValueAsString(loginSuccessResponse));
                    } catch (IOException e) {
                        log.error("JSON 응답 생성 중 오류 발생", e);
                    }
                });
        log.info("로그인에 성공하였습니다. userId : {}", userId);
        log.info("로그인에 성공하였습니다. AccessToken : {}", accessToken);
        log.info("발급된 AccessToken 만료 기간 : {}", accessTokenExpiration);
    }

    private String extractUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
