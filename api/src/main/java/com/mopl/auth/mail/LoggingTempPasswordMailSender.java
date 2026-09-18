package com.mopl.auth.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TODO: 메일 발송 인프라(SMTP) 연동 전까지의 임시 구현.
 * 실제로 이메일을 보내지 않고 로그만 남긴다. 절대 이 상태로 운영 배포하면 안 됨 -
 * 비밀번호가 로그에 그대로 남으므로 로컬 개발/테스트 용도로만 사용.
 * SMTP 연동 시 이 클래스를 지우고 JavaMailSender 기반 구현체로 교체.
 */
@Slf4j
@Component
public class LoggingTempPasswordMailSender implements TempPasswordMailSender {

    @Override
    public void send(String toEmail, String tempPassword) {
        log.warn("[TODO: 메일 인프라 미연동] {} 로 임시 비밀번호 발송 (실제 발송 안 됨): {}", toEmail, tempPassword);
    }
}