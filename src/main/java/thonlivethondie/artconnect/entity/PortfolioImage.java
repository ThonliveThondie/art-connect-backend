package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "portfolio_images")
public class PortfolioImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "image_name", length = 255)
    private String imageName; // 원본 파일명

    @Column(name = "image_size")
    private Long imageSize; // 파일 크기 (bytes)

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder; // 이미지 표시 순서

    @Column(name = "is_thumbnail", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isThumbnail = false; // 썸네일 여부

    @Builder
    public PortfolioImage(Portfolio portfolio,
                          String imageUrl,
                          String imageName,
                          Long imageSize,
                          Integer displayOrder,
                          Boolean isThumbnail) {
        this.portfolio = portfolio;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.imageSize = imageSize;
        this.displayOrder = displayOrder;
        this.isThumbnail = isThumbnail != null ? isThumbnail : false;
    }
}
