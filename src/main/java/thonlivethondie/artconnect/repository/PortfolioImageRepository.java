package thonlivethondie.artconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import thonlivethondie.artconnect.entity.PortfolioImage;

import java.util.List;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {

    /**
     * 특정 포트폴리오의 모든 이미지의 썸네일 상태를 false로 설정하고, 특정 이미지만 true로 설정
     */
    @Modifying
    @Query("UPDATE PortfolioImage pi SET pi.isThumbnail = CASE WHEN pi.id = :imageId THEN true ELSE false END WHERE pi.portfolio.id = :portfolioId")
    void updateThumbnailStatus(@Param("portfolioId") Long portfolioId, @Param("imageId") Long imageId);
}
