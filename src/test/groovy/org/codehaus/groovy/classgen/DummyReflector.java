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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.runtime.Reflector;
import org.codehaus.groovy.reflection.CachedMethod;

/**
 * This is a scratch class used to experiment with ASM to see what kind of
 * stuff is output for normal Java code
 */
public class DummyReflector extends Reflector {

    public DummyReflector() {
    }

    /*
    public Object invoke(MetaMethod method, Object object, Object[] arguments) {
        switch (method.getMethodIndex()) {
            case 1 :
                return InvokerHelper.toObject(object.hashCode());
            case 2 :
                return object.toString();
            case 3 :
                return InvokerHelper.toObject(object.equals(arguments[0]));
            case 4 :
                return new ObjectRange((Comparable) arguments[0], (Comparable) arguments[1]);
            case 5 :
                return ((String) object).toCharArray();
            case 7 :
                return new Character("hello".charAt(2));
            case 8 :
                return null;
            default :
                return noSuchMethod(method, object, arguments);
        }
    }
    */

    public Object invoke(CachedMethod method, Object object, Object[] arguments) {
/*
        switch (method.getMethodIndex()) {
            case 1:
                return ((String) object).toCharArray();
            case 2:
                return new Boolean(((List) object).contains(arguments[0]));
            default:
                return noSuchMethod(method, object, arguments);
        }
*/
        return null;
    }

    public Object invokeConstructorAt(Object at, Object constructor, Object[] arguments) {
        return null; // noSuchMethod(method, object, arguments);
    }

    public Object invokeConstructorOf(Object constructor, Object[] arguments) {
        return null; // noSuchMethod(method, object, arguments);
    }

    char[] blah() {
        return "foo".toCharArray();
    }

}
