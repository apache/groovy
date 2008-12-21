package groovy.jmx.builder;

public class JmxBuilderException extends RuntimeException {
    public JmxBuilderException() {
        super();
    }

    public JmxBuilderException(String ex) {
        super(ex);
    }

    public JmxBuilderException(Throwable ex) {
        super(ex);
    }

    public JmxBuilderException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
