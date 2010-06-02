package groovy.transform;

/**
 * Intended target when {@code @}PackageScope is placed at the class level.
 *
 * @author Paul King
 * @since 1.8.0
 */
public enum PackageScopeTarget {
    /**
     * Make the Class have package protected visibility.
     */
    CLASS,

    /**
     * Make the Class methods have package protected visibility.
     */
    METHODS,

    /**
     * Make the Class fields have package protected visibility.
     */
    FIELDS
}
