package thonlivethondie.artconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 키워드 매칭 점수가 포함된 디자이너 정보 DTO 클래스
 * 디자이너 정보와 함께 매칭 점수를 저장합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoredDesignerDto {

    /**
     * 디자이너 정보
     */
    private RecommendedDesignerDto designer;

    /**
     * 키워드 매칭 점수
     */
    private int matchingScore;

    /**
     * 매칭된 키워드 목록 (디버깅용)
     */
    private java.util.List<String> matchedKeywords;
}
