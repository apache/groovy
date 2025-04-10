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
package groovy.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Provides some iterable based generators.
 *
 * @since 5.0.0
 */
public class Iterables {

    /**
     * An iterator returning a map for each combination of elements the iterables sources.
     *
     * <pre class="groovyTestCase">
     * assert Iterables.combine(x: 1..2, y: 'a'..'c').collect().toString()
     *     == '[[x:1, y:a], [x:1, y:b], [x:1, y:c], [x:2, y:a], [x:2, y:b], [x:2, y:c]]'
     * assert Iterables.combine(x: 1..3, y: 'a'..'b').collect().toString()
     *     == '[[x:1, y:a], [x:1, y:b], [x:2, y:a], [x:2, y:b], [x:3, y:a], [x:3, y:b]]'
     * </pre>
     *
     * @param map the named source iterables
     * @return the output iterator of named combinations
     */
    public static <K, T> Iterator<Map<K, T>> combine(Map<K, ? extends Iterable<T>> map) {
        return new CrossProductIterator<>(map);
    }

    private static class CrossProductIterator<K, T> implements Iterator<Map<K, T>> {
        private final Map<K, ? extends Iterable<T>> iterables;
        private final Map<K, Iterator<T>> iterators = new LinkedHashMap<>();
        private final List<K> keys = new ArrayList<>();
        private Map<K, T> current;
        private boolean loaded;
        private boolean exhausted;

        private CrossProductIterator(Map<K, ? extends Iterable<T>> iterables) {
            this.iterables = iterables;
            for (Map.Entry<K, ? extends Iterable<T>> entry : iterables.entrySet()) {
                K key = entry.getKey();
                keys.add(key);
                iterators.put(key, entry.getValue().iterator());
            }
        }

        private void loadFirst() {
            current = new LinkedHashMap<>();
            for (K key : keys) {
                Iterator<T> iterator = iterators.get(key);
                if (!iterator.hasNext()) {
                    exhausted = true;
                    return;
                }
                T next = iterator.next();
                current.put(key, next);
            }
        }

        @Override
        public boolean hasNext() {
            if (!loaded) {
                loadNext();
                loaded = true;
            }
            return !exhausted;
        }

        @Override
        public Map<K, T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("CrossProductIterator has been exhausted and contains no more elements");
            }
            Map<K, T> ret = current;
            loaded = false;
            return ret;
        }

        private void loadNext() {
            if (current == null) {
                loadFirst();
            } else {
                current = new LinkedHashMap<>(current);
                for (int i = keys.size() - 1; i >= 0; i--) {
                    K key = keys.get(i);
                    if (iterators.get(key).hasNext()) {
                        T next = iterators.get(key).next();
                        current.put(key, next);
                        break;
                    } else if (i > 0) {
                        iterators.put(key, iterables.get(key).iterator());
                        current.put(key, iterators.get(key).next());
                    } else {
                        exhausted = true;
                    }
                }
            }
        }
    }
}
