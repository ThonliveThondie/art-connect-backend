package thonlivethondie.artconnect.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 시안 제출 응답 DTO
 */
public record WorkSubmissionResponseDto(
    Long id,
    String comment,
    String storeName,
    String designerName,
    LocalDate endDate,
    LocalDateTime createdAt,
    List<WorkSubmissionImageDto> images,
    List<FeedbackDto> feedbacks
) {
}
