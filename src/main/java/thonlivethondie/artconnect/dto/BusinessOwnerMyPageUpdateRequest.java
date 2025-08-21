package thonlivethondie.artconnect.dto;

/**
 * 소상공인 마이페이지 수정 요청 DTO
 * 기본 정보만 포함
 */
public record BusinessOwnerMyPageUpdateRequest(
        String nickname,
        String phoneNumber
) {
}
