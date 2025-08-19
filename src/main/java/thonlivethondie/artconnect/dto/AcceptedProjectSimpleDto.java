package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.common.WorkRequestStatus;
import thonlivethondie.artconnect.entity.WorkRequest;

/**
 * 수락한 프로젝트 간소화된 응답 DTO
 * 디자이너가 수락한 프로젝트 목록 조회 시 사용
 */
public record AcceptedProjectSimpleDto(
    Long id,
    String projectTitle,
    String designerName,
    WorkRequestStatus status
) {
    
    /**
     * WorkRequest 엔티티로부터 AcceptedProjectSimpleDto 생성
     */
    public static AcceptedProjectSimpleDto from(WorkRequest workRequest) {
        return new AcceptedProjectSimpleDto(
            workRequest.getId(),
            workRequest.getProjectTitle(),
            workRequest.getDesigner().getNickname(),
            workRequest.getStatus()
        );
    }
}
