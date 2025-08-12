package thonlivethondie.artconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thonlivethondie.artconnect.entity.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>, PortfolioRepositoryCustom {
}
