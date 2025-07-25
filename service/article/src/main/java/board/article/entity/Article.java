package board.article.entity;

import board.article.service.request.ArticleCreateRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {
    @Id
    private Long articleId;
    @NonNull
    private String title;
    @NonNull
    private String content;
    @NonNull
    private Long boardId; // shard key
    @NonNull
    private Long writerId;
    @NonNull
    private LocalDateTime createdAt;
    @NonNull
    private LocalDateTime modifiedAt;

    public static Article create(Long articleId, ArticleCreateRequest request) {
        Article article = new Article();
        article.articleId = articleId;
        article.title = request.getTitle();
        article.content = request.getContent();
        article.boardId = request.getBoardId();
        article.writerId = request.getWriterId();
        article.createdAt = LocalDateTime.now();
        article.modifiedAt = article.createdAt;
        return article;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.modifiedAt = LocalDateTime.now();
    }
}
