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

package groovyx.gpars.forkjoin;

import groovy.lang.Closure;
import java.util.concurrent.RecursiveTask;

/**
 * A helper class to wrap closures for {@code callAsync} on {@code GParsPool}
 *
 * @author Vaclav Pech
 */
public final class CallAsyncTask<V> extends RecursiveTask<V> {
    private final Closure<V> code;

    public CallAsyncTask(final Closure<V> code) {
        this.code = code;
    }

    @Override
    protected V compute() {
        return code.call();
    }
}
