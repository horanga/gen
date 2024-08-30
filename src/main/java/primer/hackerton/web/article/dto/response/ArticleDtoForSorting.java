package primer.hackerton.web.article.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticleDtoForSorting {

    private String title;
    private String link;
    private String pubDate;
}
