package com.hanyahunya.stockbasket.domain.stock.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stocks")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Stock {

    @Id
    @Column(name = "stock_code")
    private String stockCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketType market;

//    private String sector; // 넣을까 말까
}
