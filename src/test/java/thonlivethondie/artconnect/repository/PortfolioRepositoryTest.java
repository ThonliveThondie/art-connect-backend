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
import thonlivethondie.artconnect.entity.Store;
import thonlivethondie.artconnect.entity.User;
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

    @Autowired
    private StoreRepository storeRepository;

    private User designer1;
    private User designer2;
    private User designer3;
    private Store store1;
    private Store store2;

    @BeforeEach
    void setUp() {
        // 테스트용 디자이너 사용자 생성
        designer1 = User.builder()
                .email("designer1@test.com")
                .nickname("디자이너1")
                .specialty("웹 디자인")
                .userType(UserType.DESIGNER)
                .role(Role.USER)
                .socialType(SocialType.KAKAO)
                .build();

        designer2 = User.builder()
                .email("designer2@test.com")
                .nickname("디자이너2")
                .specialty("그래픽 디자인")
                .userType(UserType.DESIGNER)
                .role(Role.USER)
                .socialType(SocialType.GOOGLE)
                .build();

        designer3 = User.builder()
                .email("designer3@test.com")
                .nickname("디자이너3")
                .specialty("UI/UX 디자인")
                .userType(UserType.DESIGNER)
                .role(Role.USER)
                .socialType(SocialType.NAVER)
                .build();

        // 소상공인 사용자 생성
        User storeOwner1 = User.builder()
                .email("owner1@test.com")
                .nickname("사장님1")
                .userType(UserType.BUSINESS_OWNER)
                .role(Role.USER)
                .socialType(SocialType.KAKAO)
                .build();

        User storeOwner2 = User.builder()
                .email("owner2@test.com")
                .nickname("사장님2")
                .userType(UserType.BUSINESS_OWNER)
                .role(Role.USER)
                .socialType(SocialType.GOOGLE)
                .build();

        // 사용자 저장
        userRepository.saveAll(Arrays.asList(designer1, designer2, designer3, storeOwner1, storeOwner2));

        // 테스트용 매장 생성
        store1 = Store.builder()
                .user(storeOwner1)
                .storeName("카페 테스트")
                .phoneNumber("010-1111-1111")
                .address("서울시 강남구")
                .build();

        store2 = Store.builder()
                .user(storeOwner2)
                .storeName("레스토랑 테스트")
                .phoneNumber("010-2222-2222")
                .address("서울시 서초구")
                .build();

        storeRepository.saveAll(Arrays.asList(store1, store2));

        // 테스트용 포트폴리오 생성
        Portfolio portfolio1 = Portfolio.builder()
                .designer(designer1)
                .store(store1)
                .title("카페 웹사이트 리뉴얼")
                .description("모던하고 깔끔한 카페 웹사이트를 제작했습니다. 반응형 디자인을 적용하여 모바일에서도 완벽하게 동작합니다.")
                .category("웹사이트")
                .projectDuration("2주")
                .thumbnailUrl("https://example.com/portfolio1.jpg")
                .build();

        Portfolio portfolio2 = Portfolio.builder()
                .designer(designer2)
                .store(store2)
                .title("레스토랑 브랜딩 패키지")
                .description("이탈리안 레스토랑의 브랜드 아이덴티티를 구축했습니다. 로고, 메뉴판, 간판 등 전체적인 그래픽 디자인을 담당했습니다.")
                .category("브랜딩")
                .projectDuration("1개월")
                .thumbnailUrl("https://example.com/portfolio2.jpg")
                .build();

        Portfolio portfolio3 = Portfolio.builder()
                .designer(designer1)
                .store(store1)
                .title("카페 모바일 앱 UI 디자인")
                .description("카페 주문 앱의 사용자 인터페이스를 디자인했습니다. 직관적이고 사용하기 쉬운 UI를 제공합니다.")
                .category("모바일")
                .projectDuration("3주")
                .thumbnailUrl("https://example.com/portfolio3.jpg")
                .build();

        Portfolio portfolio4 = Portfolio.builder()
                .designer(designer3)
                .store(store2)
                .title("레스토랑 예약 시스템 UX 개선")
                .description("레스토랑 예약 시스템의 사용자 경험을 개선했습니다. 사용자 리서치를 통해 최적화된 UX를 제공합니다.")
                .category("UX")
                .projectDuration("2주")
                .thumbnailUrl("https://example.com/portfolio4.jpg")
                .build();

        // 포트폴리오 저장
        portfolioRepository.saveAll(Arrays.asList(portfolio1, portfolio2, portfolio3, portfolio4));
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
    @DisplayName("단일 키워드로 포트폴리오 카테고리에서 검색")
    void findDesignersByKeywords_SingleKeyword_Category() {
        // given
        List<String> keywords = Arrays.asList("브랜딩");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너2");
    }

    @Test
    @DisplayName("단일 키워드로 사용자 전문분야에서 검색")
    void findDesignersByKeywords_SingleKeyword_UserSpecialty() {
        // given
        List<String> keywords = Arrays.asList("UI/UX");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너3");
    }

    @Test
    @DisplayName("여러 키워드로 검색 - OR 조건")
    void findDesignersByKeywords_MultipleKeywords_OrCondition() {
        // given
        List<String> keywords = Arrays.asList("카페", "브랜딩");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getNickname)
                .containsExactlyInAnyOrder("디자이너1", "디자이너2");
    }

    @Test
    @DisplayName("대소문자 구분 없이 검색")
    void findDesignersByKeywords_CaseInsensitive() {
        // given
        List<String> keywords = Arrays.asList("웹사이트", "MOBILE");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너1");
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
        assertThat(designer1Count).isEqualTo(1);
    }

    @Test
    @DisplayName("부분 문자열 매칭 확인")
    void findDesignersByKeywords_PartialStringMatch() {
        // given
        List<String> keywords = Arrays.asList("그래픽");

        // when
        List<User> result = portfolioRepository.findDesignersByKeywords(keywords);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("디자이너2");
        assertThat(result.get(0).getSpecialty()).contains("그래픽");
    }
}
