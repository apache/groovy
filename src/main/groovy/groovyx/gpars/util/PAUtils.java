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

//package groovyx.gpars.util;
//
//import groovy.lang.Closure;
//import groovyx.gpars.ReactorMessagingRunnable;
//import groovyx.gpars.dataflow.DataflowVariable;
//import groovyx.gpars.scheduler.Pool;
//import org.codehaus.groovy.runtime.InvokerInvocationException;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentMap;
//
///**
// * Handy methods build PA from different types
// *
// * @author Vaclav Pech
// *         Date: 24th Oct 2010
// */
//@SuppressWarnings({"UtilityClass", "AbstractClassWithoutAbstractMethods", "AbstractClassNeverImplemented", "StaticMethodOnlyUsedInOneClass"})
//public abstract class PAUtils {
//    public static <T> Collection<T> createCollection(final Iterable<T> object) {
//        final Collection<T> collection = new ArrayList<T>();
//        for (final T item : object) {
//            collection.add(item);
//        }
//        return collection;
//    }
//
//    public static <T> Collection<T> createCollection(final Iterator<T> iterator) {
//        final Collection<T> collection = new ArrayList<T>();
//        while (iterator.hasNext()) {
//            collection.add(iterator.next());
//        }
//        return collection;
//    }
//
//    public static String[] createArray(final CharSequence value) {
//        final String[] chars = new String[value.length()];
//        for (int i = 0; i < value.length(); i++) {
//            chars[i] = String.valueOf(value.charAt(i));
//        }
//        return chars;
//    }
//
//    public static <K, V> Map.Entry<K, V>[] createArray(final Map<K, V> map) {
//        @SuppressWarnings({"unchecked"})
//        final Map.Entry<K, V>[] result = new Map.Entry[map.size()];
//        int i = 0;
//        for (final Map.Entry<K, V> entry : map.entrySet()) {
//            result[i] = entry;
//            i++;
//        }
//        return result;
//    }
//
//    /**
//     * If the passed-in closure expects two arguments, it is considered to be a map-iterative code and is then wrapped
//     * with a single-argument closure, which unwraps the key:value pairs for the original closure.
//     * If the supplied closure doesn't expect two arguments, it is returned unchanged.
//     *
//     * @param cl The closure to use for parallel methods
//     * @return The original or an unwrapping closure
//     */
//    public static <T> Closure<T> buildClosureForMaps(final Closure<T> cl) {
//        if (cl.getMaximumNumberOfParameters() == 2) return new Closure<T>(cl.getOwner()) {
//            private static final long serialVersionUID = -7502769124461342939L;
//
//            @Override
//            public T call(final Object arguments) {
//                @SuppressWarnings({"unchecked"})
//                final Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) arguments;
//                return cl.call(entry.getKey(), entry.getValue());
//            }
//
//            @Override
//            public T call(final Object[] args) {
//                return this.call(args[0]);
//            }
//        };
//        return cl;
//    }
//
//    /**
//     * If the passed-in closure expects three arguments, it is considered to be a map-iterative_with_index code and is then wrapped
//     * with a two-argument closure, which unwraps the key:value pairs for the original closure.
//     * If the supplied closure doesn't expect three arguments, it is returned unchanged.
//     *
//     * @param cl The closure to use for parallel methods
//     * @return The original or an unwrapping closure
//     */
//    public static <T> Closure<T> buildClosureForMapsWithIndex(final Closure<T> cl) {
//        if (cl.getMaximumNumberOfParameters() == 3) return new Closure<T>(cl.getOwner()) {
//            private static final long serialVersionUID = 4777456744250574403L;
//
//            @SuppressWarnings({"rawtypes", "RawUseOfParameterizedType"})
//            @Override
//            public Class[] getParameterTypes() {
//                return new Class[]{Map.Entry.class, Integer.class};
//            }
//
//            @Override
//            public int getMaximumNumberOfParameters() {
//                return 2;
//            }
//
//            @Override
//            public T call(final Object[] args) {
//                @SuppressWarnings({"unchecked"})
//                final Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) args[0];
//                final Integer index = (Integer) args[1];
//                return cl.call(entry.getKey(), entry.getValue(), index);
//            }
//        };
//
//        return cl;
//    }
//
//    /**
//     * Builds a resulting map out of an map entry collection
//     *
//     * @param result The collection containing map entries
//     * @return A corresponding map instance
//     */
//    public static <K, V> Map<K, V> buildResultMap(final Collection<Map.Entry<K, V>> result) {
//        final Map<K, V> map = new HashMap<K, V>(result.size());
//        for (final Map.Entry<K, V> item : result) {
//            map.put(item.getKey(), item.getValue());
//        }
//        return map;
//    }
//
//
//    /**
//     * Builds a comparator depending on the number of arguments accepted by the supplied closure.
//     *
//     * @param handler The one or two argument closure to build a comparator on
//     * @return A new Comparator to use
//     */
//    public static Comparator<Object> createComparator(final Closure<Object> handler) {
//        if (handler.getMaximumNumberOfParameters() == 2) return new Comparator<Object>() {
//            @Override
//            public int compare(final Object o1, final Object o2) {
//                return (Integer) handler.call(o1, o2);
//            }
//        };
//        else return new Comparator<Object>() {
//            @SuppressWarnings({"unchecked"})
//            @Override
//            public int compare(final Object o1, final Object o2) {
//                return ((Comparable<Object>) handler.call(o1)).compareTo(handler.call(o2));
//            }
//        };
//    }
//
//    /**
//     * Creates a closure that will insert elements in the appropriate group
//     *
//     * @param cl  The distinction closure
//     * @param map The map of groups to contribute to
//     * @return null
//     */
//    public static <K, T> Closure<Object> createGroupByClosure(final Closure<K> cl, final ConcurrentMap<K, List<T>> map) {
//        return new Closure<Object>(cl.getOwner(), cl.getDelegate()) {
//            private static final long serialVersionUID = 5495474569312257163L;
//
//            @Override
//            @SuppressWarnings({"unchecked"})
//            public Object call(final Object arguments) {
//                final K result = cl.call(arguments);
//                final List<T> localList = new ArrayList<T>();
//                localList.add((T) arguments);
//                final List<T> myList = Collections.synchronizedList(localList);
//                final Collection<T> list = map.putIfAbsent(result, myList);
//                if (list != null) list.add((T) arguments);
//                return null;
//            }
//        };
//    }
//
//    /**
//     * Performs a single step in the evaluation of parameters passed into an asynchronous function
//     *
//     * @param pool             The thread pool to use
//     * @param args             The list of original arguments
//     * @param current          The index of the current argument to evaluate
//     * @param soFarArgs        A list of arguments evaluated so far
//     * @param result           The DFV expecting the function result to be bound to once calculated
//     * @param original         The original non-asynchronous function to invoke once all arguments are available
//     * @param pooledThreadFlag Indicates, whether we now run in a pooled thread so we don't have to schedule the original function invocation, once all arguments have been bound
//     */
//    @SuppressWarnings({"unchecked"})
//    public static <T> void evaluateArguments(final Pool pool, final Object[] args, final int current, final List<Object> soFarArgs,
//                                             final DataflowVariable<Object> result, final Closure<T> original, final boolean pooledThreadFlag) {
//        if (current == args.length) {
//            if (pooledThreadFlag) {
//                try {
//                    final Object call = original.call(soFarArgs.toArray(new Object[soFarArgs.size()]));
//                    result.leftShift(call);
//                } catch (InvokerInvocationException e) {
//                    result.bind(e.getCause());
//                } catch (Exception all) {
//                    result.bind(all);
//                } catch (Error error) {
//                    result.bind(error);
//                    throw error;
//                }
//            } else {
//                pool.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            result.leftShift(original.call(soFarArgs.toArray(new Object[soFarArgs.size()])));
//                        } catch (InvokerInvocationException e) {
//                            result.bind(e.getCause());
//                        } catch (Exception all) {
//                            result.bind(all);
//                        } catch (Error error) {
//                            result.bind(error);
//                            throw error;
//                        }
//                    }
//                });
//            }
//        } else {
//            final Object currentArgument = args[current];
//            if (currentArgument instanceof DataflowVariable) {
//                final DataflowVariable<Object> variable = (DataflowVariable<Object>) currentArgument;
//                if (variable.isBound()) {
//                    Object currentValue = null;
//                    try {
//                        currentValue = variable.getVal();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException("Interrupted while processing arguments", e);
//                    }
//                    if (currentValue instanceof Throwable) result.leftShift(currentValue);
//                    else {
//                        soFarArgs.add(currentValue);
//                        evaluateArguments(pool, args, current + 1, soFarArgs, result, original, pooledThreadFlag);
//                    }
//                } else {
//                    variable.whenBound(pool, new ReactorMessagingRunnable() {
//                        @Override
//                        protected Object doRun(final Object argument) {
//                            if (argument instanceof Throwable) result.leftShift(argument);
//                            else {
//                                soFarArgs.add(argument);
//                                evaluateArguments(pool, args, current + 1, soFarArgs, result, original, true);
//                            }
//                            return null;
//                        }
//                    });
//                }
//            } else {
//                soFarArgs.add(currentArgument);
//                evaluateArguments(pool, args, current + 1, soFarArgs, result, original, pooledThreadFlag);
//            }
//        }
//    }
//}
