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
package org.codehaus.groovy.macro.matcher;

import groovy.lang.Closure;

/**
 * Utility methods shared by AST matcher support classes.
 */
class MatcherUtils {
    /**
     * Clones a closure and sets its delegate for matcher evaluation.
     *
     * @param predicate the predicate closure to clone
     * @param delegate the delegate to assign
     * @param <T> the closure result type
     * @return the cloned closure
     */
    @SuppressWarnings("unchecked")
    static <T> Closure<T> cloneWithDelegate(final Closure<T> predicate, final Object delegate) {
        Closure<T> clone = (Closure<T>) predicate.clone();
        clone.setDelegate(delegate);
        clone.setResolveStrategy(Closure.DELEGATE_FIRST);
        return clone;
    }
}
