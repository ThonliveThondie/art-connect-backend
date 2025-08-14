package thonlivethondie.artconnect.dto;

import lombok.Builder;
import thonlivethondie.artconnect.entity.Store;

import java.util.List;

@Builder
public record StoreResponseDto(
        Long storeId,
        String storeName,
        String address,
        String phoneNumber,
        String operatingHours,
        List<StoreImageDto> storeImages
) {
    public StoreResponseDto {
        // 불변성을 위한 방어적 복사
        storeImages = storeImages != null ? List.copyOf(storeImages) : List.of();
    }
    
    public static StoreResponseDto from(Store store) {
        return StoreResponseDto.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .phoneNumber(store.getPhoneNumber())
                .operatingHours(store.getOperatingHours())
                .storeImages(store.getStoreImages().stream()
                        .map(StoreImageDto::from)
                        .toList())
                .build();
    }
    
    // 편의 메서드들
    public boolean hasImages() {
        return !storeImages.isEmpty();
    }
    
    public int getImageCount() {
        return storeImages.size();
    }
}
