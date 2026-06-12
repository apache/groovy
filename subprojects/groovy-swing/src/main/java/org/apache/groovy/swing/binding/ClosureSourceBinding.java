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
package org.apache.groovy.swing.binding;

import groovy.lang.Closure;

/**
 * Uses a closure invocation to compute a source value for a binding.
 *
 * @since Groovy 1.1
 */
public class ClosureSourceBinding implements SourceBinding {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * The closure used to compute source values.
     */
    Closure closure;
    /**
     * Arguments supplied when invoking the closure.
     */
    Object[] arguments;

    /**
     * Creates a source binding that invokes the closure with no arguments.
     *
     * @param closure the closure used to compute source values
     */
    public ClosureSourceBinding(Closure closure) {
        this(closure, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Creates a source binding that invokes the closure with the supplied arguments.
     *
     * @param closure the closure used to compute source values
     * @param arguments the closure arguments to supply
     */
    public ClosureSourceBinding(Closure closure, Object[] arguments) { //TODO in Groovy 2.0 use varargs?
        this.closure = closure;
        this.arguments = arguments;
    }

    /**
     * Returns the closure used to compute source values.
     *
     * @return the source closure
     */
    public Closure getClosure() {
        return closure;
    }

    /**
     * Replaces the closure used to compute source values.
     *
     * @param closure the new source closure
     */
    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    /**
     * Invokes the configured closure with the current argument list.
     *
     * @return the computed source value
     */
    @Override
    public Object getSourceValue() {
        return closure.call(arguments);
    }

    /**
     * Replaces the full argument list passed to the closure.
     *
     * @param arguments the closure arguments to use
     */
    public void setClosureArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Replaces the closure arguments with a single argument.
     *
     * @param argument the single closure argument to use
     */
    public void setClosureArgument(Object argument) {
        this.arguments = new Object[] {argument};
    }
}
