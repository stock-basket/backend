package com.hanyahunya.stockbasket.domain.news.entity;
import com.hanyahunya.stockbasket.domain.stock.entity.Stock;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_code", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, unique = true)
    private String sourceUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime publishedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime crawledAt;

    @PrePersist
    protected void onCreate() {
        this.crawledAt = LocalDateTime.now();
    }
}
