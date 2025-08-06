package thonlivethondie.artcorner.jwt;

import thonlivethondie.artcorner.jwt.exception.JwtTokenExpiredException;
import thonlivethondie.artcorner.jwt.exception.JwtTokenInvalidException;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TokenProvider implements AuthenticationProvider {

    private final JwtHandler jwtHandler;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        String tokenValue = jwtAuthenticationToken.token();

        try {
            JwtUserClaim claims = jwtHandler.parseToken(tokenValue);
            return new JwtAuthentication(claims);
        } catch (ExpiredJwtException e) {
            throw new JwtTokenExpiredException(e);
        } catch (Exception e) {
            throw new JwtTokenInvalidException(e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
