package com.hanyahunya.stockbasket.domain.stock.repository;

import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, String> {

    @Query(value = "SELECT DISTINCT stock_code FROM user_stock", nativeQuery = true)
    List<String> findAllRegisteredStockCodes();

    @Query(value = """
            SELECT DISTINCT us.stock_code
            FROM user_stock us
            JOIN user_settings uset ON us.user_id = uset.user_id
            WHERE uset.is_volatility_alert_enabled = true
            """, nativeQuery = true)
    List<String> findVolatilityEnabledStockCodes();

    @Query(value = "SELECT user_id FROM user_stock WHERE stock_code = :stockCode", nativeQuery = true)
    List<byte[]> findUserIdsByStockCode(@Param("stockCode") String stockCode);

    List<Stock> findByNameContainingIgnoreCaseOrStockCodeContainingIgnoreCase(String name, String stockCode);
}
