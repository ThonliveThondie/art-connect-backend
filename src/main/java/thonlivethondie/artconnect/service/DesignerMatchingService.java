package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thonlivethondie.artconnect.common.DesignCategory;
import thonlivethondie.artconnect.common.DesignStyle;
import thonlivethondie.artconnect.dto.*;
import thonlivethondie.artconnect.entity.Portfolio;
import thonlivethondie.artconnect.entity.PortfolioDesignCategory;
import thonlivethondie.artconnect.entity.PortfolioImage;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.repository.PortfolioRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesignerMatchingService {

    private final PortfolioRepository portfolioRepository;

    /**
     * AI의 제안을 기반으로 적합한 디자이너를 찾습니다.
     *
     * @param proposal AI가 생성한 디자인 제안
     * @return 추천된 디자이너 목록
     */
    @Transactional(readOnly = true)
    public List<RecommendedDesignerDto> findMatchingDesigners(AiProposalDto proposal) {
        log.info("디자이너 매칭 시작 - proposal: {}", proposal);

        // 1. AI 제안에서 키워드 추출
        List<String> keywords = extractKeywords(proposal);
        log.info("추출된 키워드: {}", keywords);

        // 2. 한국어 키워드를 영어 키워드로 변환 (매핑)
        List<String> mappedKeywords = mapKoreanToEnglishKeywords(keywords);
        log.info("매핑된 키워드: {}", mappedKeywords);

        // 3. 원본 키워드와 매핑된 키워드를 합쳐서 검색
        List<String> allKeywords = Stream.concat(keywords.stream(), mappedKeywords.stream())
                .distinct()
                .collect(Collectors.toList());
        log.info("최종 검색 키워드: {}", allKeywords);

        // 4. 키워드를 사용하여 디자이너 검색
        List<User> matchingDesigners = portfolioRepository.findDesignersByKeywords(allKeywords);
        log.info("매칭된 디자이너 수: {}", matchingDesigners.size());

        // 5. User 엔티티를 RecommendedDesignerDto로 변환
        List<RecommendedDesignerDto> recommendedDesigners = convertToRecommendedDesignerDto(matchingDesigners);

        log.info("디자이너 매칭 완료 - 추천된 디자이너 수: {}", recommendedDesigners.size());
        return recommendedDesigners;
    }

    /**
     * AI의 제안을 기반으로 점수가 매겨진 디자이너 목록을 반환합니다.
     * 키워드 매칭 점수에 따라 정렬되며, 최대 10명까지 반환합니다.
     *
     * @param proposal AI가 생성한 디자인 제안
     * @return 점수순으로 정렬된 디자이너 목록 (최대 10명)
     */
    @Transactional(readOnly = true)
    public List<ScoredDesignerDto> findScoredMatchingDesigners(AiProposalDto proposal) {
        log.info("점수 기반 디자이너 매칭 시작 - proposal: {}", proposal);

        // 1. AI 제안에서 키워드 추출
        List<String> keywords = extractKeywords(proposal);
        log.info("추출된 키워드: {}", keywords);

        // 2. 한국어 키워드를 영어 키워드로 변환 (매핑)
        List<String> mappedKeywords = mapKoreanToEnglishKeywords(keywords);
        log.info("매핑된 키워드: {}", mappedKeywords);

        // 3. 원본 키워드와 매핑된 키워드를 합쳐서 검색
        List<String> allKeywords = Stream.concat(keywords.stream(), mappedKeywords.stream())
                .distinct()
                .collect(Collectors.toList());
        log.info("최종 검색 키워드: {}", allKeywords);

        // 4. 키워드를 사용하여 디자이너 검색
        List<User> matchingDesigners = portfolioRepository.findDesignersByKeywords(allKeywords);
        log.info("매칭된 디자이너 수: {}", matchingDesigners.size());

        // 5. 각 디자이너에 대해 키워드 매칭 점수 계산
        List<ScoredDesignerDto> scoredDesigners = calculateMatchingScores(matchingDesigners, allKeywords);

        // 6. 점수순으로 정렬하고 상위 10명만 선택
        List<ScoredDesignerDto> topDesigners = scoredDesigners.stream()
                .sorted((a, b) -> Integer.compare(b.getMatchingScore(), a.getMatchingScore()))
                .limit(10)
                .collect(Collectors.toList());

        log.info("점수 기반 디자이너 매칭 완료 - 상위 디자이너 수: {}", topDesigners.size());
        return topDesigners;
    }

    /**
     * 한국어 키워드를 영어 키워드로 매핑합니다.
     * DesignStyle과 DesignCategory enum의 description을 name으로 변환합니다.
     *
     * @param keywords 원본 키워드 목록
     * @return 매핑된 영어 키워드 목록
     */
    private List<String> mapKoreanToEnglishKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }

        return keywords.stream()
                .flatMap(keyword -> {
                    String trimmedKeyword = keyword.trim();
                    return Stream.of(
                            mapDesignStyleKeyword(trimmedKeyword),
                            mapDesignCategoryKeyword(trimmedKeyword)
                    ).filter(mapped -> !mapped.isEmpty());
                })
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 한국어 키워드를 DesignStyle enum name으로 매핑합니다.
     *
     * @param keyword 한국어 키워드
     * @return 매핑된 영어 키워드 (소문자)
     */
    private String mapDesignStyleKeyword(String keyword) {
        for (DesignStyle style : DesignStyle.values()) {
            if (style.getDescription().contains(keyword) || keyword.contains(style.getDescription())) {
                return style.name().toLowerCase();
            }
        }
        return "";
    }

    /**
     * 한국어 키워드를 DesignCategory enum name으로 매핑합니다.
     *
     * @param keyword 한국어 키워드
     * @return 매핑된 영어 키워드 (소문자)
     */
    private String mapDesignCategoryKeyword(String keyword) {
        for (DesignCategory category : DesignCategory.values()) {
            if (category.getDescription().contains(keyword) || keyword.contains(category.getDescription())) {
                return category.name().toLowerCase();
            }
        }
        return "";
    }

    /**
     * AI 제안에서 키워드를 추출합니다.
     * designDirection, targetCustomer, requiredDesigns 필드에서 의미있는 키워드를 추출합니다.
     *
     * @param proposal AI 제안 DTO
     * @return 추출된 키워드 목록
     */
    private List<String> extractKeywords(AiProposalDto proposal) {
        if (proposal == null) {
            return List.of();
        }

        return Stream.of(
                        extractKeywordsFromText(proposal.getDesignDirection()),
                        extractKeywordsFromText(proposal.getTargetCustomer()),
                        extractKeywordsFromText(proposal.getRequiredDesigns())
                )
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 텍스트에서 키워드를 추출합니다.
     * 쉼표, 공백을 기준으로 분리하되, 의미있는 복합어는 보존합니다.
     *
     * @param text 키워드를 추출할 텍스트
     * @return 추출된 키워드 목록
     */
    private List<String> extractKeywordsFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        // 먼저 쉼표와 큰 단위로 분리
        return Arrays.stream(text.split("[,]+"))
                .map(String::trim)
                .filter(phrase -> !phrase.isEmpty())
                .flatMap(phrase -> {
                    // 각 구문을 더 세밀하게 분리하되, 복합어는 보존
                    return Arrays.stream(phrase.split("[\\s\\-/&()]+"))
                            .map(String::trim)
                            .filter(keyword -> !keyword.isEmpty() && keyword.length() >= 2)
                            .filter(this::isValidKeyword);
                })
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 키워드가 유효한지 검증합니다.
     * 의미없는 단어들을 필터링합니다.
     *
     * @param keyword 검증할 키워드
     * @return 유효한 키워드인 경우 true
     */
    private boolean isValidKeyword(String keyword) {
        // 불용어 목록 (의미없는 단어들)
        String[] stopWords = {
                "그리고", "하고", "또는", "등", "같은", "느낌", "스타일", "디자인",
                "위한", "대한", "있는", "하는", "되는", "것", "수", "를", "을", "이", "가", "의", "에", "로", "으로"
        };

        String lowerKeyword = keyword.toLowerCase();

        // 불용어 체크
        for (String stopWord : stopWords) {
            if (lowerKeyword.equals(stopWord)) {
                return false;
            }
        }

        // 숫자만 있는 키워드 제외
        if (keyword.matches("\\d+")) {
            return false;
        }

        return true;
    }

    /**
     * User 엔티티 목록을 RecommendedDesignerDto 목록으로 변환합니다.
     *
     * @param users 변환할 User 엔티티 목록
     * @return 변환된 RecommendedDesignerDto 목록
     */
    private List<RecommendedDesignerDto> convertToRecommendedDesignerDto(List<User> users) {
        return users.stream()
                .map(this::convertToRecommendedDesignerDto)
                .collect(Collectors.toList());
    }

    /**
     * User 엔티티를 RecommendedDesignerDto로 변환합니다.
     *
     * @param user 변환할 User 엔티티
     * @return 변환된 RecommendedDesignerDto
     */
    private RecommendedDesignerDto convertToRecommendedDesignerDto(User user) {
        RecommendedDesignerDto dto = new RecommendedDesignerDto();
        dto.setUserId(user.getId());
        dto.setNickname(user.getNickname());
        
        // 전문분야 목록을 문자열로 변환
        String specialtiesStr = user.getSelectedSpecialities().stream()
                .map(category -> category.getDescription())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        dto.setSpecialty(specialtiesStr);
        
        // 프로필 이미지 URL 설정
        dto.setProfileImageUrl(user.getImageUrl());
        
        // 포트폴리오 이미지 최대 2개 가져오기
        List<PortfolioImageSimpleDto> portfolioImages = getPortfolioImages(user.getId());
        dto.setPortfolioImageUrl(portfolioImages);

        return dto;
    }

    /**
     * 디자이너의 포트폴리오 이미지를 최대 2개까지 가져옵니다.
     * 썸네일 이미지를 우선적으로 선택하고, 없으면 일반 이미지를 선택합니다.
     *
     * @param userId 디자이너의 사용자 ID
     * @return 포트폴리오 이미지 DTO 목록 (최대 2개)
     */
    private List<PortfolioImageSimpleDto> getPortfolioImages(Long userId) {
        try {
            // 해당 디자이너의 포트폴리오 목록 조회
            List<Portfolio> portfolios = portfolioRepository.findByDesignerId(userId);
            
            if (portfolios.isEmpty()) {
                return List.of();
            }

            // 모든 포트폴리오에서 이미지 수집
            List<PortfolioImage> allImages = portfolios.stream()
                    .flatMap(portfolio -> portfolio.getPortfolioImages().stream())
                    .collect(Collectors.toList());

            if (allImages.isEmpty()) {
                return List.of();
            }

            // 썸네일 이미지 우선 선택
            List<PortfolioImage> thumbnailImages = allImages.stream()
                    .filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
                    .limit(2)
                    .collect(Collectors.toList());

            // 썸네일이 2개 미만이면 일반 이미지로 보충
            if (thumbnailImages.size() < 2) {
                List<PortfolioImage> nonThumbnailImages = allImages.stream()
                        .filter(image -> !Boolean.TRUE.equals(image.getIsThumbnail()))
                        .limit(2 - thumbnailImages.size())
                        .collect(Collectors.toList());
                
                thumbnailImages.addAll(nonThumbnailImages);
            }

            // PortfolioImage 엔티티를 PortfolioImageSimpleDto로 변환
            return thumbnailImages.stream()
                    .map(this::convertToPortfolioImageSimpleDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("포트폴리오 이미지 조회 중 오류 발생 (userId: {}): {}", userId, e.getMessage());
            return List.of();
        }
    }

    /**
     * PortfolioImage 엔티티를 PortfolioImageSimpleDto로 변환합니다.
     * 순환 참조를 방지하기 위해 필요한 정보만 추출합니다.
     *
     * @param portfolioImage 변환할 PortfolioImage 엔티티
     * @return 변환된 PortfolioImageSimpleDto
     */
    private PortfolioImageSimpleDto convertToPortfolioImageSimpleDto(PortfolioImage portfolioImage) {
        PortfolioImageSimpleDto dto = new PortfolioImageSimpleDto();
        dto.setId(portfolioImage.getId());
        dto.setImageUrl(portfolioImage.getImageUrl());
        dto.setImageName(portfolioImage.getImageName());
        dto.setIsThumbnail(portfolioImage.getIsThumbnail());
        
        return dto;
    }

    /**
     * 디자이너 목록에 대해 키워드 매칭 점수를 계산합니다.
     *
     * @param designers 점수를 계산할 디자이너 목록
     * @param keywords 매칭에 사용할 키워드 목록
     * @return 점수가 계산된 디자이너 목록
     */
    private List<ScoredDesignerDto> calculateMatchingScores(List<User> designers, List<String> keywords) {
        return designers.stream()
                .map(designer -> calculateDesignerScore(designer, keywords))
                .collect(Collectors.toList());
    }

    /**
     * 개별 디자이너에 대해 키워드 매칭 점수를 계산합니다.
     *
     * @param designer 점수를 계산할 디자이너
     * @param keywords 매칭에 사용할 키워드 목록
     * @return 점수가 계산된 디자이너 DTO
     */
    private ScoredDesignerDto calculateDesignerScore(User designer, List<String> keywords) {
        int totalScore = 0;
        List<String> matchedKeywords = new ArrayList<>();

        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            int keywordScore = 0;

            // 1. 전문분야 매칭 (가중치: 2)
            if (designer.getSelectedSpecialities() != null) {
                for (DesignCategory category : designer.getSelectedSpecialities()) {
                    if (category.name().toLowerCase().contains(lowerKeyword) || 
                        category.getDescription().toLowerCase().contains(lowerKeyword)) {
                        keywordScore += 2;
                        break;
                    }
                }
            }

            // 2. 디자인 스타일 매칭 (가중치: 3)
            if (designer.getSelectedDesignStyles() != null) {
                for (DesignStyle style : designer.getSelectedDesignStyles()) {
                    if (style.name().toLowerCase().contains(lowerKeyword) || 
                        style.getDescription().toLowerCase().contains(lowerKeyword)) {
                        keywordScore += 3;
                        break;
                    }
                }
            }

            // 3. 포트폴리오 매칭 (가중치: 1)
            if (designer.getPortfolios() != null) {
                for (Portfolio portfolio : designer.getPortfolios()) {
                    boolean portfolioMatched = false;

                    // 포트폴리오 제목 매칭
                    if (portfolio.getTitle() != null && 
                        portfolio.getTitle().toLowerCase().contains(lowerKeyword)) {
                        keywordScore += 1;
                        portfolioMatched = true;
                    }

                    // 포트폴리오 설명 매칭
                    if (!portfolioMatched && portfolio.getDescription() != null && 
                        portfolio.getDescription().toLowerCase().contains(lowerKeyword)) {
                        keywordScore += 1;
                        portfolioMatched = true;
                    }

                    // 포트폴리오 디자인 카테고리 매칭
                    if (!portfolioMatched && portfolio.getDesignCategories() != null) {
                        for (PortfolioDesignCategory portfolioDesignCategory : portfolio.getDesignCategories()) {
                            DesignCategory category = portfolioDesignCategory.getDesignCategory();
                            if (category.name().toLowerCase().contains(lowerKeyword) || 
                                category.getDescription().toLowerCase().contains(lowerKeyword)) {
                                keywordScore += 1;
                                break;
                            }
                        }
                    }
                }
            }

            if (keywordScore > 0) {
                totalScore += keywordScore;
                matchedKeywords.add(keyword);
            }
        }

        // RecommendedDesignerDto 생성
        RecommendedDesignerDto designerDto = convertToRecommendedDesignerDto(designer);

        // ScoredDesignerDto 생성
        return new ScoredDesignerDto(designerDto, totalScore, matchedKeywords);
    }
}
