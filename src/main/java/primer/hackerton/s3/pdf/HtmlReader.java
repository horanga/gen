package primer.hackerton.s3.pdf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import primer.hackerton.s3.openapi.JsonDto;
import primer.hackerton.s3.openapi.ReportDto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlReader {

    private static Pattern pattern = Pattern.compile("node1\\['dcmNo'\\]\\s*=\\s*\"(\\d+)\";");

    public void setDcmNoToJsonFile(String path) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<ReportDto>> map = objectMapper.readValue(new File(path), new TypeReference<Map<String, List<ReportDto>>>() {
        });

        Map<String, List<JsonDto>> result = new HashMap<>();

        for (Map.Entry<String, List<ReportDto>> entry : map.entrySet()) {
            List<ReportDto> reports = entry.getValue();
            List<JsonDto> resultList = new ArrayList<>();

            for (ReportDto report : reports) {
                String rceptNo = report.getRceptNo();
                String dcmNo = getDcmNo(rceptNo);

                resultList.add(JsonDto.toDto(report, dcmNo));
            }

            result.put(entry.getKey(), resultList);
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("output.json"), result);
        System.out.println("dcmNo가 추가된 JSON 파일이 생성되었습니다.");


    }


    public String getDcmNo(String rcpNo) {
        String url = "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=" + rcpNo;
        String dcmNo = "";

        try {
            // 1. URL에서 HTML 문서 가져오기
            Document doc = Jsoup.connect(url)
                    .timeout(10000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .get();

            // 2. 모든 스크립트 태그 가져오기
            Elements scriptElements = doc.select("script");

            // 3. 자바스크립트 코드에서 dcmNo를 찾기 위한 정규 표현식 패턴 정의

            // 4. 스크립트 태그 내에서 dcmNo 추출
            for (Element element : scriptElements) {
                String scriptContent = element.html();
                Matcher matcher = pattern.matcher(scriptContent);

                if (matcher.find()) {
                    dcmNo = matcher.group(1); // 첫 번째 캡처 그룹이 dcmNo 값
                    break; // 첫 번째로 찾은 dcmNo 값을 반환
                }
            }
        } catch (IOException e) {

        }

        return dcmNo;
    }
}
