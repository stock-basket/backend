package com.hanyahunya.stockbasket.domain.news.repository;
import com.hanyahunya.stockbasket.domain.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findAllByStockIdOrderByPublishedAtDesc(Long stockId);
    List<News> findAllByStockIdAndPublishedAtAfterOrderByPublishedAtDesc(Long stockId, LocalDateTime after);
    boolean existsBySourceUrl(String sourceUrl);
}
