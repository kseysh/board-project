package board.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {
    private String path;

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            
    private static final int DEPTH_CHUNK_SIZE = 5;
    private static final int MAX_DEPTH = 5;

    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length()-1)).repeat(DEPTH_CHUNK_SIZE);

    public static CommentPath create(String path){
        if(isDepthOverflowed(path)){
            throw new IllegalStateException("depth overflow");
        }
        CommentPath commentPath = new CommentPath();
        commentPath.path = path;
        return commentPath;
    }

    private static boolean isDepthOverflowed(String path){
        return calDepth(path) > MAX_DEPTH;
    }

    private static int calDepth(String path){
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    public int getDepth(){
        return calDepth(path);
    }

    public boolean isRoot(){
        return calDepth(path) == 1;
    }

    public String getParentPath(){
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
    }

    // 자신의 path를 가지는 댓글을 limit 1로 찾기 위해서는 child가 아닌, descendant가 찾아질 수 있음
    // 이 descendant를 이용하여 chlid의 comment path를 찾는 함수
    public CommentPath createChildCommentPath(String descendantsTopPath){
        if(descendantsTopPath == null){
            return CommentPath.create(path + MIN_CHUNK);
        }
        String childrenTopPath = findChildrenTopPath(descendantsTopPath);
        return CommentPath.create(increase(childrenTopPath));
    }

    private String findChildrenTopPath(String descendantsTopPath) {
        return descendantsTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    private String increase(String path){
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);
        if (isChunkOverflowed(lastChunk)){
            throw new IllegalStateException("chunk overflowed");
        }

        int value = convertCharSetToInt(lastChunk) + 1;
        String result = convertIntToCharSet(value);

        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    // charset을 Int 값으로 치환한다.
    int convertCharSetToInt(String chunk){
        int value = 0;
        int charsetLength = CHARSET.length();
        for(char ch : chunk.toCharArray()){
            value = value * charsetLength + CHARSET.indexOf(ch);
        }

        return value;
    }

    private String convertIntToCharSet(int value) {
        int charsetLength = CHARSET.length();
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < DEPTH_CHUNK_SIZE; i++){
            result.insert(0, CHARSET.charAt(value % charsetLength));
            value /= charsetLength;
        }
        return result.toString();
    }

    private boolean isChunkOverflowed(String lastChunk) {
        return MAX_CHUNK.equals(lastChunk);
    }

}
