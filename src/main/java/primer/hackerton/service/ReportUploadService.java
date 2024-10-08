package primer.hackerton.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import primer.hackerton.domain.report.dto.ReportInfo;
import primer.hackerton.web.report.dto.request.SearchDto;
import primer.hackerton.web.report.dto.response.ReportResult;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportUploadService {
    public static final String QUARTERLY_REPORT = "분기보고서";
    private static final String AUDIT_REPORT = "감사보고서";
    private static final String PDF_DOWNLOAD_URL = "https://dart.fss.or.kr/pdf/download/pdf.do?";
    private static final String RCP_NO_PARAM = "rcp_no=";
    private static final String DCM_NO_PARAM = "&dcm_no=";

    private final ReportInfoCrawlerService reportInfoCrawlerService;
    private final S3Service s3Service;

    public void uploadAllPdfsToS3() throws IOException, InterruptedException {
        List<ReportInfo> reportInfos = reportInfoCrawlerService.crawlAllReportInfo();

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


    public ReportResult uploadPDFToS3(SearchDto searchDto) throws InterruptedException, IOException {
        ReportInfo reportInfo = reportInfoCrawlerService.crawlReportInfoByCompanyName(searchDto.getCompanyName(), QUARTERLY_REPORT);
        ReportResult result = new ReportResult();
        if(reportInfo==null){
            reportInfo = reportInfoCrawlerService.crawlReportInfoByCompanyName(searchDto.getCompanyName(), AUDIT_REPORT);
            result.setType(AUDIT_REPORT);
        } else{
            result.setType(QUARTERLY_REPORT);
        }
        String pdfLink = reportInfo.getReportLink();
        String dcmNo = reportInfo.getDocumentNumber();
        String date = getDate(reportInfo);
        String url = makeUrl(pdfLink, dcmNo);

        String reportUrl = s3Service.uploadReportsToS3(searchDto.getCompanyName(), url, date);
        result.setReportUrl(reportUrl);


        return result;
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
