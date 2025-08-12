package thonlivethondie.artconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI가 추천하는 디자이너 정보를 위한 DTO 클래스
 * 추천된 디자이너의 기본 정보를 담습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedDesignerDto {

    private Long userId;

    private String nickname;

    private String specialty;

    private Integer experience;

    // rating과 profileImageUrl은 아직 미지수
    private Double rating;
    private String profileImageUrl;
}
