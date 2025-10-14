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

// package groovyx.gpars.pa;

// import groovy.lang.Closure;
// import org.codehaus.groovy.runtime.DefaultGroovyMethods;

// import java.util.Iterator;
// import java.util.Map;

// /**
//  * Holds a temporary reduce result for groupBy
//  */
// @SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter", "ReturnOfCollectionOrArrayField"})
// final class CombineHolder {

//     private final Map<Object, Object> content;

//     CombineHolder(final Map<Object, Object> content) {
//         this.content = content;
//     }

//     public Map<Object, Object> getContent() {
//         return content;
//     }

//     @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
//     CombineHolder merge(final CombineHolder other, final Closure<?> accumulation, final Closure<?> initialValue) {
//         for (final Map.Entry<Object, Object> item : other.content.entrySet()) {
//             final Iterator<?> valuesToIterateOver = DefaultGroovyMethods.iterator(item.getValue());
//             while (valuesToIterateOver.hasNext()) {
//                 final Object next = valuesToIterateOver.next();
//                 addToMap(item.getKey(), next, accumulation, initialValue);
//             }
//         }
//         return this;
//     }

//     CombineHolder addToMap(final Object key, final Object item, final Closure<?> accumulation, final Closure<?> initialValue) {
//         final Object v = content.get(key);
//         final Object currentValue = v != null ? v : initialValue.call();
//         final Object newValue = accumulation.call(currentValue, item);
//         content.put(key, newValue);
//         return this;
//     }
// }
