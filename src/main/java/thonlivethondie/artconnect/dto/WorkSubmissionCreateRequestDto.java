package thonlivethondie.artconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 시안 제출 요청 DTO
 * 디자이너가 comment와 시안 사진을 업로드할 때 사용
 */
public record WorkSubmissionCreateRequestDto(
    @NotBlank(message = "코멘트는 필수입니다.")
    @Size(max = 2000, message = "코멘트는 2000자를 초과할 수 없습니다.")
    String comment
) {
}
