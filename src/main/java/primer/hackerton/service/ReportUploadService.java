package primer.hackerton.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import primer.hackerton.domain.report.dto.ReportInfo;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportUploadService {

    private static final String PDF_DOWNLOAD_URL = "https://dart.fss.or.kr/pdf/download/pdf.do?";
    private static final String RCP_NO_PARAM = "rcp_no=";
    private static final String DCM_NO_PARAM = "&dcm_no=";

    private final ReportInfoCrawlerService reportInfoCrawlerService;
    private final S3Service s3Service;

    public void uploadAllPdfsToS3() throws IOException, InterruptedException {
        List<ReportInfo> reportInfos = reportInfoCrawlerService.crawlReportInfo();

        String date = getDate(reportInfos.get(0));
        int count = 0;
        for (ReportInfo reportInfo : reportInfos) {
            count++;
            if (count % 100 == 0) {
                Thread.sleep(60000);
            }
            String companyName = reportInfo.getCompanyName();
            String pdfLink = reportInfo.getReportLink();
            String dcmNo = reportInfo.getDocumentNumber();
            String url = makeUrl(pdfLink, dcmNo);
            s3Service.uploadReportsToS3(companyName, url, date);
            Thread.sleep(1000);
        }
    }

    private String getDate(ReportInfo reportInfos) {
      return reportInfos.getReportName().replaceAll("[^0-9.]", "");
    }

    private String makeUrl(String pdfLink, String dcmNo) {

        String[] split = pdfLink.split("rcpNo=");
        //split[1] = rcp_no

        return new StringBuilder()
                .append(PDF_DOWNLOAD_URL)
                .append(RCP_NO_PARAM)
                .append(split[1])
                .append(DCM_NO_PARAM)
                .append(dcmNo)
                .toString();
    }
}
