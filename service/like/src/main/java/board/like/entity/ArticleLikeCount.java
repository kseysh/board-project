package board.like.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "article_like_count")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleLikeCount {
    @Id
    private Long articleId; // shard key
    private Long likeCount;
    @Version
    private Long version;

    public static ArticleLikeCount init(Long articleId, Long likeCount) {
        ArticleLikeCount articleLikeCount = new ArticleLikeCount();
        articleLikeCount.articleId = articleId;
        articleLikeCount.likeCount = likeCount;
        return articleLikeCount;
    }

    public void increase(){
        this.likeCount++;
    }

    public void decrease(){
        this.likeCount--;
    }

}
