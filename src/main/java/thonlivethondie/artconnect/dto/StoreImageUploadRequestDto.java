package thonlivethondie.artconnect.dto;

public record StoreImageUploadRequestDto(
        Long storeId,
        Integer imageOrder,
        Boolean isMain
) {
}
