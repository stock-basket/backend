package com.hanyahunya.stockbasket.domain.alert.repository;
import com.hanyahunya.stockbasket.domain.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Alert> findAllByUserIdAndReadIsFalseOrderByCreatedAtDesc(UUID userId);
    int countByUserIdAndReadIsFalse(UUID userId);
}
