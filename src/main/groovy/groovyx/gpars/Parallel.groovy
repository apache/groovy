// GPars - Groovy Parallel Systems
//
// Copyright © 2008–2012, 2014  The original author or authors
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

// TODO: delete
//import groovyx.gpars.pa.PAWrapper

/**
 * The Parallel interface holds methods that ParallelEnhancer adds to classes or instances when they get enhanced.
 *
 * @author Vaclav Pech
 * Date: Nov 1, 2009
 */
final class Parallel {

    /**
     * Iterates over a collection/object with the <i>each()</i> method using an asynchronous variant of the supplied closure
     * to evaluate each collection's element.
     * After this method returns, all the closures have been finished and all the potential shared resources have been updated
     * by the threads.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public def eachParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.eachParallel(getRealSelf(), cl)
        }
    }

    /**
     * Iterates over a collection/object with the <i>eachWithIndex()</i> method using an asynchronous variant of the supplied closure
     * to evaluate each collection's element.
     * After this method returns, all the closures have been finished and all the potential shared resources have been updated
     * by the threads.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public def eachWithIndexParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.eachWithIndexParallel(getRealSelf(), cl)
        }
    }

    /**
     * Iterates over a collection/object with the <i>collect()</i> method using an asynchronous variant of the supplied closure
     * to evaluate each collection's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     * */
    public def collectParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            enhance(GParsPoolUtil.collectParallel(getRealSelf(), cl))
        }
    }

    /**
     * Iterates over a collection/object with the <i>collectMany()</i> method using an asynchronous variant of the supplied closure
     * to evaluate each collection's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     * */
    public def collectManyParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            enhance(GParsPoolUtil.collectManyParallel(getRealSelf(), cl))
        }
    }

    /**
     * Performs the <i>findAll()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public def findAllParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            enhance(GParsPoolUtil.findAllParallel(getRealSelf(), cl))
        }
    }

    /**
     * Performs the <i>grep()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public def grepParallel(Object filter) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            enhance(GParsPoolUtil.grepParallel(getRealSelf(), filter))
        }
    }

    /**
     * Performs the <i>split()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public def splitParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            enhance(GParsPoolUtil.splitParallel(getRealSelf(), cl))
        }
    }

    /**
     * Performs the <i>count()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public def countParallel(Object filter) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            enhance(GParsPoolUtil.countParallel(getRealSelf(), filter))
        }
    }

    /**
     * Performs the <i>find()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public def findParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.findParallel(getRealSelf(), cl)
        }
    }

    /**
     * Performs the <i>find()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * Unlike with the <i>find</i> method, findAnyParallel() does not guarantee
     * that the a matching element with the lowest index is returned.
     * The findAnyParallel() method evaluates elements lazily and stops processing further elements of the collection once a match has been found.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public def findAnyParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.findAnyParallel(getRealSelf(), cl)
        }
    }

    /**
     * Performs the <i>all()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public boolean everyParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.everyParallel(getRealSelf(), cl)
        }
    }

    /**
     * Performs the <i>any()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * The anyParallel() method is lazy and once a positive answer has been given by at least one element, it avoids running
     * the supplied closure on subsequent elements.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public boolean anyParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.anyParallel(getRealSelf(), cl)
        }
    }

    /**
     * Performs the <i>groupBy()</i> operation using an asynchronous variant of the supplied closure
     * to evaluate each collection's/object's element.
     * After this method returns, all the closures have been finished and the caller can safely use the result.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * If any of the collection's elements causes the closure to throw an exception, the exception is re-thrown.
     */
    public def groupByParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.groupByParallel(getRealSelf(), cl)
        }
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its min() method using the supplied
     * closure as the comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the minimum of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>min(Closure cl)</i> method, which delegates to the <i>GParsPoolUtil</i> class.
     * If the supplied closure takes two arguments it is used directly as a comparator.
     * If the supplied closure takes one argument, the values returned by the supplied closure for individual elements are used for comparison by the implicit comparator.
     * @param cl A one or two-argument closure
     */
    public def minParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.minParallel(getRealSelf(), cl)
        }
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its min() method using the default comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the minimum of the elements in the collection.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>min(Closure cl)</i> method, which delegates to the <i>GParsPoolUtil</i> class.
     */
    public def minParallel() {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.minParallel(getRealSelf())
        }
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its max() method using the supplied
     * closure as the comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the maximum of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>min(Closure cl)</i> method, which delegates to the <i>GParsPoolUtil</i> class.
     * If the supplied closure takes two arguments it is used directly as a comparator.
     * If the supplied closure takes one argument, the values returned by the supplied closure for individual elements are used for comparison by the implicit comparator.
     * @param cl A one or two-argument closure
     */
    public def maxParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.maxParallel(getRealSelf(), cl)
        }
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its max() method using the default comparator.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the maximum of the elements in the collection.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>min(Closure cl)</i> method, which delegates to the <i>GParsPoolUtil</i> class.
     */
    public def maxParallel() {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.maxParallel(getRealSelf())
        }
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and summarizes its elements using the foldParallel()
     * method with the + operator and the reduction operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the sum of the elements in the collection.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>min(Closure cl)</i> method, which delegates to the <i>GParsPoolUtil</i> class.
     */
    public def sumParallel() {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.sumParallel(getRealSelf())
        }
    }

//    /**
//     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
//     * closure as the reduction operation.
//     * The closure will be effectively invoked concurrently on the elements of the collection.
//     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
//     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
//     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
//     * have a new <i>min(Closure cl)</i> method, which delegates to the <i>GParsPoolUtil</i> class.
//     */
//    @Deprecated
//    public def foldParallel(Closure cl) {
//        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
//            GParsPoolUtil.foldParallel(getRealSelf(), cl)
//        }
//    }
//
//    /**
//     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
//     * closure as the reduction operation.
//     * The closure will be effectively invoked concurrently on the elements of the collection.
//     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
//     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
//     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
//     * have a new <i>min(Closure cl)</i> method, which delegates to the <i>GParsPoolUtil</i> class.
//     * @param seed A seed value to initialize the operation
//     */
//    @Deprecated
//    public def foldParallel(seed, Closure cl) {
//        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
//            GParsPoolUtil.foldParallel(getRealSelf(), seed, cl)
//        }
//    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
     * closure as the reduction operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>min(Closure cl)</i> method, which delegates to the <i>GParsPoolUtil</i> class.
     */
    public def injectParallel(Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.injectParallel(getRealSelf(), cl)
        }
    }

    /**
     * Creates a Parallel Array out of the supplied collection/object and invokes its reduce() method using the supplied
     * closure as the reduction operation.
     * The closure will be effectively invoked concurrently on the elements of the collection.
     * After all the elements have been processed, the method returns the reduction result of the elements in the collection.
     * It's important to protect any shared resources used by the supplied closure from race conditions caused by multi-threaded access.
     * Alternatively a DSL can be used to simplify the code. All collections/objects within the <i>withPool</i> block
     * have a new <i>min(Closure cl)</i> method, which delegates to the <i>GParsPoolUtil</i> class.
     * @param seed A seed value to initialize the operation
     */
    public def injectParallel(seed, Closure cl) {
        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
            GParsPoolUtil.injectParallel(getRealSelf(), seed, cl)
        }
    }

//    /**
//     * Creates a PAWrapper around a ParallelArray wrapping te elements of the original collection.
//     * This allows further parallel processing operations on the collection to chain and so effectively leverage the underlying
//     * ParallelArray implementation.
//     */
//    public PAWrapper getParallel() {
//        GParsPool.ensurePool(ParallelEnhancer.threadPool.forkJoinPool) {
//            GParsPoolUtil.getParallel(getRealSelf())
//        }
//    }

    /**
     * Indicates, whether the iterative methods like each() or collect() have been made parallel.
     */
    public def boolean isConcurrent() { return false }

    /**
     * Overrides the iterative methods like each(), collect() and such, so that they call their parallel variants from the GParsPoolUtil class
     * like eachParallel(), collectParallel() and such.
     * The first time it is invoked on a collection the method creates a TransparentParallel class instance and mixes it
     * in the object it is invoked on. After mixing-in, the isConcurrent() method will return true.
     * Delegates to GParsPoolUtil.makeConcurrent().
     * @param collection The object to make transparent
     * @return The instance of the TransparentParallel class wrapping the original object and overriding the iterative methods with new parallel behavior
     */
    static Object makeConcurrent(Object collection) {
        GParsPoolUtil.makeConcurrent(collection)
    }

    /**
     * Gives the iterative methods like each() or find() the original sequential semantics.
     * @param collection The collection to apply the change to
     * @return The collection itself
     */
    static Object makeSequential(Object collection) {
        GParsPoolUtil.makeSequential(collection)
        return collection
    }

    /**
     * Makes the collection concurrent for the passed-in block of code.
     * The iterative methods like each or collect are given concurrent semantics inside the passed-in closure.
     * Once the closure finishes, the original sequential semantics of the methods is restored.
     * @param collection The collection to enhance
     * @param code The closure to run with the collection enhanced.
     */
    static void asConcurrent(Object collection, Closure code) {
        GParsPoolUtil.asConcurrent(collection, code)
    }

    /**
     * Enhances to resulting collection so that parallel methods can be chained.
     */
    @SuppressWarnings("GroovyMultipleReturnPointsPerMethod")
    private static def enhance(Object collection) {
        ParallelEnhancer.enhanceInstance(collection)
    }

    /**
     * Retrieves the mixed it parent and potentially casts it dynamically to a Map.
     * The cast to Map is needed since Maps additionally accept a two-argument closures in iterative methods.
     * @return The mixin parent, optionally converted into a Map
     */
    private def getRealSelf() {
        final def self = mixedIn[Object]
        if (Map.isAssignableFrom(self.getClass())) return self as Map
        return self
    }
}
