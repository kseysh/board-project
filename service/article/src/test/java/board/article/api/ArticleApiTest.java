package board.article.api;

import static board.article.fixtures.ArticleFixture.ARTICLE_CREATE_REQUEST_FIXTURE;
import static board.article.fixtures.ArticleFixture.UPDATED_CONTENT;
import static board.article.fixtures.ArticleFixture.UPDATED_TITLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import board.article.service.request.ArticleUpdateRequest;
import board.article.service.response.ArticlePageResponse;
import board.article.service.response.ArticleResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@Slf4j
@Transactional
class ArticleApiTest {

    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create();
        assertEquals(ARTICLE_CREATE_REQUEST_FIXTURE.getTitle(), response.getTitle());
        assertEquals(ARTICLE_CREATE_REQUEST_FIXTURE.getContent(), response.getContent());
    }

    ArticleResponse create() {
        return restClient.post()
                .uri("/v1/articles")
                .body(ARTICLE_CREATE_REQUEST_FIXTURE)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void readTest() {
        ArticleResponse createdArticle = create();
        ArticleResponse response = read(createdArticle.getArticleId());
        assertEquals(createdArticle.getArticleId(), response.getArticleId());
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        ArticleResponse createdArticle = create();
        update(createdArticle.getArticleId(), UPDATED_TITLE, UPDATED_CONTENT);
        ArticleResponse response = read(createdArticle.getArticleId());
        assertEquals(UPDATED_TITLE, response.getTitle());
        assertEquals(UPDATED_CONTENT, response.getContent());
    }

    void update(Long articleId, String title, String content) {
        restClient.put()
                .uri("/v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest(title, content))
                .retrieve()
                .toBodilessEntity();
    }

    @Test
    void deleteTest() {
        ArticleResponse createdArticle = create();
        restClient.delete()
                .uri("/v1/articles/{articleId}", createdArticle.getArticleId())
                .retrieve()
                .toBodilessEntity();
    }

    @Test
    void readAllTest() {
        Long pageSize = 30L;
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&pageSize="+pageSize+"&page=50000")
                .retrieve()
                .body(ArticlePageResponse.class);
        assertEquals(pageSize, response.getArticles().size());
        for (ArticleResponse article : response.getArticles()) {
            log.info("articleId = " + article.getArticleId());
        }
    }
}