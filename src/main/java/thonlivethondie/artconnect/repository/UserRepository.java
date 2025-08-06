package thonlivethondie.artconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thonlivethondie.artconnect.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
}
