package primer.hackerton.s3.openapi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JsonDto {

    private String corpCode;
    private String reportName;
    private String rceptNo;
    private String dcmNo;

    public static JsonDto toDto(ReportDto dto, String dcmNo){
        return new JsonDto(dto.getCorpCode(), dto.getReportName(), dto.getRceptNo(), dcmNo);
    }
}
