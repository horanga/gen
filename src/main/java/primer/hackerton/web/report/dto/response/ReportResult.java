package primer.hackerton.web.report.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportResult {
    private String reportUrl;
    private String type;
}
