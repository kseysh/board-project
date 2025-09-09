package board.comment.api;

import static board.comment.fixtures.CommentFixture.COMMENT_CREATE_REQUEST1;
import static board.comment.fixtures.CommentFixture.COMMENT_CREATE_REQUEST2;
import static board.comment.fixtures.CommentFixture.COMMENT_CREATE_REQUEST3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import board.comment.fixtures.CommentFixture.CommentCreateRequest;
import board.comment.service.response.CommentPageResponse;
import board.comment.service.response.CommentResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClient;

@Slf4j
class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
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
    void readAllTest(){
        Long pageSize = 10L;
        CommentPageResponse response = restClient.get()
                .uri("v1/comments?articleId=1&page=1&pageSize=%s".formatted(pageSize.toString()))
                .retrieve()
                .body(CommentPageResponse.class);

        assertEquals(pageSize, response.getComments().size());

        for (CommentResponse comment : response.getComments()) {
            if(comment.getCommentId().equals(comment.getParentCommentId())){
                log.info("commentId=%s".formatted(comment.getCommentId()));
            }else{
                log.info("\tcommentId=%s".formatted(comment.getCommentId()));
            }
        }
    }

    @Test
    void readAllWithInfiniteScrollTest(){
        Long pageSize = 5L;
        List<CommentResponse> response1 = restClient.get()
                .uri("v1/comments/infinite-scroll?articleId=1&pageSize=%s".formatted(pageSize.toString()))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assertEquals(pageSize, response1.size());
        log.info("first page");
        for (CommentResponse comment : response1) {
            if(comment.getCommentId().equals(comment.getParentCommentId())){
                log.info("commentId=%s".formatted(comment.getCommentId()));
            }else{
                log.info("\tcommentId=%s".formatted(comment.getCommentId()));
            }
        }

        Long lastParentCommentId = response1.get(response1.size() - 1).getParentCommentId();
        Long lastCommentId = response1.get(response1.size() - 1).getCommentId();
        List<CommentResponse> response2 = restClient.get()
                .uri("v1/comments/infinite-scroll?articleId=1&pageSize=%s&lastParentCommentId=%s&lastCommentId=%s"
                        .formatted(pageSize.toString(), lastParentCommentId.toString(), lastCommentId.toString()))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assertEquals(pageSize, response2.size());
        log.info("second page");
        for (CommentResponse comment : response2) {
            if(comment.getCommentId().equals(comment.getParentCommentId())){
                log.info("commentId=%s".formatted(comment.getCommentId()));
            }else{
                log.info("\tcommentId=%s".formatted(comment.getCommentId()));
            }
        }
    }

    @Test
    @DisplayName("root 댓글은 삭제시 하위 댓글이 있으면 삭제 표시만 된다.")
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
