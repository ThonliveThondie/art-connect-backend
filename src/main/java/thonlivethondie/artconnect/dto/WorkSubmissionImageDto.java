package thonlivethondie.artconnect.dto;

/**
 * 시안 이미지 DTO
 */
public record WorkSubmissionImageDto(
    Long id,
    String imageUrl,
    String imageName,
    Long imageSize,
    String imageType
) {
}
