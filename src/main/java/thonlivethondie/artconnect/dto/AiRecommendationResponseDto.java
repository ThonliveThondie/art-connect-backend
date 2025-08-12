package thonlivethondie.artconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * AI 추천 서비스의 전체 응답을 위한 DTO 클래스
 * AI가 생성한 디자인 제안과 추천 디자이너 목록을 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendationResponseDto {

    private AiProposalDto proposal;

    private List<RecommendedDesignerDto> recommendedDesigners;
}
