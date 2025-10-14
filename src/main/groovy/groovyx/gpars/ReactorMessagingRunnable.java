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

package groovyx.gpars;

import groovy.lang.Closure;

/**
 * Represents a single-argument Closure when using GPars ReactiveActors (reactors) through the Java API.
 * The doRun() method is meant to be defined by implementers to specify the closure body.
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"unchecked"})
public abstract class ReactorMessagingRunnable<T, V> extends Closure {
    private static final long serialVersionUID = 4796783310470426395L;
    private static final Class<?>[] PARAMETER_TYPES = {Object.class};
    private static final String REACTOR_MESSAGING_RUNNABLE_NEED_EXACTLY_ONE_ARGUMENT_TO_RUN = "ReactorMessagingRunnable needs exactly one argument to run.";

    protected ReactorMessagingRunnable() {
        super(null);
    }

    protected ReactorMessagingRunnable(final Object owner) {
        super(owner);
    }

    @Override
    public final int getMaximumNumberOfParameters() {
        return 1;
    }

    @Override
    public final Class<?>[] getParameterTypes() {
        return PARAMETER_TYPES.clone();
    }

    @Override
    public final Object call() {
        throw new UnsupportedOperationException(REACTOR_MESSAGING_RUNNABLE_NEED_EXACTLY_ONE_ARGUMENT_TO_RUN);
    }

    @Override
    public final Object call(final Object[] args) {
        if (args.length != 1)
            throw new UnsupportedOperationException(REACTOR_MESSAGING_RUNNABLE_NEED_EXACTLY_ONE_ARGUMENT_TO_RUN);
        return doRun((T) args[0]);
    }

    @Override
    public final Object call(final Object arguments) {
        return doRun((T) arguments);
    }

    /**
     * Defines the action performed by the Runnable
     *
     * @param argument The parameter passed from the caller to the closure
     * @return The result to return to the caller of the closure and thus to send as a reply from the ReactiveActor
     */
    protected abstract V doRun(final T argument);
}
