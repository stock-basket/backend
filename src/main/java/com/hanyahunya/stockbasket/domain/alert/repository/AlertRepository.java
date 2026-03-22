package com.hanyahunya.stockbasket.domain.alert.repository;
import com.hanyahunya.stockbasket.domain.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<Alert> findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    int countByUserIdAndIsReadFalse(Long userId);
}
