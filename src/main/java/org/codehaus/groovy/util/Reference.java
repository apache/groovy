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
package org.codehaus.groovy.util;

/**
 * Abstraction over reference implementations that keep a cleanup handler alongside the referent.
 *
 * @param <T> the referent type
 * @param <V> the handler type
 */
public interface Reference<T,V extends Finalizable> {
    /**
     * Returns the current referent.
     *
     * @return the referent, or {@code null} if it is no longer available
     */
    T get();

    /**
     * Clears the current referent.
     */
    void clear();

    /**
     * Returns the handler that should be notified when the reference is processed.
     *
     * @return the associated handler
     */
    V getHandler();
}
