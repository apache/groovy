/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * This class is a general adapter to adapt a map of closures to
 * any Java interface.
 */
public class ConvertedMap extends ConversionHandler {

    private static final long serialVersionUID = 8535543126684786030L;

    /**
     * to create a ConvertedMap object.
     *
     * @param closures the map of closures
     */
    protected ConvertedMap(Map closures) {
        super(closures);
    }

    @Override
    public Object invokeCustom(Object proxy, Method method, Object[] args)
            throws Throwable {
        Map m = (Map) getDelegate();
        Closure cl = (Closure) m.get(method.getName());
        if(cl == null && "toString".equals(method.getName())) {
            return m.toString();
        }
        if (cl == null) {
            throw new UnsupportedOperationException();
        }
        return cl.call(args);
    }

    @Override
    public String toString() {
        return DefaultGroovyMethods.toString(getDelegate());
    }

    @Override
    protected boolean checkMethod(Method method) {
        return isCoreObjectMethod(method);
    }

    /**
     * Checks whether a method is a core method from java.lang.Object.
     * Such methods often receive special treatment because they are
     * deemed fundamental enough to not be tampered with.
     * call toString() is an exception to allow overriding toString() by a closure specified in the map
     *
     * @param method the method to check
     * @return true if the method is deemed to be a core method
     */
    public static boolean isCoreObjectMethod(Method method) {
        return ConversionHandler.isCoreObjectMethod(method) && !"toString".equals(method.getName());
    }
}

