package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.common.WorkRequestStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * 시안 및 피드백 내역 조회 응답 DTO
 */
public record WorkSubmissionListResponseDto(
    Long workRequestId,
    WorkRequestStatus status,
    String projectTitle,
    String storeName,
    String designerName,
    LocalDate endDate,
    List<WorkSubmissionResponseDto> submissions
) {
}
