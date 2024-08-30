package primer.hackerton.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import primer.hackerton.domain.report.HtmlParser;
import primer.hackerton.domain.report.dto.ReportInfo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportInfoCrawlerService {

    private static final String ALL_COMPANY = "All";
    private static final int FIRST_PAGE = 1;

    private static final String PDF_SEARCH_API_URL = "https://dart.fss.or.kr/dsab007/detailSearch.ax";
    private final RestTemplate restTemplate;
    private final HtmlParser htmlParser;

    public List<ReportInfo> crawlAllReportInfo() throws InterruptedException, IOException {

        int page = FIRST_PAGE;
        List<ReportInfo> crawlResult;
        while (true) {
            crawlResult = htmlParser.extractReportInfoFromHtml(crawlReportInfoByPage(page));
            page += 1;
            if (crawlResult == null) {
                break;
            }
        }
        return filter(crawlResult);
    }

    public ReportInfo crawlReportInfoByCompanyName(String companyName) throws InterruptedException {
        MultiValueMap<String, Object> params = buildSearchParameters(FIRST_PAGE, companyName);
        String html = fetchHtmlResponse(params);
        List<ReportInfo> reportInfos = htmlParser.extractReportInfoFromHtml(html);

        ReportInfo reportInfo = null;
        for (int i = 0; i < reportInfos.size() - 1; i++) {
            String submissionDate1 = reportInfos.get(i).getSubmissionDate();
            String submissionDate2 = reportInfos.get(i + 1).getSubmissionDate();

            if (compareDate(submissionDate1, submissionDate2)) {
                reportInfo = reportInfos.get(i);
            }
        }
        return reportInfo;
    }

    public String crawlReportInfoByPage(int page) throws InterruptedException {
        MultiValueMap<String, Object> params = buildSearchParameters(page, ALL_COMPANY);
        String html = fetchHtmlResponse(params);
        if (html == null) return null;

        return html;
    }

    private static MultiValueMap<String, Object> buildSearchParameters(int page, String companyname) {

        LocalDate endDate = LocalDate.now();
        // 1년 전 날짜 계산
        LocalDate startDate = endDate.minusYears(1);
        // 날짜 포맷 지정 (yyyyMMdd)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        if (companyname.equals(ALL_COMPANY)) {
            params.add("textCrpNm", "");
            params.add("textCrpNm2", "");
        } else {
            params.add("textCrpNm", companyname);
            params.add("textCrpNm2", companyname);
        }

        params.add("currentPage", page);
        params.add("maxResults", "100");
        params.add("maxLinks", "10");
        params.add("sort", "date");
        params.add("series", "desc");
        params.add("textCrpCik", "");
        params.add("lateKeyword", "");
        params.add("keyword", "");
        params.add("reportNamePopYn", "");
        params.add("textkeyword", "");
        params.add("businessCode", "all");
        params.add("autoSearch", "N");
        params.add("option", "report");
        params.add("reportName", "분기보고서");
        params.add("tocSrch", "");
        params.add("textPresenterNm", "");
        params.add("startDate", startDate.format(formatter));
        params.add("endDate", endDate.format(formatter));
        params.add("decadeType", "");
        params.add("finalReport", "recent");
        params.add("businessNm", "");
        params.add("corporationType", "E");
        params.add("closingAccountsMonth", "all");
        params.add("tocSrch2", "");
        return params;
    }

    private String fetchHtmlResponse(MultiValueMap<String, Object> params) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<?> httpEntity = new HttpEntity<>(params, httpHeaders);
        String html = restTemplate.exchange(
                PDF_SEARCH_API_URL,
                HttpMethod.POST,
                httpEntity,
                String.class
        ).getBody();

        if (html.contains("조회 결과가 없습니다.")) {
            return null;
        }
        return html;
    }

    private List<ReportInfo> filter(List<ReportInfo> list) {
        Map<String, ReportInfo> map = new HashMap<>();

        for (ReportInfo newReportInfo : list) {
            String companyName = newReportInfo.getCompanyName();

            if (!map.containsKey(companyName)) {
                map.put(companyName, newReportInfo);
            } else {
                ReportInfo previouseReportInfo = map.get(companyName);
                if (!compareDate(previouseReportInfo.getSubmissionDate(), newReportInfo.getSubmissionDate())) {
                    //최신 보고서가 아닌 경우, 최신 보고서로 업데이트한다.
                    map.put(companyName, newReportInfo);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private boolean compareDate(String str1, String str2) {

        String[] date1 = str1.replaceAll("[^0-9.]", "").split("\\.");
        String[] date2 = str2.replaceAll("[^0-9.]", "").split("\\.");
        LocalDate localDate1 = LocalDate.of(
                Integer.parseInt(date1[0]),
                Integer.parseInt(date1[1]),
                1);
        LocalDate localDate2 = LocalDate.of(
                Integer.parseInt(date2[0]),
                Integer.parseInt(date2[1]),
                1);

        if (localDate1.isAfter(localDate2)) {
            return true; //최신 보고서다.
        } else {
            return false; //최신 보고서가 아니다.
        }

    }
}
