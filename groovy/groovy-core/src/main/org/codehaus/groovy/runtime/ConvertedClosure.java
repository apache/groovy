package org.codehaus.groovy.runtime;

import java.lang.reflect.Method;
import groovy.lang.Closure;

/**
 * This class is a general adapter to adapt a closure to any Java interface.
 * <p>
 * @author Ben Yu
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * Jul 27, 2006 3:50:51 PM
 */
public class ConvertedClosure extends ConversionHandler {
    private String methodName;
    
    /**
     * to create a ConvertedClosure object.
     * @param closure the closure object.
     */
    public ConvertedClosure(Closure closure, String method) {
        super(closure);
        this.methodName = method;
    }
    
    public ConvertedClosure(Closure closure) {
        this(closure,null);
    }
    
    public Object invokeCustom(Object proxy, Method method, Object[] args)
    throws Throwable {
        if (methodName!=null && !methodName.equals(method.getName())) return null;
        return ((Closure) getDelegate()).call(args);
    }
}

