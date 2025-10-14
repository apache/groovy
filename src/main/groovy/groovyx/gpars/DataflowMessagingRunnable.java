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

package groovyx.gpars;

import groovy.lang.Closure;
import groovyx.gpars.dataflow.operator.DataflowProcessor;

import java.util.Arrays;

/**
 * Represents a multi-argument Closure when using GPars dataflow operators and selectors through the Java API.
 * The doRun() method is meant to be defined by implementers to specify the closure body.
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"rawtypes", "RawUseOfParameterizedType"})
public abstract class DataflowMessagingRunnable extends Closure {
    private static final long serialVersionUID = 4796783310470426395L;
    private final Class[] defaultParamTypes;
    private final int numberOfParameters;

    protected DataflowMessagingRunnable(final int numberOfParameters) {
        this(null, numberOfParameters);
    }

    protected DataflowMessagingRunnable(final Object owner, final int numberOfParameters) {
        super(owner);
        this.numberOfParameters = numberOfParameters;
        this.defaultParamTypes = new Class[numberOfParameters];
        Arrays.fill(this.defaultParamTypes, Object.class);
    }

    /**
     * Retrieves the owning processor (operator or selector) giving the DataflowMessagingRunnable a way to call methods like bindOutput()
     *
     * @return The owning processor
     */
    public DataflowProcessor getOwningProcessor() {
        return (DataflowProcessor) getDelegate();
    }

    @Override
    public int getMaximumNumberOfParameters() {
        return numberOfParameters;
    }

    /**
     * Returns types expected by the Runnable. By default instances of the Object class are demanded.
     * The size of the array must match the numberOfParameters parameter to the constructor.
     *
     * @return Types of expected arguments
     */
    @Override
    public Class[] getParameterTypes() {
        return defaultParamTypes.clone();
    }

    @Override
    public final Object call() {
        throw new UnsupportedOperationException("DFMessageRunnable needs arguments to run.");
    }

    @Override
    public final Object call(final Object... args) {
        doRun(args);
        return null;
    }

    /**
     * Defines the action performed by the Runnable
     *
     * @param arguments The parameters passed from the caller to the closure
     */
    protected abstract void doRun(final Object... arguments);
}
