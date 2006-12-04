/*
 * ConvertedClosure.java created on 12.10.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.codehaus.groovy.runtime;

import java.lang.reflect.Method;
import java.util.Map;

import groovy.lang.Closure;

/**
 * This class is a general adapter to adapt a map of closures to
 * any Java interface.
 * <p>
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 */
public class ConvertedMap extends ConversionHandler {
        
    /**
     * to create a ConvertedMap object.
     * @param map the map of closres
     */
    protected ConvertedMap(Map closures) {
        super(closures);
    }
    
    public Object invokeCustom(Object proxy, Method method, Object[] args)
    throws Throwable {
        Map m = (Map) getDelegate();
        Closure cl = (Closure) m.get(method.getName());
        return cl.call(args);
    }
    
    public String toString() {
        return DefaultGroovyMethods.toString((Map) getDelegate());
    }
}

