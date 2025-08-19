package thonlivethondie.artconnect.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import thonlivethondie.artconnect.common.UserType;
import thonlivethondie.artconnect.entity.QPortfolio;
import thonlivethondie.artconnect.entity.QPortfolioDesignCategory;
import thonlivethondie.artconnect.entity.QUser;
import thonlivethondie.artconnect.entity.QUserDesignCategory;
import thonlivethondie.artconnect.entity.QUserDesignStyleCategory;
import thonlivethondie.artconnect.entity.User;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PortfolioRepositoryImpl implements PortfolioRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 키워드 목록을 사용하여 디자이너를 검색합니다.
     * <p>
     * 검색 대상 필드:
     * - Portfolio: title, description, designCategories (포트폴리오가 있는 경우)
     * - User: speciality (전문분야 카테고리), designStyleCategories (디자인 스타일 카테고리)
     *
     * @param keywords 검색할 키워드 목록
     * @return 키워드와 매칭되는 디자이너 목록 (중복 제거, 최대 50개)
     */
    @Override
    public List<User> findDesignersByKeywords(List<String> keywords) {
        QPortfolio portfolio = QPortfolio.portfolio;
        QPortfolioDesignCategory portfolioDesignCategory = QPortfolioDesignCategory.portfolioDesignCategory;
        QUser user = QUser.user;
        QUserDesignCategory userDesignCategory = QUserDesignCategory.userDesignCategory;
        QUserDesignStyleCategory userDesignStyleCategory = QUserDesignStyleCategory.userDesignStyleCategory;

        // 키워드가 null이거나 비어있는 경우 빈 리스트 반환
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }

        // User의 전문분야 및 디자인 스타일 검색 조건
        BooleanBuilder userCategoryBuilder = new BooleanBuilder();

        // Portfolio 검색 조건 (포트폴리오가 있는 디자이너 대상)
        BooleanBuilder portfolioBuilder = new BooleanBuilder();

        // 각 키워드에 대해 OR 조건으로 검색 조건 추가
        for (String keyword : keywords) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String trimmedKeyword = keyword.trim().toLowerCase();

                // User 전문분야 검색 (UserDesignCategory 통해 검색)
                userCategoryBuilder.or(userDesignCategory.designCategory.stringValue().lower().contains(trimmedKeyword));
                
                // User 디자인 스타일 검색 (UserDesignStyleCategory 통해 검색)
                userCategoryBuilder.or(userDesignStyleCategory.designStyle.stringValue().lower().contains(trimmedKeyword));

                // Portfolio 검색 조건
                BooleanBuilder portfolioKeywordBuilder = new BooleanBuilder();
                portfolioKeywordBuilder.or(portfolio.title.lower().contains(trimmedKeyword))
                        .or(portfolio.description.lower().contains(trimmedKeyword))
                        .or(portfolioDesignCategory.designCategory.stringValue().lower().contains(trimmedKeyword));

                portfolioBuilder.or(portfolioKeywordBuilder);
            }
        }

        // 조건이 없는 경우 빈 리스트 반환
        if (!userCategoryBuilder.hasValue() && !portfolioBuilder.hasValue()) {
            return List.of();
        }

        // User 테이블을 기준으로 검색 (디자이너만)
        // 전문분야 및 디자인 스타일로 먼저 검색하고, 포트폴리오 조건은 LEFT JOIN으로 추가
        BooleanBuilder finalCondition = new BooleanBuilder();
        finalCondition.and(user.userType.eq(UserType.DESIGNER));

        if (userCategoryBuilder.hasValue()) {
            finalCondition.and(userCategoryBuilder);
        }

        return queryFactory
                .select(user)
                .from(user)
                .leftJoin(user.speciality, userDesignCategory)
                .leftJoin(user.designStyleCategories, userDesignStyleCategory)
                .leftJoin(user.portfolios, portfolio)
                .leftJoin(portfolio.designCategories, portfolioDesignCategory)
                .where(finalCondition.or(
                        user.userType.eq(UserType.DESIGNER).and(portfolioBuilder)
                ))
                .distinct()
                .limit(50)
                .fetch();
    }
}
