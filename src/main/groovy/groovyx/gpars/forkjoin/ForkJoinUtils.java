// GPars - Groovy Parallel Systems
//
// Copyright © 2008--2011  The original author or authors
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

package groovyx.gpars.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import java.util.concurrent.ExecutionException;

/**
 * @author Vaclav Pech
 */
@SuppressWarnings({"UtilityClass", "AbstractClassWithoutAbstractMethods", "AbstractClassNeverImplemented"})
public abstract class ForkJoinUtils {
    private ForkJoinUtils() {
    }

    @SuppressWarnings({"StaticMethodOnlyUsedInOneClass"})
    public static <T> T runForkJoin(final ForkJoinPool pool, final ForkJoinTask<T> rootWorker) throws ExecutionException, InterruptedException {
        if (pool == null)
            throw new IllegalStateException("Cannot initialize ForkJoin. The pool has not been set. Perhaps, we're not inside a GParsPool.withPool() block.");
        return pool.submit(rootWorker).get();
    }

    @SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "OverloadedVarargsMethod"})
    public static <T> T runForkJoin(final ForkJoinPool pool, final Object... args) throws ExecutionException, InterruptedException {
        return runForkJoin(pool, new FJWorker<T>(args));
    }
}
