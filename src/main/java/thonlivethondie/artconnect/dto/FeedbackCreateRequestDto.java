package thonlivethondie.artconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 피드백 작성 요청 DTO
 */
public record FeedbackCreateRequestDto(
    @NotBlank(message = "피드백 내용은 필수입니다.")
    @Size(max = 1000, message = "피드백 내용은 1000자를 초과할 수 없습니다.")
    String content
) {
}
