// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2013  The original author or authors
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

package groovyx.gpars.dataflow.operator;

import groovy.lang.Closure;
import groovyx.gpars.group.PGroup;

import java.util.Collection;
import java.util.Map;

/**
 * Dataflow selectors and operators (processors) form the basic units in dataflow networks. They are typically combined into oriented graphs that transform data.
 * They accept a set of input and output dataflow channels so that once values are available to be consumed in all
 * the input channels the operator's body is triggered on the values, potentially generating values to be written into the output channels.
 * The output channels at the same time are suitable to be used as input channels by some other dataflow processors.
 * The channels allow processors to communicate.
 * <p>
 * Dataflow selectors and operators enable creation of highly concurrent applications yet the abstraction hides the low-level concurrency primitives
 * and exposes much friendlier API.
 * Since selectors and operators internally leverage the actor implementation, they reuse a pool of threads and so the actual number of threads
 * used by the calculation can be kept much lower than the actual number of processors used in the network.
 * </p>
 *
 * @author Vaclav Pech
 *         Date: Sep 9, 2009
 */
@SuppressWarnings({"RawUseOfParameterizedType", "unchecked"})
public final class DataflowOperator extends DataflowProcessor {

    /**
     * Creates an operator
     * After creation the operator needs to be started using the start() method.
     *
     * @param group    The group the thread pool of which o use
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @param code     The operator's body to run each time all inputs have a value to read
     */
    @SuppressWarnings({"ThisEscapedInObjectConstruction"})
    public DataflowOperator(final PGroup group, final Map channels, final Closure code) {
        super(channels, code);
        final int parameters = code.getMaximumNumberOfParameters();
        if (verifyChannelParameters(channels, parameters, code.getParameterTypes()))
            throw new IllegalArgumentException("The operator's body accepts " + parameters + " parameters while it is given " + countInputChannels(channels) + " input streams. The numbers must match.");
        if (shouldBeMultiThreaded(channels)) {
            checkMaxForks(channels);
            this.actor = new ForkingDataflowOperatorActor(this, group, extractOutputs(channels), extractInputs(channels), (Closure) code.clone(), (Integer) channels.get(MAX_FORKS));
        } else {
            this.actor = new DataflowOperatorActor(this, group, extractOutputs(channels), extractInputs(channels), (Closure) code.clone());
        }
        for (final DataflowEventListener listener : listeners) {
            listener.registered(this);
        }
    }

    private static boolean verifyChannelParameters(final Map channels, final int parameters, final Class[] parameterTypes) {
        if (channels == null) return true;
        final Collection inputs = (Collection) channels.get(INPUTS);
        return inputs == null || inputs.isEmpty() || parameters == 0 || parameters > inputs.size() ||
                (parameters < inputs.size() &&
                        !parameterTypes[parameterTypes.length - 1].isArray());
    }

    private static String countInputChannels(final Map channels) {
        if (channels == null) return "Null";
        final Collection inputs = (Collection) channels.get(INPUTS);
        return String.valueOf(inputs.size());
    }
}
