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

package org.apache.groovy.util;

import org.apache.groovy.internal.util.Supplier;

/**
 * Just hold an object
 * @param <T> the type of object
 */
public class ObjectHolder<T> {
    private T object;

    /**
     * Creates an empty holder.
     */
    public ObjectHolder() {}

    /**
     * Creates a holder with an initial object.
     *
     * @param object the initial object
     */
    public ObjectHolder(T object) {
        this.object = object;
    }

    /**
     * Returns the currently held object.
     *
     * @return the held object
     */
    public T getObject() {
        return object;
    }

    /**
     * Returns the currently held object, initializing it from the supplier when needed.
     *
     * @param def the supplier used when no object has been set
     * @return the held object
     */
    public T getObject(Supplier<? extends T> def) {
        if (null == object) {
            object = def.get();
        }
        return object;
    }

    /**
     * Replaces the currently held object.
     *
     * @param object the object to hold
     */
    public void setObject(T object) {
        this.object = object;
    }
}
