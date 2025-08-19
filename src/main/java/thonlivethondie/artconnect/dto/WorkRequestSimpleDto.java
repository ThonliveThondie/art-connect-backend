package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.entity.WorkRequest;

/**
 * 작업의뢰서 간소화된 정보를 담는 DTO
 * 목록 조회 시 필요한 핵심 정보만 포함
 */
public record WorkRequestSimpleDto(
        Long id,
        String projectTitle,
        String storeName,
        Long budget
) {
    
    /**
     * WorkRequest 엔티티로부터 WorkRequestSimpleDto 생성
     */
    public static WorkRequestSimpleDto from(WorkRequest workRequest) {
        return new WorkRequestSimpleDto(
                workRequest.getId(),
                workRequest.getProjectTitle(),
                workRequest.getStore().getStoreName(),
                workRequest.getBudget()
        );
    }
}
