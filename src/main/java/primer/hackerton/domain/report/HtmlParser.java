package primer.hackerton.domain.report;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import primer.hackerton.domain.report.dto.ReportInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlParser {

    private static final Pattern DOCUMENT_NUMBER_PATTERN = Pattern.compile("node1\\['dcmNo'\\]\\s*=\\s*\"(\\d+)\";");
    private static final String SEARCH_API_BASE_URL = "https://dart.fss.or.kr/";
    private static final String USER_AGENT_INFO = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3";
    private static final String[] REPORT_EXCLUSION_KEYWORDS = {"[기재정정]", "정정"};
    private static final int ONE_MINUTE = 60 * 1000;
    private static final int ONE_SECOND = 1000;
    private static final int TEN_SECONDS = 10 * 1000;

    public List<ReportInfo> extractReportInfoFromHtml(String html) throws InterruptedException {
        List<ReportInfo> reportInfos = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements rows = doc.select("table.tbList tbody tr");

        /*URL 접속량을 제어하기 위한 변수
        URL 접속 한 번당 count++, count가 100번될 때마다 1분간 작업을 멈춘다.
        작업을 빠르게 진행하면 IP 차단됨
        */
        int count = 0;

        for (Element row : rows) {
            ReportInfo reportInfo = new ReportInfo();
            count += 1;

            if (count % 100 == 0) {
                Thread.sleep(ONE_MINUTE);
            }

            String companyName = extractCompanyNameFromRow(row);
            reportInfo.setCompanyName(companyName);

            String reportName = row.select("td").get(2).text();
            if (reportName.contains(REPORT_EXCLUSION_KEYWORDS[0]) || reportName.contains(REPORT_EXCLUSION_KEYWORDS[1])) {
                continue;
            }
            populateReportInfoFromRow(row, reportInfo, reportInfos);
            Thread.sleep(ONE_SECOND);
        }
        return reportInfos;
    }

    public static String extractCompanyNameFromRow(Element row) {
        return row.select("td").get(1).text().split(" ")[1];
    }

    private void populateReportInfoFromRow(Element row, ReportInfo reportInfo, List<ReportInfo> reportInfos) {
        reportInfo.setReportName(row.select("td").get(2).text());
        reportInfo.setReportLink(row.select("td").get(2).select("a").attr("href"));
        reportInfo.setSubmissionDate(row.select("td").get(4).text());
        String dcmNum = extractDocumentNumberFromUrl(reportInfo.getReportLink());
        reportInfo.setDocumentNumber(dcmNum);
        reportInfos.add(reportInfo);
    }

    public String extractDocumentNumberFromUrl(String reportUrl) {

        String url = SEARCH_API_BASE_URL + reportUrl;
        String dcmNo = "";

        try {
            Document doc = Jsoup.connect(url)
                    .timeout(TEN_SECONDS)
                    .userAgent(USER_AGENT_INFO)
                    .get();


            Elements scriptElements = doc.select("script");
            for (Element element : scriptElements) {
                String scriptContent = element.html();
                Matcher matcher = DOCUMENT_NUMBER_PATTERN.matcher(scriptContent);

                if (matcher.find()) {
                    dcmNo = matcher.group(1);
                    break;
                }
            }
        } catch (IOException e) {

        }

        return dcmNo;
    }
}
