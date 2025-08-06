package thonlivethondie.artcorner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thonlivethondie.artcorner.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
