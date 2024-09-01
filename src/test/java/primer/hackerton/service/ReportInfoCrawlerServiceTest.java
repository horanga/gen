package primer.hackerton.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;
import primer.hackerton.domain.report.HtmlParser;
import primer.hackerton.web.article.dto.response.ArticleDto;
import primer.hackerton.web.article.dto.response.ArticleDtoForSorting;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@SpringBootTest
class ReportInfoCrawlerServiceTest {

    @Value("${naver.client-id}")
    private String NAVER_CLIENT_ID;
    @Value("${naver.secret}")
    private String NAVER_SECRET_KEY;


    private static final String PDF_SEARCH_API_URL = "https://dart.fss.or.kr/dsab007/detailSearch.ax";
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ReportInfoCrawlerService reportInfoCrawlerService;
    @Autowired
    private ArticleService articleService;

    private static final String SEARCH_URL = "https://openapi.naver.com/v1/search/news.json";
    private static final String QUERY_PARAM = "?query=";
    private static final String DISPLAY_PARAM = "&display=";
    private static final String SORT_PARAM = "&sort=";
    private static final String SORT_CRITERIA = "date";
    private static final int MAX_ARTICLE = 100;

    @Test
    void test1() throws IOException, InterruptedException {

        Map<String, Integer> map = new HashMap<>();
        Set<String> set = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            String html = reportInfoCrawlerService.crawlReportInfoByPage(i);
            Document doc = Jsoup.parse(html);
            Elements rows = doc.select("table.tbList tbody tr");

            for (Element row : rows) {
                String companyName = HtmlParser.extractCompanyNameFromRow(row);
                set.add(companyName);

            }
        }

        List<String> list = new ArrayList<>(set);

        for(int i =0; i<list.size(); i++) {
            String companyName = list.get(i);
            RequestEntity<Void> req = RequestEntity
                    .get(makesUrl(companyName))
                    .header("X-Naver-Client-Id", NAVER_CLIENT_ID)
                    .header("X-Naver-Client-Secret", NAVER_SECRET_KEY)
                    .build();

            String response = restTemplate.exchange(req, String.class).getBody();
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


            }

            List<ArticleDtoForSorting> ar = articles.stream().filter(j -> j.getTitle().contains(companyName)).toList();
            for(int j=0; j<ar.size(); j++) {
                try {


                    String domain = new URL(ar.get(j).getLink()).getHost();
                    map.put(domain, map.getOrDefault(domain, 0) + 1);
                } catch (MalformedURLException e){

                }
            }
        }
        // Map.Entry 리스트 생성
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());

        // Entry 리스트를 value 값으로 내림차순 정렬
        entryList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // 정렬된 리스트로부터 key-value 쌍 출력
        for (Map.Entry<String, Integer> entry : entryList) {
            System.out.println("Domain: " + entry.getKey() + ", Count: " + entry.getValue());
        }
    }

    private String makesUrl(String query) {
        return new StringBuffer(SEARCH_URL)
                .append(QUERY_PARAM)
                .append(query)
                .append(DISPLAY_PARAM)
                .append(MAX_ARTICLE)
                .append(SORT_PARAM)
                .append(SORT_CRITERIA)
                .toString();
    }



}