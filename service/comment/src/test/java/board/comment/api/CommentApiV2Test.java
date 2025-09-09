package board.comment.api;

import static board.comment.fixtures.CommentFixture.COMMENT_CREATE_REQUEST_V2_1;
import static board.comment.fixtures.CommentFixture.COMMENT_CREATE_REQUEST_V2_2;
import static board.comment.fixtures.CommentFixture.COMMENT_CREATE_REQUEST_V2_3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import board.comment.fixtures.CommentFixture.CommentCreateRequestV2;
import board.comment.service.response.CommentPageResponse;
import board.comment.service.response.CommentResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClient;

@Slf4j
class CommentApiV2Test {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void createTest(){
        CommentResponse response1 = createComment(COMMENT_CREATE_REQUEST_V2_1);
        CommentCreateRequestV2 request2 = COMMENT_CREATE_REQUEST_V2_2;
        request2.setParentPath(response1.getPath());
        CommentResponse response2 = createComment(request2);
        CommentCreateRequestV2 request3 = COMMENT_CREATE_REQUEST_V2_3;
        request3.setParentPath(response2.getPath());
        CommentResponse response3 = createComment(request3);

        log.info("commentId=%s".formatted(response1.getCommentId()) + " path=%s".formatted(response1.getPath()));
        log.info("\tcommentId=%s".formatted(response2.getCommentId()) + " path=%s".formatted(response2.getPath()));
        log.info("\tcommentId=%s".formatted(response3.getCommentId()) + " path=%s".formatted(response3.getPath()));
    }

    CommentResponse createComment(CommentCreateRequestV2 request){
        return restClient.post()
                .uri("/v2/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void readTest(){
        CommentResponse commentResponse = createComment(COMMENT_CREATE_REQUEST_V2_1);
        CommentResponse response = read(commentResponse.getCommentId());
        assertEquals(commentResponse.getCommentId(), response.getCommentId());
    }

    CommentResponse read(Long commentId){
        return restClient.get()
                .uri("/v2/comments/{commentId}", commentId)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void delete(){
        // given
        CommentResponse parentComment = createComment(COMMENT_CREATE_REQUEST_V2_1);

        // when
        delete(parentComment.getCommentId());

        // then
        assertThrows(InternalServerError.class, () ->  read(parentComment.getCommentId()));
    }

    void delete(Long commentId){
        restClient.delete()
                .uri("/v2/comments/{commentId}", commentId)
                .retrieve()
                .toBodilessEntity();
    }

    @Test
    void readAllTest(){
        Long pageSize = 10L;
        CommentPageResponse response = restClient.get()
                .uri("v2/comments?articleId=1&page=1&pageSize=%s".formatted(pageSize.toString()))
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
                .uri("v2/comments/infinite-scroll?articleId=1&pageSize=%s".formatted(pageSize.toString()))
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

        String lastPath = response1.get(response1.size() - 1).getPath();
        log.info("lastPath=%s".formatted(lastPath));
        List<CommentResponse> response2 = restClient.get()
                .uri("v2/comments/infinite-scroll?articleId=1&pageSize=%s&lastPath=%s"
                        .formatted(pageSize.toString(), lastPath))
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
}
