package primer.hackerton.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import primer.hackerton.domain.report.dto.ReportInfo;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class ReportInfoCrawlerServiceTest {

    @Autowired
    private ReportInfoCrawlerService reportInfoCrawlerService;


    @Test
    void test1() throws IOException, InterruptedException {
        List<ReportInfo> reportInfos = reportInfoCrawlerService.crawlReportInfo();
    }
}