package com.hanyahunya.stockbasket.domain.alert.service;
import com.hanyahunya.stockbasket.domain.alert.dto.AlertResponse;
import com.hanyahunya.stockbasket.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    @Override
    public List<AlertResponse> getMyAlerts(Long userId) {
        return List.of();
    }

    @Override
    public List<AlertResponse> getUnreadAlerts(Long userId) { return List.of(); }

    @Override
    public void markAsRead(Long alertId) {}

    @Override
    public void markAllAsRead(Long userId) {}

    @Override
    public void detectAndCreatePriceAlerts() {}

    @Override
    public int getUnreadCount(Long userId) { return 0; }
}
