package board.comment.api;

import board.comment.service.CommentService;
import board.comment.service.request.CommentCreateRequest;
import board.comment.service.response.CommentPageResponse;
import board.comment.service.response.CommentResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/v1/comments/{commentId}")
    public CommentResponse read(
            @PathVariable Long commentId
    ){
        return commentService.read(commentId);
    }

    @PostMapping("/v1/comments")
    public CommentResponse create(@RequestBody CommentCreateRequest request){
        return commentService.create(request);
    }

    @DeleteMapping("/v1/comments/{commentId}")
    public void delete(@PathVariable Long commentId){
        commentService.delete(commentId);
    }

    @GetMapping("/v1/comments")
    public CommentPageResponse readAll(
            @RequestParam("articleId") Long articleId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ){
        return commentService.readAll(articleId, page, pageSize);
    }

    @GetMapping("/v1/comments/infinite-scroll")
    public List<CommentResponse> readAllWithInfiniteScroll(
            @RequestParam("articleId") Long articleId,
            @RequestParam(value = "lastParentCommentId", required = false) Long lastParentCommentId,
            @RequestParam(value = "lastCommentId", required = false) Long lastCommentId,
            @RequestParam("pageSize") Long pageSize
    ){
        return commentService.readAllWithInfiniteScroll(articleId, lastParentCommentId, lastCommentId, pageSize);
    }

}
