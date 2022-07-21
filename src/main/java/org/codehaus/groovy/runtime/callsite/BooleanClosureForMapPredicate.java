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

import groovy.lang.Closure;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Helper class for internal use only.
 * This creates a Predicate by calling a {@link Closure} and converting the result to a boolean.
 * {@link BooleanReturningMethodInvoker} is used for caching.
 */
public class BooleanClosureForMapPredicate<K, V> implements Predicate<Map.Entry<K, V>> {
    private final BooleanReturningMethodInvoker bmi;
    private final Closure wrapped;
    private final int numberOfArguments;

    public BooleanClosureForMapPredicate(Closure wrapped) {
        this.wrapped = wrapped;
        bmi = new BooleanReturningMethodInvoker("call");
        numberOfArguments = wrapped.getMaximumNumberOfParameters();
    }

    private boolean call(Object... args) {
        return bmi.invoke(wrapped, args);
    }

    /**
     * If the call to the backing {@link Closure} is done on a {@link Closure}
     * taking one argument, then we give in the {@link Map.Entry}, otherwise we will
     * give in the key and value.
     */
    @Override
    public boolean test(Map.Entry<K, V> entry) {
        if (numberOfArguments == 2) {
            return call(entry.getKey(), entry.getValue());
        } else {
            return call(entry);
        }
    }
}
