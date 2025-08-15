package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.common.DesignCategory;
import thonlivethondie.artconnect.common.DesignStyle;
import thonlivethondie.artconnect.common.UserType;
import thonlivethondie.artconnect.entity.User;

import java.util.List;

/**
 * 디자이너 마이페이지 응답 DTO
 * 기본 정보 + 디자이너 전용 필드 포함
 */
public record DesignerMyPageResponse(
        String nickname,
        String email,
        String phoneNumber,
        String imageUrl,
        UserType userType,
        String education,
        String major,
        List<DesignCategory> designCategories,
        List<DesignStyle> designStyles
) {
    public DesignerMyPageResponse {
        // 불변성을 위한 방어적 복사
        designCategories = designCategories != null ? List.copyOf(designCategories) : List.of();
        designStyles = designStyles != null ? List.copyOf(designStyles) : List.of();
    }

    public static DesignerMyPageResponse from(User user) {
        return new DesignerMyPageResponse(
                user.getNickname(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getImageUrl(),
                user.getUserType(),
                user.getEducation(),
                user.getMajor(),
                user.getSelectedSpecialities(),
                user.getSelectedDesignStyles()
        );
    }
}
