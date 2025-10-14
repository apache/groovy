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

package groovyx.gpars.dataflow.operator;

import groovy.lang.Closure;
import groovyx.gpars.dataflow.Dataflow;
import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.dataflow.DataflowWriteChannel;
import groovyx.gpars.group.DefaultPGroup;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.scheduler.Pool;

import java.util.List;
import java.util.Map;

/**
 * A builder for operator pipelines. The greatest benefit of using the Pipeline class compared to chaining the channels directly is
 * the ease with which a custom thread pool/group can be applied to all the operators along the constructed chain.
 *
 * @author Vaclav Pech
 */
public final class Pipeline {
    private final PGroup group;
    private DataflowReadChannel output;
    private boolean complete = false;

    public Pipeline(final DataflowReadChannel output) {
        this(Dataflow.retrieveCurrentDFPGroup(), output);
    }

    public Pipeline(final Pool pool, final DataflowReadChannel output) {
        this(new DefaultPGroup(pool), output);
        if (pool == null) throw new IllegalArgumentException("A pipeline needs a thread pool to work with.");
    }

    public Pipeline(final PGroup group, final DataflowReadChannel output) {
        if (output == null) throw new IllegalArgumentException("Cannot build a pipeline around a null channel.");
        if (group == null) throw new IllegalArgumentException("A pipeline needs a PGroup instance to work with.");
        this.group = group;
        this.output = output;
    }

    public PGroup getGroup() {
        return group;
    }

    public DataflowReadChannel getOutput() {
        return output;
    }

    public boolean isComplete() {
        return complete;
    }

    private void checkState() {
        if (complete) throw new IllegalStateException("The pipeline has been closed already.");
    }

    /**
     * Creates and attaches a new operator
     *
     * @param closure The function to invoke on all incoming values as part of the new operator's body
     * @param <V>     The type of values returned from the supplied closure
     * @return This Pipeline instance
     */
    public <V> Pipeline chainWith(final Closure<V> closure) {
        checkState();
        output = output.chainWith(group, closure);
        return this;
    }

    /**
     * Creates and attaches a new operator
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param closure The function to invoke on all incoming values as part of the new operator's body
     * @param <V>     The type of values returned from the supplied closure
     * @return This Pipeline instance
     */
    public <V> Pipeline chainWith(final Map<String, Object> params, final Closure<V> closure) {
        checkState();
        output = output.chainWith(group, params, closure);
        return this;
    }

    /**
     * Creates and attaches a new operator
     *
     * @param closure The function to invoke on all incoming values as part of the new operator's body
     * @param <V>     The type of values returned from the supplied closure
     * @return This Pipeline instance
     */
    public <V> Pipeline or(final Closure<V> closure) {
        return chainWith(closure);
    }

    /**
     * Creates and attaches a new operator that will filter data using the provided closure
     *
     * @param closure The filter function to invoke on all incoming values to decide whether to pass the value on or not
     * @return This Pipeline instance
     */
    public Pipeline filter(final Closure<Boolean> closure) {
        checkState();
        output = output.filter(group, closure);
        return this;
    }

    /**
     * Creates and attaches a new operator that will filter data using the provided closure
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param closure The filter function to invoke on all incoming values to decide whether to pass the value on or not
     * @return This Pipeline instance
     */
    public Pipeline filter(final Map<String, Object> params, final Closure<Boolean> closure) {
        checkState();
        output = output.filter(group, params, closure);
        return this;
    }

    /**
     * Makes the output of the pipeline to be an input for the specified channel
     *
     * @param target The channel to copy data into
     * @param <V>    The type of values passed between the channels
     */
    public <V> void into(final DataflowWriteChannel<V> target) {
        checkState();
        output.into(group, target);
        complete = true;
    }

    /**
     * Makes the output of the pipeline to be an input for the specified channel
     *
     * @param params Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param target The channel to copy data into
     * @param <V>    The type of values passed between the channels
     */
    public <V> void into(final Map<String, Object> params, final DataflowWriteChannel<V> target) {
        checkState();
        output.into(group, params, target);
        complete = true;
    }

    /**
     * Makes the output of the pipeline to be an input for the specified channel
     *
     * @param target The channel to copy data into
     * @param <V>    The type of values passed between the channels
     */
    public <V> void or(final DataflowWriteChannel<V> target) {
        into(target);
    }

    /**
     * Splits the output of the pipeline to be an input for the specified channels
     *
     * @param target1 The first channel to copy data into
     * @param target2 The second channel to copy data into
     * @param <V>     The type of values passed between the channels
     */
    public <V> void split(final DataflowWriteChannel<V> target1, final DataflowWriteChannel<V> target2) {
        checkState();
        output.split(group, target1, target2);
        complete = true;
    }

    /**
     * Makes the output of the pipeline to be an input for the specified channels
     *
     * @param targets The channels to copy data into
     * @param <V>     The type of values passed between the channels
     */
    public <V> void split(final List<DataflowWriteChannel<V>> targets) {
        checkState();
        output.split(group, targets);
        complete = true;
    }

    /**
     * Splits the output of the pipeline to be an input for the specified channels
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param target1 The first channel to copy data into
     * @param target2 The second channel to copy data into
     * @param <V>     The type of values passed between the channels
     */
    public <V> void split(final Map<String, Object> params, final DataflowWriteChannel<V> target1, final DataflowWriteChannel<V> target2) {
        checkState();
        output.split(group, params, target1, target2);
        complete = true;
    }

    /**
     * Makes the output of the pipeline to be an input for the specified channels
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param targets The channels to copy data into
     * @param <V>     The type of values passed between the channels
     */
    public <V> void split(final Map<String, Object> params, final List<DataflowWriteChannel<V>> targets) {
        checkState();
        output.split(group, params, targets);
        complete = true;
    }

    /**
     * Taps into the pipeline at the current position, sending all data that pass through the pipeline also to the channel specified.
     *
     * @param target The channel to tap data into
     * @param <V>    The type of values passed between the channels
     * @return This Pipeline instance
     */
    public <V> Pipeline tap(final DataflowWriteChannel<V> target) {
        checkState();
        output = output.tap(group, target);
        return this;
    }

    /**
     * Taps into the pipeline at the current position, sending all data that pass through the pipeline also to the channel specified.
     *
     * @param params Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param target The channel to tap data into
     * @param <V>    The type of values passed between the channels
     * @return This Pipeline instance
     */
    public <V> Pipeline tap(final Map<String, Object> params, final DataflowWriteChannel<V> target) {
        checkState();
        output = output.tap(group, params, target);
        return this;
    }

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param other   The channel to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    Pipeline merge(final DataflowReadChannel<Object> other, final Closure closure) {
        checkState();
        output = output.merge(group, other, closure);
        return this;
    }

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param others  The channels to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    Pipeline merge(final List<DataflowReadChannel<Object>> others, final Closure closure) {
        checkState();
        output = output.merge(group, others, closure);
        return this;
    }

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param other   The channel to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    Pipeline merge(final Map<String, Object> params, final DataflowReadChannel<Object> other, final Closure closure) {
        checkState();
        output = output.merge(group, params, other, closure);
        return this;
    }

    /**
     * Merges channels together as inputs for a single dataflow operator.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param others  The channels to merge with
     * @param closure The function to invoke on all incoming values as part of the new operator's body. The number of arguments to the closure must match the number of input channels.
     * @return A channel of the same type as this channel, which the new operator will output into.
     */
    Pipeline merge(final Map<String, Object> params, final List<DataflowReadChannel<Object>> others, final Closure closure) {
        checkState();
        output = output.merge(group, params, others, closure);
        return this;
    }

    /**
     * Directs the output to one of the two output channels depending on the boolean result of the provided closure.
     *
     * @param trueBranch  The channel to send data to if the closure returns true
     * @param falseBranch The channel to send data to if the closure returns true
     * @param code        A closure directing data to either the true or the false output branch
     */
    public <T> void binaryChoice(final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code) {
        checkState();
        output.binaryChoice(group, trueBranch, falseBranch, code);
        complete = true;
    }

    /**
     * Directs the output to one of the two output channels depending on the boolean result of the provided closure.
     *
     * @param params      Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param trueBranch  The channel to send data to if the closure returns true
     * @param falseBranch The channel to send data to if the closure returns true
     * @param code        A closure directing data to either the true or the false output branch
     */
    public <T> void binaryChoice(final Map<String, Object> params, final DataflowWriteChannel<T> trueBranch, final DataflowWriteChannel<T> falseBranch, final Closure<Boolean> code) {
        checkState();
        output.binaryChoice(group, params, trueBranch, falseBranch, code);
        complete = true;
    }

    /**
     * Directs the output to one of the output channels depending on the int result of the provided closure.
     *
     * @param outputs The channels to send data to.
     * @param code    A closure returning an index of the output channel to direct the data to
     */
    public <T> void choice(final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code) {
        checkState();
        output.choice(group, outputs, code);
        complete = true;
    }

    /**
     * Directs the output to one of the output channels depending on the int result of the provided closure.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param outputs The channels to send data to.
     * @param code    A closure returning an index of the output channel to direct the data to
     */
    public <T> void choice(final Map<String, Object> params, final List<DataflowWriteChannel<T>> outputs, final Closure<Integer> code) {
        checkState();
        output.choice(group, params, outputs, code);
        complete = true;
    }

    /**
     * Allows the closure to output different values to different output channels.
     *
     * @param outputs The channels to send data to.
     * @param code    A closure returning a list of values to pass to the output channels. Values are output to the output channels with identical index.
     */
    public void separate(final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code) {
        checkState();
        output.separate(group, outputs, code);
        complete = true;
    }

    /**
     * Allows the closure to output different values to different output channels.
     *
     * @param params  Additional parameters to initialize the operator with (e.g. listeners or maxForks)
     * @param outputs The channels to send data to.
     * @param code    A closure returning a list of values to pass to the output channels. Values are output to the output channels with identical index.
     */
    public void separate(final Map<String, Object> params, final List<DataflowWriteChannel<?>> outputs, final Closure<List<Object>> code) {
        checkState();
        output.separate(group, params, outputs, code);
        complete = true;
    }
}
