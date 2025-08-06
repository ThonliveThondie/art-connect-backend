package thonlivethondie.artconnect.common.exception;

import lombok.Getter;
import org.springframework.core.NestedRuntimeException;

@Getter
public abstract class ArtConnectException extends NestedRuntimeException {
    private final ErrorCode errorCode;

    protected ArtConnectException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected ArtConnectException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    protected ArtConnectException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
