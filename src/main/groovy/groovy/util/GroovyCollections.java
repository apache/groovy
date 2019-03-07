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

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Collections utility class
 */
public class GroovyCollections {
    /**
     * Finds all combinations of items from the given collections.
     *
     * @param collections the given collections
     * @return a List of the combinations found
     * @see #combinations(Collection)
     */
    public static List combinations(Object[] collections) {
        return combinations((Iterable)Arrays.asList(collections));
    }

    /**
     * Finds all non-null subsequences of a list.
     * E.g. <code>subsequences([1, 2, 3])</code> would be:
     * [[1, 2, 3], [1, 3], [2, 3], [1, 2], [1], [2], [3]]
     *
     * @param items the List of items
     * @return the subsequences from items
     */
    public static <T> Set<List<T>> subsequences(List<T> items) {
        // items.inject([]){ ss, h -> ss.collect { it + [h] }  + ss + [[h]] }
        Set<List<T>> ans = new HashSet<List<T>>();
        for (T h : items) {
            Set<List<T>> next = new HashSet<List<T>>();
            for (List<T> it : ans) {
                List<T> sublist = new ArrayList<T>(it);
                sublist.add(h);
                next.add(sublist);
            }
            next.addAll(ans);
            List<T> hlist = new ArrayList<T>();
            hlist.add(h);
            next.add(hlist);
            ans = next;
        }
        return ans;
    }

    /**
     * @param collections the given collections
     * @deprecated use combinations(Iterable)
     */
    @Deprecated
    public static List combinations(Collection collections) {
        return combinations((Iterable)collections);
    }

    /**
     * Finds all combinations of items from the given Iterable aggregate of collections.
     * So, <code>combinations([[true, false], [true, false]])</code>
     * is <code>[[true, true], [false, true], [true, false], [false, false]]</code>
     * and <code>combinations([['a', 'b'],[1, 2, 3]])</code>
     * is <code>[['a', 1], ['b', 1], ['a', 2], ['b', 2], ['a', 3], ['b', 3]]</code>.
     * If a non-collection item is given, it is treated as a singleton collection,
     * i.e. <code>combinations([[1, 2], 'x'])</code> is <code>[[1, 'x'], [2, 'x']]</code>.
     * If an empty collection is found within the given collections, the result will be an empty list.
     *
     * @param collections the Iterable of given collections
     * @return a List of the combinations found
     * @since 2.2.0
     */
    public static List combinations(Iterable collections) {
        List collectedCombos = new ArrayList();
        for (Object collection : collections) {
            Iterable items = DefaultTypeTransformation.asCollection(collection);
            if (collectedCombos.isEmpty()) {
                for (Object item : items) {
                    List l = new ArrayList();
                    l.add(item);
                    collectedCombos.add(l);
                }
            } else {
                List savedCombos = new ArrayList(collectedCombos);
                List newCombos = new ArrayList();
                for (Object value : items) {
                    for (Object savedCombo : savedCombos) {
                        List oldList = new ArrayList((List) savedCombo);
                        oldList.add(value);
                        newCombos.add(oldList);
                    }
                }
                collectedCombos = newCombos;
            }

            if (collectedCombos.isEmpty()) 
                break;
        }
        return collectedCombos;
    }

    public static <T> List<List<T>> inits(Iterable<T> collections) {
        List<T> copy = DefaultGroovyMethods.toList(collections);
        List<List<T>> result = new ArrayList<List<T>>();
        for (int i = copy.size(); i >= 0; i--) {
            List<T> next = copy.subList(0, i);
            result.add(next);
        }
        return result;
    }

    public static <T> List<List<T>> tails(Iterable<T> collections) {
        List<T> copy = DefaultGroovyMethods.toList(collections);
        List<List<T>> result = new ArrayList<List<T>>();
        for (int i = 0; i <= copy.size(); i++) {
            List<T> next = copy.subList(i, copy.size());
            result.add(next);
        }
        return result;
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
        return min((Iterable<T>)Arrays.asList(items));
    }

    /**
     * @deprecated use min(Iterable)
     */
    @Deprecated
    public static <T> T min(Collection<T> items) {
        return min((Iterable<T>)items);
    }

    /**
     * Selects the minimum value found in an Iterable of items.
     *
     * @param items an Iterable
     * @return the minimum value
     * @since 2.2.0
     */
    public static <T> T min(Iterable<T> items) {
        T answer = null;
        for (T value : items) {
            if (value != null) {
                if (answer == null || ScriptBytecodeAdapter.compareLessThan(value, answer)) {
                    answer = value;
                }
            }
        }
        return answer;
    }

    /**
     * Selects the maximum value found in an array of items, so
     * min([2, 4, 6] as Object[]) == 6.
     *
     * @param items an array of items
     * @return the maximum value
     */
    public static <T> T max(T[] items) {
        return max((Iterable<T>)Arrays.asList(items));
    }

    /**
     * @deprecated use max(Iterable)
     */
    @Deprecated
    public static <T> T max(Collection<T> items) {
        return max((Iterable<T>)items);
    }

    /**
     * Selects the maximum value found in an Iterable.
     *
     * @param items a Collection
     * @return the maximum value
     * @since 2.2.0
     */
    public static <T> T max(Iterable<T> items) {
        T answer = null;
        for (T value : items) {
            if (value != null) {
                if (answer == null || ScriptBytecodeAdapter.compareGreaterThan(value, answer)) {
                    answer = value;
                }
            }
        }
        return answer;
    }

    /**
     * Sums all the items from an array of items.
     *
     * @param items an array of items
     * @return the sum of the items
     */
    public static Object sum(Object[] items) {
        return sum((Iterable)Arrays.asList(items));
    }

    /**
     * @deprecated use sum(Iterable)
     */
    @Deprecated
    public static Object sum(Collection items) {
        return sum((Iterable)items);
    }

    /**
     * Sums all the given items.
     *
     * @param items an Iterable of items
     * @return the sum of the item
     * @since 2.2.0
     */
    public static Object sum(Iterable items) {
        return DefaultGroovyMethods.sum(items);
    }

}
