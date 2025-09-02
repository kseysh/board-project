package board.comment.data;

import board.comment.entity.Comment;
import board.common.snowflake.Snowflake;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class DataInitializer {
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
            executorService.submit(() -> {
                insert(); // 각 작업은 insert 메서드를 호출하여 데이터를 삽입
                latch.countDown();
                System.out.println("latch.getCount() = " + latch.getCount());
            });
        }
        latch.await();
        executorService.shutdown();
    }

    void insert() {
        transactionTemplate.executeWithoutResult(status -> {
            Comment prev = null;
            for(int i = 0; i < BULK_INSERT_SIZE; i++) {
                Comment comment = Comment.create(
                        snowflake.nextId(),
                        "content",
                        i % 2 == 0 ? null : prev.getCommentId(),
                        1L,
                        1L

                );
                prev = comment;
                entityManager.persist(comment);
            }
        });
    }

    @Test
    void truncate() {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM Comment").executeUpdate(); // Article 엔티티의 모든 데이터를 삭제
        });
        System.out.println("Data truncated successfully.");
    }
}
