package thonlivethondie.artconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI가 생성한 디자인 제안을 위한 DTO 클래스
 * AI가 분석한 디자인 방향성과 타겟 고객, 필요한 디자인 정보를 담습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiProposalDto {

    private String designDirection;

    private String targetCustomer;

    private String requiredDesigns;
}
