package com.hanyahunya.stockbasket.domain.user.service;

/**
 * 회원가입 시 이메일 인증 코드 발송 및 검증을 담당하는 서비스 인터페이스.
 *
 * <p>구현체에서는 Redis 또는 인메모리 저장소를 활용하여 코드와 TTL 을 관리한다.
 */
public interface EmailVerificationService {

    /**
     * 주어진 이메일로 6자리 인증 코드를 생성하여 발송한다.
     *
     * @param email 수신 이메일 주소
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         이메일이 이미 가입된 경우 (USER_409)
     */
    void sendVerificationCode(String email);

    /**
     * 이메일과 코드 쌍을 검증한다.
     *
     * @param email 이메일 주소
     * @param code  사용자가 입력한 6자리 코드
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         코드가 만료되었거나 일치하지 않는 경우 (TOKEN_401)
     */
    void verifyCode(String email, String code);
}
