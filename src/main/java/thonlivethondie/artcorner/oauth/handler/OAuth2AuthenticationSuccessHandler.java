package thonlivethondie.artcorner.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import thonlivethondie.artcorner.jwt.JwtHandler;
import thonlivethondie.artcorner.jwt.JwtUserClaim;
import thonlivethondie.artcorner.entity.UserRole;
import thonlivethondie.artcorner.oauth.service.OAuth2UserPrincipal;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtHandler jwtHandler;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();

        Long userId = principal.getUser().getId();
        UserRole role = principal.getUser().getRole();

        JwtUserClaim claim = new JwtUserClaim(userId, role);
        String accessToken = jwtHandler.createAccessToken(claim);
        String refreshToken = jwtHandler.createRefreshToken();

        String targetUrl = "http://localhost:5173/auth/success";

        String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        log.info("OAuth2 로그인 성공, 리다이렉트 URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
