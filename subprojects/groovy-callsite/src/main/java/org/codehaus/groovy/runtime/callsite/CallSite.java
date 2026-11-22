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
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.GroovyObject;
import org.apache.groovy.lang.annotation.GroovyABI;

/**
 * Classic (non-{@code invokedynamic}) call site.
 * <p>
 * Prefer invokedynamic call sites for new code. Planned for
 * deprecation/removal in a future Groovy version.
 */
public interface CallSite {
    /**
     * Returns the array that owns this site.
     *
     * @return the owning call-site array
     */
    CallSiteArray getArray();

    /**
     * Returns this site's index in {@link #getArray()}.
     *
     * @return the index
     */
    int getIndex();

    /**
     * Returns the method or property name this site calls.
     *
     * @return the name
     */
    String getName();

    /**
     * Reads the named property of {@code receiver}.
     *
     * @param receiver the object whose property is read
     * @return the property value
     * @throws Throwable when the read fails
     */
    Object getProperty(Object receiver) throws Throwable;

    /**
     * Reads the named property of {@code receiver}, returning null when
     * {@code receiver} is null.
     *
     * @param receiver the object whose property is read; may be null
     * @return the property value, or null when {@code receiver} is null
     * @throws Throwable when the read fails
     */
    @GroovyABI(since = "1.6.0")
    Object callGetPropertySafe (Object receiver) throws Throwable;

    /**
     * Reads the named property of {@code receiver}.
     *
     * @param receiver the object whose property is read
     * @return the property value
     * @throws Throwable when the read fails
     */
    @GroovyABI(since = "1.6.0")
    Object callGetProperty (Object receiver) throws Throwable;

    /**
     * Reads the named property through {@link GroovyObject#getProperty(String)}.
     *
     * @param receiver the Groovy object whose property is read
     * @return the property value
     * @throws Throwable when the read fails
     */
    @GroovyABI(since = "1.6.0")
    Object callGroovyObjectGetProperty (Object receiver) throws Throwable;

    /**
     * Reads the named property through {@link GroovyObject#getProperty(String)},
     * returning null when {@code receiver} is null.
     *
     * @param receiver the Groovy object whose property is read; may be null
     * @return the property value, or null when {@code receiver} is null
     * @throws Throwable when the read fails
     */
    @GroovyABI(since = "1.6.0")
    Object callGroovyObjectGetPropertySafe (Object receiver) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with {@code args}.
     *
     * @param receiver the target of the call
     * @param args the arguments, in order
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object call (Object receiver, Object[] args) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with no arguments.
     *
     * @param receiver the target of the call
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object call (Object receiver) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with one argument.
     *
     * @param receiver the target of the call
     * @param arg1 the only argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object call (Object receiver, Object arg1) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with two arguments.
     *
     * @param receiver the target of the call
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object call (Object receiver, Object arg1, Object arg2) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with three arguments.
     *
     * @param receiver the target of the call
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object call (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with four arguments.
     *
     * @param receiver the target of the call
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @param arg4 the fourth argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object call (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with {@code args}, returning
     * null when {@code receiver} is null.
     *
     * @param receiver the target of the call; may be null
     * @param args the arguments, in order
     * @return the value returned by the method, or null when {@code receiver} is null
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callSafe (Object receiver, Object[] args) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with no arguments, returning
     * null when {@code receiver} is null.
     *
     * @param receiver the target of the call; may be null
     * @return the value returned by the method, or null when {@code receiver} is null
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callSafe (Object receiver) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with one argument, returning
     * null when {@code receiver} is null.
     *
     * @param receiver the target of the call; may be null
     * @param arg1 the only argument
     * @return the value returned by the method, or null when {@code receiver} is null
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callSafe (Object receiver, Object arg1) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with two arguments, returning
     * null when {@code receiver} is null.
     *
     * @param receiver the target of the call; may be null
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the value returned by the method, or null when {@code receiver} is null
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callSafe (Object receiver, Object arg1, Object arg2) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with three arguments, returning
     * null when {@code receiver} is null.
     *
     * @param receiver the target of the call; may be null
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @return the value returned by the method, or null when {@code receiver} is null
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callSafe (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;

    /**
     * Invokes the named method on {@code receiver} with four arguments, returning
     * null when {@code receiver} is null.
     *
     * @param receiver the target of the call; may be null
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @param arg4 the fourth argument
     * @return the value returned by the method, or null when {@code receiver} is null
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callSafe (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;

    /**
     * Invokes the named method on the current object with {@code args}.
     *
     * @param receiver the current Groovy object
     * @param args the arguments, in order
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callCurrent (GroovyObject receiver, Object [] args) throws Throwable;

    /**
     * Invokes the named method on the current object with no arguments.
     *
     * @param receiver the current Groovy object
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callCurrent (GroovyObject receiver) throws Throwable;

    /**
     * Invokes the named method on the current object with one argument.
     *
     * @param receiver the current Groovy object
     * @param arg1 the only argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callCurrent (GroovyObject receiver, Object arg1) throws Throwable;

    /**
     * Invokes the named method on the current object with two arguments.
     *
     * @param receiver the current Groovy object
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callCurrent (GroovyObject receiver, Object arg1, Object arg2) throws Throwable;

    /**
     * Invokes the named method on the current object with three arguments.
     *
     * @param receiver the current Groovy object
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callCurrent (GroovyObject receiver, Object arg1, Object arg2, Object arg3) throws Throwable;

    /**
     * Invokes the named method on the current object with four arguments.
     *
     * @param receiver the current Groovy object
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @param arg4 the fourth argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callCurrent (GroovyObject receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;

    /**
     * Invokes the named static method on {@code receiver} with {@code args}.
     *
     * @param receiver the class that declares the method
     * @param args the arguments, in order
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callStatic (Class receiver, Object [] args) throws Throwable;

    /**
     * Invokes the named static method on {@code receiver} with no arguments.
     *
     * @param receiver the class that declares the method
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callStatic (Class receiver) throws Throwable;

    /**
     * Invokes the named static method on {@code receiver} with one argument.
     *
     * @param receiver the class that declares the method
     * @param arg1 the only argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callStatic (Class receiver, Object arg1) throws Throwable;

    /**
     * Invokes the named static method on {@code receiver} with two arguments.
     *
     * @param receiver the class that declares the method
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callStatic (Class receiver, Object arg1, Object arg2) throws Throwable;

    /**
     * Invokes the named static method on {@code receiver} with three arguments.
     *
     * @param receiver the class that declares the method
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callStatic (Class receiver, Object arg1, Object arg2, Object arg3) throws Throwable;

    /**
     * Invokes the named static method on {@code receiver} with four arguments.
     *
     * @param receiver the class that declares the method
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @param arg4 the fourth argument
     * @return the value returned by the method
     * @throws Throwable when the call fails
     */
    @GroovyABI(since = "1.6.0")
    Object callStatic (Class receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;

    /**
     * Invokes a constructor selected for {@code receiver} with {@code args}.
     *
     * @param receiver the constructor's owner, as supplied by the caller
     * @param args the constructor arguments, in order
     * @return the constructed object
     * @throws Throwable when construction fails
     */
    @GroovyABI(since = "1.6.0")
    Object callConstructor (Object receiver, Object [] args) throws Throwable;

    /**
     * Invokes a constructor selected for {@code receiver} with no arguments.
     *
     * @param receiver the constructor's owner, as supplied by the caller
     * @return the constructed object
     * @throws Throwable when construction fails
     */
    @GroovyABI(since = "1.6.0")
    Object callConstructor (Object receiver) throws Throwable;

    /**
     * Invokes a constructor selected for {@code receiver} with one argument.
     *
     * @param receiver the constructor's owner, as supplied by the caller
     * @param arg1 the only constructor argument
     * @return the constructed object
     * @throws Throwable when construction fails
     */
    @GroovyABI(since = "1.6.0")
    Object callConstructor (Object receiver, Object arg1) throws Throwable;

    /**
     * Invokes a constructor selected for {@code receiver} with two arguments.
     *
     * @param receiver the constructor's owner, as supplied by the caller
     * @param arg1 the first constructor argument
     * @param arg2 the second constructor argument
     * @return the constructed object
     * @throws Throwable when construction fails
     */
    @GroovyABI(since = "1.6.0")
    Object callConstructor (Object receiver, Object arg1, Object arg2) throws Throwable;

    /**
     * Invokes a constructor selected for {@code receiver} with three arguments.
     *
     * @param receiver the constructor's owner, as supplied by the caller
     * @param arg1 the first constructor argument
     * @param arg2 the second constructor argument
     * @param arg3 the third constructor argument
     * @return the constructed object
     * @throws Throwable when construction fails
     */
    @GroovyABI(since = "1.6.0")
    Object callConstructor (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;

    /**
     * Invokes a constructor selected for {@code receiver} with four arguments.
     *
     * @param receiver the constructor's owner, as supplied by the caller
     * @param arg1 the first constructor argument
     * @param arg2 the second constructor argument
     * @param arg3 the third constructor argument
     * @param arg4 the fourth constructor argument
     * @return the constructed object
     * @throws Throwable when construction fails
     */
    @GroovyABI(since = "1.6.0")
    Object callConstructor (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;
}
