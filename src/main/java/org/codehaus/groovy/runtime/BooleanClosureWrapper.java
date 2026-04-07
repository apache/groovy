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
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.Map;

/**
 * Helper class for internal use only. This allows calling a {@link Closure} and
 * converting the result to a boolean using Groovy truth semantics:
 * null returns false, Boolean is unboxed, and for other types
 * {@code asBoolean()} is called.
 *
 * @since 6.0.0
 */
public class BooleanClosureWrapper {
    private final Closure wrapped;
    private final int numberOfArguments;

    public BooleanClosureWrapper(Closure wrapped) {
        this.wrapped = wrapped;
        this.numberOfArguments = wrapped.getMaximumNumberOfParameters();
    }

    /**
     * Normal closure call with boolean conversion.
     */
    public boolean call(Object... args) {
        Object result = wrapped.call(args);
        if (result == null) return false;
        if (result instanceof Boolean) return (Boolean) result;
        return DefaultTypeTransformation.castToBoolean(result);
    }

    /**
     * Bridge for a call based on a map entry. If the call is done on a {@link Closure}
     * taking one argument, then we give in the {@link Map.Entry}, otherwise we will
     * give in the key and value.
     */
    public <K, V> boolean callForMap(Map.Entry<K, V> entry) {
        if (numberOfArguments == 2) {
            return call(entry.getKey(), entry.getValue());
        } else {
            return call(entry);
        }
    }
}
