package primer.hackerton.web.article.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleDto {

    private String title;
    private String link;
    private String content;
    private String pubDate;

    public static ArticleDto toDto(ArticleDtoForSorting dto, String content) {
        return new ArticleDto(dto.getTitle(), dto.getLink(), content, dto.getPubDate());
    }
}
