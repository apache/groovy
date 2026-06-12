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
package org.apache.groovy.contracts.domain;

import org.apache.groovy.contracts.util.Validate;
import org.codehaus.groovy.ast.MethodNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Maps methods to their accumulated contract assertions.
 *
 * @param <T> the assertion type stored for each method
 */
public class AssertionMap<T extends Assertion<T>> implements Iterable<Map.Entry<MethodNode, T>> {

    private final Map<MethodNode, T> internalMap;

    /**
     * Creates an empty assertion map.
     */
    public AssertionMap() {
        this.internalMap = new HashMap<>();
    }

    /**
     * Adds the given assertion to the method entry using logical conjunction.
     *
     * @param methodNode the method whose assertion should be updated
     * @param assertion the assertion to merge
     */
    public void and(final MethodNode methodNode, final T assertion) {
        Validate.notNull(methodNode);
        Validate.notNull(assertion);

        if (!internalMap.containsKey(methodNode)) {
            internalMap.put(methodNode, assertion);
        } else {
            internalMap.get(methodNode).and(assertion);
        }
    }

    /**
     * Adds the given assertion to the method entry using logical disjunction.
     *
     * @param methodNode the method whose assertion should be updated
     * @param assertion the assertion to merge
     */
    public void or(final MethodNode methodNode, final T assertion) {
        Validate.notNull(methodNode);
        Validate.notNull(assertion);

        if (!internalMap.containsKey(methodNode)) {
            internalMap.put(methodNode, assertion);
        } else {
            internalMap.get(methodNode).or(assertion);
        }
    }

    /**
     * Joins the assertion into the method entry using the default merge strategy.
     *
     * @param methodNode the method whose assertion should be updated
     * @param assertion the assertion to merge
     */
    public void join(final MethodNode methodNode, final T assertion) {
        and(methodNode, assertion);
    }

    /**
     * Indicates whether an assertion has already been recorded for the given method.
     *
     * @param methodNode the method to look up
     * @return {@code true} if an assertion is present
     */
    public boolean contains(final MethodNode methodNode) {
        return internalMap.containsKey(methodNode);
    }

    /**
     * Returns an iterator over the stored method/assertion pairs.
     *
     * @return an iterator across the current entries
     */
    @Override
    public Iterator<Map.Entry<MethodNode, T>> iterator() {
        return internalMap.entrySet().iterator();
    }

    /**
     * Returns the number of method entries currently stored.
     *
     * @return the assertion entry count
     */
    public int size() {
        return internalMap.size();
    }

    /**
     * Returns the assertion associated with the supplied method.
     *
     * @param methodNode the method to look up
     * @return the matching assertion, or {@code null} if none has been stored
     */
    public T get(final MethodNode methodNode) {
        return internalMap.get(methodNode);
    }
}
