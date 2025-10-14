package groovyx.gpars.activeobject;

/**
 * @author Kirill Vergun (code@o-nix.me)
 * @since 1.3
 */
public interface ActorWithExceptionHandler {
    Object recoverFromException(String methodName, Exception e);
}