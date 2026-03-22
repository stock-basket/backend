package com.hanyahunya.stockbasket.domain.analysis.repository;
import com.hanyahunya.stockbasket.domain.analysis.entity.NewsAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NewsAnalysisRepository extends JpaRepository<NewsAnalysis, Long> {
    Optional<NewsAnalysis> findByNewsId(Long newsId);
    boolean existsByNewsId(Long newsId);
}
