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
package groovy.lang;

import org.codehaus.groovy.runtime.FormatHelper;

import java.io.Serial;

/**
 * An exception occurred when invoking a Closure with the wrong number and/or
 * types of arguments
 */
public class IncorrectClosureArgumentsException extends GroovyRuntimeException {

    @Serial private static final long serialVersionUID = 4208144749858078754L;
    private final Closure closure;
    private final Object arguments;
    private final Class[] expected;

    /**
     * Creates an exception describing mismatched closure arguments.
     *
     * @param closure the target closure
     * @param arguments the supplied arguments
     * @param expected the expected parameter types
     */
    public IncorrectClosureArgumentsException(Closure closure, Object arguments, Class[] expected) {
        super(
            "Incorrect arguments to closure: "
                + closure
                + ". Expected: "
                + FormatHelper.toArrayString(expected)
                + ", actual: "
                + FormatHelper.toString(arguments));  // arguments is Object, not array
        this.closure = closure;
        this.arguments = arguments;
        this.expected = expected;
    }

    /**
     * Returns the supplied arguments.
     *
     * @return the supplied arguments
     */
    public Object getArguments() {
        return arguments;
    }

    /**
     * Returns the target closure.
     *
     * @return the target closure
     */
    public Closure getClosure() {
        return closure;
    }

    /**
     * Returns the expected parameter types.
     *
     * @return the expected parameter types
     */
    public Class[] getExpected() {
        return expected;
    }

}
