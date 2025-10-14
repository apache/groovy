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

// package groovyx.gpars.pa

// import groovyx.gpars.GParsPool
// import groovyx.gpars.GParsPoolUtil
// import groovyx.gpars.TransparentParallel
// import groovyx.gpars.dataflow.DataflowVariable
// // TODO: delete
// //import groovyx.gpars.extra166y.Ops
// //import groovyx.gpars.extra166y.ParallelArray
// //import groovyx.gpars.extra166y.ParallelArrayWithMapping
// // TODO: deal with deprecation
// import groovyx.gpars.memoize.LRUProtectionStorage
// import groovyx.gpars.memoize.NullProtectionStorage
// import groovyx.gpars.memoize.NullValue
// import groovyx.gpars.scheduler.FJPool
// import groovyx.gpars.util.PAUtils
// import java.util.concurrent.ForkJoinPool

// import java.lang.ref.ReferenceQueue
// import java.lang.ref.SoftReference
// import java.util.concurrent.Future

// import static groovyx.gpars.util.PAGroovyUtils.createCollection

// /**
//  * Off-loads some of the heavy-on-Groovy functionality from GParsPoolUtil
//  *
//  * @author Vaclav Pech
//  */
// class GParsPoolUtilHelper {

//     final static NullValue MEMOIZE_NULL = new NullValue();

//     static boolean convertToBoolean(final o) {
//         o as Boolean
//     }

//     public static Closure async(Closure cl) {
//         return { Object... args ->
//             if (args != null && args.size() == 0) GParsPoolUtil.callParallel(cl)
//             else GParsPoolUtil.callParallel({ -> cl(* args) })
//         }
//     }

//     public static <T> Future<T> callAsync(final Closure<T> cl, final Object... args) {
//         return GParsPoolUtil.callParallel({-> cl(* args) });
//     }

//     public static Closure asyncFun(final Closure original, final boolean blocking, final FJPool pool = null) {
//         final FJPool localPool = pool ?: retrieveFJPool();

//         return { final Object[] args ->
//             final DataflowVariable result = new DataflowVariable()
//             PAUtils.evaluateArguments(localPool ?: new FJPool(GParsPoolUtil.retrievePool()), args.clone(), 0, [], result, original, false)
//             blocking ? result.get() : result
//         }
//     }

//     private static FJPool retrieveFJPool() {
//         final retrievedPool = GParsPool.retrieveCurrentPool()
//         if (retrievedPool != null) {
//             return new FJPool(retrievedPool);
//         }
//         return null
//     }

//     public static <T> Closure<T> buildMemoizeFunction(Map cache, Closure<T> cl) {
//         return { Object... args ->
//             final def key = args?.toList() ?: []
//             T result = cache[key]
//             if (result == null) {
//                 result = cl.call(* args)
//                 //noinspection GroovyConditionalCanBeElvis
//                 cache[key] = result != null ? result : MEMOIZE_NULL
//             }
//             result == MEMOIZE_NULL ? null : result
//         }
//     }

//     public static <T> Closure<T> buildSoftReferenceMemoizeFunction(int protectedCacheSize, Map cache, Closure<T> cl) {
//         def lruProtectionStorage = protectedCacheSize > 0 ?
//             new LRUProtectionStorage(protectedCacheSize) :
//             new NullProtectionStorage() //Nothing should be done when no elements need protection against eviction

//         final ReferenceQueue queue = new ReferenceQueue()

//         return { Object... args ->
//             if (queue.poll() != null) cleanUpNullReferences(cache, queue)  //if something has been evicted, do a clean-up
//             final def key = args?.toList() ?: []
//             T result = cache[key]?.get()
//             if (result == null) {
//                 result = cl.call(* args)
//                 if (result == null) {
//                     result = MEMOIZE_NULL
//                 }
//                 cache[key] = new SoftReference(result, queue)
//             }
//             lruProtectionStorage.touch(key, result)
//             result == MEMOIZE_NULL ? null : result
//         }
//     }

//     public static void cleanUpNullReferences(final Map cache, final ReferenceQueue queue) {
//         //noinspection GroovyEmptyStatementBody
//         while (queue.poll() != null) {
//         }  //empty the reference queue
//         cache.findAllParallel({ entry -> entry.value.get() == null }).eachParallel { entry -> cache.remove entry.key }
//     }

//     @SuppressWarnings("GroovyMultipleReturnPointsPerMethod")
//     public static ParallelArray createPA(final Object collection, final ForkJoinPool pool) {
//         if (collection instanceof Object[]) {
//             return createPAFromArray(collection, pool);
//         }
//         if (collection instanceof Map) {
//             return GParsPoolUtil.createPA((Map) collection, pool)
//         }
//         if (collection.respondsTo('toArray')) {
//             return createPAFromCollection(collection, pool)
//         }
//         if (collection instanceof CharSequence) {
//             return createPAFromArray(PAUtils.createArray((CharSequence) collection), pool)
//         }
//         if (collection instanceof Iterable) {
//             return createPAFromCollection(PAUtils.createCollection((Iterable) collection), pool)
//         }
//         if (collection instanceof Iterator) {
//             return createPAFromCollection(PAUtils.createCollection((Iterator) collection), pool)
//         }
//         return createPAFromCollection(createCollection(collection), pool)
//     }

//     public static <T> ParallelArray<T> createPAFromCollection(final Collection<T> collection, final ForkJoinPool pool) {
//         return ParallelArray.createFromCopy(collection.toArray(new T[collection.size()]), pool)
//     }

//     public static ParallelArray createPAFromCollection(final def collection, final ForkJoinPool pool) {
//         return ParallelArray.createFromCopy(collection.toArray(new Object[collection.size()]), pool)
//     }

//     public static <T> ParallelArray<T> createPAFromArray(final T[] array, final ForkJoinPool pool) {
//         return ParallelArray.createFromCopy(array, pool);
//     }

//     /**
//      * Overrides the iterative methods like each(), collect() and such, so that they call their parallel variants from the GParsPoolUtil class
//      * like eachParallel(), collectParallel() and such.
//      * The first time it is invoked on a collection the method creates a TransparentParallel class instance and mixes it
//      * in the object it is invoked on. After mixing-in, the isConcurrent() method will return true.
//      * Delegates to GParsPoolUtil.makeConcurrent().
//      * @param collection The object to make transparent
//      * @return The instance of the TransparentParallel class wrapping the original object and overriding the iterative methods with new parallel behavior
//      */
//     public static Object makeConcurrent(final Object collection) {
//         if (!(collection.respondsTo('isConcurrent'))) throw new IllegalStateException("Cannot make the object transparently concurrent. Apparently we're not inside a GParsPool.withPool() block nor the collection has been enhanced with ParallelEnhancer.enhance().")
//         //noinspection GroovyGetterCallCanBePropertyAccess
//         if (collection.isConcurrent()) {
//             collection.concurrencyActive = true
//         } else collection.getMetaClass().mixin(TransparentParallel)
//         return collection
//     }

//     /**
//      * Gives the iterative methods like each() or find() the original sequential semantics.
//      * @param collection The collection to apply the change to
//      * @return The collection itself
//      */
//     public static Object makeSequential(final Object collection) {
//         if (!(collection.respondsTo('isConcurrent'))) throw new IllegalStateException("Cannot make the object sequential. Apparently we're not inside a GParsPool.withPool() block nor the collection has been enhanced with ParallelEnhancer.enhance().")
//         if (collection.isConcurrent()) {
//             collection.concurrencyActive = false
//         }

//         return collection
//     }

//     public static void eachParallelPA(final ParallelArray pa, final Closure cl) {
//         pa.apply({ cl(it) } as Ops.Procedure)
//     }

//     public static <T> ParallelArrayWithMapping eachWithIndex(ParallelArray<List<Object>> parallelArray, Closure cl) {
//         return parallelArray.withMapping({ cl(it[0], it[1]) } as Ops.Op)
//     }

//     public static <T> Collection<T> findAllParallelPA(ParallelArray<T> pa, Closure cl) {
//         pa.withFilter(new ClosurePredicate({ cl(it) as Boolean })).all().asList();
//     }

//     public static <T> T findParallelPA(ParallelArray<T> pa, Closure cl) {
//         final ParallelArray found = pa.withFilter(new ClosurePredicate({ cl(it) as Boolean })).all();
//         if (found.size() > 0) return found.get(0);
//         else return null;
//     }

//     public static <T> T findAnyParallelPA(ParallelArray<T> pa, Closure cl) {
//         return pa.withFilter(new ClosurePredicate({ cl(it) as Boolean })).any();
//     }

//     public static <T> Collection<T> grepParallelPA(ParallelArray<T> pa, Object filter) {
//         pa.withFilter(new ClosurePredicate({ filter.isCase it })).all().asList();
//     }




//     @Deprecated
//     @SuppressWarnings("GroovyAssignabilityCheck")
//     public static <T> T foldParallel(final Collection<T> collection, seed, final Closure cl) {
//         return GParsPoolUtilHelper.createPAFromCollection(collection + seed, GParsPoolUtil.retrievePool()).reduce(new ClosureReducer(cl), null);
//     }

//     @Deprecated
//     public static Object foldParallel(final Object collection, seed, final Closure cl) {
//         final ParallelArray pa = GParsPoolUtilHelper.createPA(collection, GParsPoolUtil.retrievePool());
//         pa.appendElement(seed);
//         return pa.reduce(new ClosureReducer(cl), null);
//     }

//     @SuppressWarnings("GroovyAssignabilityCheck")
//     public static <T> T injectParallel(final Collection<T> collection, seed, final Closure cl) {
//         return GParsPoolUtilHelper.createPAFromCollection(collection + seed, GParsPoolUtil.retrievePool()).reduce(new ClosureReducer(cl), null);
//     }

//     public static Object injectParallel(final Object collection, seed, final Closure cl) {
//         final ParallelArray pa = GParsPoolUtilHelper.createPA(collection, GParsPoolUtil.retrievePool());
//         pa.appendElement(seed);
//         return pa.reduce(new ClosureReducer(cl), null);
//     }
// }
