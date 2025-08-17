package thonlivethondie.artconnect.dto;

import lombok.Builder;
import thonlivethondie.artconnect.common.DesignCategory;
import thonlivethondie.artconnect.entity.Portfolio;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PortfolioResponseDto(
        Long portfolioId,
        Long designerId,
        String designerNickname,
        String title,
        String description,
        List<DesignCategory> designCategories,
        List<PortfolioImageDto> portfolioImages,
        String thumbnailUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public PortfolioResponseDto {
        // 불변성을 위한 방어적 복사
        designCategories = designCategories != null ? List.copyOf(designCategories) : List.of();
        portfolioImages = portfolioImages != null ? List.copyOf(portfolioImages) : List.of();
    }

    public static PortfolioResponseDto from(Portfolio portfolio) {
        return PortfolioResponseDto.builder()
                .portfolioId(portfolio.getId())
                .designerId(portfolio.getDesigner().getId())
                .designerNickname(portfolio.getDesigner().getNickname())
                .title(portfolio.getTitle())
                .description(portfolio.getDescription())
                .designCategories(portfolio.getSelectedDesignCategories())
                .portfolioImages(portfolio.getPortfolioImages().stream()
                        .map(PortfolioImageDto::from)
                        .toList())
                .thumbnailUrl(portfolio.getThumbnailUrl())
                .createdAt(portfolio.getCreateDate())
                .updatedAt(portfolio.getUpdatedDate())
                .build();
    }

    // 편의 메서드들
    public boolean hasImages() {
        return !portfolioImages.isEmpty();
    }

    public int getImageCount() {
        return portfolioImages.size();
    }

    public boolean hasThumbnail() {
        return thumbnailUrl != null && !thumbnailUrl.isEmpty();
    }

    public int getCategoryCount() {
        return designCategories.size();
    }
}
