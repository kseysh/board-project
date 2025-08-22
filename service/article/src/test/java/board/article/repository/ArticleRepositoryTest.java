package board.article.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import board.article.entity.Article;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository articleRepository;

    @Test
    void findAllTest() {
        final long LIMIT = 30L;
        List<Article> articles = articleRepository.findAll(1L, 1499970L, LIMIT);
        assertEquals(LIMIT, articles.size());
        for (Article article : articles) {
            log.info("article = {}", article);
        }
    }

    @Test
    void countTest() {
        final long LIMIT = 10000L;
        Long count = articleRepository.count(1L, LIMIT);
        assertEquals(LIMIT, count);
    }
}