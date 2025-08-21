package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import thonlivethondie.artconnect.common.UserType;
import thonlivethondie.artconnect.common.exception.BadRequestException;
import thonlivethondie.artconnect.common.exception.ErrorCode;

import thonlivethondie.artconnect.common.OperatingHours;
import thonlivethondie.artconnect.dto.StoreResponseDto;
import thonlivethondie.artconnect.dto.StoreUpdateRequestDto;
import thonlivethondie.artconnect.entity.Store;
import thonlivethondie.artconnect.entity.StoreImage;
import thonlivethondie.artconnect.entity.StoreOperatingHours;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.repository.StoreImageRepository;
import thonlivethondie.artconnect.repository.StoreRepository;
import thonlivethondie.artconnect.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreImageRepository storeImageRepository;
    private final AwsS3Service awsS3Service;

    // 통합 업데이트 메서드
    public StoreResponseDto createOrUpdateStore(Long userId, StoreUpdateRequestDto dto) {
        User user = validateBusinessOwner(userId);

        Optional<Store> existingStore = storeRepository.findByUserId(userId);

        Store store;

        if (existingStore.isPresent()) {
            store = existingStore.get();
            // 매장명 중복 검증 (변경되는 경우만)
            if (dto.storeName() != null && !dto.storeName().equals(store.getStoreName())) {
                if (storeRepository.existsByStoreName(dto.storeName())) {
                    throw new BadRequestException(ErrorCode.DUPLICATE_STORE_NAME);
                }
            }

            // 운영시간 엔티티 변환
            List<StoreOperatingHours> operatingHoursList = convertToStoreOperatingHours(store, dto.operatingHours());

            // 필드별 업데이트 (null이 아닌 값만)
            store.updateStoreInfo(dto.storeName(), dto.storeType(), dto.phoneNumber(), operatingHoursList);
        } else {
            // 매장명 중복 검증
            if (dto.storeName() != null && storeRepository.existsByStoreName(dto.storeName())) {
                throw new BadRequestException(ErrorCode.DUPLICATE_STORE_NAME);
            }

            // 1. 먼저 Store 엔티티 생성 및 저장
            store = Store.builder()
                    .user(user)
                    .storeName(dto.storeName())
                    .storeType(dto.storeType())
                    .phoneNumber(dto.phoneNumber())
                    .build();

            store = storeRepository.save(store);

            // 2. Store 저장 후 운영시간 설정
            if (dto.operatingHours() != null && !dto.operatingHours().isEmpty()) {
                List<StoreOperatingHours> operatingHoursList = convertToStoreOperatingHours(store, dto.operatingHours());
                store.setOperatingHours(operatingHoursList);
                store = storeRepository.save(store); // 운영시간과 함께 다시 저장
            }
        }

        return StoreResponseDto.from(store);
    }

    private User validateBusinessOwner(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() != UserType.BUSINESS_OWNER) {
            throw new BadRequestException(ErrorCode.INVALID_USER_TYPE);
        }

        return user;
    }

    @Transactional(readOnly = true)
    public Optional<StoreResponseDto> getMyStore(Long userId) {
        // 1. 사용자의 매장과 이미지를 함께 조회 (Fetch Join 사용)
        Optional<Store> storeOptional = storeRepository.findByUserIdWithImages(userId);

        // 2. DTO 변환 후 반환
        return storeOptional.map(StoreResponseDto::from);
    }

    /**
     * 매장 이미지 업로드
     */
    public StoreResponseDto uploadStoreImages(Long userId, List<MultipartFile> images) {
        // 1. 사용자 및 매장 유효성 검증
        validateBusinessOwner(userId);
        Store store = getStoreByUserId(userId);

        log.info("매장 이미지 업로드 시작 - storeId: {}, 이미지 개수: {}", store.getId(), images.size());

        // 2. 빈 파일 필터링
        List<MultipartFile> validImages = images.stream()
                .filter(image -> !image.isEmpty())
                .toList();

        if (validImages.isEmpty()) {
            log.warn("업로드할 유효한 이미지가 없습니다.");
            return StoreResponseDto.from(store);
        }

        // 3. S3에 이미지 업로드
        try {
            List<String> imageUrls = awsS3Service.uploadFile(validImages);
            log.info("S3 업로드 완료 - 업로드된 URL 개수: {}", imageUrls.size());

            // 4. StoreImage 엔티티 생성 및 저장
            for (int i = 0; i < validImages.size() && i < imageUrls.size(); i++) {
                MultipartFile image = validImages.get(i);
                String imageUrl = imageUrls.get(i);

                StoreImage storeImage = StoreImage.builder()
                        .store(store)
                        .imageName(image.getOriginalFilename())
                        .imageUrl(imageUrl)
                        .imageSize(image.getSize())
                        .imageType(image.getContentType())
                        .build();

                store.getStoreImages().add(storeImage);
                log.info("매장 이미지 엔티티 생성 - 파일명: {}, URL: {}, 크기: {}, 타입: {}",
                        image.getOriginalFilename(), imageUrl, image.getSize(), image.getContentType());
            }

            // 5. Store 엔티티 저장 (cascade로 StoreImage들도 함께 저장됨)
            Store savedStore = storeRepository.save(store);
            log.info("매장 이미지 업로드 완료 - 총 이미지 수: {}", savedStore.getStoreImages().size());

            return StoreResponseDto.from(savedStore);

        } catch (Exception e) {
            log.error("매장 이미지 업로드 실패", e);
            throw new BadRequestException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    /**
     * 매장 이미지 삭제
     */
    public StoreResponseDto deleteStoreImage(Long userId, Long imageId) {
        // 1. 사용자 및 매장 유효성 검증
        validateBusinessOwner(userId);
        Store store = getStoreByUserId(userId);

        // 2. 이미지 찾기 및 권한 확인
        StoreImage storeImage = storeImageRepository.findById(imageId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.STORE_IMAGE_NOT_FOUND));

        if (!storeImage.getStore().getId().equals(store.getId())) {
            throw new BadRequestException(ErrorCode.STORE_IMAGE_ACCESS_DENIED);
        }

        // 3. S3에서 이미지 삭제 (옵션)
        try {
            String fileName = extractFileNameFromUrl(storeImage.getImageUrl());
            awsS3Service.deleteFile(fileName);
        } catch (Exception e) {
            log.warn("S3 이미지 삭제 실패: {}", storeImage.getImageUrl(), e);
        }

        // 4. 데이터베이스에서 삭제
        store.getStoreImages().remove(storeImage);
        storeImageRepository.delete(storeImage);

        log.info("매장 이미지 삭제 완료 - imageId: {}", imageId);
        return StoreResponseDto.from(store);
    }


    /**
     * 사용자 ID로 매장 조회
     */
    private Store getStoreByUserId(Long userId) {
        return storeRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.STORE_NOT_FOUND));
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "";
        }
        return imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
    }

    /**
     * OperatingHours 열거형을 StoreOperatingHours 엔티티로 변환
     */
    private List<StoreOperatingHours> convertToStoreOperatingHours(Store store, List<OperatingHours> operatingHours) {
        if (operatingHours == null || operatingHours.isEmpty()) {
            return List.of();
        }

        return operatingHours.stream()
                .map(operatingHour -> StoreOperatingHours.builder()
                        .store(store)
                        .operatingHours(operatingHour)
                        .build())
                .toList();
    }
}
