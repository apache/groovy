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
package org.apache.groovy.ginq.provider.collection.runtime;

import java.io.Serializable;

/**
 * Represents window frame bounds
 *
 * @param <T1> the type of lower frame bound
 * @param <T2> the type of upper frame bound
 * @since 4.0.0
 */
abstract class AbstractBound<T1, T2> implements Serializable {
    private static final long serialVersionUID = 6028167393745578578L;
    private final T1 lower;
    private final T2 upper;

    /**
     * Construct a new BoundTuple2 instance with lower and upper frame bounds
     *
     * @param lower the lower frame bound
     * @param upper the upper frame bound
     * @since 4.0.0
     */
    AbstractBound(T1 lower, T2 upper) {
        this.lower = lower;
        this.upper = upper;
    }

    /**
     * Returns the lower frame bound
     *
     * @return the lower frame bound
     * @since 4.0.0
     */
    public T1 getLower() {
        return lower;
    }

    /**
     * Returns the upper frame bound
     *
     * @return the upper frame bound
     * @since 4.0.0
     */
    public T2 getUpper() {
        return upper;
    }
}
