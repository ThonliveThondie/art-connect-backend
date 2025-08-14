package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.common.DesignCategory;
import thonlivethondie.artconnect.common.WorkRequestStatus;
import thonlivethondie.artconnect.entity.WorkRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WorkRequestResponseDto(
        Long id,
        String projectTitle,
        String businessOwnerName,
        String designerName,
        String storeName,
        LocalDate endDate,
        Long budget,
        String productService,
        String targetCustomers,
        String nowStatus,
        String goal,
        List<DesignCategory> designCategories,
        String additionalDescription,
        String additionalRequirement,
        WorkRequestStatus status,
        LocalDateTime createdDate,
        List<WorkRequestImageDto> images
) {
    public WorkRequestResponseDto {
        // 불변성을 위한 방어적 복사
        designCategories = designCategories != null ? List.copyOf(designCategories) : List.of();
        images = images != null ? List.copyOf(images) : List.of();
    }

    // 정적 팩토리 메서드
    public static WorkRequestResponseDto from(WorkRequest workRequest) {
        return new WorkRequestResponseDto(
                workRequest.getId(),
                workRequest.getProjectTitle(),
                workRequest.getBusinessOwner().getNickname(),
                workRequest.getDesigner().getNickname(),
                workRequest.getStore().getStoreName(),
                workRequest.getEndDate(),
                workRequest.getBudget(),
                workRequest.getProductService(),
                workRequest.getTargetCustomers(),
                workRequest.getNowStatus(),
                workRequest.getGoal(),
                workRequest.getSelectedDesignCategories(),
                workRequest.getAdditionalDescription(),
                workRequest.getAdditionalRequirement(),
                workRequest.getStatus(),
                workRequest.getCreateDate(),
                workRequest.getWorkRequestImages().stream()
                        .map(WorkRequestImageDto::from)
                        .toList()
        );
    }

    // 편의 메서드들
    public boolean isAccepted() {
        return status == WorkRequestStatus.ACCEPTED;
    }

    public boolean isPending() {
        return status == WorkRequestStatus.PENDING;
    }

    public int getDesignCategoryCount() {
        return designCategories.size();
    }

    public boolean isWithImages() {
        return !images.isEmpty();
    }
}
