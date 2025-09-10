package board.like.controller;

import static org.junit.jupiter.api.Assertions.*;

import board.like.service.response.ArticleLikeResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClient;

@Slf4j
class ArticleLikeControllerTest {
    RestClient restClient = RestClient.create("http://localhost:9002");

    @Test
    void likeAndUnlikeTest(){
        Long articleId = 9999L;

        like(articleId, 1L);
        like(articleId, 2L);
        like(articleId, 3L);

        ArticleLikeResponse response1 = read(articleId, 1L);
        ArticleLikeResponse response2 = read(articleId, 2L);
        ArticleLikeResponse response3 = read(articleId, 3L);

        log.info("response1: {}", response1);
        log.info("response2: {}", response2);
        log.info("response3: {}", response3);

        unlike(articleId, 1L);
        unlike(articleId, 2L);
        unlike(articleId, 3L);

        assertThrows(InternalServerError.class, () -> read(articleId, 1L));
        assertThrows(InternalServerError.class, () -> read(articleId, 2L));
        assertThrows(InternalServerError.class, () -> read(articleId, 3L));
    }

    void like(Long articleId, Long userId) {
        restClient.post()
                .uri("/v1/article-likes/articles/{articleId}/users/{userId}", articleId, userId)
                .retrieve()
                .toBodilessEntity();
    }

    void unlike(Long articleId, Long userId) {
        restClient.delete()
                .uri("/v1/article-likes/articles/{articleId}/users/{userId}", articleId, userId)
                .retrieve()
                .toBodilessEntity();
    }

    ArticleLikeResponse read(Long articleId, Long userId) {
        return restClient.get()
                .uri("/v1/article-likes/articles/{articleId}/users/{userId}", articleId, userId)
                .retrieve()
                .body(ArticleLikeResponse.class);
    }
}