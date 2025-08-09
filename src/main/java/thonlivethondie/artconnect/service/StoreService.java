package thonlivethondie.artconnect.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thonlivethondie.artconnect.common.UserType;
import thonlivethondie.artconnect.common.exception.BadRequestException;
import thonlivethondie.artconnect.common.exception.ErrorCode;
import thonlivethondie.artconnect.dto.StoreResponseDto;
import thonlivethondie.artconnect.dto.StoreUpdateRequestDto;
import thonlivethondie.artconnect.entity.Store;
import thonlivethondie.artconnect.entity.User;
import thonlivethondie.artconnect.repository.StoreRepository;
import thonlivethondie.artconnect.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // 통합 업데이트 메서드
    @Transactional
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

            // 필드별 업데이트 (null이 아닌 값만)
            store.updateStoreInfo(dto.storeName(), dto.address(), dto.phoneNumber(), dto.operatingHours());
        } else {
            // 매장명 중복 검증
            if (dto.storeName() != null && storeRepository.existsByStoreName(dto.storeName())) {
                throw new BadRequestException(ErrorCode.DUPLICATE_STORE_NAME);
            }

            store = Store.builder()
                    .user(user)
                    .storeName(dto.storeName())
                    .phoneNumber(dto.phoneNumber())
                    .address(dto.address())
                    .operatingHours(dto.operatingHours())
                    .build();

            store = storeRepository.save(store);
        }

        return StoreResponseDto.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .phoneNumber(store.getPhoneNumber())
                .operatingHours(store.getOperatingHours())
                .build();
    }

    private User validateBusinessOwner(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() != UserType.BUSINESS_OWNER) {
            throw new BadRequestException(ErrorCode.INVALID_USER_TYPE);
        }

        return user;
    }

    public Optional<StoreResponseDto> getMyStore(Long userId) {
        // 1. 사용자의 매장 조회
        Optional<Store> storeOptional = storeRepository.findByUserId(userId);

        // 2. DTO 변환 후 반환
        return storeOptional.map(store -> StoreResponseDto.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .phoneNumber(store.getPhoneNumber())
                .operatingHours(store.getOperatingHours())
                .build());
    }
}
