package thonlivethondie.artconnect.dto;

import java.time.LocalDateTime;

/**
 * 피드백 DTO
 */
public record FeedbackDto(
    Long id,
    String content,
    String authorName,
    String authorType, // "BUSINESS_OWNER" 또는 "DESIGNER"
    LocalDateTime createdAt
) {
}
