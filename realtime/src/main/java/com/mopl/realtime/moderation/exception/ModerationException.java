package com.mopl.realtime.moderation.exception;

import org.springframework.http.HttpStatus;

// API 모듈의 ErrorCode/Exception 형식을 따르되 realtime이 API 모듈에 의존하지 않는다.
public class ModerationException extends RuntimeException {
    public enum ErrorCode {
        MESSAGE_BLOCKED("MODERATION_001", "욕설/비속어가 포함된 메시지는 전송할 수 없습니다."),
        CHAT_RESTRICTED("MODERATION_002", "채팅 및 DM 전송이 일시적으로 제한되었습니다.");

        private final String code;
        private final String message;
        ErrorCode(String code, String message) { this.code = code; this.message = message; }
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public HttpStatus getStatus() { return HttpStatus.FORBIDDEN; }
    }

    private final ErrorCode errorCode;
    public ModerationException(ErrorCode errorCode) { super(errorCode.getMessage()); this.errorCode = errorCode; }
    public ErrorCode getErrorCode() { return errorCode; }
}
