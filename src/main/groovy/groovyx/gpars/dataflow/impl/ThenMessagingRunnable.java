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

package groovyx.gpars.dataflow.impl;

import groovy.lang.Closure;
import groovyx.gpars.MessagingRunnable;
import groovyx.gpars.dataflow.DataflowVariable;

/**
 * @author Vaclav Pech
 */

public class ThenMessagingRunnable<T, V> extends MessagingRunnable<T> {
    private final DataflowVariable<V> result;
    private final Closure<V> closure;
    private final Closure<V> errorHandler;

    public ThenMessagingRunnable(final DataflowVariable<V> result, final Closure<V> closure) {
        this(result, closure, null);
    }

    public ThenMessagingRunnable(final DataflowVariable<V> result, final Closure<V> closure, final Closure<V> errorHandler) {
        if (closure.getMaximumNumberOfParameters() > 1)
            throw new IllegalArgumentException("The supplied closure expects more than one argument.");
        if (errorHandler != null && errorHandler.getMaximumNumberOfParameters() > 1)
            throw new IllegalArgumentException("The supplied error handler expects more than one argument.");
        this.result = result;
        this.closure = closure;
        this.errorHandler = errorHandler;
    }

    @Override
    protected void doRun(final T argument) {
        if (argument instanceof Throwable) {
            if (errorHandler != null && shallHandle(errorHandler, (Throwable) argument)) {
                try {
                    result.leftShift(errorHandler.getMaximumNumberOfParameters() == 1 ? errorHandler.call(argument) : errorHandler.call());
                } catch (Throwable e) {
                    result.bindError(e);
                }
            } else {
                result.bindError((Throwable) argument);
            }
        } else {
            try {
                result.leftShift(closure.getMaximumNumberOfParameters() == 1 ? closure.call(argument) : closure.call());
            } catch (Throwable e) {
                result.bindError(e);
            }
        }
    }

    private boolean shallHandle(final Closure<V> errorHandler, final Throwable e) {
        final Class[] types = errorHandler.getParameterTypes();
        if (types.length == 0) return true;
        return types[0].isAssignableFrom(e.getClass());
    }
}
