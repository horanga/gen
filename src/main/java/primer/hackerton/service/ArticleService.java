package primer.hackerton.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import primer.hackerton.web.article.dto.response.ArticleDto;
import primer.hackerton.web.article.dto.response.ArticleDtoForSorting;

import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ArticleService {

    private static final String SEARCH_URL = "https://openapi.naver.com/v1/search/news.json";
    private static final String QUERY_PARAM = "?query=";
    private static final String DISPLAY_PARAM = "&display=";
    private static final String SORT_PARAM = "&sort=";
    private static final String SORT_DATE_CRITERIA = "date";
    private static final String SORT_ACCURACY_CRITERIA = "sim";
    private static final int MAX_ARTICLE = 100;

    @Value("${naver.client-id}")
    private String NAVER_CLIENT_ID;
    @Value("${naver.secret}")
    private String NAVER_SECRET_KEY;

    private final RestTemplate restTemplate;

    public List<ArticleDto> getArticles(String companyName, int count) {

        RequestEntity<Void> req = setRequestParam(companyName, SORT_DATE_CRITERIA);

        String response = restTemplate.exchange(req, String.class).getBody();
        List<ArticleDtoForSorting> articles = parseArticles(response);
        List<ArticleDto> filter = filter(articles, companyName, count);

        /*
        최신 날짜순으로 하면 제목에 회사 이름이 걸리지 않을 수 있음.
        그래서, 정확도 순으로 한번 더 검색하게끔 로직 구현
         */

        if(filter.isEmpty() || filter.size()<count){
            req = setRequestParam(companyName, SORT_ACCURACY_CRITERIA);
            response = restTemplate.exchange(req, String.class).getBody();
            articles = parseArticles(response);
            int countOfNeededArticles = count- filter.size();
            filter.addAll(filter(articles, companyName, countOfNeededArticles));
        }

        return filter;
    }

    private List<ArticleDto> filter(List<ArticleDtoForSorting> list, String companyName, int count) {

        return list.parallelStream()
                .filter(i ->
                {
                    String title = i.getTitle();
                    String companySubstring = companyName.substring(0, Math.min(3, companyName.length()));
                    return title.contains(companySubstring)
                            || title.length() == 2 && title.contains(companyName.substring(0, 2))
                            || title.length() > 2 && title.contains(companySubstring);
                }).map(i ->
                        {
                            try {
                                String content = getNews(i);

                                if (content == null) {
                                    return null;
                                }
                                return ArticleDto.toDto(i, content);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .filter(Objects::nonNull)
                .limit(count)
                .toList();
    }

    private String makesUrl(String query, String criteria) {
        return new StringBuffer(SEARCH_URL)
                .append(QUERY_PARAM)
                .append(query)
                .append(DISPLAY_PARAM)
                .append(MAX_ARTICLE)
                .append(SORT_PARAM)
                .append(criteria)
                .toString();
    }

    private static String getNews(ArticleDtoForSorting i) throws IOException {
        Document doc = null;
        try {


            doc = Jsoup.connect(i.getLink()).get();


        } catch (Exception e) {
            log.error("https 에러");
        }

        if(doc==null){
            return null;
        }
        Element articleContent = null;

        articleContent = doc.select("article#dic_area").first();
        if (articleContent == null) {
            articleContent = doc.select("article#dic_area").first();
        }

        if (articleContent == null) {
            articleContent = doc.select("#article_main").first();
        }

        if (articleContent == null) {
            articleContent = doc.select(".article").first();
        }

        if (articleContent == null) {
            articleContent = doc.select("#articleBody").first();
        }

        if (articleContent == null) {
            articleContent = doc.select("#article-view-content-div").first();
        }

        if (articleContent == null) {
            articleContent = doc.select(".articleView").first();
        }

        if (articleContent == null) {
            articleContent = doc.select(".read-news-main-contents").first();
        }

        if (articleContent == null) {
            articleContent = doc.select("#news-contents").first();
        }
        if (articleContent == null) {
            articleContent = doc.select(".news_bm").first();
        }

        if (articleContent == null) {
            return null;
        }
        return articleContent.text();
    }

    private RequestEntity<Void> setRequestParam(String companyName, String criteria) {
        RequestEntity<Void> req = RequestEntity
                .get(makesUrl(companyName, SORT_DATE_CRITERIA))
                .header("X-Naver-Client-Id", NAVER_CLIENT_ID)
                .header("X-Naver-Client-Secret", NAVER_SECRET_KEY)
                .build();
        return req;
    }

    private static List<ArticleDtoForSorting> parseArticles(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<ArticleDtoForSorting> articles = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.path("items");
            if (itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    ArticleDtoForSorting article = objectMapper.treeToValue(item, ArticleDtoForSorting.class);
                    articles.add(article);
                }
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
        return articles;
    }
}
