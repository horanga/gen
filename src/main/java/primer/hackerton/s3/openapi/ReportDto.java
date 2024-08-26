package primer.hackerton.s3.openapi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class ReportDto {

    private String corpCode;
    private String reportName;
    private String rceptNo;

    public ReportDto(String corpCode, String reportName, String rceptNo) {
        this.corpCode = corpCode;
        this.reportName = reportName;
        this.rceptNo = rceptNo;
    }
}
