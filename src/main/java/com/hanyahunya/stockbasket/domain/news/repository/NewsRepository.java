package com.hanyahunya.stockbasket.domain.news.repository;

import com.hanyahunya.stockbasket.domain.analysis.entity.SentimentType;
import com.hanyahunya.stockbasket.domain.news.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findAllByStock_StockCodeOrderByPublishedAtDesc(String stockCode);
    List<News> findAllByStock_StockCodeAndPublishedAtAfterOrderByPublishedAtDesc(String stockCode, LocalDateTime after);
    boolean existsBySourceUrl(String sourceUrl);

    @Query("SELECT n FROM News n " +
           "WHERE n.stock IN (SELECT VALUE(s) FROM User u JOIN u.stocks s WHERE u.id = :userId) " +
           "ORDER BY n.publishedAt DESC")
    Page<News> findNewsByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT na.news FROM NewsAnalysis na " +
           "WHERE na.news.stock IN (SELECT VALUE(s) FROM User u JOIN u.stocks s WHERE u.id = :userId) " +
           "AND na.sentimentType = :sentimentType " +
           "ORDER BY na.news.publishedAt DESC")
    Page<News> findNewsByUserIdAndSentiment(@Param("userId") UUID userId,
                                            @Param("sentimentType") SentimentType sentimentType,
                                            Pageable pageable);

    @Query("SELECT na.news FROM NewsAnalysis na " +
           "WHERE na.news.stock IN (SELECT VALUE(s) FROM User u JOIN u.stocks s WHERE u.id = :userId) " +
           "AND na.impactScore >= 80 " +
           "ORDER BY na.impactScore DESC, na.news.publishedAt DESC")
    Page<News> findUrgentNewsByUserId(@Param("userId") UUID userId, Pageable pageable);

    Page<News> findAllByStock_StockCodeOrderByPublishedAtDesc(String stockCode, Pageable pageable);
}
