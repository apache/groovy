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

package groovyx.gpars.dataflow.operator.component;

import groovyx.gpars.dataflow.DataflowChannelListener;
import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.dataflow.operator.DataflowEventAdapter;
import groovyx.gpars.dataflow.operator.DataflowProcessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Listens to an operator/selector and reports its state and activity to a GracefulShutdownMonitor, shared with other listeners.
 *
 * @author Vaclav Pech
 */
public class GracefulShutdownListener extends DataflowEventAdapter {
    private boolean collectingMessages = false;
    private final AtomicInteger activeForks = new AtomicInteger(0);
    private final OperatorStateMonitor monitor;
    private DataflowProcessor processor=null;
    private volatile boolean shutdownFlag = false;
    private final AtomicLong messagesInChannels = new AtomicLong(0L);

    /**
     * Hooks hooks the shared monitor
     * @param monitor The monitor that will orchestrate the shutdown
     */
    public GracefulShutdownListener(final OperatorStateMonitor monitor) {
        this.monitor = monitor;
        monitor.registerProcessorListener(this);
    }

    /**
     * When hooked into an operator/selectors, the listener will tap to its input channels to get notified about all incoming messages.
     * These incoming messages will be stored temporarily so as the listener can keep track of it until the message is passed to the operator/selector.
     * The messageArrived() event handler will then remove the message from the temporary storage.
     * However, it is not guaranteed that the channel reports an incoming message before the corresponding messageArrived() handler gets invoked.
     * These cases are fine with respect to shutdown, however, we still need to take care of such situation in order to remove the message from teh temporary cache.
     * @param processor The reporting dataflow operator/selector
     */
    @Override
    public void registered(final DataflowProcessor processor) {
        this.processor = processor;
        processor.registerChannelListenersToAllInputs(new DataflowChannelListener<Object>() {
            @Override
            public void onMessage(final Object message) {
                messagesInChannels.incrementAndGet();
            }
        });
    }

    /**
     * Entering a non-idle state, so a notification needs to be sent to the monitor.
     * Also, the received message must be removed from the temporary message cache (or added to it so that so that the not-yet-arrived notification from the channel can remove it).
     * @param processor The reporting dataflow operator/selector
     * @param channel   The input channel holding the message
     * @param index     The index of the input channel within the operator
     * @param message   The incoming message
     * @return The same object that was passed in as the last argument
     */
    @Override
    public Object messageArrived(final DataflowProcessor processor, final DataflowReadChannel<Object> channel, final int index, final Object message) {
        fireEvent();
        collectingMessages = true;
        this.messagesInChannels.decrementAndGet();
        return message;
    }

    /**
     * Entering a non-idle state, so a notification needs to be sent to the monitor.
     * Also, the received message must be removed from the temporary message cache (or added to it so that so that the not-yet-arrived notification from the channel can remove it).
     * @param processor The reporting dataflow operator/selector
     * @param channel   The input channel holding the message
     * @param index     The index of the input channel within the operator
     * @param message   The incoming message
     * @return The same object that was passed in as the last argument
     */
    @Override
    public Object controlMessageArrived(final DataflowProcessor processor, final DataflowReadChannel<Object> channel, final int index, final Object message) {
        fireEvent();
        collectingMessages=false;
        this.messagesInChannels.decrementAndGet();
        return message;
    }

    /**
     * Entering a different non-idle state, so a notification needs to be sent to the monitor.
     * @param processor The reporting dataflow operator/selector
     * @param messages  The incoming messages
     * @return The same set of messages that was passed in
     */
    @Override
    public List<Object> beforeRun(final DataflowProcessor processor, final List<Object> messages) {
        fireEvent();
        collectingMessages=false;
        activeForks.incrementAndGet();
        return messages;
    }

    /**
     * Enters an idle state, so a notification needs to be sent to the monitor.
     * @param processor The reporting dataflow operator/selector
     * @param messages  The incoming messages that have been processed
     */
    @Override
    public void afterRun(final DataflowProcessor processor, final List<Object> messages) {
        fireEvent();
        activeForks.decrementAndGet();
    }

    /**
     * If shutdown is in progress, we'll notify the monitor
     */
    private void fireEvent() {
        if (shutdownFlag) monitor.stateChanged();
    }

    /**
     * Starts the shutdown phase by turning shutdownFlag on
     */
    final void initiateShutdown() {
        shutdownFlag = true;
    }

    /**
     * A quick check on, whether the operator/selector is in the Idle state
     * @return True, if the current state is Idle
     */
    public final boolean isIdle() {
        return !collectingMessages && activeForks.get()==0;
    }

    /**
     * A more sophisticated test for being Idle
     * @return True, if the operator/selector state is Idle, there are no messages in the input channels and there are no messages in the intermediate state between having been removed from the channel and being accepted by the operator
     */
    public final boolean isIdleAndNoIncomingMessages() {
        if(processor==null)
            throw new IllegalStateException("The GracefulShutdownListener has not been registered with a dataflow processor yet.");
        return isIdle() && messagesInChannels.get()<=0L;
    }

    /**
     * Used by the monitor to terminate the underlying operator/selector
     */
    final void terminateProcessor() {
        processor.terminate();
    }
}
