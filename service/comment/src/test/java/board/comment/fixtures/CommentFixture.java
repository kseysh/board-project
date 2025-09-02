package board.comment.fixtures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class CommentFixture {

    public static CommentCreateRequest COMMENT_CREATE_REQUEST1
            = new CommentCreateRequest(1L, "my comment1", null, 1L);
    public static CommentCreateRequest COMMENT_CREATE_REQUEST2
            = new CommentCreateRequest(1L, "my comment2", null, 1L);
    public static CommentCreateRequest COMMENT_CREATE_REQUEST3
            = new CommentCreateRequest(1L, "my comment3", null, 1L);

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequest{
        private Long articleId;
        private String content;
        @Setter
        private Long parentCommentId;
        private Long writerId;

    }
}
