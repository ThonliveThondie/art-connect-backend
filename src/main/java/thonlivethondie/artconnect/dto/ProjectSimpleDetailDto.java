package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.entity.WorkRequest;

import java.time.LocalDate;

/**
 * 프로젝트 간소화된 상세 조회 DTO
 * 클릭한 프로젝트의 기본 정보만 포함
 */
public record ProjectSimpleDetailDto(
    String storeName,
    String designerName,
    LocalDate endDate
) {
    
    /**
     * WorkRequest 엔티티로부터 ProjectSimpleDetailDto 생성
     */
    public static ProjectSimpleDetailDto from(WorkRequest workRequest) {
        return new ProjectSimpleDetailDto(
            workRequest.getStore().getStoreName(),
            workRequest.getDesigner().getNickname(),
            workRequest.getEndDate()
        );
    }
}
