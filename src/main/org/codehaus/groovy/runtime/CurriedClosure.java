
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
 
/**
 * Represents wrapper around a Closure to support currying
 * 
 * @author Jochen Theodorou
 */
public class CurriedClosure extends Closure {

    private Object[] curriedParams;
    
    public CurriedClosure(Closure uncurriedClosure, Object[] arguments) {
        super(uncurriedClosure);
        curriedParams = arguments;
        maximumNumberOfParameters = uncurriedClosure.getMaximumNumberOfParameters()-arguments.length;
    }

    public Object[] getUncurriedArguments(Object[] arguments) {
        final Object newCurriedParams[] = new Object[curriedParams.length + arguments.length];
        System.arraycopy(curriedParams, 0, newCurriedParams, 0, curriedParams.length);
        System.arraycopy(arguments, 0, newCurriedParams, curriedParams.length, arguments.length);
        return newCurriedParams;        
    }

    public void setDelegate(Object delegate) {
        throw new UnsupportedOperationException("setting delegates for curried closures is unsupported");
    }

}
