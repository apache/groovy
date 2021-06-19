package org.codehaus.groovy.vmplugin.v16;

import org.codehaus.groovy.GroovyBugError;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class ProxyDefaultMethodHandle {
    private static final MethodHandle INVOKE_DEFAULT_METHOD_HANDLE;
    static {
        try {
            // `invokeDefault` is JDK 16+ API, but we still build Groovy with JDK11,
            // so use method handle instead of invoking the method directly
            INVOKE_DEFAULT_METHOD_HANDLE = MethodHandles.lookup().findStatic(
                                                InvocationHandler.class, "invokeDefault",
                                                MethodType.methodType(Object.class, Object.class, Method.class, Object[].class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new GroovyBugError(e);
        }
    }

    private final Proxy proxy;
    private final Method method;

    ProxyDefaultMethodHandle(Proxy proxy, Method method) {
        this.proxy = proxy;
        this.method = method;
    }

    Object invokeWithArguments(Object... arguments) throws Throwable {
        Object proxy = this.proxy;
        return INVOKE_DEFAULT_METHOD_HANDLE.invokeExact(proxy, method, arguments);
    }
}
