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

import java.util.*;

/**
 * A Collections utility class
 *
 * @author Paul King
 */
public class Collections {
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
     * [['a', 'b'], [1, 2]].transpose() == [['a', 1], ['b', 2]].
     *
     * @param lists an array of lists
     * @return a List of the transposed lists
     */
    public static List transpose(Object[] lists) {
        return transpose(Arrays.asList(lists));
    }

    /**
     * Transposes a collection of lists. So,
     * [['a', 'b'], [1, 2]].transpose() == [['a', 1], ['b', 2]].
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

}
