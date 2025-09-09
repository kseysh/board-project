package board.comment.data;

import board.comment.entity.CommentPath;
import board.comment.entity.CommentV2;
import board.common.snowflake.Snowflake;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Slf4j
class DataInitializerV2 {
    @PersistenceContext
    EntityManager entityManager; // EntityManager를 사용하여 JPA 엔티티를 관리
    @Autowired
    TransactionTemplate transactionTemplate; // TransactionTemplate을 사용하여 트랜잭션 관리
    Snowflake snowflake = Snowflake.getInstance();
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT); // CountDownLatch를 사용하여 모든 스레드가 작업을 완료할 때까지 대기

    static final int BULK_INSERT_SIZE = 1000;
    static final int EXECUTE_COUNT = 5000; // 5,000,000 데이터 삽입

    @Test
    void initialize() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for(int i = 0; i < EXECUTE_COUNT; i++) {
            int start = i * BULK_INSERT_SIZE;
            int end = start + BULK_INSERT_SIZE;
            executorService.submit(() -> {
                insert(start, end); // 멀티 스레드로 동작하기 때문에 path의 unique 제약에 방해되지 않게 구간을 나누어줌
                latch.countDown();
                log.info("latch.getCount() = {}", latch.getCount());
            });
        }
        latch.await();
        executorService.shutdown();
    }

    void insert(int start, int end) {
        transactionTemplate.executeWithoutResult(status -> {
            for(int i = start; i < end; i++) {
                CommentV2 comment = CommentV2.create(
                        snowflake.nextId(),
                        "content",
                        1L,
                        1L,
                        toPath(i)
                );
                try {
                    entityManager.persist(comment);
                } catch (Exception e) {
                    log.error("Insert failed for comment: {}", comment, e);
                    throw e; // 다시 던져서 트랜잭션 롤백 유지
                }
            }
        });
    }

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int DEPTH_CHUNK_SIZE = 5;

    private CommentPath toPath(int value) {
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
            path.insert(0, CHARSET.charAt(value % CHARSET.length()));
            value /= CHARSET.length();
        }
        return CommentPath.create(path.toString());
    }

    @Test
    void truncate() {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM CommentV2").executeUpdate(); // Comment 엔티티의 모든 데이터를 삭제
        });
        log.info("Data truncated successfully.");
    }
}
