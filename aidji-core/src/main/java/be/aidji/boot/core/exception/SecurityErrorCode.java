package be.aidji.boot.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SecurityErrorCode implements ErrorCode {

    BEARER_TOKEN_EXPIRED("SECU-001", "Bearer token expired", 401),
    BEARER_TOKEN_NOT_VALID("SECU-002", "Bearer token not valid", 401),
    ACCESS_DENIED("SECU-003", "Access denied", 403),
    UNAUTHORIZED("SECU-004", "Unauthorized", 403),;


    private final String code;
    private final String defaultMessage;
    private final int httpStatus;
}
