// GPars - Groovy Parallel Systems
//
// Copyright © 2008–2012, 2014  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package groovyx.gpars.pa

import groovyx.gpars.GParsPoolUtil

// import static groovyx.gpars.util.PAUtils.createComparator

/**
 * Wraps a ParallelArray instance in map/reduce operation chains.
 */
abstract class AbstractPAWrapper<T> {

    /**
     * The wrapper ParallelArray instance
     */
    final pa

    /**
     * Creates an instance wrapping the supplied instance of ParallelArray
     */
    AbstractPAWrapper(final pa) {
        this.pa = pa
    }

    /**
     * Reconstructs a collection from the wrapped ParallelArray instance
     * @return A collection containing all elements of the wrapped ParallelArray
     */
    final Object getCollection() {
        this.pa as ArrayList
    }

    /**
     * Performs a parallel reduce operation. It will use the supplied two-argument closure to gradually reduce two elements into one.
     * @param cl A two-argument closure merging two elements into one. The return value of the closure will replace the original two elements.
     * @return The product of reduction
     */
    final T reduce(final Closure cl) {
        return GParsPoolUtil.injectParallel(pa, cl)
    }

    /**
     * Performs a parallel reduce operation. It will use the supplied two-argument closure to gradually reduce two elements into one.
     * @param cl A two-argument closure merging two elements into one. The return value of the closure will replace the original two elements.
     * @return The product of reduction
     */
    final T reduce(seed, final Closure cl) {
        return GParsPoolUtil.injectParallel(pa, seed, cl) as T
    }

    /**
     * Summarizes all elements of the collection in parallel using the "plus()" operator of the elements
     * @return The summary od all elements in the collection
     */
    final T sum() {
        return GParsPoolUtil.sumParallel(pa) as T
    }

    /**
     * Size of the collection
     * @return The number of elements in the collection
     */
    final int size() {
        pa.size()
    }

    /**
     * Finds in parallel the minimum of all values in the collection. The implicit comparator is used.
     * @return The minimum element of the collection
     */
    final T min() {
        return GParsPoolUtil.minParallel(pa) as T
    }

    /**
     * Finds in parallel the minimum of all values in the collection. The supplied comparator is used.
     * If the supplied closure takes two arguments it is used directly as a comparator.
     * If the supplied closure takes one argument, the values returned by the supplied closure for individual elements are used for comparison by the implicit comparator.
     * @param cl A one or two-argument closure
     * @return The minimum element of the collection
     */
    final T min(final Closure cl) {
        return GParsPoolUtil.minParallel(pa, cl) as T
    }

    /**
     * Finds in parallel the maximum of all values in the collection. The implicit comparator is used.
     * @return The maximum element of the collection
     */
    final T max() {
        return GParsPoolUtil.maxParallel(pa) as T
    }

    /**
     * Finds in parallel the maximum of all values in the collection. The supplied comparator is used.
     * If the supplied closure takes two arguments it is used directly as a comparator.
     * If the supplied closure takes one argument, the values returned by the supplied closure for individual elements are used for comparison by the implicit comparator.
     * @param cl A one or two-argument closure
     * @return The maximum element of the collection
     */
    final T max(final Closure cl) {
        return GParsPoolUtil.maxParallel(pa, cl) as T
    }

    /**
     * Returns a sorted parallel collection
     * If the supplied closure takes two arguments it is used directly as a comparator.
     * If the supplied closure takes one argument, the values returned by the supplied closure for individual elements are used for comparison by the implicit comparator.
     * @param cl A one or two-argument closure
     * @return A sorted collection holding all the elements
     */
    final AbstractPAWrapper sort(final Closure cl = { it }) {
        return new PAWrapper(pa.sort(cl)) as AbstractPAWrapper
    }

    /**
     * Performs parallel groupBy operation.
     * After all the elements have been processed, the method returns a map of groups of the original elements.
     * Elements in the same group gave identical results when the supplied closure was invoked on them.
     * Please note that the method returns a regular map, not a PAWrapper instance.
     * You can use the "getParallel()" method on the returned map to turn it into a parallel collection again.
     * @param cl A single-argument closure returning the value to use for grouping (the key in the resulting map).
     * @return A map following the Groovy specification for groupBy
     */
    Map groupBy(Closure cl) {
        return GParsPoolUtil.groupByParallel(pa, cl) as Map
    }

    /**
     * Performs a parallel combine operation.
     * The operation accepts a collection of tuples (two-element lists). The element at position 0 is treated as a key,
     * while the element at position 1 is considered to be the value.
     * By running 'combine' on such a collection of tuples, you'll back a map of get items with the same keys (the key is represented by tuple[0])
     * to be combined into a single element under their common key.
     * E.g. [[a, b], [c, d], [a, e], [c, f]] will be combined into [a : b+e, c : d+f], while the '+' operation on the values needs to be provided by the user as the accumulation closure.
     *
     * The keys will be mutually compared using their equals and hashCode methods.
     *
     * The 'accumulation function' argument needs to specify a function to use for combining (accumulating) the values belonging to the same key.
     * An 'initial accumulator value' needs to be provided as well. Since the 'combine' method processes items in parallel, the 'initial accumulator value' will be reused multiple times.
     * Thus the provided value must allow for reuse. It should be either a cloneable or immutable value or a closure returning a fresh initial accumulator each time requested.
     * Good combinations of accumulator functions and reusable initial values include:
     * <br/>accumulator = {List acc, value -&gt; acc << value} initialValue = []
     * <br/>accumulator = {List acc, value -&gt; acc << value} initialValue = {-&gt; []}* <br/>accumulator = {int sum, int value -&gt; acc + value} initialValue = 0
     * <br/>accumulator = {int sum, int value -&gt; sum + value} initialValue = {-&gt; 0}* <br/>accumulator = {ShoppingCart cart, Item value -&gt; cart.addItem(value)} initialValue = {-&gt; new ShoppingCart()}* <br/>
     * The return type is a map.
     * E.g. [['he', 1], ['she', 2], ['he', 2], ['me', 1], ['she, 5], ['he', 1] with the initial value provided a 0 will be combined into
     * ['he' : 4, 'she' : 7, 'he', : 2, 'me' : 1]
     *
     * Please note that the method returns a regular map, not a PAWrapper instance.
     * You can use the "getParallel()" method on the returned map to turn it into a parallel collection again.
     * @param initialValue The initial value for an accumulator. Since it will be used repeatedly, it should be either an unmodifiable value, a cloneable instance or a closure returning a fresh initial/empty accumulator each time requested
     * @param accumulator A two-argument closure, first argument being the accumulator and second holding the currently processed value. The closure is supposed to returned a modified accumulator after accumulating the value.
     * @return A map holding the final accumulated values for each unique key in the original collection of tuples.
     */
    Map combine(final Object initialValue, final Closure accumulation) {
//        switch (initialValue) {
//            case Closure: return combineImpl((Closure) initialValue, accumulation)
//            case Cloneable: return combineImpl({ initialValue.clone() }, accumulation)
//            default: return combineImpl({ initialValue }, accumulation)
//        }
    }

    Map combineImpl(final Closure initialValue, final Closure accumulation) {
//        combineImpl({ it[0] }, { it[1] }, initialValue, accumulation)
    }

    Map combineImpl(extractKey, extractValue, Closure initialValue, Closure accumulation) {
//
//        def result = reduce { a, b ->
//            if (a in CombineHolder) {
//                if (b in CombineHolder) return a.merge(b, accumulation, initialValue)
//                else return a.addToMap(extractKey(b), extractValue(b), accumulation, initialValue)
//            } else {
//                def aKey = extractKey(a)
//                final Object aValue = extractValue(a)
//                if (b in CombineHolder) return b.addToMap(aKey, aValue, accumulation, initialValue)
//                else {
//                    def bKey = extractKey(b)
//                    final Object bValue = extractValue(b)
//
//                    if (aKey == bKey) {
//                        def c = accumulation(accumulation(initialValue(), aValue), bValue)
//                        return [(aKey): c] as CombineHolder
//                    } else {
//                        def c = accumulation(initialValue(), aValue)
//                        def holder = [(aKey): c] as CombineHolder
//                        return holder.addToMap(bKey, bValue, accumulation, initialValue)
//                    }
//                }
//            }
//        }
//        if (result instanceof CombineHolder) {
//            return result.getContent()
//        } else {
//            final Object newValue = accumulation.call(initialValue.call(), extractValue(result));
//            def map = [:]
//            map[extractKey(result)] = newValue
//            return map
//        }
    }

    /**
     * Applies concurrently the supplied function to all elements in the collection, returning a collection containing
     * the transformed values.
     * @param A closure calculating a transformed value from the original one
     * @return A collection holding the new values
     */
    final AbstractPAWrapper map(final Closure cl) {
        return new PAWrapper(GParsPoolUtil.collectParallel(pa, cl))
    }

    /**
     * Filters concurrently elements in the collection based on the outcome of the supplied function on each of the elements.
     * @param A closure indicating whether to propagate the given element into the filtered collection
     * @return A collection holding the allowed values
     */
    AbstractPAWrapper filter(final Closure cl) {
        return new PAWrapper(GParsPoolUtil.findAllParallel(pa, cl))
    }
}
