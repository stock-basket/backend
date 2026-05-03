package com.hanyahunya.stockbasket.infra.news;

public interface NewsIngestionService {
    void ingestByStock(String stockCode, int count);
    void ingestByStock(String stockCode);
    void ingestAll(int count);
}
