package board.comment.api;

import static board.comment.fixtures.CommentFixture.COMMENT_CREATE_REQUEST1;
import static board.comment.fixtures.CommentFixture.COMMENT_CREATE_REQUEST2;
import static board.comment.fixtures.CommentFixture.COMMENT_CREATE_REQUEST3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import board.comment.fixtures.CommentFixture.CommentCreateRequest;
import board.comment.service.response.CommentResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClient;

@Slf4j
class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    @Transactional
    void createTest(){
        CommentResponse response1 = createComment(COMMENT_CREATE_REQUEST1);
        CommentResponse response2 = createComment(COMMENT_CREATE_REQUEST2);
        CommentResponse response3 = createComment(COMMENT_CREATE_REQUEST3);

        log.info("commentId=%s".formatted(response1.getCommentId()));
        log.info("\tcommentId=%s".formatted(response2.getCommentId()));
        log.info("\tcommentId=%s".formatted(response3.getCommentId()));
    }

    CommentResponse createComment(CommentCreateRequest request){
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    @Transactional
    void readTest(){
        CommentResponse commentResponse = createComment(COMMENT_CREATE_REQUEST1);
        CommentResponse response = read(commentResponse.getCommentId());
        assertEquals(commentResponse.getCommentId(), response.getCommentId());
    }

    CommentResponse read(Long commentId){
        return restClient.get()
                .uri("/v1/comments/{commentId}", commentId)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    @DisplayName("root 댓글은 삭제시 하위 댓글이 있으면 삭제 표시만 된다.")
    @Transactional
    void deleteRootCommentWithNotDeleteChildCommentTest(){
        // given
        CommentResponse parentComment = createComment(COMMENT_CREATE_REQUEST1);
        CommentCreateRequest childRequest = COMMENT_CREATE_REQUEST2;
        childRequest.setParentCommentId(parentComment.getCommentId());
        createComment(childRequest);

        // when
        delete(parentComment.getCommentId());

        // then
        assertTrue(read(parentComment.getCommentId()).getDeleted());
    }

    @Test
    @DisplayName("root 댓글은 하위 댓글이 모두 삭제되면 삭제된다.")
    @Transactional
    void deleteRootCommentWithDeletedChildCommentTest(){
        // given
        CommentResponse parentComment = createComment(COMMENT_CREATE_REQUEST1);
        CommentCreateRequest childRequest = COMMENT_CREATE_REQUEST2;
        childRequest.setParentCommentId(parentComment.getCommentId());
        CommentResponse childComment = createComment(childRequest);

        // when
        delete(parentComment.getCommentId());
        delete(childComment.getCommentId());

        // then
        assertThrows(InternalServerError.class, () ->  read(parentComment.getCommentId()));
        assertThrows(InternalServerError.class, () ->  read(childComment.getCommentId()));
    }

    void delete(Long commentId){
        restClient.delete()
                .uri("/v1/comments/{commentId}", commentId)
                .retrieve()
                .toBodilessEntity();
    }


}
