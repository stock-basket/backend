package com.hanyahunya.stockbasket.infra.news.crawler;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
class NaverNewsCrawler implements NewsCrawler {

    // 모바일 브라우저 User-Agent (네이버 모바일 뉴스 페이지에 더 적합하도록 설정)
    private static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1";
    private static final int TIMEOUT_MS = 5000;

    @Override
    public NewsCrawlResult crawl(String url) {
        if (url == null || url.isBlank() || !url.startsWith("https://n.news.naver.com")) {
            log.warn("[NaverNewsCrawler] 지원하지 않는 URL입니다: {}", url);
            return null;
        }

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .get();

            String publisher = extractPublisher(doc);
            String content = extractContent(doc);

            if (content == null || content.isBlank()) {
                log.warn("[NaverNewsCrawler] 본문 추출 실패: {}", url);
                return null;
            }

            return NewsCrawlResult.builder()
                    .publisher(publisher)
                    .content(content)
                    .build();

        } catch (IOException e) {
            log.error("[NaverNewsCrawler] 크롤링 중 오류 발생 - URL: {}, Message: {}", url, e.getMessage());
            return null;
        }
    }

    @Override
    public Provider getProvider() {
        return Provider.NAVER;
    }

    /**
     * HTML 문서에서 언론사 이름을 추출합니다. (여러 케이스 대비)
     */
    private String extractPublisher(Document doc) {
        String publisher = null;

        // 1순위: 말씀해주신 최근 모바일/PC 네이버 뉴스 텍스트 태그
        Element spanPublisher = doc.selectFirst("span.media_end_head_top_press");
        if (spanPublisher != null) {
            publisher = spanPublisher.text();
        }

        // 2순위: 네이버 뉴스 헤더의 로고 이미지 title 속성
        if (publisher == null || publisher.isBlank()) {
            Element logoImg = doc.selectFirst("img.media_end_head_top_logo_img");
            if (logoImg != null && logoImg.hasAttr("title")) {
                publisher = logoImg.attr("title");
            }
        }

        // 3순위: 또 다른 형태의 텍스트 태그 (과거 또는 특정 레이아웃)
        if (publisher == null || publisher.isBlank()) {
            Element textLogo = doc.selectFirst("em.media_end_head_top_logo_text");
            if (textLogo != null) {
                publisher = textLogo.text();
            }
        }

        // 4순위: 스포츠/연예 뉴스 등에서 쓰이는 메타 태그 (오픈그래프)
        if (publisher == null || publisher.isBlank()) {
            Element metaPublisher = doc.selectFirst("meta[name=twitter:creator]");
            if (metaPublisher != null) {
                publisher = metaPublisher.attr("content");
            }
        }

        return (publisher != null && !publisher.isBlank()) ? publisher.trim() : "알 수 없는 언론사";
    }

    /**
     * HTML 문서에서 뉴스 본문을 추출하고 불필요한 태그를 제거합니다.
     */
    private String extractContent(Document doc) {
        Element contentElement = null;

        // 1순위: 일반적인 네이버 뉴스 본문 영역
        contentElement = doc.selectFirst("#dic_area");

        // 2순위: 네이버 스포츠, 연예 기사 등 다른 레이아웃 대응
        if (contentElement == null) {
            contentElement = doc.selectFirst("#articeBody");
        }
        if (contentElement == null) {
            contentElement = doc.selectFirst("#newsEndContents");
        }

        if (contentElement != null) {
            // 본문 분석에 방해되는 요소들 제거 (기자 정보, 사진 설명, VOD 등)
            contentElement.select("em.img_desc, span.end_photo_org, div.vod_area, .byline_s").remove();

            // 순수 텍스트 추출 (HTML 태그 자동 제거됨)
            String cleanText = contentElement.text();

            // 연속된 공백이나 줄바꿈 정리
            return cleanText.replaceAll("\\s+", " ").trim();
        }
        return null;
    }
}