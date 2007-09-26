package org.codehaus.groovy.runtime.metaclass;

import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.Reflector;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class StdMetaMethod extends ReflectionMetaMethod {

    private Class callClass;
    private Class interfaceClass;
    private Reflector reflector;
    private int methodIndex;

    protected StdMetaMethod(CachedMethod method) {
        super(method);
    }

    public Object invoke(Object object, Object[] arguments) {
        try {
            if (reflector != null) {
                return reflector.invoke(this, object, arguments);
            } else {
                return method.invokeByReflection(object, arguments);
            }
        } catch (InvocationTargetException ite) {
            throw new InvokerInvocationException(ite.getCause());
        } catch (Exception e) {
            throw new InvokerInvocationException(e);
        }
    }

    public Class getCallClass() {
        return callClass;
    }

    public void setCallClass(Class c) {
        callClass=c;
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public void setMethodIndex(int methodIndex) {
        this.methodIndex = methodIndex;
    }

    public Reflector getReflector() {
        return reflector;
    }

    public void setReflector(Reflector reflector) {
        this.reflector = reflector;
    }

    public Class getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    private static HashMap cache = new HashMap();
    public synchronized static StdMetaMethod createStdMetaMethod(CachedMethod element) {
        StdMetaMethod method = (StdMetaMethod) cache.get(element);
        if (method == null) {
            method = new StdMetaMethod(element);
            cache.put(element, method);
        }
        return method;
    }
}
