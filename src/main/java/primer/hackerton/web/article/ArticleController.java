package primer.hackerton.web.article;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import primer.hackerton.service.ArticleService;
import primer.hackerton.web.article.dto.response.ArticleDto;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/articles")
@RestController
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/search")
    public List<ArticleDto> search(@RequestParam("companyName") String companyName) {
        return articleService.getArticles(companyName);
    }
}
