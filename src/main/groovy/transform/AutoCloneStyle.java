package groovy.transform;

/**
 * Intended style to use for cloning when using the {@code @}AutoClone annotation.
 *
 * @author Paul King
 * @since 1.8.0
 */
public enum AutoCloneStyle {
    /**
     * Uses only cloning.
     */
    CLONE,

    /**
     * Uses the copy constructor pattern.
     */
    COPY_CONSTRUCTOR,

    /**
     * Uses serialization to clone.
     */
    SERIALIZATION
}
