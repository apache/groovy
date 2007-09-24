package org.codehaus.groovy.reflection;

import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CachedField {
    public final Field field;

    CachedClass cachedClass;

    public CachedField (CachedClass clazz, Field field) {
      this.field = field;
      cachedClass = clazz;
    }

    public String getName() {
        return field.getName();
    }

    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public Class getType() {
        return field.getType();
    }

    public int getModifiers() {
        return field.getModifiers();
    }
}
