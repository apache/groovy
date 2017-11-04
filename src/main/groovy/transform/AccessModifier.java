package groovy.transform;

import java.lang.reflect.Modifier;

/**
 * This enumeration can be used in {@link Immutable} annotation for setting access modifier for constructors
 *
 * @author Dominik Przybysz
 */
public enum AccessModifier {
    PUBLIC(Modifier.PUBLIC),
    PROTECTED(Modifier.PROTECTED),
    PACKAGE(0),
    PRIVATE(Modifier.PRIVATE);

    private final int modifier;

    AccessModifier(int modifier) {
        this.modifier = modifier;
    }

    public int getModifier() {
        return modifier;
    }
}
