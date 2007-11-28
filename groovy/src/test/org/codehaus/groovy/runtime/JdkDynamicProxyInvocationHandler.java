package org.codehaus.groovy.runtime;

import java.lang.reflect.*;

public class JdkDynamicProxyInvocationHandler implements InvocationHandler {

	/* InvocationHandler implementation. */
	Object proxiedObject;

    private JdkDynamicProxyInvocationHandler (Object obj) {
        this.proxiedObject = obj;
    }

    public static Object getProxiedObject (Object obj) {
        Class cl = obj.getClass();
        return Proxy.newProxyInstance(
            cl.getClassLoader(),
            cl.getInterfaces(),
            new JdkDynamicProxyInvocationHandler (obj)
        );
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws IllegalAccessException, InvocationTargetException {
        return m.invoke (proxiedObject, args);
    }
}
