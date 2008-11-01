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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This class is a general adapter to map a call to an Java interface 
 * to a given delegate.
 * <p>
 * @author Ben Yu
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 */
public abstract class ConversionHandler implements InvocationHandler, Serializable {
    private Object delegate;
    
    /**
     * Creates a ConversionHandler with an deleagte.
     * @param delegate the delegate
     * @throws IllegalArgumentException if the given delegate is null
     */
    public ConversionHandler(Object delegate) {
        if (delegate==null) throw new IllegalArgumentException("delegate must not be null");
        this.delegate = delegate;
    }
    
    /**
     * gets the delegate.
     * @return the delegate
     */
    public Object getDelegate(){
        return delegate;
    }
    
    /**
     * This method is a default implementation for the invoke method
     * given in Invocationhandler. Any call to an method with an
     * declaring class that is not Object is redirected to invokeCustom. 
     * Methods like tostring, equals and hashcode are called on the class
     * itself instead of the delegate. It is better to overwrite the 
     * invokeCustom method where the Object related methods are filtered out.
     * 
     * @see #invokeCustom(Object, Method, Object[])
     * @see InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     * 
     * @param proxy the proxy
     * @param method the method
     * @param args the arguments
     * @return the result of the invocation by method or delegate
     * @throws Throwable any exception caused by the delegate or the method
     */
    public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable {
        if(!isObjectMethod(method)){
            return invokeCustom(proxy,method,args);
        }
        try {
            return method.invoke(this, args);
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }  
    }
    
    /**
     * This method is called for all Methods not defined on Object. 
     * The delegate should be called here.
     * 
     * @param proxy the proxy
     * @param method the method
     * @param args the arguments
     * @return the result of the invocation of the delegate
     * @throws Throwable any exception causes by the delegate
     * @see #invoke(Object, Method, Object[])
     * @see InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     * 
     */
    public abstract Object invokeCustom(Object proxy, Method method, Object[] args) throws Throwable;
    
    /**
     * Indicates whether some other object is "equal to" this one.
     * The delegate is used if the class of the parameter and the
     * current class are equal. In other cases the method will return 
     * false. The exact class is here used, if inheritance is needed,
     * this method must be overwritten. 
     *        
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof Proxy){
            obj = Proxy.getInvocationHandler(obj);
        }
        
        if (obj instanceof ConversionHandler){
            return (((ConversionHandler)obj).getDelegate()).equals(delegate);
        } else {
            return false;
        }
    }

    /**
     * Returns a hash code value for the delegate. 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegate.hashCode();
    }
    
    /**
     * Returns a String version of the delegate.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegate.toString();
    }
    
    private static boolean isObjectMethod(Method mtd){
        return mtd.getDeclaringClass().equals(Object.class);
    }
}
