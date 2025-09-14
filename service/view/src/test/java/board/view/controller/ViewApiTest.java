package board.view.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
class ViewApiTest {
    RestClient restClient = RestClient.create("http://localhost:9003");

    @Test
    void viewTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(10000);

        for(int i=0; i<10000; i++) {
            executorService.submit(() -> {
                restClient.post()
                        .uri("/v1/article-views/articles/{articleId}/users/{userId}", 4L, 1L)
                        .retrieve()
                        .toBodilessEntity();
                latch.countDown();
            });
        }

        latch.await();

        Long count = restClient.get()
                .uri("/v1/article-views/articles/{articleId}/count", 4L)
                .retrieve()
                .body(Long.class);

        log.info("count = {}", count);
    }
}
