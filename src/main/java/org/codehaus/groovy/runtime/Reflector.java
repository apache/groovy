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

import groovy.lang.MissingMethodException;
import org.codehaus.groovy.reflection.CachedMethod;


/**
 * Provides as alternative to reflection using bytecode generation.
 */
public class Reflector {

    /**
     * Does not call {@code method}. This base implementation always delegates to
     * {@link #noSuchMethod(CachedMethod, Object, Object[])}. Generated reflectors
     * override this method to perform the call.
     *
     * @param method the method that was selected
     * @param object the receiver, passed through unused by this implementation
     * @param arguments the arguments, passed through
     * @return nothing; this implementation always throws
     * @throws MissingMethodException always
     */
    public Object invoke(CachedMethod method, Object object, Object[] arguments) {
        return noSuchMethod(method, object, arguments);
    }

    /**
     * Throws a non-static {@link MissingMethodException} using {@code method}'s name
     * and declaring class. {@code object} is ignored. A null {@code arguments} array
     * is recorded as an empty argument list; a non-null array is retained as given.
     *
     * @param method supplies the missing name and declaring class
     * @param object unused
     * @param arguments arguments recorded on the exception; {@code null} becomes an empty array
     * @return nothing; this method always throws
     * @throws MissingMethodException always
     */
    protected Object noSuchMethod(CachedMethod method, Object object, Object[] arguments) {
        throw new MissingMethodException(method.getName(), method.getDeclaringClass().getTheClass(), arguments, false);
    }
}
