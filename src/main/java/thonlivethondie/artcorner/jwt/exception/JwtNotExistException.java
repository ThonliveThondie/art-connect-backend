package thonlivethondie.artcorner.jwt.exception;

public class JwtNotExistException extends JwtAuthenticationException {
    public JwtNotExistException() {
        super("JWT 토큰이 존재하지 않습니다.");
    }
}