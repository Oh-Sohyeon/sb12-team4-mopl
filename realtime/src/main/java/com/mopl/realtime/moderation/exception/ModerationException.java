package com.mopl.realtime.moderation.exception;

import org.springframework.http.HttpStatus;
import com.mopl.realtime.moderation.repository.ChatRestrictionStore.Restriction;

// API 모듈의 ErrorCode/Exception 형식을 따르되 realtime이 API 모듈에 의존하지 않는다.
public class ModerationException extends RuntimeException {
    public enum ErrorCode {
        MESSAGE_BLOCKED("MODERATION_001", "욕설/비속어가 포함된 메시지는 전송할 수 없습니다."),
        CHAT_RESTRICTED("CHAT_RESTRICTED", "반복적인 메시지 이용 정책 위반으로 채팅 이용이 일시적으로 제한되었습니다.");

        private final String code;
        private final String message;
        ErrorCode(String code, String message) { this.code = code; this.message = message; }
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public HttpStatus getStatus() { return HttpStatus.FORBIDDEN; }
    }

    private final ErrorCode errorCode;
    private final Restriction restriction;
    public ModerationException(ErrorCode errorCode) { this(errorCode, null); }
    public ModerationException(ErrorCode errorCode, Restriction restriction) {
        super(errorCode.getMessage()); this.errorCode = errorCode; this.restriction = restriction;
    }
    public Restriction getRestriction() { return restriction; }
    public ErrorCode getErrorCode() { return errorCode; }
}
