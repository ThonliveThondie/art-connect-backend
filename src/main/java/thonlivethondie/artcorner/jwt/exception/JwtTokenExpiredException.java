package thonlivethondie.artcorner.jwt.exception;

public class JwtTokenExpiredException extends JwtAuthenticationException {
    public JwtTokenExpiredException(Throwable cause) {
        super("JWT 토큰이 만료.", cause);
    }
}
