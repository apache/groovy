package groovy.lang;

public class MissingPropertyExceptionNoStack extends MissingPropertyException {

    public MissingPropertyExceptionNoStack(String propertyName, Class theClass) {
        super(propertyName, theClass);
    }

    public Throwable fillInStackTrace() {
        return this;
    }
}
