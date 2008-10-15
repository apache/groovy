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
package groovy.util;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.*;

/**
 * A Collections utility class
 *
 * @author Paul King
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
     *
     * @param collections the given collections
     * @return a List of the combinations found
     */
    public static List combinations(Collection collections) {
        List collectedCombos = new ArrayList();
        for (Iterator outer = collections.iterator(); outer.hasNext();) {
            Collection items = DefaultTypeTransformation.asCollection(outer.next());
            if (collectedCombos.isEmpty()) {
                for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                    List l = new ArrayList();
                    l.add(iterator.next());
                    collectedCombos.add(l);
                }
            } else {
                List savedCombos = new ArrayList(collectedCombos);
                List newCombos = new ArrayList();
                for (Iterator inner = items.iterator(); inner.hasNext();) {
                    Object value = inner.next();
                    for (Iterator combos = savedCombos.iterator(); combos.hasNext();) {
                        List oldlist = new ArrayList((List) combos.next());
                        oldlist.add(value);
                        newCombos.add(oldlist);
                    }
                }
                collectedCombos = newCombos;
            }
        }
        return collectedCombos;
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
        if (lists.isEmpty() || lists.size() == 0) return result;
        int minSize = Integer.MAX_VALUE;
        for (Iterator outer = lists.iterator(); outer.hasNext();) {
            List list = (List) DefaultTypeTransformation.castToType(outer.next(), List.class);
            if (list.size() < minSize) minSize = list.size();
        }
        if (minSize == 0) return result;
        for (int i = 0; i < minSize; i++) {
            result.add(new ArrayList());
        }
        for (Iterator outer = lists.iterator(); outer.hasNext();) {
            List list = (List) DefaultTypeTransformation.castToType(outer.next(), List.class);
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
    public static Object min(Object[] items) {
        return min(Arrays.asList(items));
    }

    /**
     * Selects the minimum value found in a collection of items.
     *
     * @param items a Collection
     * @return the minimum value
     */
    public static Object min(Collection items) {
        Object answer = null;
        for (Iterator iter = items.iterator(); iter.hasNext();) {
            Object value = iter.next();
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
    public static Object max(Object[] items) {
        return max(Arrays.asList(items));
    }

    /**
     * Selects the maximum value found in a collection
     *
     * @param items a Collection
     * @return the maximum value
     */
    public static Object max(Collection items) {
        Object answer = null;
        for (Iterator iter = items.iterator(); iter.hasNext();) {
            Object value = iter.next();
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
        return DefaultGroovyMethods.sum(Arrays.asList(items));
    }

    /**
     * Sums all the items from a collection of items.
     *
     * @param items a collection of items
     * @return the sum of the items
     */
    public static Object sum(Collection items) {
        return DefaultGroovyMethods.sum(items);
    }

}
