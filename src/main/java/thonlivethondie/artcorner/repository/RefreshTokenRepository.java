package thonlivethondie.artcorner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thonlivethondie.artcorner.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
