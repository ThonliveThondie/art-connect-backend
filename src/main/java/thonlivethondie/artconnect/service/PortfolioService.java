package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import thonlivethondie.artconnect.common.UserType;
import thonlivethondie.artconnect.common.exception.BadRequestException;
import thonlivethondie.artconnect.common.exception.ErrorCode;
import thonlivethondie.artconnect.dto.PortfolioRequestDto;
import thonlivethondie.artconnect.dto.PortfolioResponseDto;
import thonlivethondie.artconnect.entity.Portfolio;
import thonlivethondie.artconnect.entity.PortfolioImage;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.repository.PortfolioImageRepository;
import thonlivethondie.artconnect.repository.PortfolioRepository;
import thonlivethondie.artconnect.repository.UserRepository;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;

    /**
     * 새 포트폴리오 생성
     */
    public PortfolioResponseDto createPortfolio(Long userId, PortfolioRequestDto request) {
        User designer = validateDesigner(userId);
        Portfolio portfolio = createNewPortfolio(designer, request);
        return PortfolioResponseDto.from(portfolio);
    }

    /**
     * 포트폴리오 업데이트
     */
    public PortfolioResponseDto updatePortfolio(Long userId, Long portfolioId, PortfolioRequestDto request) {
        validateDesigner(userId);
        Portfolio portfolio = getPortfolioByIdAndUserId(portfolioId, userId);
        portfolio = updatePortfolioInfo(portfolio, request);
        return PortfolioResponseDto.from(portfolio);
    }

    /**
     * 포트폴리오 이미지 업로드
     */
    public PortfolioResponseDto uploadPortfolioImages(Long userId, Long portfolioId, List<MultipartFile> images) {
        validateDesigner(userId);
        Portfolio portfolio = getPortfolioByIdAndUserId(portfolioId, userId);

        log.info("포트폴리오 이미지 업로드 시작 - portfolioId: {}, 이미지 개수: {}", portfolio.getId(), images.size());

        // 빈 파일 필터링
        List<MultipartFile> validImages = images.stream()
                .filter(image -> !image.isEmpty())
                .toList();

        if (validImages.isEmpty()) {
            log.warn("업로드할 유효한 이미지가 없습니다.");
            return PortfolioResponseDto.from(portfolio);
        }

        try {
            // S3에 이미지 업로드
            List<String> imageUrls = awsS3Service.uploadFile(validImages);
            log.info("S3 업로드 완료 - 업로드된 URL 개수: {}", imageUrls.size());

            // 현재 이미지가 없는 경우 첫 번째 업로드 이미지를 썸네일로 설정
            boolean isFirstImageUpload = portfolio.getPortfolioImages().isEmpty();

            // PortfolioImage 엔티티 생성 및 저장
            for (int i = 0; i < validImages.size() && i < imageUrls.size(); i++) {
                MultipartFile image = validImages.get(i);
                String imageUrl = imageUrls.get(i);

                PortfolioImage portfolioImage = PortfolioImage.builder()
                        .portfolio(portfolio)
                        .imageName(image.getOriginalFilename())
                        .imageUrl(imageUrl)
                        .imageSize(image.getSize())
                        .isThumbnail(isFirstImageUpload && i == 0) // 첫 번째 업로드 시 첫 번째 이미지를 썸네일로 설정
                        .build();

                portfolio.getPortfolioImages().add(portfolioImage);
                log.info("포트폴리오 이미지 엔티티 생성 - 파일명: {}, URL: {}, 크기: {}",
                        image.getOriginalFilename(), imageUrl, image.getSize());
            }

            // 썸네일 URL 업데이트 (첫 번째 업로드인 경우)
            if (isFirstImageUpload && !portfolio.getPortfolioImages().isEmpty()) {
                // 첫 번째 이미지의 URL을 썸네일로 설정
                String firstImageUrl = portfolio.getPortfolioImages().get(0).getImageUrl();
                portfolio.updateThumbnailUrl(firstImageUrl);
            }

            Portfolio savedPortfolio = portfolioRepository.save(portfolio);
            log.info("포트폴리오 이미지 업로드 완료 - 총 이미지 수: {}", savedPortfolio.getPortfolioImages().size());

            return PortfolioResponseDto.from(savedPortfolio);

        } catch (Exception e) {
            log.error("포트폴리오 이미지 업로드 실패", e);
            throw new BadRequestException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    /**
     * 포트폴리오 이미지 삭제
     */
    @Transactional
    public PortfolioResponseDto deletePortfolioImage(Long userId, Long portfolioId, Long imageId) {
        validateDesigner(userId);
        Portfolio portfolio = getPortfolioByIdAndUserId(portfolioId, userId);

        // 이미지 찾기 및 권한 확인
        PortfolioImage portfolioImage = portfolioImageRepository.findById(imageId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.PORTFOLIO_IMAGE_NOT_FOUND));

        if (!portfolioImage.getPortfolio().getId().equals(portfolio.getId())) {
            throw new BadRequestException(ErrorCode.PORTFOLIO_IMAGE_ACCESS_DENIED);
        }

        // S3에서 이미지 삭제
        try {
            String fileName = extractFileNameFromUrl(portfolioImage.getImageUrl());
            awsS3Service.deleteFile(fileName);
        } catch (Exception e) {
            log.warn("S3 이미지 삭제 실패: {}", portfolioImage.getImageUrl(), e);
        }

        // 삭제할 이미지가 썸네일인지 또는 썸네일 URL과 일치하는지 확인
        boolean deletingThumbnail = portfolioImage.getIsThumbnail();
        boolean thumbnailUrlMatches = portfolio.getThumbnailUrl() != null &&
                portfolio.getThumbnailUrl().equals(portfolioImage.getImageUrl());

        // 데이터베이스에서 삭제
        portfolio.getPortfolioImages().remove(portfolioImage);
        portfolioImageRepository.delete(portfolioImage);

        // 썸네일 이미지를 삭제한 경우 또는 썸네일 URL이 삭제되는 이미지와 같은 경우, 새로운 썸네일 설정
        if ((deletingThumbnail || thumbnailUrlMatches) && !portfolio.getPortfolioImages().isEmpty()) {
            updateThumbnailToFirstImage(portfolio);
            log.info("썸네일 재설정 - 삭제된 이미지 URL: {}", portfolioImage.getImageUrl());
        } else if (portfolio.getPortfolioImages().isEmpty()) {
            // 모든 이미지가 삭제된 경우 썸네일 URL 제거
            portfolio.updateThumbnailUrl(null);
            log.info("모든 이미지 삭제로 인한 썸네일 URL 제거");
        }

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        log.info("포트폴리오 이미지 삭제 완료 - imageId: {}", imageId);
        return PortfolioResponseDto.from(savedPortfolio);
    }

    /**
     * 내 포트폴리오 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PortfolioResponseDto> getMyPortfolios(Long userId) {
        validateDesigner(userId);

        // 먼저 포트폴리오 기본 정보를 조회
        List<Portfolio> portfolios = portfolioRepository.findByDesignerId(userId);

        // 각 포트폴리오에 대해 이미지와 카테고리를 별도로 로드
        portfolios.forEach(this::loadPortfolioImagesAndCategories);

        return portfolios.stream()
                .map(PortfolioResponseDto::from)
                .toList();
    }

    /**
     * 특정 포트폴리오 조회
     */
    @Transactional(readOnly = true)
    public PortfolioResponseDto getPortfolio(Long userId, Long portfolioId) {
        validateDesigner(userId);
        Portfolio portfolio = getPortfolioByIdAndUserId(portfolioId, userId);
        return PortfolioResponseDto.from(portfolio);
    }

    /**
     * 소상공인용 - 특정 디자이너의 포트폴리오 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PortfolioResponseDto> getDesignerPortfolios(Long designerId) {
        // 디자이너가 존재하는지 확인
        validateDesigner(designerId);

        // 디자이너의 공개 포트폴리오만 조회
        List<Portfolio> portfolios = portfolioRepository.findByDesignerId(designerId);

        // 각 포트폴리오에 대해 이미지와 카테고리를 별도로 로드
        portfolios.forEach(this::loadPortfolioImagesAndCategories);

        return portfolios.stream()
                .map(PortfolioResponseDto::from)
                .toList();
    }

    /**
     * 소상공인용 - 특정 디자이너의 특정 포트폴리오 조회
     */
    @Transactional(readOnly = true)
    public PortfolioResponseDto getDesignerPortfolio(Long designerId, Long portfolioId) {
        // 디자이너가 존재하는지 확인
        validateDesigner(designerId);

        // 포트폴리오 조회 및 권한 확인
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.PORTFOLIO_NOT_FOUND));
        
        // 해당 포트폴리오가 요청한 디자이너의 것인지 확인
        if (!portfolio.getDesigner().getId().equals(designerId)) {
            throw new BadRequestException(ErrorCode.PORTFOLIO_NOT_FOUND);
        }

        // 이미지와 카테고리 로드
        loadPortfolioImagesAndCategories(portfolio);

        return PortfolioResponseDto.from(portfolio);
    }

    /**
     * 포트폴리오 삭제
     */
    public void deletePortfolio(Long userId, Long portfolioId) {
        validateDesigner(userId);
        Portfolio portfolio = getPortfolioByIdAndUserId(portfolioId, userId);

        // 포트폴리오와 연관된 이미지들도 S3에서 삭제
        for (PortfolioImage image : portfolio.getPortfolioImages()) {
            try {
                String fileName = extractFileNameFromUrl(image.getImageUrl());
                awsS3Service.deleteFile(fileName);
            } catch (Exception e) {
                log.warn("S3 이미지 삭제 실패: {}", image.getImageUrl(), e);
            }
        }

        portfolioRepository.delete(portfolio);
        log.info("포트폴리오 삭제 완료 - portfolioId: {}", portfolioId);
    }

    /**
     * 디자이너 유효성 검증
     */
    private User validateDesigner(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() != UserType.DESIGNER) {
            throw new BadRequestException(ErrorCode.INVALID_USER_TYPE);
        }

        return user;
    }

    /**
     * 새 포트폴리오 생성
     */
    private Portfolio createNewPortfolio(User designer, PortfolioRequestDto request) {
        Portfolio portfolio = Portfolio.builder()
                .designer(designer)
                .title(request.title())
                .description(request.description())
                .build();

        // 디자인 카테고리 설정
        if (request.designCategories() != null && !request.designCategories().isEmpty()) {
            portfolio.setDesignCategories(request.designCategories());
        }

        return portfolioRepository.save(portfolio);
    }

    /**
     * 기존 포트폴리오 정보 업데이트
     */
    private Portfolio updatePortfolioInfo(Portfolio portfolio, PortfolioRequestDto request) {
        // 기본 정보 업데이트
        portfolio.updatePortfolioInfo(request.title(), request.description());

        // 디자인 카테고리 업데이트
        if (request.designCategories() != null) {
            portfolio.setDesignCategories(request.designCategories());
        }

        return portfolioRepository.save(portfolio);
    }

    private Portfolio getPortfolioByIdAndUserId(Long portfolioId, Long userId) {
        // 먼저 포트폴리오 기본 정보를 조회
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 포트폴리오 소유자 검증
        if (!portfolio.getDesigner().getId().equals(userId)) {
            throw new BadRequestException(ErrorCode.PORTFOLIO_ACCESS_DENIED);
        }

        // 이미지와 카테고리를 별도로 로드 (MultipleBagFetchException 방지)
        loadPortfolioImagesAndCategories(portfolio);

        return portfolio;
    }

    /**
     * 포트폴리오의 이미지와 카테고리를 별도로 로드
     */
    private void loadPortfolioImagesAndCategories(Portfolio portfolio) {
        // Hibernate.initialize를 사용하여 컬렉션 초기화
        org.hibernate.Hibernate.initialize(portfolio.getPortfolioImages());
        org.hibernate.Hibernate.initialize(portfolio.getDesignCategories());
    }

    /**
     * 포트폴리오의 썸네일을 ID가 가장 작은 이미지로 업데이트
     */
    private void updateThumbnailToFirstImage(Portfolio portfolio) {
        // ID가 가장 작은 이미지 찾기
        portfolio.getPortfolioImages().stream()
                .min(Comparator.comparingLong(img -> img.getId() != null ? img.getId() : Long.MAX_VALUE))
                .ifPresent(firstImage -> {
                    // 새로운 썸네일 설정을 위해 DB에서 업데이트
                    portfolioImageRepository.updateThumbnailStatus(portfolio.getId(), firstImage.getId());

                    // 포트폴리오 썸네일 URL 업데이트
                    portfolio.updateThumbnailUrl(firstImage.getImageUrl());

                    log.info("새로운 썸네일 설정 완료 - imageId: {}, imageUrl: {}",
                            firstImage.getId(), firstImage.getImageUrl());
                });
    }

    private String extractFileNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "";
        }
        return imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
    }
}
