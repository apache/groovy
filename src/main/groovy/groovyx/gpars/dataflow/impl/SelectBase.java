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

package groovyx.gpars.dataflow.impl;

import groovyx.gpars.dataflow.Dataflow;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.SelectableChannel;
import groovyx.gpars.dataflow.expression.DataflowExpression;
import groovyx.gpars.group.PGroup;

import java.util.*;

/**
 * The implementation of the core for all selects.
 *
 * @author Vaclav Pech
 *         Date: 29th Sep 2010
 */
@SuppressWarnings({"rawtypes", "RawUseOfParameterizedType"})
public final class SelectBase<T> {

    private final List<SelectableChannel<? extends T>> channels;
    private final int numberOfChannels;

    /**
     * Since DataflowVariables should be only read once, they need to be disabled after selecting their value
     * The array stores a boolean flag for each index, indicating, whether the channel/variable has been disabled
     */
    private final boolean[] disabledDFVs;

    /**
     * Unsatisfied requests for value, each holding a list of guards and a routine to invoke once a value is available
     */
    private final Collection<SelectRequest<T>> pendingRequests = new ArrayList<SelectRequest<T>>();

    @SuppressWarnings({"UnsecureRandomNumberGeneration"})
    private final Random position = new Random();

    /**
     * Stores the input channel and registers for the wheneverBound() event on each
     *
     * @param pGroup   The group, the thread pool of which should be used for notification message handlers
     * @param channels All the input channels to select on
     */
    public SelectBase(final PGroup pGroup, final List<SelectableChannel<? extends T>> channels) {
        this.channels = Collections.unmodifiableList(channels);
        numberOfChannels = channels.size();
        disabledDFVs = new boolean[numberOfChannels];
        Arrays.fill(disabledDFVs, false);
        for (int i = 0; i < numberOfChannels; i++) {
            final SelectableChannel<? extends T> channel = channels.get(i);
            final PGroup originalGroup = Dataflow.retrieveCurrentDFPGroup();
            try {
                Dataflow.activeParallelGroup.set(pGroup);
                //noinspection ThisEscapedInObjectConstruction
                channel.wheneverBound(new SelectCallback<T>(this, i, channel));
            } finally {
                Dataflow.activeParallelGroup.set(originalGroup);
            }
        }
    }

    /**
     * Invoked by the SelectCallback instances, potentially concurrently to inform about new values being available for read from channels.
     *
     * @param index   The index of the ready channel
     * @param channel The channel itself
     * @throws InterruptedException If the thread is interrupted during value retrieval from the channel
     */
    @SuppressWarnings({"MethodOnlyUsedFromInnerClass"})
    void boundNotification(final int index, final SelectableChannel<? extends T> channel) throws InterruptedException {
        synchronized (channels) {
            for (final SelectRequest<T> selectRequest : pendingRequests) {
                if (selectRequest.matchesMask(index) && !disabledDFVs[index]) {
                    final DataflowExpression<? extends T> value = channel.poll();
                    if (value != null) {
                        pendingRequests.remove(selectRequest);
                        disableDFV(index, channel);
                        selectRequest.valueFound(index, value.getVal());
                        return;
                    }
                }
            }
        }
    }

    /**
     * Invoked whenever the Select is asked for the next value. Depending on the supplied startIndex value it scans
     * all input channels and reads the first one found, which currently has a value available for read.
     * If no input channel is ready, the supplied SelectRequest instance is registered to be notified by the wheneverBound() channel listeners.
     *
     * @param startIndex    The index of the channel to check first for available messages, -1 if start at a random position. Continue scanning by increasing the index, once the size is reached start from 0.
     * @param selectRequest The request that holds the guards and expects a notification once a value is selected
     * @throws InterruptedException If the thread gets interrupted while reading messages from the channels
     */
    public void doSelect(final int startIndex, final SelectRequest<T> selectRequest) throws InterruptedException {
        final int startPosition = startIndex == -1 ? position.nextInt(numberOfChannels) : startIndex;

        synchronized (channels) {
            for (int i = 0; i < numberOfChannels; i++) {
                final int currentPosition = (startPosition + i) % numberOfChannels;
                if (selectRequest.matchesMask(currentPosition) && !disabledDFVs[currentPosition]) {
                    final SelectableChannel<? extends T> channel = channels.get(currentPosition);
                    final DataflowExpression<? extends T> value = channel.poll();
                    if (value != null) {
                        disableDFV(currentPosition, channel);
                        selectRequest.valueFound(currentPosition, value.getVal());
                        return;
                    }
                }
            }
            pendingRequests.add(selectRequest);
        }
    }

    /**
     * Sets the flag in the disabledDFVs array, if the channel is a DFV
     *
     * @param currentPosition The position to mark
     * @param channel         The channel being considered
     */
    private void disableDFV(final int currentPosition, final SelectableChannel<? extends T> channel) {
        if (channel instanceof DataflowVariable) disabledDFVs[currentPosition] = true;
    }
}
