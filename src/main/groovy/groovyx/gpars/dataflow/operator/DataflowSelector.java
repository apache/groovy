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
import groovyx.gpars.dataflow.Select;
import groovyx.gpars.group.PGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Dataflow selectors and operators (processors) form the basic units in dataflow networks. They are typically combined into oriented graphs that transform data.
 * They accept a set of input and output dataflow channels so that once values are available to be consumed in any
 * of the input channels the selector's body is triggered on the values, potentially generating values to be written into the output channels.
 * The output channels at the same time are suitable to be used as input channels by some other dataflow processors.
 * The channels allow processors to communicate.
 * <p>
 * Dataflow selectors and operators enable creation of highly concurrent applications yet the abstraction hides the low-level concurrency primitives
 * and exposes much friendlier API.
 * Since selectors and operators internally leverage the actor implementation, they reuse a pool of threads and so the actual number of threads
 * used by the calculation can be kept much lower than the actual number of processors used in the network.
 * </p>
 * <p>
 * Selectors select a random value from the values available in the input channels. Optionally the selector's guards mask
 * can be altered to limit the number of channels considered for selection.
 * </p>
 *
 * @author Vaclav Pech
 *         Date: Sep 9, 2009
 */
@SuppressWarnings({"RawUseOfParameterizedType", "unchecked"})
public class DataflowSelector extends DataflowProcessor {

    protected final Select select;
    protected final List<Boolean> guards;

    /**
     * Creates a selector
     * After creation the selector needs to be started using the start() method.
     *
     * @param group    A parallel group to use threads from in the internal actor
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @param code     The selector's body to run each time all inputs have a value to read
     */
    @SuppressWarnings({"ThisEscapedInObjectConstruction"})
    public DataflowSelector(final PGroup group, final Map channels, final Closure code) {
        super(channels, code);
        final int parameters = code.getMaximumNumberOfParameters();
        if (verifyChannelParameters(channels, parameters))
            throw new IllegalArgumentException("The selector's body must accept one or two parameters, while it currently requests " + parameters + " unique parameters.");
        final List inputs = extractInputs(channels);
        final List outputs = extractOutputs(channels);

        if (shouldBeMultiThreaded(channels)) {
            checkMaxForks(channels);
            this.actor = new ForkingDataflowSelectorActor(this, group, outputs, inputs, (Closure) code.clone(), (Integer) channels.get(MAX_FORKS));
        } else {
            this.actor = new DataflowSelectorActor(this, group, outputs, inputs, (Closure) code.clone());
        }
        select = new Select(group, inputs);
        guards = Collections.synchronizedList(new ArrayList<Boolean>((int) inputs.size()));
        //fill in the provided or default guard flags
        final List<Boolean> gs = (List<Boolean>) channels.get("guards");
        if (gs != null) {
            for (int i = 0; i < inputs.size(); i++) {
                guards.add(i, gs.get(i));
            }
        } else {
            //noinspection UnusedDeclaration
            for (final Object input : inputs) guards.add(Boolean.TRUE);
        }
        for (final DataflowEventListener listener : listeners) {
            listener.registered(this);
        }
    }

    private static boolean verifyChannelParameters(final Map channels, final int parameters) {

        if (channels == null) return true;
        final Collection inputs = (Collection) channels.get(INPUTS);
        return inputs == null || inputs.isEmpty() || parameters < 1 || parameters > 2;
    }

    private static String countInputChannels(final Map channels) {
        if (channels == null) return "Null";
        final Collection inputs = (Collection) channels.get(INPUTS);
        return String.valueOf(inputs.size());
    }

    /**
     * Used to enable/disable individual input channels from next selections
     *
     * @param index The index of the channel to enable/disable
     * @param flag  True, if the channel should be included in selection, false otherwise
     */
    public final void setGuard(final int index, final boolean flag) {
        guards.set(index, flag);
    }

    /**
     * Used to enable/disable individual input channels from next selections
     *
     * @param flags The flags to apply to channels
     */
    public final void setGuards(final List<Boolean> flags) {
        for (int i = 0; i < flags.size(); i++) {
            guards.set(i, flags.get(i));
        }
    }

    /**
     * Ask for another select operation on the internal select instance.
     * The selector's guards are applied to the selection.
     */
    protected void doSelect() {
        try {
            select.select(this.actor, guards);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Cannot select a value.", e);
        }
    }

    /**
     * Indicates, whether the selector has some guards enabled and so can select a value from the input channels
     * @return True, if at least input channel guard is enabled
     */
    final boolean allGuardsClosed() {
        for (final Boolean guard : guards) {
            if(guard==Boolean.TRUE) return false;
        }
        return true;
    }
}
