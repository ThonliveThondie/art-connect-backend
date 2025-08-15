package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thonlivethondie.artconnect.dto.AiProposalDto;
import thonlivethondie.artconnect.dto.RecommendedDesignerDto;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.repository.PortfolioRepository;

import java.util.Arrays;
import java.util.List;
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
    public List<RecommendedDesignerDto> findMatchingDesigners(AiProposalDto proposal) {
        log.info("디자이너 매칭 시작 - proposal: {}", proposal);

        // 1. AI 제안에서 키워드 추출
        List<String> keywords = extractKeywords(proposal);
        log.info("추출된 키워드: {}", keywords);

        // 2. 키워드를 사용하여 디자이너 검색
        List<User> matchingDesigners = portfolioRepository.findDesignersByKeywords(keywords);
        log.info("매칭된 디자이너 수: {}", matchingDesigners.size());

        // 3. User 엔티티를 RecommendedDesignerDto로 변환
        List<RecommendedDesignerDto> recommendedDesigners = convertToRecommendedDesignerDto(matchingDesigners);

        log.info("디자이너 매칭 완료 - 추천된 디자이너 수: {}", recommendedDesigners.size());
        return recommendedDesigners;
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
        dto.setProfileImageUrl(user.getImageUrl());

        // 경력과 평점은 현재 User 엔티티에 없으므로 기본값 설정
        // 추후 Portfolio나 다른 엔티티에서 계산하여 설정할 수 있음
        dto.setExperience(null);
        dto.setRating(null);

        return dto;
    }
}
