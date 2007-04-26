
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
        ((Closure)getOwner()).setDelegate(delegate);
    }
    
    public Object clone() {
        Closure uncurriedClosure = (Closure) ((Closure) getOwner()).clone();
        return new CurriedClosure(uncurriedClosure,curriedParams);
    }
    
    public Class[] getParameterTypes() {
        Class[] oldParams = ((Closure)getOwner()).getParameterTypes();
        Class[] newParams = new Class[oldParams.length-curriedParams.length];
        System.arraycopy(oldParams, curriedParams.length, newParams, 0, newParams.length);
        return newParams;  
    }
}
