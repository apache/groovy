package org.codehaus.groovy.runtime.dgmimpl;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.callsite.CallSiteAwareMetaMethod;

import java.lang.reflect.Modifier;

public abstract class ArrayMetaMethod extends CallSiteAwareMetaMethod {
    protected static final CachedClass INTEGER_CLASS = ReflectionCache.getCachedClass(Integer.class);
    protected static final CachedClass [] INTEGER_CLASS_ARR = new CachedClass[] {INTEGER_CLASS};

    protected static int normaliseIndex(int i, int size) {
        int temp = i;
        if (i < 0) {
            i += size;
        }
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative array index [" + temp + "] too large for array size " + size);
        }
        return i;
    }

    public int getModifiers() {
        return Modifier.PUBLIC;
    }
}
