package thonlivethondie.artcorner.jwt;


import thonlivethondie.artcorner.entity.User;
import thonlivethondie.artcorner.entity.UserRole;


public record JwtUserClaim(
        Long userId,
        UserRole role
) {
    public static JwtUserClaim create(Long userId, UserRole role) {
        return new JwtUserClaim(userId, role);
    }
}
