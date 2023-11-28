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
     * Returns the NullObject reference.
     *
     * @return the null object
     */
    public static NullObject getNullObject() {
        return INSTANCE;
    }

    private NullObject() {
        if (INSTANCE != null) {
            throw new RuntimeException("Can't instantiate NullObject. Use NullObject.getNullObject()");
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Since this is implemented as a singleton, avoid the use of the clone method.
     *
     * @return never
     * @throws NullPointerException
     */
    @Override
    public Object clone() {
        throw new NullPointerException("Cannot invoke method clone() on null object");
    }

    /**
     * null is only equal to null.
     *
     * @param o the reference object with which to compare
     * @return true if this object is the same as the to argument
     */
    @Override
    public boolean equals(final Object o) {
        return o == null || o == INSTANCE;
    }

    /**
     * @return never
     * @throws NullPointerException
     */
    @Override
    public int hashCode() {
        throw new NullPointerException("Cannot invoke method hashCode() on null object");
    }

    @Override
    public String toString() {
        return "null";
    }

    /**
     * Tries to get a property on null, which fails except for "class" and "metaClass".
     *
     * @return never
     * @throws NullPointerException
     */
    @Override
    public Object getProperty(final String name) {
        if ("class".equals(name)) return getClass(); // GROOVY-4487
        if ("metaClass".equals(name)) return DefaultGroovyMethods.getMetaClass(this);
        throw new NullPointerException("Cannot get property '" + name + "' on null object");
    }

    /**
     * Tries to set a property on null, which fails.
     *
     * @throws NullPointerException
     */
    @Override
    public  void  setProperty(final String name, final Object value) {
        throw new NullPointerException("Cannot set property '" + name + "' on null object");
    }

    /**
     * Tries to invoke a method on null, which fails.
     *
     * @return never
     * @throws NullPointerException
     */
    @Override
    public Object invokeMethod(final String name, final Object arguments) {
        throw new NullPointerException("Cannot invoke method " + name + "() on null object");
    }

    //--------------------------------------------------------------------------

    /**
     * A null object coerces to false.
     *
     * @return false
     */
    public boolean asBoolean() {
        return false;
    }

    /**
     * Type conversion method for null.
     *
     * @return null
     */
    public Object asType(final Class c) {
        if (c.isPrimitive()) throw new IllegalArgumentException("null to " + c);
        return null;
    }

    /**
     * Tests for equal references.
     *
     * @return true if object is null
     */
    public boolean is(final Object o) {
        return equals(o);
    }

    /**
     * iterator() method to be able to iterate on null.
     * Note: this part is from Invoker
     *
     * @return an empty iterator
     */
    public Iterator iterator() {
        return Collections.emptyIterator();
    }

    /**
     * Fallback for {@code null+null}. The {@link plus(String)} variant catches
     * the case of adding a non-null String to null.
     *
     * @return never
     * @throws NullPointerException
     */
    public Object plus(final Object o) {
        throw new NullPointerException("Cannot execute null+" + o);
    }

    /**
     * Allows to add a String to null.
     * The result is concatenated String of the result of calling
     * toString() on this object and the String in the parameter.
     *
     * @return the concatenated string
     */
    public Object plus(final String s) {
        return getMetaClass().invokeMethod(this, "toString", new Object[0]) + s;
    }

    /**
     * Allows the closure to be called for NullObject.
     *
     * @param closure the closure to call on the object
     * @return result of calling the closure
     */
    @Deprecated(since = "5.0.0") // GROOVY-4526
    public <T> T with(final Closure<T> closure) {
        return DefaultGroovyMethods.with(null, closure);
    }
}
