package primer.hackerton.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private final AmazonS3 amazonS3Client;

    @Retryable(retryFor = IOException.class, backoff = @Backoff(delay = 1000))
    public String uploadReportsToS3(String name, String fileUrl, String date) throws IOException, InterruptedException {

        String key = date + "/" + name + ".pdf";

        if (amazonS3Client.doesObjectExist(bucketName, key)) {
            return amazonS3Client.getUrl(bucketName, key).toString();
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            int contentLength = connection.getContentLength();

            try (InputStream inputStream = connection.getInputStream()) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType("application/pdf");
                metadata.setContentLength(contentLength);  // 파일 크기 설정

                amazonS3Client.putObject(new PutObjectRequest(
                        bucketName,
                        key,
                        inputStream,
                        metadata
                ).withCannedAcl(CannedAccessControlList.Private));

                return amazonS3Client.getUrl(bucketName, key).toString();
            }
        } catch (IOException e) {
            log.info(fileUrl + " 에러");
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}


