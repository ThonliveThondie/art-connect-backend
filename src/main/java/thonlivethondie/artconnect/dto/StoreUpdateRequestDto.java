package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.common.OperatingHours;

import java.util.List;

public record StoreUpdateRequestDto(
        String storeName,
        String storeType,
        String phoneNumber,
        List<OperatingHours> operatingHours
) {
}
