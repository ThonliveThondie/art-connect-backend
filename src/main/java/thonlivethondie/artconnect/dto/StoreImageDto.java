package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.entity.StoreImage;

public record StoreImageDto(
    Long id,
    String imageName,
    String imageUrl,
    Long imageSize,
    String imageType
) {
    public static StoreImageDto from(StoreImage storeImage) {
        return new StoreImageDto(
            storeImage.getId(),
            storeImage.getImageName(),
            storeImage.getImageUrl(),
            storeImage.getImageSize(),
            storeImage.getImageType()
        );
    }
    
    // 편의 메서드들
    public String getFormattedSize() {
        if (imageSize == null) return "알 수 없음";
        
        if (imageSize < 1024) {
            return imageSize + " B";
        } else if (imageSize < 1024 * 1024) {
            return String.format("%.1f KB", imageSize / 1024.0);
        } else {
            return String.format("%.1f MB", imageSize / (1024.0 * 1024.0));
        }
    }
    
    public String getFileExtension() {
        if (imageName == null || imageName.isEmpty()) {
            return "";
        }
        int lastDot = imageName.lastIndexOf('.');
        return lastDot > 0 ? imageName.substring(lastDot + 1).toLowerCase() : "";
    }
}
