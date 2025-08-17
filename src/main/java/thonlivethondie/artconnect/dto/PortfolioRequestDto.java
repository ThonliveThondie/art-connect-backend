package thonlivethondie.artconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import thonlivethondie.artconnect.common.DesignCategory;

import java.util.List;

public record PortfolioRequestDto(
        @NotBlank(message = "포트폴리오 제목은 필수입니다.")
        @Size(max = 200, message = "포트폴리오 제목은 200자 이하여야 합니다.")
        String title,

        @Size(max = 3, message = "디자인 카테고리는 최대 3개까지 선택 가능합니다.")
        List<DesignCategory> designCategories,

        String description
) {
}
