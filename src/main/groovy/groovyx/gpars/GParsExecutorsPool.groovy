// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2012  The original author or authors
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

package groovyx.gpars

import groovyx.gpars.dataflow.DataflowVariable
import groovyx.gpars.util.PoolUtils

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Enables a ExecutorService-based DSL on closures, objects and collections.
 * E.g.
 * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt;
 *     Collection<Future> result = [1, 2, 3, 4, 5].collectParallel({it * 10}.async())
 *     assertEquals(new HashSet([10, 20, 30, 40, 50]), new HashSet((Collection)result*.get()))
 *}*
 * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt;
 *     def result = [1, 2, 3, 4, 5].findParallel{Number number -&gt; number > 2}*     assert result in [3, 4, 5]
 *}*
 * @author Vaclav Pech
 * Date: Oct 23, 2008
 */
class GParsExecutorsPool {

    /**
     * Maps threads to their appropriate thread pools
     */
    private static ThreadLocalPools currentPoolStack = new ThreadLocalPools()

    public final static void shutdown() {
        currentPoolStack = null;
    }

    /**
     * Caches the default pool size.
     */
    private static final int defaultPoolSize = PoolUtils.retrieveDefaultPoolSize()

    /**
     * Retrieves the pool assigned to the current thread.
     */
    protected static ExecutorService retrieveCurrentPool() {
        currentPoolStack.current
    }

    /**
     * Creates a new pool with the default size()
     */
    private static createPool() {
        return createPool(PoolUtils.retrieveDefaultPoolSize())
    }

    /**
     * Creates a new pool with the given size()
     */
    private static createPool(int poolSize) {
        return createPool(poolSize, createDefaultThreadFactory())
    }

    private static createPool(int poolSize, ThreadFactory threadFactory) {
        if (!(poolSize in 1..Integer.MAX_VALUE)) throw new IllegalArgumentException("Invalid value $poolSize for the pool size has been specified. Please supply a positive int number.")
        if (!threadFactory) throw new IllegalArgumentException("No value specified for threadFactory.")
        return Executors.newFixedThreadPool(poolSize, threadFactory)
    }

    private static ThreadFactory createDefaultThreadFactory() {
        return { Runnable runnable ->
            final Thread thread = new Thread(runnable)
            thread.daemon = false
            thread
        } as ThreadFactory
    }

    /**
     * Creates a new instance of <i>ExecutorService</i>, binds it to the current thread, enables the ExecutorService DSL
     * and runs the supplied closure.
     * It is an identical alternative for withPool() with a shorter name.
     * Within the supplied code block the <i>ExecutorService</i> is available as the only parameter, objects have been
     * enhanced with the <i>eachParallel()</i>, <i>collectParallel()</i> and other methods from the <i>GParsExecutorsPoolUtil</i>
     * category class as well as closures can be turned into asynchronous ones by calling the <i>async()</i> method on them.
     * E.g. <i>closure,async</i> returns a new closure, which, when run will schedule the original closure
     * for processing in the pool.
     * Calling <i>images.eachParallel{processImage(it}}</i> will call the potentially long-lasting <i>processImage()</i>
     * operation on each image in the <i>images</i> collection in parallel.
     * <pre>
     * def result = new ConcurrentSkipListSet()
     * GParsExecutorsPool.withPool {ExecutorService service -&gt;
     *     [1, 2, 3, 4, 5].eachParallel{Number number -&gt; result.add(number * 10)}*     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     *}* </pre>
     * @param cl The block of code to invoke with the DSL enabled
     */
    public static withPool(Closure cl) {
        return withPool(defaultPoolSize, cl)
    }

    /**
     * Creates a new instance of <i>ExecutorService</i>, binds it to the current thread, enables the ExecutorService DSL
     * and runs the supplied closure.
     * It is an identical alternative for withPool() with a shorter name.
     * Within the supplied code block the <i>ExecutorService</i> is available as the only parameter, objects have been
     * enhanced with the <i>eachParallel()</i>, <i>collectParallel()</i> and other methods from the <i>GParsExecutorsPoolUtil</i>
     * category class as well as closures can be turned into asynchronous ones by calling the <i>async()</i> method on them.
     * E.g. <i>closure,async</i> returns a new closure, which, when run will schedule the original closure
     * for processing in the pool.
     * Calling <i>images.eachParallel{processImage(it}}</i> will call the potentially long-lasting <i>processImage()</i>
     * operation on each image in the <i>images</i> collection in parallel.
     * <pre>
     * def result = new ConcurrentSkipListSet()
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt;
     *     [1, 2, 3, 4, 5].eachParallel{Number number -&gt; result.add(number * 10)}*     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     *}* </pre>
     * @param numberOfThreads Number of threads in the newly created thread pool
     * @param cl The block of code to invoke with the DSL enabled
     */
    public static withPool(int numberOfThreads, Closure cl) {
        return withPool(numberOfThreads, createDefaultThreadFactory(), cl)
    }

    /**
     * Creates a new instance of <i>ExecutorService</i>, binds it to the current thread, enables the ExecutorService DSL
     * and runs the supplied closure.
     * It is an identical alternative for withPool() with a shorter name.
     * Within the supplied code block the <i>ExecutorService</i> is available as the only parameter, objects have been
     * enhanced with the <i>eachParallel()</i>, <i>collectParallel()</i> and other methods from the <i>GParsExecutorsPoolUtil</i>
     * category class as well as closures can be turned into asynchronous ones by calling the <i>async()</i> method on them.
     * E.g. <i>closure,async</i> returns a new closure, which, when run will schedule the original closure
     * for processing in the pool.
     * Calling <i>images.eachParallel{processImage(it}}</i> will call the potentially long-lasting <i>processImage()</i>
     * operation on each image in the <i>images</i> collection in parallel.
     * <pre>
     * def result = new ConcurrentSkipListSet()
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt;
     *     [1, 2, 3, 4, 5].eachParallel{Number number -&gt; result.add(number * 10)}*     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     *}* </pre>
     * @param numberOfThreads Number of threads in the newly created thread pool
     * @param threadFactory Factory for threads in the pool
     * @param cl The block of code to invoke with the DSL enabled
     */
    public static withPool(int numberOfThreads, ThreadFactory threadFactory, Closure cl) {
        final ExecutorService pool = createPool(numberOfThreads, threadFactory)
        try {
            return withExistingPool(pool, cl)
        } finally {
            pool.shutdown()
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
        }

    }

    /**
     * Creates a new instance of <i>ExecutorService</i>, binds it to the current thread, enables the ExecutorService DSL
     * and runs the supplied closure.
     * Within the supplied code block the <i>ExecutorService</i> is available as the only parameter, objects have been
     * enhanced with the <i>eachParallel()</i>, <i>collectParallel()</i> and other methods from the <i>GParsExecutorsPoolUtil</i>
     * category class as well as closures can be turned into asynchronous ones by calling the <i>async()</i> method on them.
     * E.g. <i>closure,async</i> returns a new closure, which, when run will schedule the original closure
     * for processing in the pool.
     * Calling <i>images.eachParallel{processImage(it}}</i> will call the potentially long-lasting <i>processImage()</i>
     * operation on each image in the <i>images</i> collection in parallel.
     * <pre>
     * def result = new ConcurrentSkipListSet()
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt;
     *     [1, 2, 3, 4, 5].eachParallel{Number number -&gt; result.add(number * 10)}*     assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     *}* </pre>
     * @param pool The <i>ExecutorService</i> to use, the service will not be shutdown after this method returns
     */
    public static withExistingPool(ExecutorService pool, Closure cl) {
        currentPoolStack << pool
        def result = null
        try {
            use(GParsExecutorsPoolUtil) {
                result = cl(pool)
            }
        } finally {
            currentPoolStack.pop()
            if (currentPoolStack.isEmpty()) currentPoolStack.remove()
        }
        return result
    }

    /**
     * Starts multiple closures in separate threads, collecting their return values
     * If an exception is thrown from the closure when called on any of the collection's elements,
     * it will be re-thrown in the calling thread when it calls the Future.get() method.
     * @return The result values of all closures
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static List<Object> executeAsyncAndWait(Closure... closures) {
        return GParsExecutorsPoolUtil.processResult(executeAsync(closures))
    }

    /**
     * Starts multiple closures in separate threads, collecting their return values
     * If an exception is thrown from the closure when called on any of the collection's elements,
     * it will be re-thrown in the calling thread when it calls the Future.get() method.
     * @return The result values of all closures
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static List<Object> executeAsyncAndWait(List<Closure> closures) {
        return executeAsyncAndWait(* closures)
    }

    /**
     * Starts multiple closures in separate threads, collecting Futures for their return values
     * Reuses the pool defined by the surrounding withPool() call.
     * If an exception is thrown from the closure when called on any of the collection's elements,
     * it will be re-thrown in the calling thread when it calls the Future.get() method.
     * @return Futures for the result values or exceptions of all closures
     */
    public static List<Future<Object>> executeAsync(Closure... closures) {
        ExecutorService executorService = retrieveCurrentPool()
        if (executorService == null) throw new IllegalStateException("No active thread pool available to execute closures asynchronously. Consider wrapping the function call with GParsExecutorsPool.withPool().")
        List<Future<Object>> result = closures.collect { cl ->
            executorService.submit({
                cl.call()
            } as Callable<Object>)
        }
        result
    }

    /**
     * Starts multiple closures in separate threads, collecting Futures for their return values
     * If an exception is thrown from the closure when called on any of the collection's elements,
     * it will be re-thrown in the calling thread when it calls the Future.get() method.
     * @return Futures for the result values or exceptions of all closures
     */
    public static List<Future<Object>> executeAsync(List<Closure> closures) {
        return executeAsync(* closures)
    }

    /**
     * Runs the supplied closures asynchronously and in parallel, returning the first result obtained and cancelling the other (slower) calculations.
     * Typically used to run several different calculations in parallel, all of which are supposed to give the same result,
     * but may last different amount of time each. If the system has enough threads available, the calculations can be test-run
     * in parallel and the fastest result is then used, while the other results are cancelled or discarded.
     * @param alternatives All the functions to invoke in parallel
     * @return The fastest result obtained
     */
    public static def speculate(List<Closure> alternatives) {
        speculate(* alternatives)
    }

    /**
     * Runs the supplied closures asynchronously and in parallel, returning the first result obtained and cancelling the other (slower) calculations.
     * Typically used to run several different calculations in parallel, all of which are supposed to give the same result,
     * but may last different amount of time each. If the system has enough threads available, the calculations can be test-run
     * in parallel and the fastest result is then used, while the other results are cancelled or discarded.
     * @param alternatives All the functions to invoke in parallel
     * @return The fastest result obtained
     */
    public static def speculate(Closure... alternatives) {
        def result = new DataflowVariable()
        def futures = new DataflowVariable()
        final AtomicInteger failureCounter = new AtomicInteger(0)
        futures << GParsExecutorsPool.executeAsync(alternatives.collect {
            original ->
                {->
                    //noinspection GroovyEmptyCatchBlock
                    try {
                        def localResult = original()
                        futures.val*.cancel(true)
                        result << localResult
                    } catch (Exception e) {
                        int counter = failureCounter.incrementAndGet()
                        if (counter == alternatives.size()) {
                            result << new IllegalStateException('All speculations failed', e)
                        }
                    }
                }
        })
        def r = result.val
        if (r instanceof Exception) throw r
        return r
    }
}
