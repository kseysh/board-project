package board.comment.service;

import static java.util.function.Predicate.not;

import board.comment.entity.Comment;
import board.comment.repository.CommentRepository;
import board.comment.service.request.CommentCreateRequest;
import board.comment.service.response.CommentResponse;
import board.common.snowflake.Snowflake;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final Snowflake snowflake = Snowflake.getInstance();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(CommentCreateRequest request){
        Optional<Comment> parent = findParent(request);
        long commentId = snowflake.nextId();
        Comment newComment = commentRepository.save(
                Comment.create(
                        commentId,
                        request.getContent(),
                        parent.map(Comment::getCommentId).orElse(commentId),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(newComment);
    }

    private Optional<Comment> findParent(CommentCreateRequest request){
        Long parentCommentId = request.getParentCommentId();
        if(parentCommentId == null){
            return Optional.empty();
        }
        return commentRepository.findById(parentCommentId)
                .filter(not(Comment::getDeleted))
                .filter(Comment::isRoot);
    }

    public CommentResponse read(Long commentId){
        return CommentResponse.from(commentRepository.findById(commentId).orElseThrow());
    }

    @Transactional
    public void delete(Long commentId){
        commentRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if(hasChildren(comment)){
                        comment.delete();
                    }else{
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(Comment comment){
        // 자기 자신과 자식은 parent_comment_id를 parent의 comment_id로 가지고 있다.
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2L;
    }

    private void delete(Comment comment){
        commentRepository.delete(comment);
        if (!comment.isRoot()){
            commentRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
        }
    }

}
