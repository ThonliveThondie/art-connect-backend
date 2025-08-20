package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.common.WorkRequestStatus;
import thonlivethondie.artconnect.entity.WorkRequest;

/**
 * 디자이너용 완료된 프로젝트 응답 DTO
 * 디자이너가 완료된 프로젝트 목록 조회 시 사용
 */
public record CompletedProjectForDesignerDto(
    Long id,
    String projectTitle,
    String businessOwnerName,
    String storeName,
    WorkRequestStatus status
) {
    
    /**
     * WorkRequest 엔티티로부터 CompletedProjectForDesignerDto 생성
     */
    public static CompletedProjectForDesignerDto from(WorkRequest workRequest) {
        return new CompletedProjectForDesignerDto(
            workRequest.getId(),
            workRequest.getProjectTitle(),
            workRequest.getBusinessOwner().getNickname(),
            workRequest.getStore().getStoreName(),
            workRequest.getStatus()
        );
    }
}
