package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.common.UserType;

/**
 * 소상공인 마이페이지 응답 DTO
 * 기본 정보만 포함
 */
public record BusinessOwnerMyPageResponse(
        String nickname,
        String email,
        String phoneNumber,
        String imageUrl,
        UserType userType
) {
    public static BusinessOwnerMyPageResponse from(
            String nickname,
            String email,
            String phoneNumber,
            String imageUrl,
            UserType userType
    ) {
        return new BusinessOwnerMyPageResponse(
                nickname,
                email,
                phoneNumber,
                imageUrl,
                userType
        );
    }
}
