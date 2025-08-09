package thonlivethondie.artconnect.dto;

import lombok.Builder;

@Builder
public record StoreResponseDto(
        Long storeId,
        String storeName,
        String address,
        String phoneNumber,
        String operatingHours
) {
}
