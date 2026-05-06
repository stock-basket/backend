package com.hanyahunya.stockbasket.domain.analysis.repository;

import java.util.List;
import java.util.Optional;

import com.hanyahunya.stockbasket.domain.analysis.entity.NewsAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsAnalysisRepository extends JpaRepository<NewsAnalysis, Long> {

    // (기존 단일 조회 메서드도 연관관계 필드를 쓰려면 언더바를 넣는 것이 좋습니다)
    Optional<NewsAnalysis> findByNews_Id(Long newsId);

    // 🌟 수정된 다중 조회 메서드 (News 안에 있는 Id 객체들의 리스트로 IN 조회)
    List<NewsAnalysis> findByNews_IdIn(List<Long> newsIds);
}