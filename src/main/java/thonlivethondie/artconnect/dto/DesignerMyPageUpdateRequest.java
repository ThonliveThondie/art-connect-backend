package thonlivethondie.artconnect.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import thonlivethondie.artconnect.common.DesignCategory;
import thonlivethondie.artconnect.common.DesignStyle;

import java.util.List;

/**
 * 디자이너 마이페이지 수정 요청 DTO
 * 기본 정보 + 디자이너 전용 필드 포함
 */
public record DesignerMyPageUpdateRequest(
        String nickname,
        String phoneNumber,
        String education,
        String major,

        @NotEmpty(message = "디자인 카테고리를 최소 1개 선택해야 합니다.")
        @Size(max = 3, message = "디자인 카테고리는 최대 3개까지 선택 가능합니다.")
        List<DesignCategory> designCategories,

        @Size(max = 3, message = "디자인 스타일은 최대 3개까지 선택 가능합니다.")
        List<DesignStyle> designStyles
) {
}
