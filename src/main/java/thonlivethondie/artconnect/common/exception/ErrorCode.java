package thonlivethondie.artconnect.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
public enum ErrorCode {
    EMAIL_DUPLICATED(BAD_REQUEST, "E001", "중복된 이메일입니다."),
    VALIDATION_ERROR(BAD_REQUEST, "E002", "입력값 검증에 실패했습니다."),
    
    // 사용자 관련 에러
    USER_NOT_FOUND(BAD_REQUEST, "U001", "사용자를 찾을 수 없습니다."),
    
    // 채팅방 관련 에러
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(BAD_REQUEST, "C002", "채팅방에 접근할 권한이 없습니다."),
    
    // 메시지 관련 에러
    PARENT_MESSAGE_NOT_FOUND(BAD_REQUEST, "M001", "부모 메시지를 찾을 수 없습니다."),

    // 매장 관련 에러
    INVALID_USER_TYPE(BAD_REQUEST, "S001", "소상공인만 매장을 생성할 수 있습니다."),
    ALREADY_HAS_STORE(BAD_REQUEST, "S002", "이미 매장을 보유하고 있습니다."),
    DUPLICATE_STORE_NAME(BAD_REQUEST, "S003", "이미 존재하는 매장명입니다."),
    STORE_NOT_FOUND(BAD_REQUEST, "S004", "매장을 찾을 수 없습니다."),
    
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E999", "서버 내부 오류가 발생했습니다.");

    private HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
