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

package groovyx.gpars.dataflow;

import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.impl.GuardedSelectRequest;
import groovyx.gpars.dataflow.impl.SelectBase;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.scheduler.Timer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A Select allows the user to select a value from multiple channels, which have a value available for read at the moment.
 * It can either pick a channel randomly, when using the plain select method, or with precedence towards channels with lower position indexes,
 * when the prioritySelect method is used.
 * If a value is not available immediately in any of the channels, Select will wait for the first value to arrive in any of the channels.
 * <p>
 * Both 'select' and 'prioritySelect' methods come in two flavours - blocking, which wait till a value is available in a channel,
 * and messaging, which send out a message to the specified message handler, as soon as a message is available.
 * Optionally, all methods allow the user to specify a boolean mask, assigning each select 's input channel a flag indicating,
 * whether it should be included in the select operation. This is useful when handling state to selectively block some inputs
 * in some states.
 * </p>
 *
 * @author Vaclav Pech
 *         Date: 30th Sep 2010
 */
@SuppressWarnings({"MethodNamesDifferingOnlyByCase"})
public class Select<T> {

    private final SelectBase<T> selectBase;


    /**
     * A value that gets bound to timeout channels through the Select.createTimeout() method
     */
    public static final String TIMEOUT = "timeout";

    /**
     * @param pGroup   The group, the thread pool of which should be used for notification message handlers
     * @param channels The input channels to select from
     */
    @SuppressWarnings({"OverloadedVarargsMethod"})
    public Select(final PGroup pGroup, final SelectableChannel<? extends T>... channels) {
        selectBase = new SelectBase<T>(pGroup, Arrays.asList(channels));
    }

    /**
     * @param pGroup   The group, the thread pool of which should be used for notification message handlers
     * @param channels The list of input channels to select from
     */
    public Select(final PGroup pGroup, final List<SelectableChannel<? extends T>> channels) {
        //noinspection unchecked
        selectBase = new SelectBase<T>(pGroup, channels);
    }

    /**
     * Selects a value from a single randomly chosen input channel, which has a value available for read.
     *
     * @return The read value. It will be of SelectResult type, holding the actual value as well as the channel index.
     * @throws InterruptedException If the current thread gets interrupted
     */
    public SelectResult<T> select() throws InterruptedException {
        return select(-1, null);
    }

    /**
     * Selects a value from a single randomly chosen input channel, which has a value available for read.
     * Only the channels marked with 'true' in the supplied mask will be considered.
     *
     * @param mask A list of boolean values indicating, whether the input channel with the same position index should be included in the selection or not
     * @return The read value. It will be of SelectResult type, holding the actual value as well as the channel index.
     * @throws InterruptedException If the current thread gets interrupted
     */
    public SelectResult<T> select(final List<Boolean> mask) throws InterruptedException {
        return select(-1, mask);
    }

    /**
     * Creates a timeout channel (DataflowVariable) that will bind a Select.TIMEOUT value after the specified timeout.
     *
     * @param timeout The delay in milliseconds to wait before the value gets bound
     * @return A DataflowVariable instance that will have the Select.TIMEOUT value bound after the specified number of milliseconds elapse
     */
    public static DataflowReadChannel<String> createTimeout(final long timeout) {
        final DataflowVariable<String> result = new DataflowVariable<String>();
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                result.bind(TIMEOUT);
            }
        };
        Timer.timer.schedule(task, timeout, TimeUnit.MILLISECONDS);
        return result;
    }

    /**
     * Selects a value from a single randomly chosen input channel, which has a value available for read.
     * The read value is send to the supplied messageStream
     *
     * @param messageStream A message stream accepting the selected value. The message will be of SelectResult type, holding the actual value as well as the channel index.
     * @throws InterruptedException If the current thread gets interrupted
     */
    public void select(final MessageStream messageStream) throws InterruptedException {
        select(messageStream, -1, null);
    }

    /**
     * Selects a value from a single randomly chosen input channel, which has a value available for read.
     * The read value is send to the supplied messageStream
     *
     * @param messageStream A message stream accepting the selected value. The message will be of SelectResult type, holding the actual value as well as the channel index.
     * @param mask          A list of boolean values indicating, whether the input channel with the same position index should be included in the selection or not
     * @throws InterruptedException If the current thread gets interrupted
     */
    public void select(final MessageStream messageStream, final List<Boolean> mask) throws InterruptedException {
        select(messageStream, -1, mask);
    }

    /**
     * Selects asynchronously a value from a single randomly chosen input channel, which has a value available for read.
     * The returned Promise will eventually get bound to the selected value (wrapped inside s SelectResult instance)
     *
     * @throws InterruptedException If the current thread gets interrupted
     */
    public Promise<SelectResult<T>> selectToPromise() throws InterruptedException {
        return selectToPromise(-1, null);
    }

    /**
     * /**
     * Selects asynchronously a value from a single randomly chosen input channel, which has a value available for read.
     * The returned Promise will eventually get bound to the selected value (wrapped inside s SelectResult instance)
     *
     * @param mask A list of boolean values indicating, whether the input channel with the same position index should be included in the selection or not
     * @throws InterruptedException If the current thread gets interrupted
     */
    public Promise<SelectResult<T>> selectToPromise(final List<Boolean> mask) throws InterruptedException {
        return selectToPromise(-1, mask);
    }

    /**
     * Selects a value from a single input channel, which has a value available for read. Channels with lower position index are preferred.
     *
     * @return The read value. It will be of SelectResult type, holding the actual value as well as the channel index.
     * @throws InterruptedException If the current thread gets interrupted
     */
    public SelectResult<T> prioritySelect() throws InterruptedException {
        return select(0, null);
    }

    /**
     * Selects a value from a single input channel, which has a value available for read. Channels with lower position index are preferred.
     * Only the channels marked with 'true' in the supplied mask will be considered.
     *
     * @param mask A list of boolean values indicating, whether the input channel with the same position index should be included in the selection or not
     * @return The read value. It will be of SelectResult type, holding the actual value as well as the channel index.
     * @throws InterruptedException If the current thread gets interrupted
     */
    public SelectResult<T> prioritySelect(final List<Boolean> mask) throws InterruptedException {
        return select(0, mask);
    }

    /**
     * Selects a value from a single input channel, which has a value available for read. Channels with lower position index are preferred.
     * The read value is send to the supplied messageStream
     *
     * @param messageStream A message stream accepting the selected value. The message will be of SelectResult type, holding the actual value as well as the channel index.
     * @throws InterruptedException If the current thread gets interrupted
     */
    public void prioritySelect(final MessageStream messageStream) throws InterruptedException {
        select(messageStream, 0, null);
    }

    /**
     * Selects a value from a single input channel, which has a value available for read. Channels with lower position index are preferred.
     * The read value is send to the supplied messageStream
     *
     * @param messageStream A message stream accepting the selected value. The message will be of SelectResult type, holding the actual value as well as the channel index.
     * @param mask          A list of boolean values indicating, whether the input channel with the same position index should be included in the selection or not
     * @throws InterruptedException If the current thread gets interrupted
     */
    public void prioritySelect(final MessageStream messageStream, final List<Boolean> mask) throws InterruptedException {
        select(messageStream, 0, mask);
    }

    /**
     * Selects asynchronously a value from a single input channel, which has a value available for read. Channels with lower position index are preferred.
     * The returned Promise will eventually get bound to the selected value (wrapped inside s SelectResult instance)
     *
     * @throws InterruptedException If the current thread gets interrupted
     */
    public Promise<SelectResult<T>> prioritySelectToPromise() throws InterruptedException {
        return selectToPromise(0, null);
    }

    /**
     * Selects asynchronously a value from a single input channel, which has a value available for read. Channels with lower position index are preferred.
     * The returned Promise will eventually get bound to the selected value (wrapped inside s SelectResult instance)
     *
     * @param mask A list of boolean values indicating, whether the input channel with the same position index should be included in the selection or not
     * @throws InterruptedException If the current thread gets interrupted
     */
    public Promise<SelectResult<T>> prioritySelectToPromise(final List<Boolean> mask) throws InterruptedException {
        return selectToPromise(0, mask);
    }

    /**
     * Selects a value from a single randomly chosen input channel, which has a value available for read.
     *
     * @return The read value. It will be of SelectResult type, holding the actual value as well as the channel index.
     * @throws InterruptedException If the current thread gets interrupted
     */
    public final SelectResult<T> call() throws InterruptedException {
        return select();
    }

    /**
     * Selects a value from a single randomly chosen input channel, which has a value available for read.
     * Only the channels marked with 'true' in the supplied mask will be considered.
     *
     * @param mask A list of boolean values indicating, whether the input channel with the same position index should be included in the selection or not
     * @return The read value. It will be of SelectResult type, holding the actual value as well as the channel index.
     * @throws InterruptedException If the current thread gets interrupted
     */
    public final SelectResult<T> call(final List<Boolean> mask) throws InterruptedException {
        return select(mask);
    }

    /**
     * Selects a value from a single randomly chosen input channel, which has a value available for read.
     * The read value is send to the supplied messageStream
     *
     * @param messageStream A message stream accepting the selected value. The message will be of SelectResult type, holding the actual value as well as the channel index.
     * @throws InterruptedException If the current thread gets interrupted
     */
    public final void call(final MessageStream messageStream) throws InterruptedException {
        select(messageStream);
    }

    /**
     * Selects a value from a single randomly chosen input channel, which has a value available for read.
     * The read value is send to the supplied messageStream
     *
     * @param messageStream A message stream accepting the selected value. The message will be of SelectResult type, holding the actual value as well as the channel index.
     * @param mask          A list of boolean values indicating, whether the input channel with the same position index should be included in the selection or not
     * @throws InterruptedException If the current thread gets interrupted
     */
    public final void call(final MessageStream messageStream, final List<Boolean> mask) throws InterruptedException {
        select(messageStream, mask);
    }

    /**
     * Invokes the internal select base with a SelectRequest instance ensuring a message is sent, once a value has been selected
     */
    private Promise<SelectResult<T>> selectToPromise(final int startIndex, final List<Boolean> mask) throws InterruptedException {
        final DataflowVariable<SelectResult<T>> result = new DataflowVariable<SelectResult<T>>();
        selectBase.doSelect(startIndex, new GuardedSelectRequest<T>(mask) {
            @Override
            public void valueFound(final int index, final T value) {
                result.bind(new SelectResult<T>(index, value));
            }
        });
        return result;
    }

    /**
     * Invokes the internal select base with a SelectRequest instance ensuring a message is sent, once a value has been selected
     */
    private void select(final MessageStream messageStream, final int startIndex, final List<Boolean> mask) throws InterruptedException {
        selectBase.doSelect(startIndex, new GuardedSelectRequest<T>(mask) {
            @Override
            public void valueFound(final int index, final T value) {
                messageStream.send(new SelectResult<T>(index, value));
            }
        });
    }

    /**
     * Invokes the internal select base with a SelectRequest instance ensuring the current thread can continue returning the correct value, once a value has been selected
     */
    private SelectResult<T> select(final int startIndex, final List<Boolean> mask) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] foundIndex = new int[1];
        @SuppressWarnings({"unchecked"}) final T[] foundValue = (T[]) new Object[1];

        selectBase.doSelect(startIndex, new GuardedSelectRequest<T>(mask) {
            @Override
            public void valueFound(final int index, final T value) {
                foundIndex[0] = index;
                foundValue[0] = value;
                latch.countDown();
            }
        });
        latch.await();
        return new SelectResult<T>(foundIndex[0], foundValue[0]);
    }
}
