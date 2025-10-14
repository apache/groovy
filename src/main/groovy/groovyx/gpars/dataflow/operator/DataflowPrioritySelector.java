// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2011, 2013  The original author or authors
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
 * Priority selectors prefer to select messages from channels with lower position index.
 * </p>
 *
 * @author Vaclav Pech
 *         Date: Sep 23, 2009
 */
public final class DataflowPrioritySelector extends DataflowSelector {

    /**
     * Creates a priority selector
     * After creation the selector needs to be started using the start() method.
     *
     * @param group    A parallel group to use threads from in the internal actor
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @param code     The selector's body to run each time all inputs have a value to read
     */
    public DataflowPrioritySelector(final PGroup group, final Map<String, Object> channels, final Closure code) {
        super(group, channels, code);
    }

    /**
     * Ask for another select operation on the internal select instance.
     * The selector's guards are applied to the selection.
     */
    @Override
    public void doSelect() {
        try {
            select.prioritySelect(this.actor, guards);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Couldn't not select a value due to an exception.", e);
        }
    }
}
