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
import groovy.lang.GroovyObjectSupport;

import java.util.Collections;
import java.util.Iterator;

public class NullObject extends GroovyObjectSupport {
    private static final NullObject INSTANCE = new NullObject();

    /**
     * private constructor
     */
    private NullObject() {
    }

    /**
     * get the NullObject reference
     *
     * @return the null object
     */
    public static NullObject getNullObject() {
        return INSTANCE;
    }

    /**
     * Since this is implemented as a singleton, we should avoid the
     * use of the clone method
     */
    @Override
    public Object clone() {
        throw new NullPointerException("Cannot invoke method clone() on null object");
    }

    /**
     * Tries to get a property on null, which will always fail
     *
     * @param property - the property to get
     * @return a NPE
     */
    @Override
    public Object getProperty(String property) {
        throw new NullPointerException("Cannot get property '" + property + "' on null object");
    }

    /**
     * Allows the closure to be called for NullObject
     *
     * @param closure the closure to call on the object
     * @return result of calling the closure
     */
    public <T> T with( Closure<T> closure ) {
        return DefaultGroovyMethods.with( null, closure ) ;
    }

    /**
     * Tries to set a property on null, which will always fail
     *
     * @param property - the proprty to set
     * @param newValue - the new value of the property
     */
    @Override
    public void setProperty(String property, Object newValue) {
        throw new NullPointerException("Cannot set property '" + property + "' on null object");
    }

    /**
     * Tries to invoke a method on null, which will always fail
     *
     * @param name the name of the method to invoke
     * @param args - arguments to the method
     * @return a NPE
     */
    @Override
    public Object invokeMethod(String name, Object args) {
        throw new NullPointerException("Cannot invoke method " + name + "() on null object");
    }

    /**
     * null is only equal to null
     *
     * @param to - the reference object with which to compare
     * @return - true if this object is the same as the to argument
     */
    @Override
    public boolean equals(Object to) {
        return to == null;
    }

    /**
     * iterator() method to be able to iterate on null.
     * Note: this part is from Invoker
     *
     * @return an iterator for an empty list
     */
    public Iterator iterator() {
        return Collections.EMPTY_LIST.iterator();
    }

    /**
     * Allows to add a String to null.
     * The result is concatenated String of the result of calling
     * toString() on this object and the String in the parameter.
     *
     * @param s - the String to concatenate
     * @return the concatenated string
     */
    public Object plus(String s) {
        return getMetaClass().invokeMethod(this, "toString", new Object[]{}) + s;
    }

    /**
     * Fallback for null+null.
     * The result is always a NPE. The plus(String) version will catch 
     * the case of adding a non null String to null.
     *
     * @param o - the Object
     * @return nothing
     */
    public Object plus(Object o) {
        throw new NullPointerException("Cannot execute null+" + o);
    }

    /**
     * The method "is" is used to test for equal references.
     * This method will return true only if the given parameter
     * is null
     *
     * @param other - the object to test
     * @return true if other is null
     */
    public boolean is(Object other) {
        return other == null;
    }

    /**
     * Type conversion method for null.
     *
     * @param c - the class to convert to
     * @return always null
     */
    public Object asType(Class c) {
        return null;
    }

    /**
     * A null object always coerces to false.
     * 
     * @return false
     */
    public boolean asBoolean() {
        return false;
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public int hashCode() {
        throw new NullPointerException("Cannot invoke method hashCode() on null object");
    }
}
