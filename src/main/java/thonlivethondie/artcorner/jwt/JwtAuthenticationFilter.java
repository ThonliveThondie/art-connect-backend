package thonlivethondie.artcorner.jwt;

import thonlivethondie.artcorner.jwt.exception.JwtAuthenticationException;
import thonlivethondie.artcorner.jwt.exception.JwtNotExistException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Optional<String> tokenOptional = resolveToken(request);

        if (tokenOptional.isEmpty()) {
            filterChain.doFilter(request, response); // 토큰 없으면 다음 필터로 넘김
            return;
        }

        try {
            String tokenValue = tokenOptional.get();
            JwtAuthenticationToken token = new JwtAuthenticationToken(tokenValue);
            Authentication authentication = this.authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException e) {
            handleJwtException(response, e);
        }
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return Optional.of(bearerToken.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }

    private void handleJwtException(HttpServletResponse response, JwtAuthenticationException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        String errorResponse = objectMapper.writeValueAsString(
                new ErrorResponse("UNAUTHORIZED", e.getMessage())
        );
        response.getWriter().write(errorResponse);
        response.flushBuffer();
    }

    // 간단한 에러 응답 객체
    private record ErrorResponse(String error, String message) {}
}