package thonlivethondie.artconnect.dto;

import lombok.Builder;
import thonlivethondie.artconnect.common.exception.ErrorCode;

import java.time.LocalDateTime;

@Builder
public record ErrorResponseDto(
        LocalDateTime timestamp,
        String code,
        String message,
        String path
) {
    public static ErrorResponseDto of(ErrorCode errorCode, String path) {
        return ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .build();
    }
}
