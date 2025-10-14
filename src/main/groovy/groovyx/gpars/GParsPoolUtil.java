// GPars - Groovy Parallel Systems
//
// Copyright © 2008--2011, 2013, 2014, 2017  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.time.Duration;
import groovyx.gpars.forkjoin.CallAsyncTask;
import groovyx.gpars.forkjoin.GParsPoolUtilHelper;
import groovyx.gpars.pa.PAWrapper;
import groovyx.gpars.scheduler.FJPool;
import groovyx.gpars.util.GeneralTimer;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// TODO: delete
//import groovyx.gpars.pa.CallClosure;
//import groovyx.gpars.pa.ClosureMapper;
//import groovyx.gpars.pa.ClosureNegationPredicate;
//import groovyx.gpars.pa.ClosurePredicate;
//import groovyx.gpars.pa.ClosureReducer;
//import groovyx.gpars.pa.PAWrapper;
//import groovyx.gpars.pa.SumClosure;
// TODO: delete
//import groovyx.gpars.util.PAUtils;
// TODO: delete
//import static groovyx.gpars.util.PAGroovyUtils.createCollection;
//import static groovyx.gpars.util.PAUtils.buildClosureForMaps;
//import static groovyx.gpars.util.PAUtils.buildClosureForMapsWithIndex;
//import static groovyx.gpars.util.PAUtils.buildResultMap;
//import static groovyx.gpars.util.PAUtils.createComparator;
//import static groovyx.gpars.util.PAUtils.createGroupByClosure;

/**
 * This class forms the core of the DSL initialized by {@code GParsPool}. The static methods of {@code GParsPoolUtil}
 * get attached to their first arguments (the Groovy Category mechanism) and can be then invoked as if they were part of
 * the argument classes.
 *
 * @author Vaclav Pech
 * @author Robert Fischer
 * @author Russel Winder
 * @author Szymon Stepniak
 *
 * @see groovyx.gpars.GParsPool
 */
public class GParsPoolUtil {

    /**
     * Allows timeouts for async operations
     */
    private static final GeneralTimer timer = GParsConfig.retrieveDefaultTimer("GParsTimeoutTimer", true);

    private static ForkJoinPool retrievePool() {
        final ForkJoinPool pool = (ForkJoinPool) GParsPool.retrieveCurrentPool();
        if (pool == null) throw new IllegalStateException("No ForkJoinPool available for the current thread");
        return pool;
    }

    /**
     * schedules the supplied closure for processing in the underlying thread pool.
     */
    public static <T> Future<T> callParallel(final Closure<T> task) {
        final ForkJoinPool pool = (ForkJoinPool) GParsPool.retrieveCurrentPool();
        if (pool == null) throw new IllegalStateException("No ForkJoinPool available for the current thread.");
        return pool.submit(new CallAsyncTask<T>(task));
    }

    /**
     * Calls a closure in a separate thread supplying the given arguments, returning a future for the potential return value.
     */
    public static <T> Future<T> callAsync(final Closure<T> cl, final Object... args) {
        return GParsPoolUtilHelper.callAsync(cl, args);
    }

    /**
     * Calls a closure in a separate thread supplying the given arguments, returning a future for the potential return value.
     * Also allows the asynchronous calculation to be cancelled after a given timeout.
     * In order to allow cancellation, the asynchronously running code must keep checking the _interrupted_ flag of its
     * own thread and cease the calculation once the flag is set to true.
     *
     * @param timeout The timeout in milliseconds to wait before the calculation gets cancelled.
     */
    public static <T> Future<T> callTimeoutAsync(final Closure<T> cl, final long timeout, final Object... args) {
        final Future<T> f = callAsync(cl, args);
        timer.schedule(() -> f.cancel(true), timeout);
        return f;
    }

    /**
     * Calls a closure in a separate thread supplying the given arguments, returning a future for the potential return value.
     * Also allows the asynchronous calculation to be cancelled after a given timeout.
     * In order to allow cancellation, the asynchronously running code must keep checking the _interrupted_ flag of its
     * own thread and cease the calculation once the flag is set to true.
     *
     * @param timeout The timeout to wait before the calculation gets cancelled.
     */
    public static <T> Future<T> callTimeoutAsync(final Closure<T> cl, final Duration timeout, final Object... args) {
        return callTimeoutAsync(cl, timeout.toMilliseconds(), args);
    }

    /**
     * Submits the task for asynchronous processing returning the Future received from the executor service.
     * Allows for the following syntax:
     * <pre>
     * executorService &lt;&lt; {println 'Inside parallel task'}
     * </pre>
     */
    public static <T> Future<T> leftShift(final ForkJoinPool pool, final Closure<T> task) {
        return pool.submit(new RecursiveTask<T>() {
            @Override
            protected T compute() {
                return task.call();
            }
        });
    }

    /**
     * Creates an asynchronous variant of the supplied closure, which, when invoked returns a future for the potential return value
     */
    public static Closure async(final Closure cl) {
        return GParsPoolUtilHelper.async(cl);
    }

    /**
     * Creates an asynchronous and composable variant of the supplied closure, which, when invoked returns a DataflowVariable for the potential return value
     */
    public static Closure asyncFun(final Closure original) {
        return asyncFun(original, false);
    }

    /**
     * Creates an asynchronous and composable variant of the supplied closure, which, when invoked returns a DataflowVariable for the potential return value
     */
    public static Closure asyncFun(final Closure original, final boolean blocking) {
        return GParsPoolUtilHelper.asyncFun(original, blocking);
    }

    /**
     * Creates an asynchronous and composable variant of the supplied closure, which, when invoked returns a DataflowVariable for the potential return value
     */
    public static Closure asyncFun(final Closure original, final FJPool pool) {
        return asyncFun(original, pool, false);
    }

    /**
     * Creates an asynchronous and composable variant of the supplied closure, which, when invoked returns a DataflowVariable for the potential return value
     */
    public static Closure asyncFun(final Closure original, final FJPool pool, final boolean blocking) {
        return GParsPoolUtilHelper.asyncFun(original, blocking, pool);
    }

//    private static <K, V> ParallelArray<Map.Entry<K, V>> createPA(final Map<K, V> collection, final ForkJoinPool pool) {
//        return GParsPoolUtilHelper.createPAFromArray(PAUtils.createArray(collection), pool);
//    }

    /**
     * Overrides the iterative methods like each(), collect() and such, so that they call their parallel variants from the GParsPoolUtil class
     * like eachParallel(), collectParallel() and such.
     * The first time it is invoked on a collection the method creates a TransparentParallel class instance and mixes it
     * in the object it is invoked on. After mixing-in, the isConcurrent() method will return true.
     * Delegates to GParsPoolUtil.makeConcurrent().
     *
     * @param collection The object to make transparent
     * @return The instance of the TransparentParallel class wrapping the original object and overriding the iterative methods with new parallel behavior
     */
    public static Object makeConcurrent(final Object collection) {
        return GParsPoolUtilHelper.makeConcurrent(collection);
    }

    /**
     * Gives the iterative methods like each() or find() the original sequential semantics.
     *
     * @param collection The collection to apply the change to
     * @return The collection itself
     */
    public static Object makeSequential(final Object collection) {
        return GParsPoolUtilHelper.makeSequential(collection);
    }

    /**
     * Makes the collection concurrent for the passed-in block of code.
     * The iterative methods like each or collect are given concurrent semantics inside the passed-in closure.
     * Once the closure finishes, the original sequential semantics of the methods is restored.
     * Must be invoked inside a withPool block.
     *
     * @param collection The collection to enhance
     * @param code       The closure to run with the collection enhanced.
     */
    public static void asConcurrent(final Object collection, final Closure code) {
        makeConcurrent(collection);
        try {
            code.call(collection);
        } finally {
            makeSequential(collection);
        }
    }

    /**
     * Indicates whether the iterative methods like each() or collect() work have been altered to work concurrently.
     */
    public static boolean isConcurrent(final Object collection) {
        return false;
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * closure as the transformation operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code eachParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = new ConcurrentSkipListSet()
     *     [1, 2, 3, 4, 5].eachParallel {Number number -&gt; result.add(number * 10)}
     *     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     * }
     * </pre>
     * <p>
     * Note that the {@code result} variable is synchronized to prevent race conditions between multiple threads.
     * </p>
     */
    public static <T> Collection<T> eachParallel(final Collection<T> collection, final Closure<?> cl) throws ExecutionException, InterruptedException {
        //GParsPoolUtilHelper.eachParallelPA(GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()), cl);
        final ForkJoinPool pool = retrievePool();
        final List<ForkJoinTask> tasks = new LinkedList<>();
        collection.stream()
                .forEach(it -> tasks.add(pool.submit(() -> callClosure(cl, it))));
        tasks.forEach(ForkJoinTask::join);
        return collection;
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * closure as the transformation operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code eachParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = new ConcurrentSkipListSet()
     *     [1, 2, 3, 4, 5].eachParallel {Number number -&gt; result.add(number * 10)}
     *     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     * }
     * </pre>
     * <p>
     * Note that the {@code result} variable is synchronized to prevent race conditions between multiple threads.
     * </p>
     */
    public static <T> Collection<T> eachParallel(final T collection, final Closure cl) throws ExecutionException, InterruptedException {
        return eachParallel(toCollection(collection), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied map and invokes the withMapping() method using the supplied
     * closure as the transformation operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code eachParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = new ConcurrentSkipListSet()
     *     [1, 2, 3, 4, 5].eachParallel {Number number -&gt; result.add(number * 10)}
     *     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     * }
     * </pre>
     * <p>
     * Note that the {@code result} variable is synchronized to prevent race conditions between multiple threads.
     * </p>
     */
    public static <K, V> Map<K, V> eachParallel(final Map<K, V> collection, final Closure<V> cl) throws InterruptedException, ExecutionException {
        eachParallel(collection.entrySet(), cl);
        return collection;
    }


    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * closure as the transformation operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code eachWithIndexParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = new ConcurrentSkipListSet()
     *     [1, 2, 3, 4, 5].eachWithIndexParallel {Number number, int index -&gt; result.add(number * 10)}
     *     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     * }
     * </pre>
     * <p>
     * Note that the {@code result} variable is synchronized to prevent race conditions between multiple threads.
     * </p>
     */
    public static <T> Collection<T> eachWithIndexParallel(final Collection<T> collection, final Closure cl) {
        final AtomicInteger counter = new AtomicInteger(0);
        final List<ForkJoinTask> tasks = new LinkedList<>();
        final ForkJoinPool pool = retrievePool();

        collection.parallelStream()
                .forEachOrdered(it -> {
                    int index = counter.getAndIncrement();
                    tasks.add(pool.submit(() -> callIndexedClosure(cl, it, index)));
                });
        tasks.forEach(ForkJoinTask::join);
        return collection;
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * closure as the transformation operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code eachWithIndexParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = new ConcurrentSkipListSet()
     *     [1, 2, 3, 4, 5].eachWithIndexParallel {Number number, int index -&gt; result.add(number * 10)}
     *     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     * }
     * </pre>
     * <p>
     * Note that the {@code result} variable is synchronized to prevent race conditions between multiple threads.
     * </p>
     */
    public static <T> Collection<T> eachWithIndexParallel(final T collection, final Closure cl) throws ExecutionException, InterruptedException {
        //eachWithIndexParallel(createCollection(collection), cl);
        return eachWithIndexParallel(toCollection(collection), cl);
    }

    /**
     * Does parallel eachWithIndex on maps
     */
    public static <K, V> Map<K, V> eachWithIndexParallel(final Map<K, V> collection, final Closure cl) throws ExecutionException, InterruptedException {
        //eachWithIndexParallel(createCollection(collection), buildClosureForMapsWithIndex(cl));
        eachWithIndexParallel(collection.entrySet(), cl);
        return collection;
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * closure as the transformation operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code collectParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].collectParallel {Number number -&gt; number * 10}
     *     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     * }
     * </pre>
     */
    public static <T> Collection<T> collectParallel(final Collection<? extends T> collection, final Closure<? extends T> cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).withMapping(new ClosureMapper(new CallClosure(cl))).all().asList();
        return (Collection<T>) retrievePool().submit(() ->
                collection.parallelStream()
                        .map(it -> callClosure(cl, it))
                        .collect(Collectors.toList())
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * closure as the transformation operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code collectParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].collectParallel {Number number -&gt; number * 10}
     *     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     * }
     * </pre>
     */
    public static <T> Collection<T> collectParallel(final Object collection, final Closure<? extends T> cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPA(collection, retrievePool()).withMapping(new ClosureMapper(new CallClosure(cl))).all().asList();
        return collectParallel(toCollection(collection), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied map and invokes the withMapping() method using the supplied
     * closure as the transformation operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code collectParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].collectParallel {Number number -&gt; number * 10}
     *     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     * }
     * </pre>
     */
    public static <T> Collection<T> collectParallel(final Map collection, final Closure<? extends T> cl) throws ExecutionException, InterruptedException {
        //return createPA(collection, retrievePool()).withMapping(new ClosureMapper(buildClosureForMaps(cl))).all().asList();
        return collectParallel(collection.entrySet(), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * <code>projection</code> closure as the transformation operation. The <code>projection</code> closure should return a
     * (possibly empty) collection of items which are subsequently flattened to produce a new collection.
     * The <code>projection</code> closure will be effectively invoked concurrently on the elements of the original collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code collectManyParallel(Closure projection)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def squaresAndCubesOfOdds = [1, 2, 3, 4, 5].collectManyParallel { Number number -&gt;
     *         number % 2 ? [number ** 2, number ** 3] : []
     *     }
     *     assert squaresAndCubesOfOdds == [1, 1, 9, 27, 25, 125]
     * }
     * </pre>
     */
    public static <T> List<T> collectManyParallel(final Collection collection, final Closure<Collection<? extends T>> projection) throws ExecutionException, InterruptedException {
        //return (List<T>) GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).withMapping(new ClosureMapper(new CallClosure(projection))).reduce(new ClosureReducer(SumClosure.getInstance()), null);
        return (List<T>) retrievePool().submit(() ->
                collection.parallelStream()
                        .map(it -> projection.call(it))
                        .flatMap(it -> ((Collection) it).stream())
                        .collect(Collectors.toList())
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * <code>projection</code> closure as the transformation operation. The <code>projection</code> closure should return a
     * (possibly empty) collection of items which are subsequently flattened to produce a new collection.
     * The <code>projection</code> closure will be effectively invoked concurrently on the elements of the original collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code collectManyParallel(Closure projection)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def squaresAndCubesOfOdds = [1, 2, 3, 4, 5].collectManyParallel { Number number -&gt;
     *         number % 2 ? [number ** 2, number ** 3] : []
     *     }
     *     assert squaresAndCubesOfOdds == [1, 1, 9, 27, 25, 125]
     * }
     * </pre>
     */
    public static <T> List<T> collectManyParallel(final Object collection, final Closure<Collection<? extends T>> projection) throws ExecutionException, InterruptedException {
        //return (List<T>) GParsPoolUtilHelper.createPA(collection, retrievePool()).withMapping(new ClosureMapper(new CallClosure(projection))).reduce(new ClosureReducer(SumClosure.getInstance()), null);
        return collectManyParallel(toCollection(collection), projection);
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * <code>projection</code> closure as the transformation operation. The <code>projection</code> closure should return a
     * (possibly empty) collection of items which are subsequently flattened to produce a new collection.
     * The <code>projection</code> closure will be effectively invoked concurrently on the elements of the original collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code collectManyParallel(Closure projection)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def squaresAndCubesOfOdds = [1, 2, 3, 4, 5].collectManyParallel { Number number -&gt;
     *         number % 2 ? [number ** 2, number ** 3] : []
     *     }
     *     assert squaresAndCubesOfOdds == [1, 1, 9, 27, 25, 125]
     * }
     * </pre>
     */
    public static <T> List<T> collectManyParallel(final Map collection, final Closure<Collection<? extends T>> projection) throws ExecutionException, InterruptedException {
        //return (List<T>) createPA(collection, retrievePool()).withMapping(new ClosureMapper(buildClosureForMaps(projection))).reduce(new ClosureReducer(SumClosure.getInstance()), null);
        return collectManyParallel(collection.entrySet(), projection);
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code findAllParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].findAllParallel {Number number -&gt; number &gt; 3}
     *     assertEquals(new HashSet([4, 5]), result)
     * }
     * </pre>
     */
    public static <T> Collection<T> findAllParallel(final Collection<T> collection, final Closure<Boolean> cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.findAllParallelPA(GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()), cl);
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .filter(it -> GParsPoolUtilHelper.convertToBoolean(cl.call(it)))
                        .collect(Collectors.toList())
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code findAllParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].findAllParallel {Number number -&gt; number &gt; 3}
     *     assertEquals(new HashSet([4, 5]), result)
     * }
     * </pre>
     */
    public static Collection<Object> findAllParallel(final Object collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return (Collection<Object>) GParsPoolUtilHelper.findAllParallelPA(GParsPoolUtilHelper.createPA(collection, retrievePool()), cl);
        return findAllParallel(toCollection(collection), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied map and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code findAllParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <code>
     * GParsPool.withPool {
     * def result = [1, 2, 3, 4, 5].findAllParallel {Number number -&gt; number &gt; 3}
     * assertEquals(new HashSet([4, 5]), result)
     * }
     * </code>
     */
    public static <K, V> Map<K, V> findAllParallel(final Map<K, V> collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return buildResultMap(GParsPoolUtilHelper.findAllParallelPA(createPA(collection, retrievePool()), buildClosureForMaps(cl)));
        return retrievePool().submit(() ->
                collection.entrySet()
                        .parallelStream()
                        .filter(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(cl, it)))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a value from the resulting Parallel Array with the minimum index.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code findParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].findParallel {Number number -&gt; number &gt; 3}
     *     assert (result in [4, 5])
     * }
     * </pre>
     */
    @SuppressWarnings("GroovyAssignabilityCheck")
    public static <T> T findParallel(final Collection<T> collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.findParallelPA(GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()), cl);
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .filter(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(cl, it)))
                        .findFirst()
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a value from the resulting Parallel Array with the minimum index.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code findParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].findParallel {Number number -&gt; number &gt; 3}
     *     assert (result in [4, 5])
     * }
     * </pre>
     */
    public static Object findParallel(final Object collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.findParallelPA(GParsPoolUtilHelper.createPA(collection, retrievePool()), cl);
        return findParallel(toCollection(collection), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied map and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a value from the resulting Parallel Array with the minimum index.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code findParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].findParallel {Number number -&gt; number &gt; 3}
     *     assert (result in [4, 5])
     * }
     * </pre>
     */
    public static <K, V> Map.Entry<K, V> findParallel(final Map<K, V> collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.findParallelPA(createPA(collection, retrievePool()), buildClosureForMaps(cl));
        return retrievePool().submit(() ->
                collection.entrySet()
                        .parallelStream()
                        .filter(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(cl, it)))
                        .findFirst()
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * Unlike with the {@code find} method, findAnyParallel() does not guarantee
     * that the a matching element with the lowest index is returned.
     * The findAnyParallel() method evaluates elements lazily and stops processing further elements of the collection once a match has been found.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a random value from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code findParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].findParallel {Number number -&gt; number &gt; 3}
     *     assert (result in [4, 5])
     * }
     * </pre>
     */
    public static <T> T findAnyParallel(final Collection<T> collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.findAnyParallelPA(GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()), cl);
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .filter(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(cl, it)))
                        .findAny()
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * Unlike with the {@code find} method, findAnyParallel() does not guarantee
     * that the a matching element with the lowest index is returned.
     * The findAnyParallel() method evaluates elements lazily and stops processing further elements of the collection once a match has been found.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a random value from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code findParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].findAnyParallel {Number number -&gt; number &gt; 3}
     *     assert (result in [4, 5])
     * }
     * </pre>
     */
    public static Object findAnyParallel(final Object collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.findAnyParallelPA(GParsPoolUtilHelper.createPA(collection, retrievePool()), cl);
        return findAnyParallel(toCollection(collection), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied map and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * Unlike with the {@code find} method, findAnyParallel() does not guarantee
     * that the matching element with the lowest index is returned.
     * The findAnyParallel() method evaluates elements lazily and stops processing further elements of the collection once a match has been found.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a random value from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code findParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].findAnyParallel {Number number -&gt; number &gt; 3}
     *     assert (result in [4, 5])
     * }
     * </pre>
     */
    public static <K, V> Map.Entry<K, V> findAnyParallel(final Map<K, V> collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.findAnyParallelPA(createPA(collection, retrievePool()), buildClosureForMaps(cl));
        return retrievePool().submit(() ->
                collection.entrySet()
                        .parallelStream()
                        .filter(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(cl, it)))
                        .findAny()
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * rule as the filter predicate.
     * The filter will be effectively used concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code grepParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].grepParallel(4..6)
     *     assertEquals(new HashSet([4, 5]), result)
     * }
     * </pre>
     */
    public static <T> Collection<T> grepParallel(final Collection<T> collection, final Object filter) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.grepParallelPA(GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()), filter);
        final Closure<Object> predicate = isClosure(filter) ?
                (Closure<Object>) filter :
                collectionContainsClosure(toCollection(filter));

        return retrievePool().submit(() ->
                collection.parallelStream()
                        .filter(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(predicate, it)))
                        .collect(Collectors.toList())
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * rule as the filter predicate.
     * The filter will be effectively used concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code grepParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].grepParallel(4..6)
     *     assertEquals(new HashSet([4, 5]), result)
     * }
     * </pre>
     */
    public static Object grepParallel(final Object collection, final Object filter) throws ExecutionException, InterruptedException {
        //return (Collection<Object>) GParsPoolUtilHelper.grepParallelPA(GParsPoolUtilHelper.createPA(collection, retrievePool()), filter);
        return grepParallel(toCollection(collection), filter);
    }

    /**
     * Creates a Parallel Array out of the supplied map and invokes the withFilter() method using the supplied
     * rule as the filter predicate.
     * The filter will be effectively used concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code grepParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].grepParallel(4..6)
     *     assertEquals(new HashSet([4, 5]), result)
     * }
     * </pre>
     */
    public static <K, V> Map<K, V> grepParallel(final Map<K, V> collection, final Object filter) throws ExecutionException, InterruptedException {
        //return buildResultMap(GParsPoolUtilHelper.grepParallelPA(createPA(collection, retrievePool()), filter instanceof Closure ? buildClosureForMaps((Closure<Object>) filter) : filter));
        final Closure<Object> predicate = isClosure(filter) ?
                (Closure<Object>) filter :
                collectionContainsClosure(toCollection(filter));
        
        return retrievePool().submit(() ->
                collection.entrySet()
                        .parallelStream()
                        .filter(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(predicate, it)))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * rule as the filter predicate.
     * The filter will be effectively used concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code grepParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     */
    public static <T> Collection<T> splitParallel(final Collection<T> collection, final Closure filter) throws ExecutionException, InterruptedException {
        //final Map groups = groupByParallel(collection, (Closure) filter);
//        (Collection<T>) [groups[true] ?: [], groups[false] ?: []]
        //return (Collection<T>) asList(groups.containsKey(Boolean.TRUE) ? groups.get(Boolean.TRUE) : new ArrayList<T>(), groups.containsKey(Boolean.FALSE) ? groups.get(Boolean.FALSE) : new ArrayList<T>());
        final Map<Boolean, List> groups = groupByParallel(collection, filter);
        return (Collection<T>) Arrays.asList(
                groups.containsKey(true) ? groups.get(true) : new ArrayList<T>(),
                groups.containsKey(false) ? groups.get(false) : new ArrayList<T>()
        );
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * rule as the filter predicate.
     * The filter will be effectively used concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code grepParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].splitParallel(4..6)
     *     assert [3, 4, 5] as Set == result[0] as Set
     *     assert [1, 2] as Set == result[1] as Set
     * }
     * </pre>
     */
    public static Object splitParallel(final Object collection, final Closure filter) throws ExecutionException, InterruptedException {
        /*
        final Map groups = groupByParallelPA(GParsPoolUtilHelper.createPA(collection, retrievePool()), (Closure) filter);
        return asList(groups.containsKey(Boolean.TRUE) ? groups.get(Boolean.TRUE) : new ArrayList<Object>(), groups.containsKey(Boolean.FALSE) ? groups.get(Boolean.FALSE) : new ArrayList<Object>());
        */
        return splitParallel(toCollection(collection), filter);
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * rule as the filter predicate.
     * The filter will be effectively used concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code grepParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].countParallel(4)
     *     assertEquals(1, result)
     * }
     * </pre>
     */
    public static int countParallel(final Collection collection, final Object filter) throws ExecutionException, InterruptedException {
        /*
        return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).withFilter(new Ops.Predicate<Object>() {
            @Override
            public boolean op(final Object o) {
                return filter.equals(o);
            }
        }).size();
        */
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .filter(it -> it.equals(filter))
                        .count()
        ).get().intValue();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * rule as the filter predicate.
     * The filter will be effectively used concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code grepParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].countParallel(4)
     *     assertEquals(1, result)
     * }
     * </pre>
     */
    public static int countParallel(final Object collection, final Object filter) throws ExecutionException, InterruptedException {
        return countParallel(toCollection(collection), filter);
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * rule as the filter predicate.
     * The filter will be effectively used concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code grepParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def isOdd = { it % 2 }
     *     def result = [1, 2, 3, 4, 5].countParallel(isOdd)
     *     assert result == 3
     * }
     * </pre>
     */
    public static int countParallel(final Collection collection, final Closure filter) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).withFilter(new ClosurePredicate(filter)).size();
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .filter(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(filter, it)))
                        .count()
        ).get().intValue();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * rule as the filter predicate.
     * The filter will be effectively used concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a collection of values from the resulting Parallel Array.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code grepParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     def result = [1, 2, 3, 4, 5].countParallel(4)
     *     assertEquals(1, result)
     * }
     * </pre>
     */
    public static int countParallel(final Object collection, final Closure filter) throws ExecutionException, InterruptedException {
        return countParallel(toCollection(collection), filter);
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * The anyParallel() method is lazy and once a positive answer has been given by at least one element, it avoids running
     * the supplied closure on subsequent elements.
     * After all the elements have been processed, the method returns a boolean value indicating, whether at least
     * one element of the collection meets the predicate.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code anyParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     assert [1, 2, 3, 4, 5].anyParallel {Number number -&gt; number &gt; 3}
     *     assert ![1, 2, 3].anyParallel {Number number -&gt; number &gt; 3}
     * }
     * </pre>
     */
    public static boolean anyParallel(final Collection collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).withFilter(new ClosurePredicate(cl)).any() != null;
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .anyMatch(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(cl, it)))
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * The anyParallel() method is lazy and once a positive answer has been given by at least one element, it avoids running
     * the supplied closure on subsequent elements.
     * After all the elements have been processed, the method returns a boolean value indicating, whether at least
     * one element of the collection meets the predicate.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code anyParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     assert [1, 2, 3, 4, 5].anyParallel {Number number -&gt; number &gt; 3}
     *     assert ![1, 2, 3].anyParallel {Number number -&gt; number &gt; 3}
     * }
     * </pre>
     */
    public static boolean anyParallel(final Object collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPA(collection, retrievePool()).withFilter(new ClosurePredicate(cl)).any() != null;
        return anyParallel(toCollection(collection), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied map and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * The anyParallel() method is lazy and once a positive answer has been given by at least one element, it avoids running
     * the supplied closure on subsequent elements.
     * After all the elements have been processed, the method returns a boolean value indicating, whether at least
     * one element of the collection meets the predicate.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code anyParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     assert [1, 2, 3, 4, 5].anyParallel {Number number -&gt; number &gt; 3}
     *     assert ![1, 2, 3].anyParallel {Number number -&gt; number &gt; 3}
     * }
     * </pre>
     */
    public static boolean anyParallel(final Map collection, final Closure cl) throws ExecutionException, InterruptedException {
        /*
        final Closure mapClosure = buildClosureForMaps(cl);
        return createPA(collection, retrievePool()).withFilter(new ClosurePredicate(mapClosure)).any() != null;
        */
        return retrievePool().submit(() ->
                collection.entrySet()
                        .parallelStream()
                        .anyMatch(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(cl, it)))
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a boolean value indicating, whether all the elements
     * of the collection meet the predicate.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code everyParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool(5) {
     *     assert ![1, 2, 3, 4, 5].everyParallel {Number number -&gt; number &gt; 3}
     *     assert [1, 2, 3].everyParallel() {Number number -&gt; number &lt;= 3}
     * }
     * </pre>
     */
    public static boolean everyParallel(final Collection collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).withFilter(new ClosureNegationPredicate(cl)).any() == null;
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .allMatch(it -> GParsPoolUtilHelper.convertToBoolean(callClosure(cl, it)))
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a boolean value indicating, whether all the elements
     * of the collection meet the predicate.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code everyParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool(5) {
     *     assert ![1, 2, 3, 4, 5].everyParallel {Number number -&gt; number &gt; 3}
     *     assert [1, 2, 3].everyParallel() {Number number -&gt; number &lt;= 3}
     * }
     * </pre>
     */
    public static boolean everyParallel(final Object collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPA(collection, retrievePool()).withFilter(new ClosureNegationPredicate(cl)).any() == null;
        return everyParallel(toCollection(collection), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied map and invokes the withFilter() method using the supplied
     * closure as the filter predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a boolean value indicating, whether all the elements
     * of the collection meet the predicate.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code everyParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool(5) {
     *     assert ![1, 2, 3, 4, 5].everyParallel {Number number -&gt; number &gt; 3}
     *     assert [1, 2, 3].everyParallel() {Number number -&gt; number &lt;= 3}
     * }
     * </pre>
     */
    public static boolean everyParallel(final Map collection, final Closure cl) throws ExecutionException, InterruptedException {
        /*
        final Closure mapClosure = buildClosureForMaps(cl);
        return createPA(collection, retrievePool()).withFilter(new ClosureNegationPredicate(mapClosure)).any() == null;
        */
        return everyParallel(collection.entrySet(), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * closure as the mapping predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a map of groups of the original elements.
     * Elements in the same group gave identical results when the supplied closure was invoked on them.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code groupByParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     assert ([1, 2, 3, 4, 5].groupByParallel {Number number -&gt; number % 2}).size() == 2
     * }
     * </pre>
     */
    public static <K, T> Map<K, List<T>> groupByParallel(final Collection<T> collection, final Closure<K> cl) throws ExecutionException, InterruptedException {
        //return groupByParallelPA(GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()), cl);
        return (Map<K, List<T>>) retrievePool().submit(() ->
                collection.parallelStream()
                        .collect(Collectors.groupingBy(it -> (T) callClosure(cl, it), Collectors.toList()))
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes the withMapping() method using the supplied
     * closure as the mapping predicate.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns a map of groups of the original elements.
     * Elements in the same group gave identical results when the supplied closure was invoked on them.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code groupByParallel(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * Example:
     * <pre>
     * GParsPool.withPool {
     *     assert ([1, 2, 3, 4, 5].groupByParallel {Number number -&gt; number % 2}).size() == 2
     * }
     * </pre>
     */
    public static <K> Map<K, List<Object>> groupByParallel(final Object collection, final Closure<K> cl) throws ExecutionException, InterruptedException {
        //return groupByParallelPA(GParsPoolUtilHelper.createPA(collection, retrievePool()), cl);
        return groupByParallel(toCollection(collection), cl);
    }

//    private static <K, T> Map<K, List<T>> groupByParallelPA(final ParallelArray<T> pa, final Closure<K> cl) {
//        final ConcurrentHashMap<K, List<T>> map = new ConcurrentHashMap<K, List<T>>();
//        GParsPoolUtilHelper.eachParallelPA(pa, createGroupByClosure(cl, map));
//        return map;
//
//    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its min() method using the supplied
     * closure as the comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the minimum of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code min(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * If the supplied closure takes two arguments it is used directly as a comparator.
     * If the supplied closure takes one argument, the values returned by the supplied closure for individual elements are used for comparison by the implicit comparator.
     *
     * @param cl A one or two-argument closure
     */
    @SuppressWarnings("GroovyAssignabilityCheck")
    public static <T> T minParallel(final Collection<T> collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).min((Comparator<T>) createComparator(cl));
        return (T) retrievePool().submit(() ->
                collection.parallelStream()
                        .min((a,b) -> cl.getMaximumNumberOfParameters() >= 2 ? (Integer) cl.call(a,b) : ((Comparable) a).compareTo(b))
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its min() method using the supplied
     * closure as the comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the minimum of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code min(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * If the supplied closure takes two arguments it is used directly as a comparator.
     * If the supplied closure takes one argument, the values returned by the supplied closure for individual elements are used for comparison by the implicit comparator.
     *
     * @param cl A one or two-argument closure
     */
    public static Object minParallel(final Object collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPA(collection, retrievePool()).min(createComparator(cl));
        return minParallel(toCollection(collection), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its min() method using the default comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the minimum of the elements in the collection.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code min(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     */
    @SuppressWarnings("GroovyAssignabilityCheck")
    public static <T> T minParallel(final Collection<T> collection) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).min();
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .min((a,b) -> ((Comparable) a).compareTo(b))
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its min() method using the default comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the minimum of the elements in the collection.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code min(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     */
    public static Object minParallel(final Object collection) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPA(collection, retrievePool()).min();;
        return retrievePool().submit(() ->
                toCollection(collection).parallelStream()
                        .min((a,b) -> ((Comparable) a).compareTo(b))
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its max() method using the supplied
     * closure as the comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the maximum of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code max(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * If the supplied closure takes two arguments it is used directly as a comparator.
     * If the supplied closure takes one argument, the values returned by the supplied closure for individual elements are used for comparison by the implicit comparator.
     *
     * @param cl A one or two-argument closure
     */
    @SuppressWarnings("GroovyAssignabilityCheck")
    public static <T> T maxParallel(final Collection<T> collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).max((Comparator<T>) createComparator(cl));
        return (T) retrievePool().submit(() ->
                collection.parallelStream()
                        .max((a,b) -> cl.getMaximumNumberOfParameters() >= 2 ? (Integer) cl.call(a,b) : ((Comparable) a).compareTo(b))
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its max() method using the supplied
     * closure as the comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the maximum of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code max(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     * If the supplied closure takes two arguments it is used directly as a comparator.
     * If the supplied closure takes one argument, the values returned by the supplied closure for individual elements are used for comparison by the implicit comparator.
     *
     * @param cl A one or two-argument closure
     */
    public static Object maxParallel(final Object collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPA(collection, retrievePool()).max(createComparator(cl));
        return maxParallel(toCollection(collection), cl);
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its max() method using the default comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the maximum of the elements in the collection.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code max(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     */
    @SuppressWarnings("GroovyAssignabilityCheck")
    public static <T> T maxParallel(final Collection<T> collection) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).max();
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .max((a,b) -> ((Comparable) a).compareTo(b))
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its max() method using the default comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the maximum of the elements in the collection.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code max(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     */
    public static Object maxParallel(final Object collection) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPA(collection, retrievePool()).max();
        return maxParallel(toCollection(collection));
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and summarizes its elements using the foldParallel()
     * method with the + operator and the reduction operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the sum of the elements in the collection.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code sun(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     */
    public static <T> T sumParallel(final Collection<T> collection) throws ExecutionException, InterruptedException {
        //return foldParallel(collection, SumClosure.getInstance());
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .reduce((t, t2) -> (T) InvokerHelper.invokeMethod(t, "plus", t2))
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and summarizes its elements using the foldParallel()
     * method with the + operator and the reduction operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the sum of the elements in the collection.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code sum(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     */
    public static Object sumParallel(final Object collection) throws ExecutionException, InterruptedException {
        //return foldParallel(collection, SumClosure.getInstance());
        return sumParallel(toCollection(collection));
    }

//    /**
//     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
//     * closure as the reduction operation.
//     * The closure will be effectively invoked concurrently on the elements of the collection.
//     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
//     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
//     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
//     * have a new {@code reduce(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
//     */
//    @SuppressWarnings("GroovyAssignabilityCheck")
//    @Deprecated
//    public static <T> T foldParallel(final Collection<T> collection, final Closure cl) {
//        return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).reduce(new ClosureReducer<T>(cl), null);
//    }
//
//    /**
//     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
//     * closure as the reduction operation.
//     * The closure will be effectively invoked concurrently on the elements of the collection.
//     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
//     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
//     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
//     * have a new {@code reduce(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
//     */
//    @Deprecated
//    public static Object foldParallel(final Object collection, final Closure cl) {
//        return GParsPoolUtilHelper.createPA(collection, retrievePool()).reduce(new ClosureReducer(cl), null);
//    }
//
//    /**
//     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
//     * closure as the reduction operation.
//     * The closure will be effectively invoked concurrently on the elements of the collection.
//     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
//     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
//     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
//     * have a new {@code reduce(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
//     *
//     * @param seed A seed value to initialize the operation
//     */
//    @Deprecated
//    public static <T> T foldParallel(final Collection<T> collection, final T seed, final Closure cl) {
//        return GParsPoolUtilHelper.foldParallel(collection, seed, cl);
//    }
//
//    /**
//     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
//     * closure as the reduction operation.
//     * The closure will be effectively invoked concurrently on the elements of the collection.
//     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
//     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
//     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
//     * have a new {@code reduce(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
//     *
//     * @param seed A seed value to initialize the operation
//     */
//    @Deprecated
//    public static Object foldParallel(final Object collection, final Object seed, final Closure cl) {
//        return GParsPoolUtilHelper.foldParallel(collection, seed, cl);
//    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
     * closure as the reduction operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code reduce(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     */
    @SuppressWarnings("GroovyAssignabilityCheck")
    public static <T> T injectParallel(final Collection<T> collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool()).reduce(new ClosureReducer<T>(cl), null);
        return retrievePool().submit(() ->
                collection.parallelStream()
                        .reduce((a,b) -> (T) cl.call(a,b))
                        .orElse(null)
        ).get();
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
     * closure as the reduction operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code reduce(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     */
    public static <T> T injectParallel(final Object collection, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.createPA(collection, retrievePool()).reduce(new ClosureReducer(cl), null);
        return (T) injectParallel(toCollection(collection), cl);
    }

    /**
     * Creates a {@code Stream} out of the supplied collection/object and invokes its reduce() method using the supplied
     * closure as the reduction operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     *
     * @param collection The collection to reduce over.
     * @param seed A seed value to initialize the operation.
     * @param cl A binary operation (assumed to be an accumulation) to apply during the reduction.
     * @return The value calculated by the reduction.
     */
    public static <T> T injectParallel(final Collection<T> collection, final T seed, final Closure cl) throws ExecutionException, InterruptedException {
        //return collection.parallelStream().reduce(seed, new ClosureReducer<T>(cl));
        T result = retrievePool().submit(() ->
                collection.parallelStream()
                        .reduce((a,b) -> (T) cl.call(a,b))
                        .orElse(null)
        ).get();

        return (T) cl.call(result, seed);
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
     * closure as the reduction operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the {@code withPool} block
     * have a new {@code reduce(Closure cl)} method, which delegates to the {@code GParsPoolUtil} class.
     *
     * @param seed A seed value to initialize the operation
     */
    public static Object injectParallel(final Object collection, final Object seed, final Closure cl) throws ExecutionException, InterruptedException {
        //return GParsPoolUtilHelper.foldParallel(collection, seed, cl);
        return injectParallel(toCollection(collection), seed, cl);
    }

    /**
     * Creates a PAWrapper around a ParallelArray wrapping the elements of the original collection.
     * This allows further parallel processing operations on the collection to chain and so effectively leverage the underlying
     * ParallelArray implementation.
     */
    public static <T> PAWrapper<T> getParallel(final Collection<T> collection) {
        return new PAWrapper<T>(collection);
    }

    /**
     * Creates a PAWrapper around a ParallelArray wrapping the elements of the original collection.
     * This allows further parallel processing operations on the collection to chain and so effectively leverage the underlying
     * ParallelArray implementation.
     */
    public static PAWrapper getParallel(final Object collection) {
        return new PAWrapper(toCollection(collection));
    }
//
//    /**
//     * Creates a ParallelArray wrapping the elements of the original collection.
//     */
//    @SuppressWarnings("GroovyAssignabilityCheck")
//    public static <T> ParallelArray<T> getParallelArray(final Collection<T> collection) {
//        return GParsPoolUtilHelper.createPAFromCollection(collection, retrievePool());
//    }
//
//    /**
//     * Creates a ParallelArray wrapping the elements of the original collection.
//     */
//    public static ParallelArray getParallelArray(final Object collection) {
//        return GParsPoolUtilHelper.createPA(collection, retrievePool());
//    }


//    private static <K, V> V valueFromClosure(final Map.Entry<K, V> e, final Closure<V> cl) {
//        if (cl.getMaximumNumberOfParameters() == 2) {
//            return cl.call(e.getKey(), e.getValue());
//        }
//        return cl.call(e);
//    }

    private static <T> Collection<T> toCollection(final Object object) {
        if (object == null) {
            return Collections.emptyList();
        }

        if (CharSequence.class.isAssignableFrom(object.getClass())) {
            final Object[] chars = new Object[((CharSequence) object).length()];
            for (int i = 0; i < ((CharSequence) object).length(); i++) {
                chars[i] = String.valueOf(((CharSequence) object).charAt(i));
            }
            return Collections.unmodifiableList(Arrays.asList((T[]) chars));
        }

        if (Iterable.class.isAssignableFrom(object.getClass())) {
            final List target = new LinkedList<>();
            ((Iterable) object).forEach(target::add);
            return Collections.unmodifiableList(target);
        }

        if (Iterator.class.isAssignableFrom(object.getClass())) {
            final List target = new LinkedList<>();
            ((Iterator) object).forEachRemaining(target::add);
            return Collections.unmodifiableList(target);
        }

        if (Map.class.isAssignableFrom(object.getClass())) {
            return Collections.unmodifiableSet(((Map) object).entrySet());
        }

        if (Map.Entry.class.isAssignableFrom(object.getClass())) {
            return Collections.unmodifiableList((List<? extends T>) Collections.singletonList(((Map.Entry) object)));
        }

        if (GroovyObjectSupport.class.isAssignableFrom(object.getClass())) {
            return inspectGroovyMetaClassIfNeeded((GroovyObjectSupport) object);
        }

        return Collections.emptyList();
    }

    private static <T> Collection<T> inspectGroovyMetaClassIfNeeded(final GroovyObjectSupport object) {
        Class<?> metaClass = (Class<?>) object.invokeMethod("getClass", null);
        
        if (List.class.isAssignableFrom(metaClass)) {
            Object[] objects = (Object[]) object.invokeMethod("toArray", null);
            return (List<T>) Arrays.asList((objects));
        }

        if (CharSequence.class.isAssignableFrom(metaClass)) {
            char[] chars = ((String) object.invokeMethod("toString", null)).toCharArray();
            final List<String> result = new LinkedList<>();
            for (int i = 0; i < chars.length; i++) {
                result.add(String.valueOf(chars[i]));
            }
            return (List<T>) Collections.unmodifiableList(result);
        }

        if (Iterator.class.isAssignableFrom(metaClass)) {
            final List<T> target = new LinkedList<>();
            object.invokeMethod("forEachRemaining", (Consumer<T>) t -> target.add(t));
            return Collections.unmodifiableList(target);
        }

        return Collections.emptyList();
    }

    private static boolean isClosure(final Object object) {
        return Closure.class.isAssignableFrom(object.getClass());
    }

    private static Closure<Object> collectionContainsClosure(final Collection collection) {
        return new Closure<Object>(null) {
            public Object doCall(Object el) {
                return collection.contains(el);
            }
        };
    }

    private static  <T> T callClosure(final Closure cl, final Object element) {
        if (Map.Entry.class.isAssignableFrom(element.getClass()) && cl.getMaximumNumberOfParameters() >= 2) {
            return (T) cl.call(((Map.Entry) element).getKey(), (((Map.Entry) element).getValue()));
        }
        return (T) cl.call(element);
    }

    private static  <T> T callIndexedClosure(final Closure cl, final Object element, int index) {
        if (Map.Entry.class.isAssignableFrom(element.getClass()) && cl.getMaximumNumberOfParameters() >= 3) {
            return (T) cl.call(((Map.Entry) element).getKey(), (((Map.Entry) element).getValue()), index);
        }
        return (T) cl.call(element, index);
    }
}
