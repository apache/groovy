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
     * @param closures the map of closres
     */
    protected ConvertedMap(Map closures) {
        super(closures);
    }
    
    public Object invokeCustom(Object proxy, Method method, Object[] args)
    throws Throwable {
        Map m = (Map) getDelegate();
        Closure cl = (Closure) m.get(method.getName());
        if(cl == null) {
            throw new UnsupportedOperationException();
        }
        return cl.call(args);
    }
    
    public String toString() {
        return DefaultGroovyMethods.toString((Map) getDelegate());
    }
}

