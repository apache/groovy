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

package groovyx.gpars.dataflow;

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.impl.DataflowChannelEventListenerManager;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.scheduler.Pool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A common interface for all dataflow variables, streams or queues
 *
 * @author Vaclav Pech
 *         Date: 21st Sep 2010
 */
public interface DataflowReadChannel<T> extends SelectableChannel<T> {

    /**
     * Asynchronously retrieves the value from the channel. Sends the actual value of the channel as a message
     * back the the supplied actor once the value has been bound.
     * The actor can perform other activities or release a thread back to the pool by calling react() waiting for the message
     * with the value of the Dataflow channel.
     *
     * @param callback An actor to send the bound value to.
     */
    void getValAsync(final MessageStream callback);

    /**
     * Asynchronously retrieves the value from the channel. Sends a message back the the supplied MessageStream
     * with a map holding the supplied attachment under the 'attachment' key and the actual value of the channel under
     * the 'result' key once the value has been bound.
     * Attachment is an arbitrary value helping the actor.operator match its request with the reply.
     * The actor/operator can perform other activities or release a thread back to the pool by calling react() waiting for the message
     * with the value of the Dataflow channel.
     *
     * @param attachment arbitrary non-null attachment if reader needs better identification of result
     * @param callback   An actor to send the bound value plus the supplied index to.
     */
    void getValAsync(final Object attachment, final MessageStream callback);

    /**
     * Reads the current value of the channel. Blocks, if the value has not been assigned yet.
     *
     * @return The actual value
     * @throws InterruptedException If the current thread gets interrupted while waiting for the channel to be bound
     */
    T getVal() throws InterruptedException;

    /**
     * Reads the current value of the channel. Blocks up to given timeout, if the value has not been assigned yet.
     *
     * @param timeout The timeout value
     * @param units   Units for the timeout
     * @return The actual value
     * @throws InterruptedException If the current thread gets interrupted while waiting for the channel to be bound
     */
    T getVal(final long timeout, final TimeUnit units) throws InterruptedException;

    /**
     * Schedule closure to be executed after data became available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled
     * <p>
     * rightShift() redefines the &gt;&gt; operator so you can write df &gt;&gt; {println it} instead of df.whenBound{println it}
     * </p>
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> rightShift(final Closure<V> closure);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    <V> void whenBound(final Closure<V> closure);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param pool    The thread pool to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    <V> void whenBound(final Pool pool, final Closure<V> closure);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param group   The PGroup to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    <V> void whenBound(final PGroup group, final Closure<V> closure);

    /**
     * Send the bound data to provided stream when it becomes available
     *
     * @param stream stream where to send result
     */
    void whenBound(final MessageStream stream);

    /**
     * Schedule closure to be executed after data became available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> then(final Closure<V> closure);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param pool    The thread pool to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> then(final Pool pool, final Closure<V> closure);

    /**
     * Schedule closure to be executed after data becomes available.
     * It is important to notice that even if the expression is already bound the execution of closure
     * will not happen immediately but will be scheduled.
     *
     * @param group   The PGroup to use for task scheduling for asynchronous message delivery
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     * @return A promise for the results of the supplied closure. This allows for chaining of then() method calls.
     */
    <V> Promise<V> then(final PGroup group, final Closure<V> closure);

    /**
     * Creates and attaches a new operator processing values from the channel
     *
     * @param closure The function to invoke on all incoming values as part of the new operator's body
     * @param <V>     The type of values returned from the supplied closure
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> chainWith(final Closure<V> closure);

    /**
     * Creates and attaches a new operator processing values from the channel
     *
     * @param pool    The thread pool to use
     * @param closure The function to invoke on all incoming values as part of the new operator's body
     * @param <V>     The type of values returned from the supplied closure
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> chainWith(final Pool pool, final Closure<V> closure);

    /**
     * Creates and attaches a new operator processing values from the channel
     *
     * @param group   The PGroup to use
     * @param closure The function to invoke on all incoming values as part of the new operator's body
     * @param <V>     The type of values returned from the supplied closure
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> chainWith(final PGroup group, final Closure<V> closure);

    /**
     * Creates and attaches a new operator processing values from the channel
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param closure The function to invoke on all incoming values as part of the new operator's body
     * @param <V>     The type of values returned from the supplied closure
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> chainWith(final Map<String, Object> params, final Closure<V> closure);

    /**
     * Creates and attaches a new operator processing values from the channel
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool    The thread pool to use
     * @param closure The function to invoke on all incoming values as part of the new operator's body
     * @param <V>     The type of values returned from the supplied closure
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> chainWith(final Pool pool, final Map<String, Object> params, final Closure<V> closure);

    /**
     * Creates and attaches a new operator processing values from the channel
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group   The PGroup to use
     * @param closure The function to invoke on all incoming values as part of the new operator's body
     * @param <V>     The type of values returned from the supplied closure
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> chainWith(final PGroup group, final Map<String, Object> params, final Closure<V> closure);

    /**
     * Creates and attaches a new operator processing values from the channel
     *
     * @param closure The function to invoke on all incoming values as part of the new operator's body
     * @param <V>     The type of values returned from the supplied closure
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> or(final Closure<V> closure);

    /**
     * Creates and attaches a new operator that will filter data using the provided closure
     *
     * @param closure The filter function to invoke on all incoming values to decide whether to pass the value on or not
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> filter(final Closure<Boolean> closure);

    /**
     * Creates and attaches a new operator that will filter data using the provided closure
     *
     * @param pool    The thread pool to use
     * @param closure The filter function to invoke on all incoming values to decide whether to pass the value on or not
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> filter(final Pool pool, final Closure<Boolean> closure);

    /**
     * Creates and attaches a new operator that will filter data using the provided closure
     *
     * @param group   The PGroup to use
     * @param closure The filter function to invoke on all incoming values to decide whether to pass the value on or not
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> filter(final PGroup group, final Closure<Boolean> closure);


    /**
     * Creates and attaches a new operator that will filter data using the provided closure
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param closure The filter function to invoke on all incoming values to decide whether to pass the value on or not
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> filter(final Map<String, Object> params, final Closure<Boolean> closure);

    /**
     * Creates and attaches a new operator that will filter data using the provided closure
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool    The thread pool to use
     * @param closure The filter function to invoke on all incoming values to decide whether to pass the value on or not
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> filter(final Pool pool, final Map<String, Object> params, final Closure<Boolean> closure);

    /**
     * Creates and attaches a new operator that will filter data using the provided closure
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group   The PGroup to use
     * @param closure The filter function to invoke on all incoming values to decide whether to pass the value on or not
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> filter(final PGroup group, final Map<String, Object> params, final Closure<Boolean> closure);


    /**
     * Makes the output of the current channel to be an input for the specified channel
     *
     * @param target The channel to copy data into
     */
    void into(final DataflowWriteChannel<T> target);

    /**
     * Makes the output of the current channel to be an input for the specified channel
     *
     * @param pool   The thread pool to use
     * @param target The channel to copy data into
     */
    void into(final Pool pool, final DataflowWriteChannel<T> target);

    /**
     * Makes the output of the current channel to be an input for the specified channel
     *
     * @param group  The PGroup to use
     * @param target The channel to copy data into
     */
    void into(final PGroup group, final DataflowWriteChannel<T> target);

    /**
     * Makes the output of the current channel to be an input for the specified channel
     *
     * @param params Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param target The channel to copy data into
     */
    void into(final Map<String, Object> params, final DataflowWriteChannel<T> target);

    /**
     * Makes the output of the current channel to be an input for the specified channel
     *
     * @param params Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool   The thread pool to use
     * @param target The channel to copy data into
     */
    void into(final Pool pool, final Map<String, Object> params, final DataflowWriteChannel<T> target);

    /**
     * Makes the output of the current channel to be an input for the specified channel
     *
     * @param params Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group  The PGroup to use
     * @param target The channel to copy data into
     */
    void into(final PGroup group, final Map<String, Object> params, final DataflowWriteChannel<T> target);

    /**
     * Makes the output of the current channel to be an input for the specified channel
     *
     * @param target The channel to copy data into
     */
    void or(final DataflowWriteChannel<T> target);

    /**
     * Splits the output of the current channel to be an input for the specified channels
     *
     * @param target1 The first channel to copy data into
     * @param target2 The second channel to copy data into
     */
    void split(final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2);

    /**
     * Splits the output of the current channel to be an input for the specified channels
     *
     * @param pool    The thread pool to use
     * @param target1 The first channel to copy data into
     * @param target2 The second channel to copy data into
     */
    void split(final Pool pool, final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2);

    /**
     * Splits the output of the current channel to be an input for the specified channels
     *
     * @param group   The PGroup to use
     * @param target1 The first channel to copy data into
     * @param target2 The second channel to copy data into
     */
    void split(final PGroup group, final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2);

    /**
     * Makes the output of the current channel to be an input for the specified channels
     *
     * @param targets The channels to copy data into
     */
    void split(final List<DataflowWriteChannel<T>> targets);

    /**
     * Makes the output of the current channel to be an input for the specified channels
     *
     * @param pool    The thread pool to use
     * @param targets The channels to copy data into
     */
    void split(final Pool pool, final List<DataflowWriteChannel<T>> targets);

    /**
     * Makes the output of the current channel to be an input for the specified channels
     *
     * @param group   The PGroup to use
     * @param targets The channels to copy data into
     */
    void split(final PGroup group, final List<DataflowWriteChannel<T>> targets);

    /**
     * Splits the output of the current channel to be an input for the specified channels
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param target1 The first channel to copy data into
     * @param target2 The second channel to copy data into
     */
    void split(final Map<String, Object> params, final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2);

    /**
     * Splits the output of the current channel to be an input for the specified channels
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool    The thread pool to use
     * @param target1 The first channel to copy data into
     * @param target2 The second channel to copy data into
     */
    void split(final Pool pool, final Map<String, Object> params, final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2);

    /**
     * Splits the output of the current channel to be an input for the specified channels
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group   The PGroup to use
     * @param target1 The first channel to copy data into
     * @param target2 The second channel to copy data into
     */
    void split(final PGroup group, final Map<String, Object> params, final DataflowWriteChannel<T> target1, final DataflowWriteChannel<T> target2);

    /**
     * Makes the output of the current channel to be an input for the specified channels
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param targets The channels to copy data into
     */
    void split(final Map<String, Object> params, final List<DataflowWriteChannel<T>> targets);

    /**
     * Makes the output of the current channel to be an input for the specified channels
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool    The thread pool to use
     * @param targets The channels to copy data into
     */
    void split(final Pool pool, final Map<String, Object> params, final List<DataflowWriteChannel<T>> targets);

    /**
     * Makes the output of the current channel to be an input for the specified channels
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group   The PGroup to use
     * @param targets The channels to copy data into
     */
    void split(final PGroup group, final Map<String, Object> params, final List<DataflowWriteChannel<T>> targets);

    /**
     * Taps into the pipeline. The supplied channel will receive a copy of all messages passed through.
     *
     * @param target The channel to tap data into
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> tap(final DataflowWriteChannel<T> target);

    /**
     * Taps into the pipeline. The supplied channel will receive a copy of all messages passed through.
     *
     * @param pool   The thread pool to use
     * @param target The channel to tap data into
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> tap(final Pool pool, final DataflowWriteChannel<T> target);

    /**
     * Taps into the pipeline. The supplied channel will receive a copy of all messages passed through.
     *
     * @param group  The PGroup to use
     * @param target The channel to tap data into
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> tap(final PGroup group, final DataflowWriteChannel<T> target);

    /**
     * Taps into the pipeline. The supplied channel will receive a copy of all messages passed through.
     *
     * @param params Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param target The channel to tap data into
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> tap(final Map<String, Object> params, final DataflowWriteChannel<T> target);

    /**
     * Taps into the pipeline. The supplied channel will receive a copy of all messages passed through.
     *
     * @param params Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool   The thread pool to use
     * @param target The channel to tap data into
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> tap(final Pool pool, final Map<String, Object> params, final DataflowWriteChannel<T> target);

    /**
     * Taps into the pipeline. The supplied channel will receive a copy of all messages passed through.
     *
     * @param params Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group  The PGroup to use
     * @param target The channel to tap data into
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    DataflowReadChannel<T> tap(final PGroup group, final Map<String, Object> params, final DataflowWriteChannel<T> target);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param other   The channel to merge with
     * @param <V>     The type of values passed between the channels
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final DataflowReadChannel<Object> other, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param pool    The thread pool to use
     * @param other   The channel to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @param <V>     The type of values passed between the channels
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final Pool pool, final DataflowReadChannel<Object> other, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param group   The PGroup to use
     * @param other   The channel to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @param <V>     The type of values passed between the channels
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final PGroup group, final DataflowReadChannel<Object> other, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param others  The channels to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @param <V>     The type of values passed between the channels
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final List<DataflowReadChannel<Object>> others, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param pool    The thread pool to use
     * @param others  The channels to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @param <V>     The type of values passed between the channels
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final Pool pool, final List<DataflowReadChannel<Object>> others, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param group   The PGroup to use
     * @param others  The channels to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @param <V>     The type of values passed between the channels
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final PGroup group, final List<DataflowReadChannel<Object>> others, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param other   The channel to merge with
     * @param <V>     The type of values passed between the channels
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final Map<String, Object> params, final DataflowReadChannel<Object> other, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool    The thread pool to use
     * @param other   The channel to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @param <V>     The type of values passed between the channels
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final Pool pool, final Map<String, Object> params, final DataflowReadChannel<Object> other, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group   The PGroup to use
     * @param other   The channel to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @param <V>     The type of values passed between the channels
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final PGroup group, final Map<String, Object> params, final DataflowReadChannel<Object> other, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param others  The channels to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @param <V>     The type of values passed between the channels
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final Map<String, Object> params, final List<DataflowReadChannel<Object>> others, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool    The thread pool to use
     * @param others  The channels to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @param <V>     The type of values passed between the channels
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final Pool pool, final Map<String, Object> params, final List<DataflowReadChannel<Object>> others, final Closure<V> closure);

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group   The PGroup to use
     * @param others  The channels to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @param <V>     The type of values passed between the channels
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    <V> DataflowReadChannel<V> merge(final PGroup group, final Map<String, Object> params, final List<DataflowReadChannel<Object>> others, final Closure<V> closure);

    /**
     * Directs the output to one of the two output channels depending on the boolean result of the provided closure.
     *
     * @param trueBranch  The channel to send data to if the closure returns true
     * @param falseBranch The channel to send data to if the closure returns true
     * @param code        A closure directing data to either the true or the false output branch
     */
    void binaryChoice(final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code);

    /**
     * Directs the output to one of the two output channels depending on the boolean result of the provided closure.
     *
     * @param pool        The thread pool to use
     * @param trueBranch  The channel to send data to if the closure returns true
     * @param falseBranch The channel to send data to if the closure returns true
     * @param code        A closure directing data to either the true or the false output branch
     */
    void binaryChoice(final Pool pool, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code);

    /**
     * Directs the output to one of the two output channels depending on the boolean result of the provided closure.
     *
     * @param group       The PGroup to use
     * @param trueBranch  The channel to send data to if the closure returns true
     * @param falseBranch The channel to send data to if the closure returns true
     * @param code        A closure directing data to either the true or the false output branch
     */
    void binaryChoice(final PGroup group, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code);

    /**
     * Directs the output to one of the two output channels depending on the boolean result of the provided closure.
     *
     * @param params      Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param trueBranch  The channel to send data to if the closure returns true
     * @param falseBranch The channel to send data to if the closure returns true
     * @param code        A closure directing data to either the true or the false output branch
     */
    void binaryChoice(final Map<String, Object> params, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code);

    /**
     * Directs the output to one of the two output channels depending on the boolean result of the provided closure.
     *
     * @param params      Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool        The thread pool to use
     * @param trueBranch  The channel to send data to if the closure returns true
     * @param falseBranch The channel to send data to if the closure returns true
     * @param code        A closure directing data to either the true or the false output branch
     */
    void binaryChoice(final Pool pool, final Map<String, Object> params, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code);

    /**
     * Directs the output to one of the two output channels depending on the boolean result of the provided closure.
     *
     * @param params      Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group       The PGroup to use
     * @param trueBranch  The channel to send data to if the closure returns true
     * @param falseBranch The channel to send data to if the closure returns true
     * @param code        A closure directing data to either the true or the false output branch
     */
    void binaryChoice(final PGroup group, final Map<String, Object> params, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code);

    /**
     * Directs the output to one of the output channels depending on the int result of the provided closure.
     *
     * @param outputs The channels to send data to of the closure returns true
     * @param code    A closure returning an index of the output channel to direct the data to
     */
    void choice(final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code);

    /**
     * Directs the output to one of the output channels depending on the int result of the provided closure.
     *
     * @param pool    The thread pool to use
     * @param outputs The channels to send data to.
     * @param code    A closure returning an index of the output channel to direct the data to
     */
    void choice(final Pool pool, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code);

    /**
     * Directs the output to one of the output channels depending on the int result of the provided closure.
     *
     * @param group   The PGroup to use
     * @param outputs The channels to send data to.
     * @param code    A closure returning an index of the output channel to direct the data to
     */
    void choice(final PGroup group, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code);

    /**
     * Directs the output to one of the output channels depending on the int result of the provided closure.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param outputs The channels to send data to of the closure returns true
     * @param code    A closure returning an index of the output channel to direct the data to
     */
    void choice(final Map<String, Object> params, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code);

    /**
     * Directs the output to one of the output channels depending on the int result of the provided closure.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool    The thread pool to use
     * @param outputs The channels to send data to.
     * @param code    A closure returning an index of the output channel to direct the data to
     */
    void choice(final Pool pool, final Map<String, Object> params, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code);

    /**
     * Directs the output to one of the output channels depending on the int result of the provided closure.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group   The PGroup to use
     * @param outputs The channels to send data to.
     * @param code    A closure returning an index of the output channel to direct the data to
     */
    void choice(final PGroup group, final Map<String, Object> params, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code);

    /**
     * Allows the closure to output different values to different output channels.
     *
     * @param outputs The channels to send data to.
     * @param code    A closure returning a list of values to pass to the output channels. Values are output to the output channels with identical index.
     */
    void separate(final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code);

    /**
     * Allows the closure to output different values to different output channels.
     *
     * @param pool    The thread pool to use
     * @param outputs The channels to send data to.
     * @param code    A closure returning a list of values to pass to the output channels. Values are output to the output channels with identical index.
     */
    void separate(final Pool pool, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code);

    /**
     * Allows the closure to output different values to different output channels.
     *
     * @param group   The PGroup to use
     * @param outputs The channels to send data to.
     * @param code    A closure returning a list of values to pass to the output channels. Values are output to the output channels with identical index.
     */
    void separate(final PGroup group, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code);

    /**
     * Allows the closure to output different values to different output channels.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param outputs The channels to send data to.
     * @param code    A closure returning a list of values to pass to the output channels. Values are output to the output channels with identical index.
     */
    void separate(final Map<String, Object> params, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code);

    /**
     * Allows the closure to output different values to different output channels.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param pool    The thread pool to use
     * @param outputs The channels to send data to.
     * @param code    A closure returning a list of values to pass to the output channels. Values are output to the output channels with identical index.
     */
    void separate(final Pool pool, final Map<String, Object> params, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code);

    /**
     * Allows the closure to output different values to different output channels.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param group   The PGroup to use
     * @param outputs The channels to send data to.
     * @param code    A closure returning a list of values to pass to the output channels. Values are output to the output channels with identical index.
     */
    void separate(final PGroup group, final Map<String, Object> params, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code);

    /**
     * Retrieves the event manager object of this channel
     *
     * @return The event manager to register custom events listeners
     */
    DataflowChannelEventListenerManager<T> getEventManager();

    /**
     * Check if value has been set already for this expression
     *
     * @return true if bound already
     */
    boolean isBound();

    /**
     * Reports the current number of elements in the channel
     *
     * @return The current snapshot of the number of elements in the channel
     */
    int length();

}
