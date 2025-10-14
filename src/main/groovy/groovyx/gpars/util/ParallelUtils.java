// GPars - Groovy Parallel Systems
//
// Copyright © 2008–2011, 2014  The original author or authors
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

package groovyx.gpars.util;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Methods moved from PAUtils and PAGroovyUtils.
 *
 * @author Original authors from respective classes.
 *
 */

@SuppressWarnings({"UtilityClass", "AbstractClassWithoutAbstractMethods", "AbstractClassNeverImplemented", "StaticMethodUsedInOneClass"})
public abstract class ParallelUtils {
    public static <T> Collection<T> createCollection(final Iterable<T> object) {
	final Collection<T> collection = new ArrayList<T>();
	for (final T item : object) {
	    collection.add(item);
	}
	return collection;
    }

    public static <T> Collection<T> createCollection(final Iterator<T> iterator) {
	final Collection<T> collection = new ArrayList<T>();
	while (iterator.hasNext()) {
	    collection.add(iterator.next());
	}
	return collection;
    }

   /**
    * If the passed-in closure expects two arguments, it is considered to be a map-iterative code and is then wrapped
    * with a single-argument closure, which unwraps the key:value pairs for the original closure.
    * If the supplied closure doesn't expect two arguments, it is returned unchanged.
    *
    * @param cl The closure to use for parallel methods
    * @return The original or an unwrapping closure
    */
   public static <T> Closure<T> buildClosureForMaps(final Closure<T> cl) {
       if (cl.getMaximumNumberOfParameters() == 2) return new Closure<T>(cl.getOwner()) {
           private static final long serialVersionUID = -7502769124461342939L;

           @Override
           public T call(final Object arguments) {
               @SuppressWarnings({"unchecked"})
               final Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) arguments;
               return cl.call(entry.getKey(), entry.getValue());
           }

           @Override
           public T call(final Object[] args) {
               return this.call(args[0]);
           }
       };
       return cl;
   }

   /**
    * If the passed-in closure expects three arguments, it is considered to be a map-iterative_with_index code and is then wrapped
    * with a two-argument closure, which unwraps the key:value pairs for the original closure.
    * If the supplied closure doesn't expect three arguments, it is returned unchanged.
    *
    * @param cl The closure to use for parallel methods
    * @return The original or an unwrapping closure
    */
   public static <T> Closure<T> buildClosureForMapsWithIndex(final Closure<T> cl) {
       if (cl.getMaximumNumberOfParameters() == 3) return new Closure<T>(cl.getOwner()) {
           private static final long serialVersionUID = 4777456744250574403L;

           @SuppressWarnings({"rawtypes", "RawUseOfParameterizedType"})
           @Override
           public Class[] getParameterTypes() {
               return new Class[]{Map.Entry.class, Integer.class};
           }

           @Override
           public int getMaximumNumberOfParameters() {
               return 2;
           }

           @Override
           public T call(final Object[] args) {
               @SuppressWarnings({"unchecked"})
               final Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) args[0];
               final Integer index = (Integer) args[1];
               return cl.call(entry.getKey(), entry.getValue(), index);
           }
       };

       return cl;
   }

    /**
     * Builds a resulting map out of an map entry collection
     *
     * @param result The collection containing map entries
     * @return A corresponding map instance
     */
    public static <K, V> Map<K, V> buildResultMap(final Collection<Map.Entry<K, V>> result) {
        final Map<K, V> map = new HashMap<K, V>(result.size());
        for (final Map.Entry<K, V> item : result) {
            map.put(item.getKey(), item.getValue());
        }
        return map;
    }

}
