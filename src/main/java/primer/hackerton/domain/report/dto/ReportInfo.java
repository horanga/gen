package primer.hackerton.domain.report.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportInfo {

    private String companyName;
    private String reportName;
    private String reportLink;
    private String submissionDate;
    private String documentNumber;
}
