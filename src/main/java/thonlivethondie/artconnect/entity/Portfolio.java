package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseEntity;
import thonlivethondie.artconnect.common.DesignCategory;
import thonlivethondie.artconnect.common.UserType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // 포트폴리오 디자인 카테고리들 (최대 3개)
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioDesignCategory> designCategories = new ArrayList<>();

    // 포트폴리오 이미지들
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioImage> portfolioImages = new ArrayList<>();

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl; // 썸네일 이미지 URL

    @Builder
    public Portfolio(User designer,
                     String title,
                     String description,
                     String thumbnailUrl) {

        // 디자이너만 포트폴리오를 생성할 수 있는 검증 로직
        if (designer.getUserType() != UserType.DESIGNER) {
            throw new IllegalArgumentException("포트폴리오는 디자이너만 생성할 수 있습니다.");
        }

        this.designer = designer;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * 연관관계 편의 메서드
     */
    // 디자인 카테고리 일괄 설정
    public void setDesignCategories(List<DesignCategory> categories) {
        if (categories.size() > 3) {
            throw new IllegalArgumentException("최대 3개의 디자인 카테고리만 선택할 수 있습니다.");
        }

        // 기존 카테고리 모두 제거
        this.designCategories.clear();

        // 새 카테고리들 추가
        for (DesignCategory category : categories) {
            PortfolioDesignCategory portfolioDesignCategory =
                    PortfolioDesignCategory.builder()
                            .portfolio(this)
                            .designCategory(category)
                            .build();

            this.designCategories.add(portfolioDesignCategory);
        }
    }

    // 디자인 카테고리 제거 메서드
    public void removeDesignCategory(DesignCategory category) {
        this.designCategories.removeIf(dc -> dc.getDesignCategory() == category);
    }

    // 디자인 카테고리 목록 조회
    public List<DesignCategory> getSelectedDesignCategories() {
        return this.designCategories.stream()
                .map(PortfolioDesignCategory::getDesignCategory)
                .collect(Collectors.toList());
    }

    /**
     * 포트폴리오 정보 업데이트
     */
    public void updatePortfolioInfo(String title, String description) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
    }

    /**
     * 썸네일 URL 업데이트
     */
    public void updateThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
