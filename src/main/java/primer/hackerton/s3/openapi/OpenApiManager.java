package primer.hackerton.s3.openapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class OpenApiManager {
    private final String ROOT_PATH = "/home/ubuntu/gen/ouput1.csv";
    private final String BASE_URL = "https://opendart.fss.or.kr/api/";
    private final String API_URL = "list.json";
    private final String SERVICE_KEY = "?crtfc_key=419d5b210cd8f71d5d4671b5c41cf1aeda55fbbf";
    private final String CORP_CODE = "&corp_code=";
    private final String BGN_DE = "&bgn_de=";

    private final RestTemplate restTemplate;
    int fileCount = 1;


    public void processReports(String path) throws InterruptedException, IOException {

        List<String[]> urlInfoList = readCsv(path);
        Map<String, List<ReportDto>> reportMap = new HashMap<>();
        int count = 0;

        for (String[] info : urlInfoList) {
            count++;
            List<Map<String, Object>> maps = getReports(info);
            log.info(String.valueOf(count));

            if(count%100==0){
                Thread.sleep(60000);
            }

            if(count==500){
                log.info(String.valueOf(count)+"번째 파일 생성시작");
                    write(reportMap);
                    reportMap.clear();
                    fileCount++;
                    count=0;
            }

            if (maps == null) continue;

            List<ReportDto> essentialReportList = new ArrayList<>();
            StringBuilder corpName = null;
            StringBuilder corpCode;
            StringBuilder reportName;
            StringBuilder rceptNo;

            for (Map<String, Object> item : maps) {
                corpName = new StringBuilder((String) item.get("corp_name"));
                corpCode = new StringBuilder((String) item.get("corp_code"));
                reportName = new StringBuilder((String) item.get("report_nm"));
                rceptNo = new StringBuilder((String) item.get("rcept_no"));

                ReportDto reportDto = new ReportDto(corpCode.toString(), reportName.toString(), rceptNo.toString());

                if (reportName.toString().contains("분기") && reportName.toString().contains("2024")|| reportName.toString().contains("감사") && reportName.toString().contains("2024")) {
                    essentialReportList.add(reportDto);
                }
            }
            if (!essentialReportList.isEmpty()) {
                reportMap.put(corpName.toString(), essentialReportList);
            }
        }
    }

    public void write(Map<String, List<ReportDto>> reportMap) {
        String fileName = generateFileName();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ROOT_PATH + "/"+fileName+".csv"))) {
            // CSV 헤더 작성
            bufferedWriter.write("Corp Name,Corp Code,Report Name,Rcept No\n");

            for (Map.Entry<String, List<ReportDto>> entry : reportMap.entrySet()) {
                String corpName = entry.getKey();
                List<ReportDto> reports = entry.getValue();

                for (ReportDto report : reports) {
                    String line = String.format("%s,%s,%s,%s\n",
                            corpName,
                            report.getCorpCode(),
                            report.getReportName(),
                            report.getRceptNo());
                    bufferedWriter.write(line);
                }
            }

            System.out.println("CSV file has been written successfully.");
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateFileName() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);
        return String.format("output_%s_%d.csv", timestamp, fileCount);
    }

    @Retryable(retryFor = ResourceAccessException.class, backoff = @Backoff(delay = 1000))
    private List<Map<String, Object>> getReports(String[] info) throws InterruptedException {
        String url = makeUrl(info[0], info[1]);

        List<Map<String, Object>> maps = new ArrayList<>();

            try {
                maps = get(url);
            } catch (ResourceAccessException e) {

            }

        if (maps==null) {
            return null;
        }
        return maps;
    }

    private static List<String[]> readCsv(String path) {
        List<String[]> list = new ArrayList<>();
        File file = new File(path);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line = bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split(",");
                String[] arr = new String[2];
                arr[0] = split[1];
                arr[1] = split[2];
                list.add(arr);
            }
        } catch (Exception e) {

        }
        return list;
    }

    public List<Map<String, Object>> get(String url) throws InterruptedException {
        HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());
        Thread.sleep(1000);

        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return (List<Map<String, Object>>) exchange.getBody().get("list");
    }

    public String makeUrl(String corpCode, String bgnDe) {
        return new StringBuilder()
                .append(BASE_URL)
                .append(API_URL)
                .append(SERVICE_KEY)
                .append(CORP_CODE)
                .append(corpCode)
                .append(BGN_DE)
                .append(bgnDe)
                .toString();
    }

}
