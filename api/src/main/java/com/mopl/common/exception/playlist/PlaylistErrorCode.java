package com.mopl.common.exception.playlist;

import com.mopl.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaylistErrorCode implements ErrorCode {

	PLAYLIST_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"PLAYLIST_001",
		"플레이리스트를 찾을 수 없습니다."
	),
	PLAYLIST_ACCESS_DENIED(
		HttpStatus.FORBIDDEN,
		"PLAYLIST_002",
		"플레이리스트에 대한 권한이 없습니다."
	);

	private final HttpStatus status;
	private final String code;
	private final String message;
}