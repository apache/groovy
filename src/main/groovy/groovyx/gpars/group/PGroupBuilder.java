// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
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

package groovyx.gpars.group;

import groovyx.gpars.scheduler.DefaultPool;
import groovyx.gpars.scheduler.FJPool;
import groovyx.gpars.scheduler.Pool;
import java.util.concurrent.ForkJoinPool;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Provides convenience factory methods to build PGroups from various types of thread pools
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"UtilityClass", "AbstractClassWithoutAbstractMethods", "AbstractClassNeverImplemented"})
public abstract class PGroupBuilder {
    private PGroupBuilder() {
    }

    /**
     * Builds a PGroup instance from a Pool instance
     *
     * @param pool The pool to wrap by the new group
     * @return The group wrapping the original pool
     */
    public static PGroup createFromPool(final Pool pool) {
        return new DefaultPGroup(pool);
    }

    /**
     * Builds a PGroup instance from a ForkJoinPool instance
     *
     * @param pool The pool to wrap by the new group
     * @return The group wrapping the original pool
     */
    public static PGroup createFromPool(final ForkJoinPool pool) {
        return new DefaultPGroup(new FJPool(pool));
    }

    /**
     * Builds a PGroup instance from a ThreadPoolExecutor instance
     *
     * @param pool The pool to wrap by the new group
     * @return The group wrapping the original pool
     */
    public static PGroup createFromPool(final ThreadPoolExecutor pool) {
        return new DefaultPGroup(new DefaultPool(pool));
    }

}
