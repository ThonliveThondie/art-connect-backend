package thonlivethondie.artconnect.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import thonlivethondie.artconnect.dto.ErrorResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequestException(
            BadRequestException e,
            HttpServletRequest request) {

        log.error("BadRequestException: {}", e.getMessage());

        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                e.getErrorCode(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("ValidationException: {}", errorMessage);

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(java.time.LocalDateTime.now())
                .code("E002")
                .message(errorMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(
            Exception e,
            HttpServletRequest request) {

        log.error("Unexpected error: {}", e.getMessage(), e);

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(java.time.LocalDateTime.now())
                .code("E999")
                .message("서버 내부 오류가 발생했습니다.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .internalServerError()
                .body(errorResponse);
    }
}
