package thonlivethondie.artconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 포트폴리오 이미지의 간단한 정보를 위한 DTO 클래스
 * 순환 참조를 방지하기 위해 필요한 정보만 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioImageSimpleDto {

  private Long id;
  
  private String imageUrl;
  
  private String imageName;
  
  private Boolean isThumbnail;
}
