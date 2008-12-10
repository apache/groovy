/*
 * Copyright 2003-2008 the original author or authors.
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
package org.codehaus.groovy.runtime;

import groovy.lang.IntRange;
import groovy.lang.EmptyRange;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Support methods for DefaultGroovyMethods and PluginDefaultMethods.
 */
public class DefaultGroovyMethodsSupport {

    // helper method for getAt and putAt
    protected static RangeInfo subListBorders(int size, IntRange range) {
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), size);
        int to = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getTo()), size);
        boolean reverse = range.isReverse();
        if (from > to) {
            // support list[1..-1]
            int tmp = to;
            to = from;
            from = tmp;
            reverse = !reverse;
        }
        return new RangeInfo(from, to + 1, reverse);
    }

    // helper method for getAt and putAt
    protected static RangeInfo subListBorders(int size, EmptyRange range) {
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), size);
        return new RangeInfo(from, from, false);
    }

    /**
     * This converts a possibly negative index to a real index into the array.
     *
     * @param i    the unnormalised index
     * @param size the array size
     * @return the normalised index
     */
    protected static int normaliseIndex(int i, int size) {
        int temp = i;
        if (i < 0) {
            i += size;
        }
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative array index [" + temp + "] too large for array size " + size);
        }
        return i;
    }

    protected static class RangeInfo {
        public int from, to;
        public boolean reverse;

        public RangeInfo(int from, int to, boolean reverse) {
            this.from = from;
            this.to = to;
            this.reverse = reverse;
        }
    }

    protected static Collection cloneSimilarCollection(Collection orig, int newCapacity) {
        Collection answer = (Collection) cloneObject(orig);
        if (answer != null) return answer;
        answer = cloneCollectionFromClass(orig);
        if (answer != null) return answer;

        // fall back to creation
        answer = createSimilarCollection(orig, newCapacity);
        answer.addAll(orig);
        return answer;
    }

    private static Object cloneObject(Object orig) {
        if (orig instanceof Cloneable) {
            try {
                return InvokerHelper.invokeMethod(orig, "clone", new Object[0]);
            } catch (Exception ex) {
                // ignore
            }
        }
        return null;
    }

    protected static Collection createSimilarOrDefaultCollection(Object object) {
        if (object instanceof Collection) {
            return createSimilarCollection((Collection) object);
        }
        return new ArrayList();
    }

    protected static Collection createSimilarCollection(Collection collection) {
        return createSimilarCollection(collection, collection.size());
    }

    protected static Collection createSimilarCollection(Collection orig, int newCapacity) {
        if (orig instanceof Set) {
            return createSimilarSet((Set) orig);
        }
        if (orig instanceof List) {
            return createSimilarList((List) orig, newCapacity);
        }
        Collection answer = createCollectionFromClass(orig);
        if (answer != null) return answer;

        if (orig instanceof Queue) {
            return new LinkedList();
        }
        return new ArrayList(newCapacity);
    }

    protected static List createSimilarList(List orig, int newCapacity) {
        List answer = (List) createCollectionFromClass(orig);
        if (answer != null) return answer;

        if (orig instanceof LinkedList) {
            answer = new LinkedList();
        } else if (orig instanceof Stack) {
            answer = new Stack();
        } else if (orig instanceof Vector) {
            answer = new Vector();
        } else {
            answer = new ArrayList(newCapacity);
        }
        return answer;
    }

    protected static Set createSimilarSet(Set orig) {
        Set answer = (Set) createCollectionFromClass(orig);
        if (answer != null) return answer;

        // fall back to some defaults
        if (orig instanceof SortedSet) {
            return new TreeSet();
        }
        if (orig instanceof LinkedHashSet) {
            return new LinkedHashSet();
        }
        return new HashSet();
    }

    protected static Map createSimilarMap(Map orig) {
        Map answer = createMapFromClass(orig);
        if (answer != null) return answer;

        // fall back to some defaults
        if (orig instanceof SortedMap) {
            return new TreeMap();
        }
        if (orig instanceof LinkedHashMap) {
            return new LinkedHashMap();
        }
        if (orig instanceof Properties) {
            return new Properties();
        }
        if (orig instanceof Hashtable) {
            return new Hashtable();
        }
        return new HashMap();
    }

    private static Collection createCollectionFromClass(Collection orig) {
        try {
            final Constructor constructor = orig.getClass().getConstructor();
            return (Collection) constructor.newInstance();
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static Collection cloneCollectionFromClass(Collection orig) {
        try {
            final Constructor constructor = orig.getClass().getConstructor(Collection.class);
            return (Collection) constructor.newInstance(orig);
        } catch (Exception e) {
            // ignore
        }
        try {
            final Constructor constructor = orig.getClass().getConstructor();
            final Collection result = (Collection) constructor.newInstance();
            result.addAll(orig);
            return result;
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static Map createMapFromClass(Map orig) {
        try {
            final Constructor constructor = orig.getClass().getConstructor();
            return (Map) constructor.newInstance();
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static Map cloneMapFromClass(Map orig) {
        try {
            final Constructor constructor = orig.getClass().getConstructor(Map.class);
            return (Map) constructor.newInstance(orig);
        } catch (Exception e) {
            // ignore
        }
        try {
            final Constructor constructor = orig.getClass().getConstructor();
            final Map result = (Map) constructor.newInstance();
            result.putAll(orig);
            return result;
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    protected static Map cloneSimilarMap(Map orig) {
        Map answer = (Map) cloneObject(orig);
        if (answer != null) return answer;
        answer = cloneMapFromClass(orig);
        if (answer != null) return answer;

        // fall back to some defaults
        if (orig instanceof TreeMap)
            return new TreeMap(orig);

        if (orig instanceof LinkedHashMap)
            return new LinkedHashMap(orig);

        if (orig instanceof Properties) {
            Map map = new Properties();
            map.putAll(orig);
            return map;
        }

        if (orig instanceof Hashtable)
            return new Hashtable(orig);

        return new HashMap(orig);
    }

    /**
     * Determines if all items of this array are of the same type.
     *
     * @param cols an array of collections
     * @return true if the collections are all of the same type
     */
    protected static boolean sameType(Collection[] cols) {
        List all = new LinkedList();
        for (int i = 0; i < cols.length; i++) {
            all.addAll(cols[i]);
        }
        if (all.size() == 0)
            return true;

        Object first = all.get(0);

        //trying to determine the base class of the collections
        //special case for Numbers
        Class baseClass;
        if (first instanceof Number) {
            baseClass = Number.class;
        } else if (first == null) {
            baseClass = NullObject.class;
        } else {
            baseClass = first.getClass();
        }

        for (int i = 0; i < cols.length; i++) {
            for (Iterator iter = cols[i].iterator(); iter.hasNext();) {
                if (!baseClass.isInstance(iter.next())) {
                    return false;
                }
            }
        }
        return true;
    }
}
