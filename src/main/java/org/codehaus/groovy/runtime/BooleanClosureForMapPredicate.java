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

import java.util.Map;
import java.util.function.Predicate;

/**
 * Adapts a {@link Closure} to a {@link Predicate} over {@link Map.Entry} values.
 * If the closure takes two parameters, the key and value are passed separately;
 * otherwise the {@link Map.Entry} itself is passed.
 *
 * @since 6.0.0
 */
public class BooleanClosureForMapPredicate<K, V> implements Predicate<Map.Entry<K, V>> {
    private final BooleanClosureWrapper bcw;
    private final int numberOfArguments;

    public BooleanClosureForMapPredicate(Closure wrapped) {
        this.bcw = new BooleanClosureWrapper(wrapped);
        this.numberOfArguments = wrapped.getMaximumNumberOfParameters();
    }

    @Override
    public boolean test(Map.Entry<K, V> entry) {
        if (numberOfArguments == 2) {
            return bcw.call(entry.getKey(), entry.getValue());
        } else {
            return bcw.call(entry);
        }
    }
}
