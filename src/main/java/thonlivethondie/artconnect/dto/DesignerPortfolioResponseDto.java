package thonlivethondie.artconnect.dto;

import lombok.Builder;
import thonlivethondie.artconnect.entity.Portfolio;
import thonlivethondie.artconnect.entity.User;

import java.util.List;

/**
 * 소상공인이 특정 디자이너의 포트폴리오 목록을 조회할 때 사용하는 DTO
 * 디자이너 정보와 해당 디자이너의 포트폴리오 목록을 함께 제공
 */
@Builder
public record DesignerPortfolioResponseDto(
        DesignerInfoDto designerInfo,
        List<PortfolioResponseDto> portfolios,
        int totalPortfolioCount
) {
    public DesignerPortfolioResponseDto {
        // 불변성을 위한 방어적 복사
        portfolios = portfolios != null ? List.copyOf(portfolios) : List.of();
    }

    /**
     * 디자이너 정보와 포트폴리오 목록으로부터 DesignerPortfolioResponseDto 생성
     */
    public static DesignerPortfolioResponseDto of(User designer, List<Portfolio> portfolios) {
        List<PortfolioResponseDto> portfolioResponseDtos = portfolios.stream()
                .map(PortfolioResponseDto::from)
                .toList();

        return DesignerPortfolioResponseDto.builder()
                .designerInfo(DesignerInfoDto.from(designer))
                .portfolios(portfolioResponseDtos)
                .totalPortfolioCount(portfolioResponseDtos.size())
                .build();
    }

    // 편의 메서드들
    public boolean hasPortfolios() {
        return !portfolios.isEmpty();
    }

    public boolean isEmpty() {
        return portfolios.isEmpty();
    }

    public String getDesignerNickname() {
        return designerInfo != null ? designerInfo.nickname() : null;
    }

    public Long getDesignerId() {
        return designerInfo != null ? designerInfo.designerId() : null;
    }
}
