package org.codehaus.groovy.runtime;

import org.codehaus.groovy.lang.Closure;


/**
 * Represents a new method on an object using a closure.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class MethodClosure implements Closure {

    private Object owner;
    
    public MethodClosure(Object owner) {
        this.owner = owner;
    }
    
    public Object getOwner() {
        return owner;
    }
}
