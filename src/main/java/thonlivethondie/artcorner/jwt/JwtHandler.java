package thonlivethondie.artcorner.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import thonlivethondie.artcorner.entity.UserRole;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class JwtHandler {

    private final SecretKey secretKey;
    private final long accessTokenExpireIn;
    private final long refreshTokenExpireIn;

    public static final String USER_ID = "USER_ID";
    public static final String USER_ROLE = "USER_ROLE";

    public JwtHandler(String secret, long accessTokenExpireIn, long refreshTokenExpireIn) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpireIn = accessTokenExpireIn;
        this.refreshTokenExpireIn = refreshTokenExpireIn;
    }

    public String createAccessToken(JwtUserClaim claim) {
        return Jwts.builder()
                .subject("AccessToken")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpireIn))
                .claims(Map.of(
                        USER_ID, claim.userId(),
                        USER_ROLE, claim.role().name()
                ))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken() {
        return Jwts.builder()
                .subject("RefreshToken")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpireIn))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public JwtUserClaim parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return convert(claims);
    }

    public Optional<JwtUserClaim> getClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(convert(claims));
        } catch (ExpiredJwtException e) {
            return Optional.of(convert(e.getClaims()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private JwtUserClaim convert(Claims claims) {
        Long userId = claims.get(USER_ID, Number.class).longValue();
        UserRole role = UserRole.valueOf(claims.get(USER_ROLE, String.class));
        return new JwtUserClaim(userId, role);
    }

    public JwtTokenPair createTokens(JwtUserClaim claim) {
        String accessToken = createAccessToken(claim);
        String refreshToken = createRefreshToken();
        return new JwtTokenPair(accessToken, refreshToken);
    }
}
