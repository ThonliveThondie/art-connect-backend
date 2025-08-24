package thonlivethondie.artconnect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import thonlivethondie.artconnect.dto.PortfolioRequestDto;
import thonlivethondie.artconnect.dto.PortfolioResponseDto;
import thonlivethondie.artconnect.service.PortfolioService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * 새 포트폴리오 생성
     */
    @PostMapping
    public ResponseEntity<PortfolioResponseDto> createPortfolio(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid PortfolioRequestDto request) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        PortfolioResponseDto response = portfolioService.createPortfolio(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 포트폴리오 업데이트
     */
    @PutMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponseDto> updatePortfolio(
            @PathVariable Long portfolioId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid PortfolioRequestDto request) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        PortfolioResponseDto response = portfolioService.updatePortfolio(userId, portfolioId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 내 포트폴리오 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<PortfolioResponseDto>> getMyPortfolios(@AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        List<PortfolioResponseDto> portfolios = portfolioService.getMyPortfolios(userId);

        return ResponseEntity.ok(portfolios);
    }

    /**
     * 특정 포트폴리오 조회
     */
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponseDto> getPortfolio(
            @PathVariable Long portfolioId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        PortfolioResponseDto portfolio = portfolioService.getPortfolio(userId, portfolioId);

        return ResponseEntity.ok(portfolio);
    }

    /**
     * 소상공인용 - 특정 디자이너의 포트폴리오 목록 조회
     */
    @GetMapping("/designer/{designerId}")
    public ResponseEntity<List<PortfolioResponseDto>> getDesignerPortfolios(
            @PathVariable Long designerId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        List<PortfolioResponseDto> portfolios = portfolioService.getDesignerPortfolios(designerId);

        return ResponseEntity.ok(portfolios);
    }

    /**
     * 소상공인용 - 특정 디자이너의 특정 포트폴리오 조회
     */
    @GetMapping("/designer/{designerId}/{portfolioId}")
    public ResponseEntity<PortfolioResponseDto> getDesignerPortfolio(
            @PathVariable Long designerId,
            @PathVariable Long portfolioId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        PortfolioResponseDto portfolio = portfolioService.getDesignerPortfolio(designerId, portfolioId);

        return ResponseEntity.ok(portfolio);
    }

    /**
     * 포트폴리오 삭제
     */
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(
            @PathVariable Long portfolioId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        portfolioService.deletePortfolio(userId, portfolioId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 포트폴리오 이미지 업로드
     */
    @PostMapping("/{portfolioId}/images")
    public ResponseEntity<PortfolioResponseDto> uploadPortfolioImages(
            @PathVariable Long portfolioId,
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("포트폴리오 이미지 업로드 요청 - userId: {}, portfolioId: {}, 이미지 개수: {}", userId, portfolioId, images.size());

        PortfolioResponseDto response = portfolioService.uploadPortfolioImages(userId, portfolioId, images);

        return ResponseEntity.ok(response);
    }

    /**
     * 포트폴리오 이미지 삭제
     */
    @DeleteMapping("/{portfolioId}/images/{imageId}")
    public ResponseEntity<PortfolioResponseDto> deletePortfolioImage(
            @PathVariable Long portfolioId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // UserDetails에서 userId 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        log.info("포트폴리오 이미지 삭제 요청 - userId: {}, portfolioId: {}, imageId: {}", userId, portfolioId, imageId);

        PortfolioResponseDto response = portfolioService.deletePortfolioImage(userId, portfolioId, imageId);

        return ResponseEntity.ok(response);
    }
}
