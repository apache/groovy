/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.runtime.iterator;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.*;


public class Iterators {
    private Iterators() {
    }

    /**
     * Iterate over given object
     *
     * @param self object to be iterated
     * @return iterator over given object
     */
    public static Iterator iterate(final Object self) {
        if (self instanceof Iterator)
            return (Iterator) self;

        if (self instanceof Collection)
            return ((Collection) self).iterator();

        if (self instanceof Map)
            return ((Map) self).entrySet().iterator();

        if (self instanceof Enumeration)
            return new EnumerationIterator((Enumeration) self);

        Class aClass = self.getClass();
        if (aClass.isArray()) {
            if (aClass.getComponentType().isPrimitive()) {
                return DefaultTypeTransformation.primitiveArrayToList(self).iterator();
            }
            return Arrays.asList((Object[]) self).iterator();
        }

        return new SingletonIterator(self);
    }

    /**
     * Iterate over the iterator
     *
     * @param self iterator to iterate
     */
    public static void run(Iterator self) {
        for (; self.hasNext();)
            self.next();
    }

    /**
     * Iterate over the iterator and collect results
     *
     * @param self the iterator
     * @return list collected elements
     */
    public static List collect(Iterator self) {
        List list = new ArrayList();
        for (; self.hasNext();)
            list.add(self.next());
        return list;
    }

    /**
     * Iterate over the iterator and collect results
     *
     * @param self the iterator
     * @return list collected elements
     */
    public static List toList(Iterator self) {
        List list = new ArrayList();
        for (; self.hasNext();)
            list.add(self.next());
        return list;
    }

    /**
     * Filter given iterator with closure.
     *
     * @see TransformIterator for details
     *
     * @param self the iterators
     * @param args additional arguments
     * @param closure filtering closure
     * @return iterator of filtered elements
     */
    public static FilterIterator withFilter(Object self, Map args, Closure closure) {
        return new FilterIterator(iterate(self), args, closure);
    }

    /**
     * Same as previous but without additional params
     *
     * @param self
     * @param closure
     * @return iterator of filtered elements
     */
    public static FilterIterator withFilter(Object self, Closure closure) {
        return withFilter(iterate(self), null, closure);
    }

    /**
     * Transform given iterator with closure.
     *
     * @see TransformIterator for details
     *
     * @param self the iterators
     * @param args additional arguments
     * @param closure filtering closure
     * @return iterator of filtered elements
     */
    public static TransformIterator withTransform(Object self, Map args, Closure closure) {
        return new TransformIterator(iterate(self), args, closure);
    }

    /**
     * Same as previous but without additional args
     *
     * @param self
     * @param closure
     * @return iterator of filtered elements
     */
    public static TransformIterator withTransform(Object self, Closure closure) {
        return withTransform(self, null, closure);
    }

    /**
     * Iterate over given collection and flatten it.
     * Flattening means iteration of content of elements, which itself is
     * - iterator
     * - collection
     * - map
     * - enumeration
     *
     * @param self the object to iterate
     * @param strategy defines how to flatten maps (keys, values, entries)
     * @return iterator of flattened elements
     */
    public static Iterator withOneLevelFlattening(Object self, int strategy) {
        return new FlatteningIterator.FlatteningOneLevelIterator(iterate(self), strategy);
    }

    /**
     * Same as previous
     *
     * @param self
     * @return iterator of flattened elements
     */
    public static Iterator withOneLevelFlattening(Object self) {
        return withOneLevelFlattening(iterate(self), 0);
    }

    /**
     * Iterate over given collection and flatten it.
     * Flattening means iteration of content of elements, which itself is
     * - iterator
     * - collection
     * - map
     * - enumeration
     *
     * @param self the object to iterate
     * @param strategy defines how to flatten maps (keys, values, entries)
     * @return iterator of flattened elements
     */
    public static Iterator withAllLevelsFlattening(Object self, int strategy) {
        return new FlatteningIterator.FlatteningAllLevelsIterator(iterate(self), strategy);
    }

    /**
     * Same as previous
     *
     * @param self
     * @return iterator of flattened elements
     */
    public static Iterator withAllLevelsFlattening(Object self) {
        return withAllLevelsFlattening(iterate(self), 0);
    }

    public static TransformIterator withKeys(Object self) {
        return withKeys(self, null);
    }

    public static TransformIterator withKeys(Object self, Map args) {
        return new TransformIterator.KeysIterator(iterate(self), args);
    }

    public static TransformIterator withValues(Object self) {
        return withValues(self, null);
    }

    public static TransformIterator withValues(Object self, Map args) {
        return new TransformIterator.ValuesIterator(iterate(self), args);
    }

    /**
     * Merge two iterators
     * Sequencies [a1,a2,a3...] [b1,b2,b3...] transformed to [a1,b1,a2,b2,...]
     *
     * @param first 1st iterator
     * @param second 2nd iterator
     * @return iterator for merged sequence
     */
    public static Iterator mergeWith(Object first, Object second) {
        return new MergeIterator(iterate(first), iterate(second));
    }

    /**
     * Merge two iterators in reverse order
     * Sequencies [a1,a2,a3...] [b1,b2,b3...] transformed to [b1,a1,b2,a2,...]
     *
     * @param first 1st iterator
     * @param second 2nd iterator
     * @return iterator for merged sequence
     */
    public static Iterator mergeInReverseOrger(Object first, Object second) {
        return new MergeIterator(iterate(second), iterate(first));
    }

    /**
     * Compose two iterators
     * Sequencies [a1,a2,...aN] [b1,b2,...bM] transformed to [a1,..aN,b1,...bM]
     *
     * @param first 1st iterator
     * @param second 2nd iterator
     * @return iterator over composed sequence
     */
    public static Iterator composeWith(Object first, Object second) {
        return new CompositeIterator(iterate(first), iterate(second));
    }

    /**
     * Compose two iterators in reverse order
     * Sequencies [a1,a2,...aN] [b1,b2,...bM] transformed to [b1,...bM,a1,..aN]
     *
     * @param first 1st iterator
     * @param second 2nd iterator
     * @return iterator over composed sequence
     */
    public static Iterator composeInReverseOrger(Object first, Object second) {
        return new CompositeIterator(iterate(second), iterate(first));
    }

    /**
     * Same as composeWith
     *
     * @param first
     * @param second
     * @return iterator over composed sequence
     */
    public static Iterator plus(Iterator first, Iterator second) {
        return new CompositeIterator(first, second);
    }

    /**
     * Returs iterator of unique elements of given iterator keeping internal set of already iterated elements
     *
     * @param self object to be iterated
     * @return iterator of unique elements
     */
    public static FilterIterator unique(Object self) {
        return new UniqueIterator(iterate(self));
    }

    /**
     * Returns iterator of lists of up to given number of sequential elements of given iterator.
     * Each list will contain exactly bucketSize elements (except probably the last one)
     *
     * @param self object to be iterated
     * @param bucketSize size of bucket
     * @return iterator of buckets
     */
    public static Iterator groupBy(Object self, int bucketSize) {
        return new GroupByIterator(iterate(self), bucketSize);
    }

    private static class SingletonIterator implements Iterator {
        Object next;
        boolean hasNext;
        private final Object self;

        public SingletonIterator(Object self) {
            this.self = self;
            next = self;
            hasNext = true;
        }

        public boolean hasNext() {
            return hasNext;
        }

        public Object next() {
            if (hasNext) {
                hasNext = false;
                return self;
            } else
                throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
