package groovy.lang;

/**
 * Use this exception to mark a method implementation as being deprecated.
 * Use the message to indicate the recommended way of calling the desired functionality.
 * Make throwing this exception the only line in the method implementation, i.e. unlike the Java
 * @-deprecated feature there is no relay to the new implementation but an early and deliberate
 * halt of execution ("fail early").
 *
 * This exception is supposed to be used in the SNAPSHOT releases only. Before release, all
 * references to this exception should be resolved and the according methods removed.
 *
 * @author Dierk Koenig
 */
public class DeprecationException extends RuntimeException {

    public DeprecationException(String message) {
        super(message);
    }

    public DeprecationException(String message, Throwable cause) {
        super(message, cause);
    }
}
