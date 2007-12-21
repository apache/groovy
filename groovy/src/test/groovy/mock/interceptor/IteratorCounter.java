package groovy.mock.interceptor;

public class IteratorCounter {
    public int count(java.util.Iterator it) {
        int count = 0;
        while (it.hasNext()) count++;
        return count;
    }
}