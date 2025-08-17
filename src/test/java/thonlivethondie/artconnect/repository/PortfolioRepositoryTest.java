package thonlivethondie.artconnect.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import thonlivethondie.artconnect.config.QueryDslConfig;
import thonlivethondie.artconnect.entity.Portfolio;
import thonlivethondie.artconnect.entity.PortfolioDesignCategory;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.entity.UserDesignCategory;
import thonlivethondie.artconnect.entity.UserDesignStyleCategory;
import thonlivethondie.artconnect.common.DesignCategory;
import thonlivethondie.artconnect.common.DesignStyle;
import thonlivethondie.artconnect.common.Role;
import thonlivethondie.artconnect.common.SocialType;
import thonlivethondie.artconnect.common.UserType;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PortfolioRepository의 커스텀 쿼리 메서드에 대한 통합 테스트
 * H2 인메모리 데이터베이스를 사용하여 실제 데이터베이스 동작을 검증합니다.
 */
@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class PortfolioRepositoryTest {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    private User designer1;
    private User designer2;
    private User designer3;

    @BeforeEach
    void setUp() {
        // 테스트용 디자이너 사용자 생성
        designer1 = User.builder()
                .email("designer1@test.com")
                .nickname("디자이너1")
                .userType(UserType.DESIGNER)
                .role(Role.USER)
                .socialType(SocialType.KAKAO)
                .build();

        designer2 = User.builder()
                .email("designer2@test.com")
                .nickname("디자이너2")
                .userType(UserType.DESIGNER)
                .role(Role.USER)
                .socialType(SocialType.GOOGLE)
                .build();

        designer3 = User.builder()
                .email("designer3@test.com")
                .nickname("디자이너3")
                .userType(UserType.DESIGNER)
                .role(Role.USER)
                .socialType(SocialType.NAVER)
                .build();

        // 사용자 저장
        userRepository.saveAll(Arrays.asList(designer1, designer2, designer3));

        // 디자이너별 전문분야 설정
        // 디자이너1: 로고, 브랜드 디자인 전문
        UserDesignCategory designer1Category1 = UserDesignCategory.of(designer1, DesignCategory.LOGO);
        UserDesignCategory designer1Category2 = UserDesignCategory.of(designer1, DesignCategory.BRAND);
        designer1.getSpeciality().addAll(Arrays.asList(designer1Category1, designer1Category2));

        // 디자이너2: 굿즈, 패키지 디자인 전문  
        UserDesignCategory designer2Category1 = UserDesignCategory.of(designer2, DesignCategory.GOODS);
        UserDesignCategory designer2Category2 = UserDesignCategory.of(designer2, DesignCategory.PACKAGE);
        designer2.getSpeciality().addAll(Arrays.asList(designer2Category1, designer2Category2));

        // 디자이너3: 배너 광고 디자인 전문
        UserDesignCategory designer3Category = UserDesignCategory.of(designer3, DesignCategory.BANNER_AD);
        designer3.getSpeciality().add(designer3Category);

        // 디자이너별 디자인 스타일 설정
        // 디자이너1: CUTE, MODERN 스타일
        UserDesignStyleCategory designer1Style1 = UserDesignStyleCategory.of(designer1, DesignStyle.CUTE);
        UserDesignStyleCategory designer1Style2 = UserDesignStyleCategory.of(designer1, DesignStyle.MODERN);
        designer1.getDesignStyleCategories().addAll(Arrays.asList(designer1Style1, designer1Style2));

        // 디자이너2: CLASSIC, LUXURIOUS 스타일
        UserDesignStyleCategory designer2Style1 = UserDesignStyleCategory.of(designer2, DesignStyle.CLASSIC);
        UserDesignStyleCategory designer2Style2 = UserDesignStyleCategory.of(designer2, DesignStyle.LUXURIOUS);
        designer2.getDesignStyleCategories().addAll(Arrays.asList(designer2Style1, designer2Style2));

        // 디자이너3: CUTE 스타일만
        UserDesignStyleCategory designer3Style = UserDesignStyleCategory.of(designer3, DesignStyle.CUTE);
        designer3.getDesignStyleCategories().add(designer3Style);

        // 테스트용 포트폴리오 생성
        Portfolio portfolio1 = Portfolio.builder()
                .designer(designer1)
                .title("카페 웹사이트 리뉴얼")
                .description("모던하고 깔끔한 카페 웹사이트를 제작했습니다. 반응형 디자인을 적용하여 모바일에서도 완벽하게 동작합니다.")
                .thumbnailUrl("https://example.com/portfolio1.jpg")
                .build();

        Portfolio portfolio2 = Portfolio.builder()
                .designer(designer2)
                .title("레스토랑 브랜딩 패키지")
                .description("이탈리안 레스토랑의 브랜드 아이덴티티를 구축했습니다. 로고, 메뉴판, 간판 등 전체적인 그래픽 디자인을 담당했습니다.")
                .thumbnailUrl("https://example.com/portfolio2.jpg")
                .build();

        Portfolio portfolio3 = Portfolio.builder()
                .designer(designer1)
                .title("카페 모바일 앱 UI 디자인")
                .description("카페 주문 앱의 사용자 인터페이스를 디자인했습니다. 직관적이고 사용하기 쉬운 UI를 제공합니다.")
                .thumbnailUrl("https://example.com/portfolio3.jpg")
                .build();

        // 포트폴리오 저장
        portfolioRepository.saveAll(Arrays.asList(portfolio1, portfolio2, portfolio3));

        // 포트폴리오별 디자인 카테고리 설정
        // 포트폴리오1: 로고 디자인
        PortfolioDesignCategory portfolio1Category = PortfolioDesignCategory.builder()
                .portfolio(portfolio1)
                .designCategory(DesignCategory.LOGO)
                .build();
        portfolio1.getDesignCategories().add(portfolio1Category);

        // 포트폴리오2: 브랜드 디자인  
        PortfolioDesignCategory portfolio2Category = PortfolioDesignCategory.builder()
                .portfolio(portfolio2)
                .designCategory(DesignCategory.BRAND)
                .build();
        portfolio2.getDesignCategories().add(portfolio2Category);

        // 포트폴리오3: 굿즈 디자인
        PortfolioDesignCategory portfolio3Category = PortfolioDesignCategory.builder()
                .portfolio(portfolio3)
                .designCategory(DesignCategory.GOODS)
                .build();
        portfolio3.getDesignCategories().add(portfolio3Category);
    }

    @Test
    @DisplayName("단일 키워드로 포트폴리오 제목에서 검색")
    void findDesignersByKeywords_SingleKeyword_Title() {
        // given
        List<String> keywords = Arrays.asList("카페");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너1");
    }

    @Test
    @DisplayName("단일 키워드로 포트폴리오 설명에서 검색")
    void findDesignersByKeywords_SingleKeyword_Description() {
        // given
        List<String> keywords = Arrays.asList("반응형");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너1");
    }

    @Test
    @DisplayName("단일 키워드로 포트폴리오 디자인 카테고리에서 검색")
    void findDesignersByKeywords_SingleKeyword_PortfolioCategory() {
        // given
        List<String> keywords = Arrays.asList("logo");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너1");
    }

    @Test
    @DisplayName("단일 키워드로 사용자 전문분야에서 검색")
    void findDesignersByKeywords_SingleKeyword_UserSpecialty() {
        // given
        List<String> keywords = Arrays.asList("brand");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너1");
    }

    @Test
    @DisplayName("단일 키워드로 사용자 디자인 스타일에서 검색")
    void findDesignersByKeywords_SingleKeyword_UserDesignStyle() {
        // given
        List<String> keywords = Arrays.asList("cute");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then - 디자이너1과 디자이너3 모두 CUTE 스타일을 가짐
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getNickname)
                .containsExactlyInAnyOrder("디자이너1", "디자이너3");
    }

    @Test
    @DisplayName("여러 키워드로 검색 - OR 조건")
    void findDesignersByKeywords_MultipleKeywords_OrCondition() {
        // given
        List<String> keywords = Arrays.asList("카페", "브랜드");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getNickname)
                .containsExactlyInAnyOrder("디자이너1", "디자이너2");
    }

    @Test
    @DisplayName("한국어 키워드로 디자인 스타일 검색")
    void findDesignersByKeywords_KoreanKeyword_DesignStyle() {
        // given - "귀여운"은 DesignerMatchingService에서 "cute"로 매핑됨
        // when - 현재는 매핑 전이므로 영어 키워드로 시뮬레이션
        List<String> mappedKeywords = Arrays.asList("cute");
        List<User> result = portfolioRepository.findDesignersByKeywords(mappedKeywords);
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getNickname)
                .containsExactlyInAnyOrder("디자이너1", "디자이너3");
    }

    @Test
    @DisplayName("한국어 키워드로 전문분야 검색")
    void findDesignersByKeywords_KoreanKeyword_Specialty() {
        // given - "로고"는 DesignerMatchingService에서 "logo"로 매핑됨
        // when - 현재는 매핑 전이므로 영어 키워드로 시뮬레이션
        List<String> mappedKeywords = Arrays.asList("logo");
        List<User> result = portfolioRepository.findDesignersByKeywords(mappedKeywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너1");
    }

    @Test
    @DisplayName("혼합 키워드 검색 (한국어 + 영어)")
    void findDesignersByKeywords_MixedKeywords() {
        // given
        List<String> keywords = Arrays.asList("카페", "cute", "브랜드");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then - 카페(디자이너1), cute(디자이너1,3), 브랜드(디자이너2) → 모든 디자이너
        assertThat(result).hasSize(3);
        assertThat(result).extracting(User::getNickname)
                .containsExactlyInAnyOrder("디자이너1", "디자이너2", "디자이너3");
    }

    @Test
    @DisplayName("대소문자 구분 없이 검색")
    void findDesignersByKeywords_CaseInsensitive() {
        // given
        List<String> keywords = Arrays.asList("카페", "CUTE");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getNickname)
                .containsExactlyInAnyOrder("디자이너1", "디자이너3");
    }

    @Test
    @DisplayName("존재하지 않는 키워드로 검색")
    void findDesignersByKeywords_NonExistentKeyword() {
        // given
        List<String> keywords = Arrays.asList("존재하지않는키워드");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("빈 키워드 리스트로 검색")
    void findDesignersByKeywords_EmptyKeywords() {
        // given
        List<String> keywords = Arrays.asList();

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null 키워드 리스트로 검색")
    void findDesignersByKeywords_NullKeywords() {
        // given
        List<String> keywords = null;

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("공백 키워드 필터링")
    void findDesignersByKeywords_WhitespaceKeywords() {
        // given
        List<String> keywords = Arrays.asList("", " ", "카페", null);

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너1");
    }

    @Test
    @DisplayName("중복 디자이너 제거 확인")
    void findDesignersByKeywords_DistinctResults() {
        // given - 디자이너1이 여러 포트폴리오를 가지고 있음
        List<String> keywords = Arrays.asList("디자인");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        // 디자이너1이 여러 포트폴리오를 가져도 한 번만 반환되어야 함
        long designer1Count = result.stream()
                .filter(user -> user.getNickname().equals("디자이너1"))
                .count();
        assertThat(designer1Count).isLessThanOrEqualTo(1);
    }

    @Test
    @DisplayName("부분 문자열 매칭 확인")
    void findDesignersByKeywords_PartialStringMatch() {
        // given
        List<String> keywords = Arrays.asList("브랜드");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너2");
    }

    @Test
    @DisplayName("포트폴리오가 없는 디자이너도 전문분야/스타일로 검색됨")
    void findDesignersByKeywords_DesignerWithoutPortfolio() {
        // given - 디자이너3은 포트폴리오가 없지만 CUTE 스타일을 가짐
        List<String> keywords = Arrays.asList("cute");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then - 디자이너3도 검색 결과에 포함되어야 함
        assertThat(result).extracting(User::getNickname)
                .contains("디자이너3");
    }
}
