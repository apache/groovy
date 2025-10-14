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

package groovyx.gpars.util;

import java.util.List;

import groovy.lang.Closure;

import groovyx.gpars.ReactorMessagingRunnable;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.scheduler.Pool;

import org.codehaus.groovy.runtime.InvokerInvocationException;

public class AsyncUtils {

    /**
     * Performs a single step in the evaluation of parameters passed into an asynchronous function
     *
     * @param pool             The thread pool to use
     * @param args             The list of original arguments
     * @param current          The index of the current argument to evaluate
     * @param soFarArgs        A list of arguments evaluated so far
     * @param result           The DFV expecting the function result to be bound to once calculated
     * @param original         The original non-asynchronous function to invoke once all arguments are available
     * @param pooledThreadFlag Indicates, whether we now run in a pooled thread so we don't have to schedule the original function invocation, once all arguments have been bound
     */
    @SuppressWarnings({"unchecked"})
    public static <T> void evaluateArguments(final Pool pool, final Object[] args, final int current, final List<Object> soFarArgs,
                                             final DataflowVariable<Object> result, final Closure<T> original, final boolean pooledThreadFlag) {
        if (current == args.length) {
            if (pooledThreadFlag) {
                try {
                    final Object call = original.call(soFarArgs.toArray(new Object[soFarArgs.size()]));
                    result.leftShift(call);
                } catch (InvokerInvocationException e) {
                    result.bind(e.getCause());
                } catch (Exception all) {
                    result.bind(all);
                } catch (Error error) {
                    result.bind(error);
                    throw error;
                }
            } else {
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            result.leftShift(original.call(soFarArgs.toArray(new Object[soFarArgs.size()])));
                        } catch (InvokerInvocationException e) {
                            result.bind(e.getCause());
                        } catch (Exception all) {
                            result.bind(all);
                        } catch (Error error) {
                            result.bind(error);
                            throw error;
                        }
                    }
                });
            }
        } else {
            final Object currentArgument = args[current];
            if (currentArgument instanceof DataflowVariable) {
                final DataflowVariable<Object> variable = (DataflowVariable<Object>) currentArgument;
                if (variable.isBound()) {
                    Object currentValue = null;
                    try {
                        currentValue = variable.getVal();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Interrupted while processing arguments", e);
                    }
                    if (currentValue instanceof Throwable) result.leftShift(currentValue);
                    else {
                        soFarArgs.add(currentValue);
                        evaluateArguments(pool, args, current + 1, soFarArgs, result, original, pooledThreadFlag);
                    }
                } else {
                    variable.whenBound(pool, new ReactorMessagingRunnable() {
                        @Override
                        protected Object doRun(final Object argument) {
                            if (argument instanceof Throwable) result.leftShift(argument);
                            else {
                                soFarArgs.add(argument);
                                evaluateArguments(pool, args, current + 1, soFarArgs, result, original, true);
                            }
                            return null;
                        }
                    });
                }
            } else {
                soFarArgs.add(currentArgument);
                evaluateArguments(pool, args, current + 1, soFarArgs, result, original, pooledThreadFlag);
            }
        }
    }
}
