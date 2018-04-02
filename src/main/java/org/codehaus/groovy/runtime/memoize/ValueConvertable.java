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

package org.codehaus.groovy.runtime.memoize;

/**
 * To support caches whose values are convertable, e.g. SoftReference, WeakReference
 *
 * @param <V1> source value type, e.g. SoftReference, WeakReference
 * @param <V2> target value type, e.g. value that SoftReference or WeakReference referenced
 */
public interface ValueConvertable<V1, V2> {
    /**
     * convert the original value to the target value
     *
     * @param value the original value
     * @return the converted value
     */
    V2 convertValue(V1 value);
}
