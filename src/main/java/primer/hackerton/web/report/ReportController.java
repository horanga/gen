package primer.hackerton.web.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import primer.hackerton.service.ReportUploadService;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/reports")
@RestController
public class ReportController {

    private final ReportUploadService reportUploadService;

    @PostMapping("/upload")
    public void uploadAllQuarterReportsToS3() {
        try {
            reportUploadService.uploadAllPdfsToS3();
        } catch (IOException | InterruptedException e) {
            log.info("pdf 파일 업로드 실패");
        }
    }

    @PostMapping("/search")
    public String searchQuarterReportsToS3(@RequestBody String searchString) {

        try {
            return reportUploadService.uploadPDFToS3(searchString);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
