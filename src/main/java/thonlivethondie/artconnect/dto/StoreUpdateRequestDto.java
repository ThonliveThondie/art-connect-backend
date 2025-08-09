package thonlivethondie.artconnect.dto;

public record StoreUpdateRequestDto(
        String storeName,
        String phoneNumber,
        String address,
        String operatingHours
) {
}
