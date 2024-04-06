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

import groovy.lang.Closure;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FromString;
import org.codehaus.groovy.runtime.ArrayGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.NumberAwareComparator;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GroovyCollections {

    /**
     * Finds all combinations of items from the given collections.
     *
     * @param collections the given collections
     * @return A list of the combinations found.
     * @see #combinations(Iterable)
     */
    public static List<List> combinations(Object[]    collections) {
        return combinations(Arrays.asList(collections));
    }

    /**
     * Finds all combinations of items from the given collections.
     * So, <code>combinations([[true, false], [true, false]])</code>
     * is <code>[[true, true], [false, true], [true, false], [false, false]]</code>
     * and <code>combinations([['a', 'b'],[1, 2, 3]])</code>
     * is <code>[['a', 1], ['b', 1], ['a', 2], ['b', 2], ['a', 3], ['b', 3]]</code>.
     * If a non-collection item is given, it is treated as a singleton collection,
     * i.e. <code>combinations([[1, 2], 'x'])</code> is <code>[[1, 'x'], [2, 'x']]</code>.
     * If an empty collection is found within the given collections, the result will be an empty list.
     *
     * @param collections the Iterable of given collections
     * @return A list of the combinations found.
     * @since 2.2.0
     */
    public static List<List> combinations(Iterable<?> collections) {
        List<List<Object>> combos = new ArrayList<>();
        for (var collection : collections) {
            if (combos.isEmpty()) {
                for (var object : DefaultTypeTransformation.asCollection(collection)) {
                    List<Object> list = new ArrayList<>();
                    list.add(object);
                    combos.add(list);
                }
            } else {
                List<List<Object>> next = new ArrayList<>(); // each list plus each item
                for (var object : DefaultTypeTransformation.asCollection(collection)) {
                    for (var combo : combos) {
                        List<Object> list = new ArrayList<>(combo);
                        list.add(object);
                        next.add(list);
                    }
                }
                combos = next;
            }
            if (combos.isEmpty())
                break;
        }
        return (List) combos;
    }

    /**
     * @since 2.5.0
     */
    public static <T> List<List<T>> inits(Iterable<T> collections) {
        List<T> copy = DefaultGroovyMethods.toList(collections);
        List<List<T>> result = new ArrayList<>();
        for (int i = copy.size(); i >= 0; i--) {
            List<T> next = copy.subList(0, i);
            result.add(next);
        }
        return result;
    }

    /**
     * @since 2.5.0
     */
    public static <T> List<List<T>> tails(Iterable<T> collections) {
        List<T> copy = DefaultGroovyMethods.toList(collections);
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i <= copy.size(); i++) {
            List<T> next = copy.subList(i, copy.size());
            result.add(next);
        }
        return result;
    }

    /**
     * Finds all non-empty subsequences of a list.
     * E.g. <code>subsequences([1, 2, 3])</code> would be:
     * [[1, 2, 3], [1, 3], [2, 3], [1, 2], [1], [2], [3]]
     *
     * @param items the List of items
     * @return the subsequences from items
     * @since 1.8.0
     */
    public static <T> Set<List<T>> subsequences(List<T> items) {
        // items.inject([]){ ss, h -> ss.collect { it + [h] }  + ss + [[h]] }
        Set<List<T>> ans = new HashSet<>();
        for (T h : items) {
            Set<List<T>> next = new HashSet<>();
            for (List<T> it : ans) {
                List<T> sublist = new ArrayList<>(it);
                sublist.add(h);
                next.add(sublist);
            }
            next.addAll(ans);
            List<T> hlist = new ArrayList<>();
            hlist.add(h);
            next.add(hlist);
            ans = next;
        }
        return ans;
    }

    /**
     * Transposes an array of lists.
     *
     * @param lists the given lists
     * @return a List of the transposed lists
     * @see #transpose(List)
     */
    public static List transpose(Object[] lists) {
        return transpose(Arrays.asList(lists));
    }

    /**
     * Transposes the given lists.
     * So, <code>transpose([['a', 'b'], [1, 2]])</code>
     * is <code>[['a', 1], ['b', 2]]</code> and
     * <code>transpose([['a', 'b', 'c']])</code>
     * is <code>[['a'], ['b'], ['c']]</code>.
     *
     * @param lists the given lists
     * @return a List of the transposed lists
     */
    public static List transpose(List lists) {
        List result = new ArrayList();
        if (lists.isEmpty()) return result;
        int minSize = Integer.MAX_VALUE;
        for (Object listLike : lists) {
            List list = (List) DefaultTypeTransformation.castToType(listLike, List.class);
            if (list.size() < minSize) minSize = list.size();
        }
        if (minSize == 0) return result;
        for (int i = 0; i < minSize; i++) {
            result.add(new ArrayList());
        }
        for (Object listLike : lists) {
            List list = (List) DefaultTypeTransformation.castToType(listLike, List.class);
            for (int i = 0; i < minSize; i++) {
                List resultList = (List) result.get(i);
                resultList.add(list.get(i));
            }
        }
        return result;
    }

    /**
     * Selects the minimum value found in an array of items, so
     * min([2, 4, 6] as Object[]) == 2.
     *
     * @param items an array of items
     * @return the minimum value
     */
    public static <T> T min(T[] items) {
        return ArrayGroovyMethods.min(items);
    }

    /**
     * Selects the minimum value found in an Iterable of items.
     *
     * @param items an Iterable
     * @return the minimum value
     * @since 2.2.0
     */
    public static <T> T min(Iterable<T> items) {
        return DefaultGroovyMethods.min(items);
    }

    /**
     * Selects the maximum value found in an array of items, so
     * max([2, 4, 6] as Object[]) == 6.
     *
     * @param items an array of items
     * @return the maximum value
     */
    public static <T> T max(T[] items) {
        return ArrayGroovyMethods.max(items);
    }

    /**
     * Selects the maximum value found in an Iterable.
     *
     * @param items a Collection
     * @return the maximum value
     * @since 2.2.0
     */
    public static <T> T max(Iterable<T> items) {
        return DefaultGroovyMethods.max(items);
    }

    /**
     * Sums all the items from an array of items.
     *
     * @param items an array of items
     * @return the sum of the items
     */
    public static Object sum(Object[] items) {
        return ArrayGroovyMethods.sum(items);
    }

    /**
     * Sums all the given items.
     *
     * @param items an Iterable of items
     * @return the sum of the item
     * @since 2.2.0
     */
    public static Object sum(Iterable<?> items) {
        return DefaultGroovyMethods.sum(items);
    }

    /**
     * Returns an ordered set of all the unique items found in the provided argument iterables.
     * <pre class="groovyTestCase">
     * assert GroovyCollections.union([1, 2], [2, 3], [1, 4]) == [1, 2, 3, 4]
     * </pre>
     *
     * @param iterables the sources of items
     * @return the ordered list of unique values found
     * @since 4.0.0
     */
    public static <T> List<T> union(Iterable<T>... iterables) {
        return union(Arrays.asList(iterables));
    }

    /**
     * Returns an ordered set of all the unique items found in the provided argument iterables.
     * <pre class="groovyTestCase">
     * assert GroovyCollections.union([[1, 2], [2, 3], [1, 4]]) == [1, 2, 3, 4]
     * </pre>
     *
     * @param iterables the list of source items
     * @return the ordered list of unique values found
     * @since 4.0.0
     */
    public static <T> List<T> union(List<Iterable<T>> iterables) {
        return union(iterables, new NumberAwareComparator<>());
    }

    /**
     * Returns an ordered set of all the unique items found in the provided argument iterables
     * using the provided comparator to compare items.
     * <pre class="groovyTestCase">
     * assert GroovyCollections.union(n -&gt; n.abs(), [1, 2, 5], [-3, -4, -5], [4, 6]) == [1, 2, 5, -3, -4, 6]
     * assert GroovyCollections.union(n -&gt; n.trunc(), [1.1, 2.2], [2.5, 3.3], [3.9, 4.1]) == [1.1, 2.2, 3.3, 4.1]
     * assert GroovyCollections.union(w -&gt; w.toUpperCase(), ['a', 'A'], ['B', 'a', 'c', 'b']) == ['a', 'B', 'c']
     * </pre>
     *
     * @param comparator a Comparator
     * @param iterables the sources of items
     * @return the ordered list of unique values found
     * @since 4.0.0
     */
    public static <T> List<T> union(Comparator<T> comparator, Iterable<T>... iterables) {
        return union(Arrays.asList(iterables), comparator);
    }

    /**
     * Returns an ordered set of all the unique items found in the provided argument iterables
     * using the provided comparator to compare items.
     * <pre class="groovyTestCase">
     * assert GroovyCollections.union([[1, 2, 5], [-3, -4, -5], [4, 6]], n -&gt; n.abs()) == [1, 2, 5, -3, -4, 6]
     * assert GroovyCollections.union([[1.1, 2.2], [2.5, 3.3], [3.9, 4.1]], n -&gt; n.trunc()) == [1.1, 2.2, 3.3, 4.1]
     * assert GroovyCollections.union([['a', 'A'], ['B', 'a', 'c', 'b']], w -&gt; w.toUpperCase()) == ['a', 'B', 'c']
     * </pre>
     *
     * @param iterables the list of source items
     * @param comparator a Comparator
     * @return the ordered list of unique values found
     * @since 4.0.0
     */
    public static <T> List<T> union(List<Iterable<T>> iterables, Comparator<T> comparator) {
        LinkedHashSet<T> ansSet = new LinkedHashSet<>();
        TreeSet<T> seen = new TreeSet<>(comparator);
        for (Iterable<T> nextList : iterables) {
            for (T next : nextList) {
                if (seen.add(next)) {
                    ansSet.add(next);
                }
            }
        }
        return new ArrayList<>(ansSet);
    }

    /**
     * Returns an ordered set of all the unique items found in the provided argument iterables
     * using the provided closure to compare items.
     * <pre class="groovyTestCase">
     * def abs = { n -&gt; n.abs() }
     * assert GroovyCollections.union(abs, [1, 2, 5], [-3, -4, -5], [4, 6]) == [1, 2, 5, -3, -4, 6]
     * </pre>
     *
     * @param condition a Closure used to determine unique items
     * @param iterables the sources of items
     * @return the ordered list of unique values found
     * @since 4.0.0
     */
    public static <T> List<T> union(@ClosureParams(value= FromString.class, options={"T","T,T"}) Closure condition, Iterable<T>... iterables) {
        return union(Arrays.asList(iterables), condition);
    }

    /**
     * Returns an ordered set of all the unique items found in the provided argument iterables
     * using the provided closure to compare items.
     * <pre class="groovyTestCase">
     * assert GroovyCollections.union([[1, 2, 5], [-3, -4, -5], [4, 6]]){ n -&gt; n.abs() } == [1, 2, 5, -3, -4, 6]
     * </pre>
     *
     * @param iterables the list of source items
     * @param condition a Closure used to determine unique items
     * @return the ordered list of unique values found
     * @since 4.0.0
     */
    public static <T> List<T> union(List<Iterable<T>> iterables, @ClosureParams(value= FromString.class, options={"T","T,T"}) Closure condition) {
        Comparator<T> comparator = condition.getMaximumNumberOfParameters() == 1
                ? new OrderBy<>(condition, true)
                : new ClosureComparator<>(condition);
        return union(iterables, comparator);
    }
}
