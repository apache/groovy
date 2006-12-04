package org.codehaus.groovy.runtime;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import groovy.lang.Closure;


/**
 * Represents a method on an object using a closure which can be invoked
 * at any time
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MethodClosure extends Closure {

    private String method;
    
    public MethodClosure(Object owner, String method) {
        super(owner);
        this.method = method;

        final Class clazz = owner.getClass();
        maximumNumberOfParameters = 0;

        Method[] methods = (Method[]) AccessController.doPrivileged(new  PrivilegedAction() {
            public Object run() {
                return clazz.getMethods();
            }
        });
        for (int j = 0; j < methods.length; j++) {
            if (method.equals(methods[j].getName()) && methods[j].getParameterTypes().length > maximumNumberOfParameters) {
                maximumNumberOfParameters = methods[j].getParameterTypes().length;
            }
        }        
        methods = (Method[]) AccessController.doPrivileged(new  PrivilegedAction() {
            public Object run() {
                return clazz.getDeclaredMethods();
            }
        });
        for (int j = 0; j < methods.length; j++) {
            if (method.equals(methods[j].getName()) && methods[j].getParameterTypes().length > maximumNumberOfParameters) {
                maximumNumberOfParameters = methods[j].getParameterTypes().length;
            }
        }

    }
    
    public String getMethod() {
        return method;
    }

    protected Object doCall(Object arguments) {
        return InvokerHelper.invokeMethod(getDelegate(), method, arguments);
    }
    
    public Object getProperty(String property) {
        if ("method".equals(property)) {
            return getMethod();
        } else  return super.getProperty(property);        
    }
}
