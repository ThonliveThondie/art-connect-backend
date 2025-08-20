package thonlivethondie.artconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 디자이너 추천 새로고침 요청을 위한 DTO 클래스
 * 세션 ID를 포함하여 새로고침 요청을 처리합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequestDto {

    /**
     * 새로고침할 세션 ID
     */
    private String sessionId;
}
