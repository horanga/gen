package primer.hackerton.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import primer.hackerton.domain.report.dto.ReportInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private final AmazonS3 amazonS3Client;

    @Retryable(retryFor = IOException.class, backoff = @Backoff(delay = 1000))
    public void uploadReportsToS3(String name, String fileUrl, String date) throws IOException, InterruptedException {

        String key = date + "/" + name + ".pdf";

        try (InputStream inputStream = new URL(fileUrl).openStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/pdf");

            amazonS3Client.putObject(new PutObjectRequest(
                    bucketName,
                    key,
                    inputStream,
                    metadata
            ).withCannedAcl(CannedAccessControlList.Private));
        } catch (IOException e) {
            log.info(fileUrl + "에러");
        }
    }
}


