package thonlivethondie.artconnect.dto;

import lombok.Builder;
import thonlivethondie.artconnect.common.DesignCategory;
import thonlivethondie.artconnect.common.DesignStyle;
import thonlivethondie.artconnect.entity.User;

import java.util.List;

/**
 * 디자이너의 기본 정보를 담는 DTO
 * 소상공인이 디자이너 정보를 조회할 때 사용
 */
@Builder
public record DesignerInfoDto(
        Long designerId,
        String nickname,
        String education,
        String major,
        List<DesignCategory> specialities,
        List<DesignStyle> designStyles,
        String profileImageUrl
) {
    public DesignerInfoDto {
        // 불변성을 위한 방어적 복사
        specialities = specialities != null ? List.copyOf(specialities) : List.of();
        designStyles = designStyles != null ? List.copyOf(designStyles) : List.of();
    }

    /**
     * User 엔티티로부터 DesignerInfoDto 생성
     */
    public static DesignerInfoDto from(User designer) {
        return DesignerInfoDto.builder()
                .designerId(designer.getId())
                .nickname(designer.getNickname())
                .education(designer.getEducation())
                .major(designer.getMajor())
                .specialities(designer.getSelectedSpecialities())
                .designStyles(designer.getSelectedDesignStyles())
                .profileImageUrl(designer.getImageUrl())
                .build();
    }

    // 편의 메서드들
    public boolean hasEducation() {
        return education != null && !education.trim().isEmpty();
    }

    public boolean hasMajor() {
        return major != null && !major.trim().isEmpty();
    }

    public boolean hasSpecialities() {
        return !specialities.isEmpty();
    }

    public boolean hasDesignStyles() {
        return !designStyles.isEmpty();
    }

    public int getSpecialityCount() {
        return specialities.size();
    }

    public int getDesignStyleCount() {
        return designStyles.size();
    }
}
