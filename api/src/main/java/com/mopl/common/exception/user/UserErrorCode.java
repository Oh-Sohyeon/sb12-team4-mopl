package com.mopl.common.exception.user;

import com.mopl.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_001",
            "존재하지 않는 사용자입니다."
    ),
    USER_LOCKED(
            HttpStatus.UNAUTHORIZED,
            "USER_002",
            "잠긴 계정입니다. 관리자에게 문의해주세요."
    ),
    USER_WITHDRAWN(
            HttpStatus.UNAUTHORIZED,
            "USER_003",
            "탈퇴한 계정입니다."
    ),
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "USER_004",
            "본인의 정보만 수정할 수 있습니다."
    ),
    INVALID_SORT_FIELD(
            HttpStatus.BAD_REQUEST,
            "USER_005",
            "지원하지 않는 정렬 기준입니다."
    ),
    EMAIL_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "USER_006",
            "이미 사용 중인 이메일입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}