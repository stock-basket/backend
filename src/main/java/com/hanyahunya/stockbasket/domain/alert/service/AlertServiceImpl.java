package com.hanyahunya.stockbasket.domain.alert.service;
import com.hanyahunya.stockbasket.domain.alert.dto.AlertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    @Override
    public List<AlertResponse> getMyAlerts(UUID userId) {
        return List.of();
    }

    @Override
    public List<AlertResponse> getUnreadAlerts(UUID userId) {
        return List.of();
    }

    @Override
    public int getUnreadCount(UUID userId) {
        return 0;
    }

    @Override
    public void markAsRead(Long alertId) {

    }

    @Override
    public void markAllAsRead(UUID userId) {

    }

    @Override
    public void detectAndCreatePriceAlerts() {

    }
}
