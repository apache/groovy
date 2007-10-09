package org.codehaus.groovy.runtime.metaclass;

import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.SingleKeyHashMap;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class StdMetaMethod extends ReflectionMetaMethod {

    protected StdMetaMethod(CachedMethod method) {
        super(method);
    }

    public Object invoke(Object object, Object[] arguments) {
        try {
          return method.invoke(object, arguments);
        } catch (IllegalAccessError e) {
            throw new InvokerInvocationException(e);
        } catch (IllegalArgumentException e) {
            throw new InvokerInvocationException(e);
        } catch (IllegalAccessException e) {
            throw new InvokerInvocationException(e);
        } catch (InvocationTargetException e) {
            throw new InvokerInvocationException(e);
        }
    }

    private static SingleKeyHashMap cache = new SingleKeyHashMap();
    public synchronized static StdMetaMethod createStdMetaMethod(CachedMethod element) {
        SingleKeyHashMap.Entry method = cache.getOrPut(element);
        if (method.value == null) {
            method.value = new StdMetaMethod(element);
        }
        return (StdMetaMethod) method.value;
    }
}
