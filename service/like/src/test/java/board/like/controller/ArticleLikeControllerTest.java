package board.like.controller;

import static org.junit.jupiter.api.Assertions.*;

import board.like.service.response.ArticleLikeResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClient;

@Slf4j
class ArticleLikeControllerTest {

    RestClient restClient = RestClient.create("http://localhost:9002");

    @Test
    void likeAndUnlikeTest() {
        Long articleId = 9999L;

        like(articleId, 1L, LockType.OPTIMISTIC);
        like(articleId, 2L, LockType.OPTIMISTIC);
        like(articleId, 3L, LockType.OPTIMISTIC);

        ArticleLikeResponse response1 = read(articleId, 1L);
        ArticleLikeResponse response2 = read(articleId, 2L);
        ArticleLikeResponse response3 = read(articleId, 3L);

        log.info("response1: {}", response1);
        log.info("response2: {}", response2);
        log.info("response3: {}", response3);

        unlike(articleId, 1L, LockType.OPTIMISTIC);
        unlike(articleId, 2L, LockType.OPTIMISTIC);
        unlike(articleId, 3L, LockType.OPTIMISTIC);

        assertThrows(InternalServerError.class, () -> read(articleId, 1L));
        assertThrows(InternalServerError.class, () -> read(articleId, 2L));
        assertThrows(InternalServerError.class, () -> read(articleId, 3L));
    }

    void like(Long articleId, Long userId, LockType lockType) {
        restClient.post()
                .uri(lockType.url, articleId, userId)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {})
                .toBodilessEntity();
    }

    void unlike(Long articleId, Long userId, LockType lockType) {
        restClient.delete()
                .uri(lockType.url, articleId, userId)
                .retrieve()
                .toBodilessEntity();
    }

    ArticleLikeResponse read(Long articleId, Long userId) {
        return restClient.get()
                .uri("/v1/article-likes/articles/{articleId}/users/{userId}", articleId, userId)
                .retrieve()
                .body(ArticleLikeResponse.class);
    }

    @Test
    void likePerformanceTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        likePerformanceTest(executorService, 1112L, LockType.PESSIMISTIC_V1);
        likePerformanceTest(executorService, 2223L, LockType.PESSIMISTIC_V2);
        likePerformanceTest(executorService, 3334L, LockType.OPTIMISTIC);
    }

    void likePerformanceTest(ExecutorService executorService, Long articleId, LockType lockType)
            throws InterruptedException
    {
        CountDownLatch latch = new CountDownLatch(3000);
        log.info("{} start", lockType);
        like(articleId, 1L, lockType);

        long start = System.nanoTime();
        for(int i = 0; i < 3000; i++) {
            long userId = i + 2;
            executorService.submit(() -> {
                like(articleId, userId, lockType);
                latch.countDown();
            });
        }

        latch.await();

        long end = System.nanoTime();

        log.info("lockType = {}, time = {}{}", lockType,  (end - start) / 1000000, "ms");
        log.info("{} end", lockType);

        log.info("count = {}", count(articleId));
    }

    Long count(Long articleId) {
        return restClient.get()
                .uri("/v1/article-likes/articles/{articleId}/count", articleId)
                .retrieve()
                .body(Long.class);
    }

    public enum LockType {
        PESSIMISTIC_V1("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock"),
        PESSIMISTIC_V2("/v2/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock"),
        OPTIMISTIC("/v1/article-likes/articles/{articleId}/users/{userId}/optimistic-lock");

        public final String url;

        LockType(String url) {
            this.url = url;
        }

    }
}