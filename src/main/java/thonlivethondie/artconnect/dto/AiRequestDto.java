package thonlivethondie.artconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI 요청을 위한 DTO 클래스
 * 사용자가 AI에게 보내는 프롬프트 정보를 담습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiRequestDto {

    private String prompt;
}
