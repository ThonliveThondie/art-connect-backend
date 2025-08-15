package thonlivethondie.artconnect.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import thonlivethondie.artconnect.common.BaseTimeEntity;
import thonlivethondie.artconnect.common.DesignCategory;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "portfolio_design_categories")
public class PortfolioDesignCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_design_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Enumerated(EnumType.STRING)
    @Column(name = "design_category", nullable = false)
    private DesignCategory designCategory;

    @Builder
    public PortfolioDesignCategory(Portfolio portfolio, DesignCategory designCategory) {
        this.portfolio = portfolio;
        this.designCategory = designCategory;
    }
}
