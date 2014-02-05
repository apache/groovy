package groovy.json.internal;

/**
 * Created by Richard on 2/5/14.
 */
public class ArrayUtils {

    public static char[] copyRange( char[] source, int startIndex, int endIndex ) {
        int len = endIndex - startIndex;
        char[] copy = new char[len];
        System.arraycopy(source, startIndex, copy, 0, Math.min(source.length - startIndex, len));
        return copy;
    }
}
