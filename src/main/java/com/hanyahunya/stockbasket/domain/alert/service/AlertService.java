package com.hanyahunya.stockbasket.domain.alert.service;
import com.hanyahunya.stockbasket.domain.alert.dto.AlertResponse;
import java.util.List;

public interface AlertService {
    List<AlertResponse> getMyAlerts(Long userId);
    List<AlertResponse> getUnreadAlerts(Long userId);
    void markAsRead(Long alertId);
    void markAllAsRead(Long userId);
    void detectAndCreatePriceAlerts();
    int getUnreadCount(Long userId);
}
