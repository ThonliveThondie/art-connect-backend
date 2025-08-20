package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.common.WorkRequestStatus;
import thonlivethondie.artconnect.entity.WorkRequest;

/**
 * 소상공인용 완료된 프로젝트 응답 DTO
 * 소상공인이 완료된 프로젝트 목록 조회 시 사용
 */
public record CompletedProjectForBusinessOwnerDto(
    Long id,
    String projectTitle,
    String designerName,
    WorkRequestStatus status
) {
    
    /**
     * WorkRequest 엔티티로부터 CompletedProjectForBusinessOwnerDto 생성
     */
    public static CompletedProjectForBusinessOwnerDto from(WorkRequest workRequest) {
        return new CompletedProjectForBusinessOwnerDto(
            workRequest.getId(),
            workRequest.getProjectTitle(),
            workRequest.getDesigner().getNickname(),
            workRequest.getStatus()
        );
    }
}
