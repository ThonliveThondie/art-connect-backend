package thonlivethondie.artconnect.dto;

import jakarta.validation.constraints.*;
import thonlivethondie.artconnect.common.DesignCategory;

import java.time.LocalDate;
import java.util.List;

public record WorkRequestCreateRequestDto(
        @NotBlank(message = "프로젝트명은 필수입니다.")
        @Size(max = 200, message = "프로젝트명은 200자를 초과할 수 없습니다.")
        String projectTitle,

        @Future(message = "희망 납기일은 현재 날짜 이후여야 합니다.")
        LocalDate endDate,

        @Positive(message = "예산은 0보다 커야 합니다.")
        Long budget,

        @NotBlank(message = "제품/서비스 소개는 필수입니다.")
        String productService,

        @NotBlank(message = "타겟 고객층은 필수입니다.")
        String targetCustomers,

        String nowStatus,

        String goal,

        @NotEmpty(message = "디자인 카테고리를 최소 1개 선택해야 합니다.")
        @Size(max = 3, message = "디자인 카테고리는 최대 3개까지 선택 가능합니다.")
        List<DesignCategory> designCategories,

        String additionalDescription,

        String additionalRequirement
) {
    public WorkRequestCreateRequestDto {

        if (designCategories != null && designCategories.size() > 3) {
            throw new IllegalArgumentException("디자인 카테고리는 최대 3개까지 선택 가능합니다.");
        }

        // 불변성을 위한 방어적 복사
        designCategories = designCategories != null ? List.copyOf(designCategories) : List.of();
    }
}
