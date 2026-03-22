package com.hanyahunya.stockbasket.domain.analysis.entity;
import com.hanyahunya.stockbasket.domain.news.entity.News;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "news_analysis")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder @AllArgsConstructor
public class NewsAnalysis {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false, unique = true)
    private News news;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SentimentType sentimentType;

    @Column(nullable = false)
    private int impactScore;

    private int marketShockScore;
    private int reliabilityScore;
    private int shortTermImpact;
    private int longTermImpact;

    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;

    @Column(columnDefinition = "TEXT")
    private String aiVerdict;

    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        this.analyzedAt = LocalDateTime.now();
    }
}
