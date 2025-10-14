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
import groovyx.gpars.actor.StaticDispatchActor;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.DataflowChannelListener;
import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.group.PGroup;

import java.util.List;


/**
 * A base actor class for operators' and selectors' actors
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
abstract class DataflowProcessorActor extends StaticDispatchActor<Object> {
    protected static final String CANNOT_OBTAIN_THE_SEMAPHORE_TO_FORK_OPERATOR_S_BODY = "Cannot obtain the semaphore to fork operator's body.";
    protected final List inputs;
    protected final List outputs;
    protected final Closure code;
    protected final DataflowProcessor owningProcessor;
    protected boolean stoppingGently = false;

    @SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter"})
    DataflowProcessorActor(final DataflowProcessor owningProcessor, final PGroup group, final List outputs, final List inputs, final Closure code) {
        super();
        setParallelGroup(group);

        this.owningProcessor = owningProcessor;
        this.outputs = outputs;
        this.inputs = inputs;
        this.code = code;
    }

    void afterStart() {
        owningProcessor.fireAfterStart();
    }

    void afterStop() {
        owningProcessor.fireAfterStop();
    }

    /**
     * Registers the provided handler to all input channels
     * @param handler The closure to invoke whenever a value gets bound to any of the input channels
     */
    final void registerChannelListenersToAllInputs(final DataflowChannelListener<Object> handler) {
        for (final Object input : inputs) {
            ((DataflowReadChannel<Object>) input).getEventManager().addDataflowChannelListener(handler);
        }
    }
    final void onException(final Throwable e) {
        reportException(e);
        terminate();
    }

    /**
     * Sends the message, ignoring exceptions caused by the actor not being active anymore
     *
     * @param message The message to send
     * @return The current actor
     */
    @Override
    public MessageStream send(final Object message) {
        try {
            super.send(message);
        } catch (IllegalStateException e) {
            if (!hasBeenStopped()) throw e;
        }
        return this;
    }

    /**
     * All messages unhandled by sub-classes will result in an exception being thrown
     *
     * @param message The unhandled message
     */
    @Override
    public void onMessage(final Object message) {
        throw new IllegalStateException("The dataflow actor doesn't recognize the message $message");
    }

    static boolean isControlMessage(final Object message) {
        return message instanceof ControlMessage;
    }

    /**
     * Handles the poisson message.
     * After receiving the poisson a dataflow operator will send the poisson to all its output channels and terminate.
     *
     * @param data The poisson to re-send
     */
    final void checkPoison(final Object data) {
        if (data instanceof PoisonPill) {
            forwardPoisonPill(data);
            owningProcessor.terminate();
            ((PoisonPill) data).countDown();
        }
    }

    protected void forwardPoisonPill(final Object data) {
        owningProcessor.bindAllOutputsAtomically(data);
    }

    final void reportException(final Throwable e) {
        owningProcessor.reportError(e);
    }

    protected Object fireMessageArrived(final Object result, final int index, final boolean controlMessage) {
        if (controlMessage) {
            return owningProcessor.fireControlMessageArrived((DataflowReadChannel) inputs.get(index), index, result);
        } else {
            return owningProcessor.fireMessageArrived((DataflowReadChannel) inputs.get(index), index, result);
        }
    }
}
