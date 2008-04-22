/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.runtime;


import groovy.lang.Closure;
 
/**
 * Represents wrapper around a Closure to support currying
 * 
 * @author Jochen Theodorou
 */
public final class CurriedClosure extends Closure {

    private Object[] curriedParams;
    
    public CurriedClosure(Closure uncurriedClosure, Object[] arguments) {
        super(uncurriedClosure);
        curriedParams = arguments;
        maximumNumberOfParameters = uncurriedClosure.getMaximumNumberOfParameters()-arguments.length;
    }
    
    public CurriedClosure(Closure uncurriedClosure, int i) {
        this(uncurriedClosure, new Object[]{Integer.valueOf(i)});
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
