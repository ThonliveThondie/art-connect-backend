package thonlivethondie.artcorner.jwt.exception;

public class JwtTokenInvalidException extends JwtAuthenticationException {
    public JwtTokenInvalidException(Throwable cause) {
        super("JWT 토큰이 유효하지 않dkdy.", cause);
    }
}
