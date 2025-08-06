package thonlivethondie.artconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thonlivethondie.artconnect.common.SocialType;
import thonlivethondie.artconnect.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * 이미 사용한 소셜 플랫폼으로 로그인한 사용자가 존재하는 지 확인하기 우해 사용하는 쿼리
     */
    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}
