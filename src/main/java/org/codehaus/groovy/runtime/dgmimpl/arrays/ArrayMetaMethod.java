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
package org.codehaus.groovy.runtime.dgmimpl.arrays;

import groovy.lang.MetaMethod;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;

import java.lang.reflect.Modifier;

/** Shared meta-method base for array {@code getAt} and {@code putAt}. */
public abstract class ArrayMetaMethod extends MetaMethod {
    /** Cached {@link Integer} class, the index type for array {@code getAt} and {@code putAt}. */
    protected static final CachedClass   INTEGER_CLASS = ReflectionCache.getCachedClass(Integer.class);
    /** One-element parameter list containing {@link #INTEGER_CLASS}. */
    protected static final CachedClass[] INTEGER_CLASS_ARR = {INTEGER_CLASS};

    /**
     * Maps a negative index from the end of the array. An index that is still negative
     * after adding {@code size} is rejected. A non-negative index is returned unchanged,
     * even when it is past the end.
     *
     * @param i the requested index, which may be negative
     * @param size the array length added to a negative index
     * @return {@code i} when {@code i} is non-negative, otherwise {@code i + size}
     * @throws ArrayIndexOutOfBoundsException when {@code i + size} is still negative
     */
    protected static int normaliseIndex(int i, int size) {
        int temp = i;
        if (i < 0) {
            i += size;
        }
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative array index [" + temp + "] too large for array size " + size);
        }
        return i;
    }

    @Override
    public int getModifiers() {
        return Modifier.PUBLIC;
    }
}
