package board.article.fixtures;

import board.article.service.request.ArticleCreateRequest;

public class ArticleFixture {
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final Long BOARD_ID = 1L; // shard key
    public static final Long WRITER_ID = 1L;
    public static final ArticleCreateRequest ARTICLE_CREATE_REQUEST_FIXTURE =
            new ArticleCreateRequest(TITLE, CONTENT, BOARD_ID, WRITER_ID);
    public static final String UPDATED_TITLE = "updated title";
    public static final String UPDATED_CONTENT = "updated content";


}
