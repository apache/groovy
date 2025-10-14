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

import groovyx.gpars.scheduler.FJPool
import groovyx.gpars.scheduler.Pool

/**
 * ParallelEnhancer allows classes or instances to be enhanced with parallel variants of iterative methods,
 * like eachParallel(), collectParallel(), findAllParallel() and others. These operations split processing into multiple
 * concurrently executable tasks and perform them on the underlying instance of the ForkJoinPool class from JSR-166y.
 * The pool itself is stored in a final property threadPool and can be managed through static methods
 * on the ParallelEnhancer class.
 * All enhanced classes and instances will share the underlying pool. Use the getThreadPool() method to get hold of the thread pool.
 *
 * @author Vaclav Pech
 * Date: Jun 15, 2009
 */
public final class ParallelEnhancer {

    /**
     * Holds the internal ForkJoinPool instance wrapped into a FJPool
     */
    private final static FJPool threadPool = new FJPool()

    /**
     * Enhances a single instance by mixing-in an instance of Parallel.
     */
    public static Object enhanceInstance(Object collection) {
        //noinspection GroovyGetterCallCanBePropertyAccess
        collection.getMetaClass().mixin Parallel
        return collection
    }

    /**
     * Enhances a class and so all instances created in the future by mixing-in an instance of Parallel.
     * Enhancing classes needs to be done with caution, since it may have impact in unrelated parts of the application.
     */
    public static void enhanceClass(Class clazz) {
        //noinspection GroovyGetterCallCanBePropertyAccess
        clazz.getMetaClass().mixin Parallel
    }

    /**
     * Retrieves the underlying pool
     */
    public static Pool getThreadPool() { return threadPool }
}
