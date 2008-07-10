package groovy.lang;

import java.lang.reflect.Modifier;

/**
 * MetaMethod which is able to decide if it responds for particular call or not
 */
public abstract class DynamicMetaMethod extends MetaMethod {
    public DynamicMetaMethod(Class [] pt) {
        super (pt);
    }

    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    public String getName() {
        return null;
    }

    public Class getReturnType() {
        return Object.class;
    }

    public abstract boolean respondsTo (String name, Object [] args);

    public abstract Object invoke (String name, Object object, Object [] args);

    public final Object invoke(Object object, Object[] arguments) {
        throw new UnsupportedOperationException();
    }
}
