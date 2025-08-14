package thonlivethondie.artconnect.dto;

import thonlivethondie.artconnect.entity.WorkRequestImage;

public record WorkRequestImageDto(
        Long id,
        String imageName,
        String imageUrl,
        Long imageSize,
        String imageType
) {
    // 정적 팩토리 메서드
    public static WorkRequestImageDto from(WorkRequestImage image) {
        return new WorkRequestImageDto(
                image.getId(),
                image.getImageName(),
                image.getImageUrl(),
                image.getImageSize(),
                image.getImageType()
        );
    }
}
