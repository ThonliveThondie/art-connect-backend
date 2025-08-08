package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseEntity;
import thonlivethondie.artconnect.common.UserType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "portfolios")
public class Portfolio extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id", nullable = false)
    private User designer;

    // 매장과의 연관관계 (소상공인의 매장)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "project_duration")
    private String projectDuration;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl; // 썸네일 이미지 URL

    // 포트폴리오 이미지들
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioImage> portfolioImages = new ArrayList<>();

    @Builder
    public Portfolio(User designer,
                     Store store,
                     String title,
                     String description,
                     String category,
                     String projectDuration,
                     String thumbnailUrl) {

        // 디자이너만 포트폴리오를 생성할 수 있는 검증 로직
        if (designer.getUserType() != UserType.DESIGNER) {
            throw new IllegalArgumentException("포트폴리오는 디자이너만 생성할 수 있습니다.");
        }

        this.designer = designer;
        this.store = store;
        this.title = title;
        this.description = description;
        this.category = category;
        this.projectDuration = projectDuration;
        this.thumbnailUrl = thumbnailUrl;
    }
}
