package org.codehaus.groovy.runtime;

import groovy.lang.Closure;


/**
 * Represents a method on an object using a closure which can be invoked
 * at any time
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MethodClosure extends Closure {

    private Object owner;
    private String method;
    
    public MethodClosure(Object owner, String method) {
        this.owner = owner;
        this.method = method;
    }
    
    public String getMethod() {
        return method;
    }

    public Object getOwner() {
        return owner;
    }

    public Object call(Object arguments) {
        return InvokerHelper.invokeMethod(owner, method, arguments);
    }
}
