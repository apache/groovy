package groovy.bugs.groovy9890;

public interface Face {
    default Object foo(long n) {
        return n;
    }
    Object foo(String s);
}
