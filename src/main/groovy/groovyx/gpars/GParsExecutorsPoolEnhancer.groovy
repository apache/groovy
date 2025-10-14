// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
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

import groovyx.gpars.scheduler.DefaultPool
import groovyx.gpars.scheduler.Pool

/**
 * GParsExecutorsPoolEnhancer allows classes or instances to be enhanced with asynchronous variants of iterative methods,
 * like eachParallel(), collectParallel(), findAllParallel() and others. These operations split processing into multiple
 * concurrently executable tasks and perform them on the underlying instance of an ExecutorService.
 * The pool itself is stored in a final property threadPool and can be managed through static methods
 * on the GParsExecutorsPoolEnhancer class.
 * All enhanced classes and instances will share the underlying pool.
 *
 * @author Vaclav Pech
 * Date: Jun 15, 2009
 */
public final class GParsExecutorsPoolEnhancer {

    /**
     * Holds the internal ExecutorService instance wrapped into a DefaultPool
     */
    @SuppressWarnings("GroovyConstantNamingConvention")
    private final static DefaultPool threadPool = new DefaultPool(true)

    /**
     * Enhances a single instance by mixing-in an instance of GParsExecutorsPoolEnhancer.
     */
    public static void enhanceInstance(Object collection) {
        //noinspection GroovyGetterCallCanBePropertyAccess
        collection.getMetaClass().mixin GParsExecutorsPoolEnhancer
    }

    /**
     * Enhances a class and so all instances created in the future by mixing-in an instance of GParsExecutorsPoolEnhancer.
     * Enhancing classes needs to be done with caution, since it may have impact in unrelated parts of the application.
     */
    public static void enhanceClass(Class clazz) {
        //noinspection GroovyGetterCallCanBePropertyAccess
        clazz.getMetaClass().mixin GParsExecutorsPoolEnhancer
    }

    /**
     * Retrieves the underlying pool
     */
    public Pool getThreadPool() { return threadPool }

    /**
     * Iterates over a collection/object with the <i>each()</i> method using an asynchronous variant of the supplied closure
     * to evaluate each collection's element. A CountDownLatch is used to make the calling thread wait for all the results.
     * After this method returns, all the closures have been finished and all the potential shared resources have been updated
     * by the threads.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public def eachParallel(Closure cl) {
        groovyx.gpars.GParsExecutorsPool.withExistingPool(threadPool.executorService) {
            GParsExecutorsPoolUtil.eachParallel(mixedIn[Object], cl)
        }
    }

    /**
     * Iterates over a collection/object with the <i>eachWithIndex()</i> method using an asynchronous variant of the supplied closure
     * to evaluate each collection's element. A CountDownLatch is used to make the calling thread wait for all the results.
     * After this method returns, all the closures have been finished and all the potential shared resources have been updated
     * by the threads.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public def eachWithIndexParallel(Closure cl) {
        groovyx.gpars.GParsExecutorsPool.withExistingPool(threadPool.executorService) {
            GParsExecutorsPoolUtil.eachWithIndexParallel(mixedIn[Object], cl)
        }
    }

    /**
     * Iterates over a collection/object with the <i>collect()</i> method using an asynchronous variant of the supplied closure
     * to evaluate each collection's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public def collectParallel(Closure cl) {
        groovyx.gpars.GParsExecutorsPool.withExistingPool(threadPool.executorService) {
            GParsExecutorsPoolUtil.collectParallel(mixedIn[Object], cl)
        }
    }

    /**
     * Performs the <i>findAll()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public def findAllParallel(Closure cl) {
        groovyx.gpars.GParsExecutorsPool.withExistingPool(threadPool.executorService) {
            GParsExecutorsPoolUtil.findAllParallel(mixedIn[Object], cl)
        }
    }

    /**
     * Performs the <i>grep()()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public def grepParallel(Closure cl) {
        groovyx.gpars.GParsExecutorsPool.withExistingPool(threadPool.executorService) {
            GParsExecutorsPoolUtil.grepParallel(mixedIn[Object], cl)
        }
    }

    /**
     * Performs the <i>find()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public def findParallel(Closure cl) {
        groovyx.gpars.GParsExecutorsPool.withExistingPool(threadPool.executorService) {
            GParsExecutorsPoolUtil.findParallel(mixedIn[Object], cl)
        }
    }

    /**
     * Performs the <i>all()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public boolean everyParallel(Closure cl) {
        groovyx.gpars.GParsExecutorsPool.withExistingPool(threadPool.executorService) {
            GParsExecutorsPoolUtil.everyParallel(mixedIn[Object], cl)
        }
    }

    /**
     * Performs the <i>any()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public boolean anyParallel(Closure cl) {
        groovyx.gpars.GParsExecutorsPool.withExistingPool(threadPool.executorService) {
            GParsExecutorsPoolUtil.anyParallel(mixedIn[Object], cl)
        }
    }

    /**
     * Performs the <i>groupBy()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * @throws AsyncException If any of the collection's elements causes the closure to throw an exception. The original exceptions will be stored in the AsyncException's concurrentExceptions field.
     */
    public def groupByParallel(Closure cl) {
        groovyx.gpars.GParsExecutorsPool.withExistingPool(threadPool.executorService) {
            GParsExecutorsPoolUtil.groupByParallel(mixedIn[Object], cl)
        }
    }
}
