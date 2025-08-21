package thonlivethondie.artconnect.dto;

import lombok.Builder;
import thonlivethondie.artconnect.entity.Store;

import java.util.List;

@Builder
public record StoreResponseDto(
        Long storeId,
        String storeName,
        String storeType,
        String phoneNumber,
        List<String> operatingHours,
        List<StoreImageDto> storeImages
) {
    public StoreResponseDto {
        // 불변성을 위한 방어적 복사
        storeImages = storeImages != null ? List.copyOf(storeImages) : List.of();
        operatingHours = operatingHours != null ? List.copyOf(operatingHours) : List.of();
    }
    
    public static StoreResponseDto from(Store store) {
        return StoreResponseDto.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .storeType(store.getStoreType())
                .phoneNumber(store.getPhoneNumber())
                .operatingHours(store.getStoreOperatingHours().stream()
                        .map(operatingHour -> operatingHour.getOperatingHours().name())
                        .toList())
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
