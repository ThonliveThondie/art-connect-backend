package thonlivethondie.artconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 디자이너 매칭 세션 정보를 저장하는 DTO 클래스
 * 한 번의 AI 추천 요청에 대한 전체 매칭 결과를 저장합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesignerMatchingSessionDto {

    /**
     * 세션 고유 ID
     */
    private String sessionId;

    /**
     * AI 제안 정보
     */
    private AiProposalDto proposal;

    /**
     * 점수순으로 정렬된 전체 매칭 디자이너 목록 (최대 10명)
     */
    private List<ScoredDesignerDto> allMatchingDesigners;

    /**
     * 세션 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 이미 반환된 디자이너 ID 목록 (중복 방지용)
     */
    private List<Long> returnedDesignerIds;
}
