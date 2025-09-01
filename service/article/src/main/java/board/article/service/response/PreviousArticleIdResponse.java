package board.article.service.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PreviousArticleIdResponse {

    Long articleId;

    public static PreviousArticleIdResponse of(Long articleId) {
        return new PreviousArticleIdResponse(articleId);
    }
}
