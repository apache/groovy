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
package bugs;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads {@code ClassLoader.parallelLockMap} key sets. Reflection hides the
 * field; a private lookup still sees it when {@code java.base/java.lang} is open.
 * VarHandle access has to be compiled as a direct call, which Groovy does not do.
 */
final class ParallelLockMapKeys {

    private static final VarHandle PARALLEL_LOCK_MAP = open();

    private ParallelLockMapKeys() {
    }

    /**
     * @return the lock-map keys, or {@code null} when this loader is not parallel capable
     */
    static Set<String> copy(ClassLoader loader) {
        Object map = PARALLEL_LOCK_MAP.get(loader);
        if (!(map instanceof ConcurrentHashMap<?, ?> values)) {
            return null;
        }
        Set<String> keys = new TreeSet<>();
        for (Object key : values.keySet()) {
            keys.add(String.valueOf(key));
        }
        return Collections.unmodifiableSet(keys);
    }

    private static VarHandle open() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(ClassLoader.class, MethodHandles.lookup());
            return lookup.findVarHandle(ClassLoader.class, "parallelLockMap", ConcurrentHashMap.class);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
