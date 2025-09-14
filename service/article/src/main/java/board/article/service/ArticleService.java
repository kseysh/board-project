package board.article.service;

import static board.article.entity.ArticleConstant.MOVABLE_PAGE_COUNT;

import board.article.entity.Article;
import board.article.entity.BoardArticleCount;
import board.article.repository.ArticleRepository;
import board.article.repository.BoardArticleCountRepository;
import board.article.service.request.ArticleCreateRequest;
import board.article.service.request.ArticleUpdateRequest;
import board.article.service.response.ArticlePageResponse;
import board.article.service.response.ArticleResponse;
import board.article.service.response.PreviousArticleIdResponse;
import board.common.page_calculator.PageLimitCalculator;
import board.common.snowflake.Snowflake;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final Snowflake snowflake = Snowflake.getInstance();
    private final ArticleRepository articleRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article article = articleRepository.save(Article.create(snowflake.nextId(), request));
        int result = boardArticleCountRepository.increase(request.getBoardId());
        if(result == 0){
            boardArticleCountRepository.save(
                    BoardArticleCount.init(request.getBoardId(), 1L)
            );
        }
        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());
        return ArticleResponse.from(article);
    }

    @Transactional(readOnly = true)
    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        articleRepository.deleteById(articleId);
        boardArticleCountRepository.decrease(article.getBoardId());
    }

    @Transactional(readOnly = true)
    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        Long offset = (page - 1) * pageSize;
        return ArticlePageResponse.of(
                articleRepository.findAll(boardId, offset, pageSize).stream()
                        .map(ArticleResponse::from)
                        .toList(),
                articleRepository.count(
                        boardId,
                        PageLimitCalculator.calculatePageLimit(page, pageSize, MOVABLE_PAGE_COUNT)
                )
        );
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> readAllWithInfiniteScroll(Long boardId, Long pageSize, Optional<Long> lastArticleId) {
        List<Article> articles = lastArticleId.isPresent() ?
                articleRepository.findAllWithInfiniteScroll(boardId, pageSize, lastArticleId.get()) :
                articleRepository.findAllWithInfiniteScroll(boardId, pageSize);
        return articles.stream().map(ArticleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PreviousArticleIdResponse findNextArticleIdAfter(Long boardId, Long articleId) {
        return PreviousArticleIdResponse.of(articleRepository.findNextIdAfter(boardId, articleId));
    }

    @Transactional(readOnly = true)
    public Long count(Long boardId) {
        return boardArticleCountRepository.findById(boardId)
                .map(BoardArticleCount::getArticleCount)
                .orElse(0L);
    }
}
