package primer.hackerton.web.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import primer.hackerton.service.ReportUploadService;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/reports")
@Controller
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
}
