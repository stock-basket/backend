package com.hanyahunya.stockbasket.domain.stock.repository;
import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, String> {
}
