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

import groovy.time.Duration
import groovyx.gpars.dataflow.DataflowVariable
import groovyx.gpars.scheduler.DefaultPool
import groovyx.gpars.scheduler.Pool
import groovyx.gpars.util.AsyncUtils
import groovyx.gpars.util.GeneralTimer

import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.Semaphore
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

import static groovyx.gpars.util.ParallelUtils.buildClosureForMaps
import static groovyx.gpars.util.ParallelUtils.buildClosureForMapsWithIndex
import static groovyx.gpars.util.ParallelUtils.buildResultMap

/**
 * This class forms the core of the DSL initialized by <i>GParsExecutorsPool</i>. The static methods of <i>GParsExecutorsPoolUtil</i>
 * get attached to their first arguments (the Groovy Category mechanism) and can be then invoked as if they were part of
 * the argument classes.
 * @see groovyx.gpars.GParsExecutorsPool
 *
 * @author Vaclav Pech
 * Date: Oct 23, 2008
 */
public class GParsExecutorsPoolUtil {

    /**
     * Allows timeouts for async operations
     */
    private static final GeneralTimer timer = GParsConfig.retrieveDefaultTimer('GParsExecutorsTimeoutTimer', true)

    // Hack needed for maps.
   private static java.util.Collection createCollection(Object object) {
       def collection = []
       for (element in object) collection << element
       return collection
   }

    /**
     * schedules the supplied closure for processing in the underlying thread pool.
     */
    private static Future callParallel(Closure task) {
        final ExecutorService pool = GParsExecutorsPool.retrieveCurrentPool()
        if (!pool) throw new IllegalStateException("No ExecutorService available for the current thread.")
        return pool.submit(new Callable() {
            @Override
            Object call() {
                return task()
            }
        })
    }

    /**
     * Calls a closure in a separate thread supplying the given arguments, returning a future for the potential return value.
     */
    public static Future callAsync(final Closure cl, final Object... args) {
        callParallel {-> cl(* args) }
    }

    /**
     * Calls a closure in a separate thread supplying the given arguments, returning a future for the potential return value.
     * Also allows the asynchronous calculation to be cancelled after a given timeout.
     * In order to allow cancellation, the asynchronously running code must keep checking the _interrupted_ flag of its
     * own thread and cease the calculation once the flag is set to true.
     * @param timeout The timeout in milliseconds to wait before the calculation gets cancelled.
     */
    public static Future callTimeoutAsync(final Closure cl, long timeout, final Object... args) {
        final Future f = callAsync(cl, args)
        timer.schedule({ f.cancel(true) } as Runnable, timeout)
        return f
    }

    /**
     * Calls a closure in a separate thread supplying the given arguments, returning a future for the potential return value.
     * Also allows the asynchronous calculation to be cancelled after a given timeout.
     * In order to allow cancellation, the asynchronously running code must keep checking the _interrupted_ flag of its
     * own thread and cease the calculation once the flag is set to true.
     * @param timeout The timeout to wait before the calculation gets cancelled.
     */
    public static Future callTimeoutAsync(final Closure cl, Duration timeout, final Object... args) {
        callTimeoutAsync(cl, timeout.toMilliseconds(), args)
    }

    /**
     * Submits the task for asynchronous processing returning the Future received from the executor service.
     * Allows for the following syntax:
     * <pre>
     * executorService << {println 'Inside parallel task'}* </pre>
     */
    public static Future leftShift(ExecutorService executorService, Closure task) {
        return executorService.submit(task as Callable)
    }

    /**
     * Creates an asynchronous variant of the supplied closure, which, when invoked returns a future for the potential return value
     */
    public static Closure async(Closure cl) {
        return { Object... args ->
            if (args != null && args.size() == 0) callParallel(cl)
            else callParallel({ -> cl(* args) })
        }
    }

    /**
     * Creates an asynchronous and composable variant of the supplied closure, which, when invoked returns a DataflowVariable for the potential return value
     */
    public static Closure asyncFun(final Closure original, final boolean blocking = false) {
        asyncFun(original, null, blocking)
    }

    /**
     * Creates an asynchronous and composable variant of the supplied closure, which, when invoked returns a DataflowVariable for the potential return value
     */
    public static Closure asyncFun(final Closure original, final Pool pool, final boolean blocking = false) {
        final Pool localPool = pool ?: retrieveLocalPool();
        return { final Object[] args ->
            final DataflowVariable result = new DataflowVariable()
            AsyncUtils.evaluateArguments(localPool ?: new DefaultPool(GParsExecutorsPool.retrieveCurrentPool() as ThreadPoolExecutor), args.clone(), 0, [], result, original, false)
            blocking ? result.get() : result
        }
    }

    private static Pool retrieveLocalPool() {
        final retrievedPool = GParsExecutorsPool.retrieveCurrentPool()
        if (retrievedPool != null) {
            return new DefaultPool(retrievedPool as ThreadPoolExecutor);
        }
        return null
    }

    /**
     * Iterates over a collection/object with the <i>each()</i> method using an asynchronous variant of the supplied closure
     * to evaluate each collection's element. A Semaphore is used to make the calling thread wait for all the results.
     * After this method returns, all the closures have been finished and all the potential shared resources have been updated
     * by the threads.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Example:
     *      GParsExecutorsPool.withPool(5) {ExecutorService service ->
     *          def result = Collections.synchronizedSet(new HashSet())
     *          service.eachParallel([1, 2, 3, 4, 5]) {Number number -&gt result.add(number * 10)}*          assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     *}* Note that the <i>result</i> variable is synchronized to prevent race conditions between multiple threads.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>eachParallel(Closure cl)</i> method, which delegates to the <i>GParsExecutorsPoolUtil</i> class.
     *    GParsExecutorsPool.withPool(5) {ExecutorService service ->
     *         def result = Collections.synchronizedSet(new HashSet())
     *        [1, 2, 3, 4, 5].eachParallel { Number number -&gt result.add(number * 10) }*         assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     *}* @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static def eachParallel(Object collection, Closure cl) {
        final List<Throwable> exceptions = Collections.synchronizedList([])
        final Semaphore semaphore = new Semaphore(0)
        Closure code = async({ Object... args ->
            try {
                cl(* args)
            } catch (Throwable e) {
                exceptions.add(e)
            } finally {
                semaphore.release()
            }
        })
        int count = 0
        for (element in collection) {
            count += 1
            code.call(element)
        }
        semaphore.acquire(count)
        if (exceptions.empty) return collection
        else throw new AsyncException("Some asynchronous operations failed. ${exceptions}", exceptions)
    }

    /**
     * Does parallel each on maps
     */
    public static Object eachParallel(Map collection, Closure cl) {
        eachParallel(createCollection(collection), buildClosureForMaps(cl))
        return collection
    }

    /**
     * Iterates over a collection/object with the <i>eachWithIndex()</i> method using an asynchronous variant of the supplied closure
     * to evaluate each collection's element. A Semaphore is used to make the calling thread wait for all the results.
     * After this method returns, all the closures have been finished and all the potential shared resources have been updated
     * by the threads.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Example:
     *      GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *          def result = Collections.synchronizedSet(new HashSet())
     *          service.eachWithIndexParallel([1, 2, 3, 4, 5]) {Number number -&gt result.add(number * 10)}*          assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     *}* Note that the <i>result</i> variable is synchronized to prevent race conditions between multiple threads.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>eachParallel(Closure cl)</i> method, which delegates to the <i>GParsExecutorsPoolUtil</i> class.
     *    GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *         def result = Collections.synchronizedSet(new HashSet())
     *        [1, 2, 3, 4, 5].eachWithIndexParallel { Number number, int index -&gt result.add(number * 10) }*         assertEquals(new HashSet([10, 20, 30, 40, 50]), result)
     *}* @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static def eachWithIndexParallel(Object collection, Closure cl) {
        final List<Throwable> exceptions = Collections.synchronizedList([])
        final Semaphore semaphore = new Semaphore(0)
        Closure code = async({ Object element, int index ->
            try {
                cl(element, index)
            } catch (Throwable e) {
                exceptions.add(e)
            } finally {
                semaphore.release()
            }
        })
        int count = 0
        for (element in collection) {
            code.call(element, count)
            count += 1
        }
        semaphore.acquire(count)
        if (exceptions.empty) return collection
        else throw new AsyncException("Some asynchronous operations failed. ${exceptions}", exceptions)
    }

    /**
     * Does parallel eachWithIndex on maps
     */
    public static Object eachWithIndexParallel(Map collection, Closure cl) {
        eachWithIndexParallel(createCollection(collection), buildClosureForMapsWithIndex(cl))
        return collection
    }

    /**
     * Iterates over a collection/object with the <i>collect()</i> method using an asynchronous variant of the supplied closure
     * to evaluate each collection's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     *     GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *         def result = service.collectParallel([1, 2, 3, 4, 5]){Number number -&gt number * 10}*         assertEquals(new HashSet([10, 20, 30, 40, 50]), new HashSet((Collection)result))
     *}*
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>collectParallel(Closure cl)</i> method, which delegates to the <i>GParsExecutorsPoolUtil</i> class.
     *     GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *         def result = [1, 2, 3, 4, 5].collectParallel{Number number -&gt number * 10}*         assertEquals(new HashSet([10, 20, 30, 40, 50]), new HashSet((Collection)result))
     *}* @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static Collection<Object> collectParallel(Object collection, Closure cl) {
        return processResult((List) collection.collect(async(cl)))
    }

    /**
     * Does parallel collect on a map
     */
    public static Collection<Object> collectParallel(Map collection, Closure cl) {
        return collectParallel(createCollection(collection), buildClosureForMaps(cl))
    }

    /**
     * Performs the <i>findAll()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     def result = service.findAllParallel([1, 2, 3, 4, 5]){Number number -&gt number > 2}*     assertEquals(new HashSet([3, 4, 5]), new HashSet((Collection)result))
     *}*
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>findAllParallel(Closure cl)</i> method, which delegates to the <i>GParsExecutorsPoolUtil</i> class.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     def result = [1, 2, 3, 4, 5].findAllParallel{Number number -&gt number > 2}*     assertEquals(new HashSet([3, 4, 5]), new HashSet((Collection)result))
     *}* @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static Collection<Object> findAllParallel(Object collection, Closure cl) {
        collectParallel(collection, { if (cl(it)) return it else return null }).findAll { it != null }
    }

    /**
     * Does parallel findAll on a map returning a map of found items
     */
    public static <K, V> Map<K, V> findAllParallel(Map<K, V> collection, Closure cl) {
        return buildResultMap((Collection<Map.Entry>) findAllParallel(createCollection(collection), buildClosureForMaps(cl)))
    }

    /**
     * Performs the <i>grep()()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     def result = service.grepParallel([1, 2, 3, 4, 5])(3..6)
     *     assertEquals(new HashSet([3, 4, 5]), new HashSet((Collection)result))
     *}*
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>findAllParallel(Closure cl)</i> method, which delegates to the <i>GParsExecutorsPoolUtil</i> class.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     def result = [1, 2, 3, 4, 5].grepParallel(3..6)
     *     assertEquals(new HashSet([3, 4, 5]), new HashSet((Collection)result))
     *}* @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static def grepParallel(Object collection, filter) {
        collectParallel(collection, { if (filter.isCase(it)) return it else return null }).findAll { it != null }
    }

    /**
     * Does parallel grep on a map
     */
    public static <K, V> Map<K, V> grepParallel(Map<K, V> collection, filter) {
        return buildResultMap((Collection<Map.Entry>) grepParallel(createCollection(collection), filter in Closure ? buildClosureForMaps((Closure) filter) : filter))
    }

    /**
     * Performs the <i>find()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     def result = service.findParallel([1, 2, 3, 4, 5]){Number number -&gt number > 2}*     assert result in [3, 4, 5]
     *}*
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>findAllParallel(Closure cl)</i> method, which delegates to the <i>GParsExecutorsPoolUtil</i> class.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     def result = [1, 2, 3, 4, 5].findParallel{Number number -&gt number > 2}*     assert result in [3, 4, 5]
     *}* @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static Object findParallel(Object collection, Closure cl) {
        collectParallel(collection, { if (cl(it)) return it else return null }).find { it != null }
    }

    /**
     * Does parallel find on a map
     */
    public static <K, V> Map.Entry<K, V> findParallel(Map<K, V> collection, Closure cl) {
        //noinspection GroovyAssignabilityCheck
        return (Map.Entry) findParallel(createCollection(collection), buildClosureForMaps(cl))
    }

    /**
     * Performs the <i>find()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element. Unlike with the <i>find</i> method, findAnyParallel() does not guarantee
     * that the a matching element with the lowest index is returned.
     * The findAnyParallel() method evaluates elements lazily and stops processing further elements of the collection once a match has been found.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     def result = service.findParallel([1, 2, 3, 4, 5]){Number number -&gt number > 2}*     assert result in [3, 4, 5]
     *}*
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>findAllParallel(Closure cl)</i> method, which delegates to the <i>GParsExecutorsPoolUtil</i> class.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     def result = [1, 2, 3, 4, 5].findParallel{Number number -&gt number > 2}*     assert result in [3, 4, 5]
     *}* @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static def findAnyParallel(Object collection, Closure cl) {
        final AtomicReference result = new AtomicReference(null)
        return processAnyResult(collection.collect { value -> {-> cl(value) ? value : null } })
//        collectParallel(collection, {if ((result.get() == null) && cl(it)) {result.set(it); return it} else return null})
//        return result.get()
    }

    /**
     * Does parallel findAny on a map
     */
    public static Object findAnyParallel(Map collection, Closure cl) {
        return findAnyParallel(createCollection(collection), buildClosureForMaps(cl))
    }

    /**
     * Performs the <i>all()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     assert service.everyParallel([1, 2, 3, 4, 5]){Number number -&gt number > 0}*     assert !service.everyParallel([1, 2, 3, 4, 5]){Number number -&gt number > 2}*}*
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>findAllParallel(Closure cl)</i> method, which delegates to the <i>GParsExecutorsPoolUtil</i> class.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     assert [1, 2, 3, 4, 5].everyParallel{Number number -&gt number > 0}*     assert ![1, 2, 3, 4, 5].everyParallel{Number number -&gt number > 2}*}* @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static boolean everyParallel(Object collection, Closure cl) {
        final AtomicBoolean flag = new AtomicBoolean(true)
        eachParallel(collection, { value -> if (flag.get() && !cl(value)) flag.set(false) })
        return flag.get()
    }

    /**
     * Does parallel every on a map
     */
    public static boolean everyParallel(Map collection, Closure cl) {
        return everyParallel(createCollection(collection), buildClosureForMaps(cl))
    }

    /**
     * Performs the <i>any()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * The anyParallel() method is lazy and once a positive answer has been given by at least one element, it avoids running
     * the supplied closure on subsequent elements.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     assert service.anyParallel([1, 2, 3, 4, 5]){Number number -&gt number > 2}*     assert !service.anyParallel([1, 2, 3, 4, 5]){Number number -&gt number > 6}*}*
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>anyParallel(Closure cl)</i> method, which delegates to the <i>GParsExecutorsPoolUtil</i> class.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     assert [1, 2, 3, 4, 5].anyParallel{Number number -&gt number > 2}*     assert ![1, 2, 3, 4, 5].anyParallel{Number number -&gt number > 6}*}* @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public static boolean anyParallel(Object collection, Closure cl) {
        return processAnyResult(collection.collect { value -> {-> cl(value) } })
//        final AtomicBoolean flag = new AtomicBoolean(false)
//        eachParallel(collection, {if ((!flag.get()) && cl(it)) flag.set(true)})
//        return flag.get()
    }

    /**
     * Does parallel any on a map
     */
    public static boolean anyParallel(Map collection, Closure cl) {
        return anyParallel(createCollection(collection), buildClosureForMaps(cl))
    }

    /**
     * Performs the <i>groupBy()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     assert service.groupByParallel(([1, 2, 3, 4, 5]){Number number -&gt number % 2}).size() == 2
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>groupByParallel(Closure cl)</i> method, which delegates to the <i>GParsExecutorsPoolUtil</i> class.
     * GParsExecutorsPool.withPool(5) {ExecutorService service -&gt
     *     assert ([1, 2, 3, 4, 5].groupByParallel{Number number -&gt number % 2}).size() == 2
     */
    public static Map groupByParallel(Object collection, Closure cl) {
        final def map = new ConcurrentHashMap()
        eachParallel(collection, {
            def result = cl(it)
            final def myList = [it].asSynchronized()
            def list = (List) map.putIfAbsent(result, myList)
            if (list != null) list.add(it)
        })
        return map
    }

    static List<Object> processResult(List<Future<Object>> futures) {
        final Collection<Throwable> exceptions = new ArrayList<Throwable>()

        final List<Object> result = futures.collect { Future<Object> f ->
            try {
                return f.get()
            } catch (Throwable e) {
                exceptions.add(e)
                return e
            }
        }

        if (exceptions.empty) return result
        else throw new AsyncException("Some asynchronous operations failed. ${exceptions}", new ArrayList(exceptions))
    }

    /**
     * Used for methods such as findAnyParallel() or anyParallel(), which may stop some alternatives once the result is known
     *
     * @param alternatives The alternative closures to run
     * @return The result (any) found
     */
    public static def processAnyResult(List<Closure> alternatives) {
        final Collection<Throwable> exceptions = new ArrayList<Throwable>().asSynchronized()
        def result = new DataflowVariable()
        def futures = new DataflowVariable()
        final AtomicInteger totalCounter = new AtomicInteger(alternatives.size())
        futures << GParsExecutorsPool.executeAsync(alternatives.collect {
            original ->
                {->
                    try {
                        def localResult = original()
                        if (localResult) {
                            futures.val*.cancel(true)
                            result.bindSafely(localResult)
                        }
                    } catch (InterruptedException ignore) {
                    } catch (Throwable e) {
                        exceptions.add(e)
                    } finally {
                        if (totalCounter.decrementAndGet() == 0 && !result.isBound()) result << null  //No more results may appear
                    }
                }
        })
        final r = result.val
        if (exceptions.empty) return r
        else throw new AsyncException("Some asynchronous operations failed. ${exceptions}", new ArrayList(exceptions))
    }
}
