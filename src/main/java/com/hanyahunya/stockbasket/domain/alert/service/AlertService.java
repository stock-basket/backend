package com.hanyahunya.stockbasket.domain.alert.service;

import com.hanyahunya.stockbasket.domain.alert.dto.AlertResponse;

import java.util.List;
import java.util.UUID;

public interface AlertService {

    /** 전체 알림 목록 (최신순) */
    List<AlertResponse> getMyAlerts(UUID userId);

    /** 미읽 알림 목록 (최신순) */
    List<AlertResponse> getUnreadAlerts(UUID userId);

    /** 미읽 알림 개수 */
    int getUnreadCount(UUID userId);

    /**
     * 단건 읽음 처리
     *
     * @throws com.hanyahunya.stockbasket.global.exception.BusinessException
     *         ALERT_NOT_FOUND(404) — 존재하지 않는 알림
     */
    void markAsRead(Long alertId);

    /** 전체 읽음 처리 */
    void markAllAsRead(UUID userId);

    /** 스케줄러가 주기적으로 호출 — 급등락 감지 후 알림 생성 */
    void detectAndCreatePriceAlerts();
}
