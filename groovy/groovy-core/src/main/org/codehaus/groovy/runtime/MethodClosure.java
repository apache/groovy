package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.MetaClass;


/**
 * Represents a method on an object using a closure which can be invoked
 * at any time
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MethodClosure extends Closure {

    private String method;
    MetaClass metaClass = InvokerHelper.getMetaClass(this);
    
    public MethodClosure(Object delegate) {
        super(delegate);
    }
    
    public MethodClosure(Object owner, String method) {
        super(owner);
        this.method = method;
    }
    
    public String getMethod() {
        return method;
    }

    public Object call(Object arguments) {
        return InvokerHelper.invokeMethod(getDelegate(), method, arguments);
    }
    
    public MetaClass getMetaClass() {
        return metaClass;
    }
}
