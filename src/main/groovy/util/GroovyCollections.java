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

import java.util.*;

/**
 * A Collections utility class
 *
 * @author Paul King
 */
public class GroovyCollections {
    /**
     * Finds all combinations of items from a collection of lists.
     *
     * @param lists an array of lists
     * @return a List of the combinations found
     */
    public static List combinations(Object[] lists) {
        return combinations(Arrays.asList(lists));
    }

    /**
     * Finds all combinations of items from a collection of lists.
     *
     * @param lists a Collection of lists
     * @return a List of the combinations found
     */
    public static List combinations(Collection lists) {
        List combinations = new ArrayList();
        for (Iterator outer = lists.iterator(); outer.hasNext();) {
            Object candidateList = outer.next();
            List list = (List) DefaultTypeTransformation.castToType(candidateList, List.class);
            if (combinations.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    List l = new ArrayList();
                    l.add(list.get(i));
                    combinations.add(l);
                }
            } else {
                List savedCombinations = new ArrayList(combinations);
                List newCombinations = new ArrayList();
                for (Iterator inner = list.iterator(); inner.hasNext();) {
                    Object value = inner.next();
                    for (Iterator combos = savedCombinations.iterator(); combos.hasNext();) {
                        List oldlist = new ArrayList((List) combos.next());
                        oldlist.add(value);
                        newCombinations.add(oldlist);
                    }
                }
                combinations = newCombinations;
            }
        }
        return combinations;
    }

    /**
     * Transposes a collection of lists. So,
     * transpose([['a', 'b'], [1, 2]] as Object[]) == [['a', 1], ['b', 2]].
     *
     * @param lists an array of lists
     * @return a List of the transposed lists
     */
    public static List transpose(Object[] lists) {
        return transpose(Arrays.asList(lists));
    }

    /**
     * Transposes a collection of lists. So,
     * transpose([['a', 'b'], [1, 2]]) == [['a', 1], ['b', 2]].
     *
     * @param lists a Collection of lists
     * @return a List of the transposed lists
     */
    public static List transpose(Collection lists) {
        List result = new ArrayList();
        if (lists.isEmpty() || lists.size() == 0) return result;
        int minSize = Integer.MAX_VALUE;
        for (Iterator outer = lists.iterator(); outer.hasNext();) {
            Object candidateList = outer.next();
            List list = (List) DefaultTypeTransformation.castToType(candidateList, List.class);
            if (list.size() < minSize) minSize = list.size();
        }
        if (minSize == 0) return result;
        for (int i = 0; i < minSize; i++) {
            result.add(new ArrayList());
        }
        for (Iterator outer = lists.iterator(); outer.hasNext();) {
            Object candidateList = outer.next();
            List list = (List) DefaultTypeTransformation.castToType(candidateList, List.class);
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

}
