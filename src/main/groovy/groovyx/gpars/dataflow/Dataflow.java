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

package groovyx.gpars.dataflow;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovyx.gpars.GParsConfig;
import groovyx.gpars.dataflow.operator.DataflowOperator;
import groovyx.gpars.dataflow.operator.DataflowProcessor;
import groovyx.gpars.dataflow.operator.DataflowSelector;
import groovyx.gpars.group.DefaultPGroup;
import groovyx.gpars.group.PGroup;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;

/**
 * Contains factory methods to create dataflow actors and starting them.
 *
 * @author Vaclav Pech, Dierk Koenig
 *         Date: Jun 4, 2009
 */
@SuppressWarnings({"rawtypes", "RawUseOfParameterizedType", "AbstractClassWithoutAbstractMethods", "AbstractClassNeverImplemented", "ConstantDeclaredInAbstractClass", "UtilityClass", "unchecked"})
public abstract class Dataflow {

    /**
     * The parallel group used by all Dataflow Concurrency actors by default.
     */
    public static final PGroup DATA_FLOW_GROUP = new DefaultPGroup(GParsConfig.retrieveDefaultPool());

    /**
     * Maps threads/tasks to parallel groups they belong to
     */
    public static final ThreadLocal<PGroup> activeParallelGroup = new ThreadLocal<PGroup>();

    /**
     * Retrieves the thread-local value of the active PGroup or the default DataflowGroup
     *
     * @return The PGroup to use for DF within the current thread
     */
    public static PGroup retrieveCurrentDFPGroup() {
        PGroup pGroup = activeParallelGroup.get();
        if (pGroup == null) {
            pGroup = Dataflow.DATA_FLOW_GROUP;
        }
        return pGroup;
    }

    /**
     * Sets the supplied PGroup as the default for the given block of code. All dataflow functions, such as task or operator
     * and callback handlers, will use the PGroup and its thread pool for their scheduling.
     *
     * @param group The group to make the default inside the block
     * @param code  The code to run with overriden default
     * @return The value returned from the supplied code block
     */
    public static Object usingGroup(final PGroup group, final Closure code) {
        final PGroup original = activeParallelGroup.get();
        try {
            activeParallelGroup.set(group);
            return code.call();
        } finally {
            activeParallelGroup.set(original);
        }
    }

    /**
     * Creates a new task assigned to a thread from the default dataflow parallel group.
     * Tasks are a lightweight version of dataflow operators, which do not define their communication channels explicitly,
     * but can only exchange data using explicit DataflowVariables and Streams.
     *
     * @param code The task body to run
     * @return A DataflowVariable, which gets assigned the value returned from the supplied code
     */
    public static <T> Promise<T> task(final Closure<T> code) {
        return retrieveCurrentDFPGroup().task(code);
    }

    /**
     * Creates a new task assigned to a thread from the current parallel group.
     * Tasks are a lightweight version of dataflow operators, which do not define their communication channels explicitly,
     * but can only exchange data using explicit DataflowVariables and Streams.
     * Registers itself with Dataflow for nested 'whenBound' handlers to use the same group.
     *
     * @param callable The task body to run
     * @return A DataflowVariable, which gets assigned the value returned from the supplied code
     */
    public static <T> Promise<T> task(final Callable<T> callable) {
        return retrieveCurrentDFPGroup().task(callable);
    }

    /**
     * Creates a new task assigned to a thread from the current parallel group.
     * Tasks are a lightweight version of dataflow operators, which do not define their communication channels explicitly,
     * but can only exchange data using explicit DataflowVariables and Streams.
     * Registers itself with Dataflow for nested 'whenBound' handlers to use the same group.
     *
     * @param runnable The task body to run
     * @return A DataflowVariable, which gets bound to null once the supplied code finishes
     */
    public static Promise<Object> task(final Runnable runnable) {
        return retrieveCurrentDFPGroup().task(runnable);
    }

    /**
     * Creates a new task assigned to a thread from the default dataflow parallel group.
     * The task is lazy, since it only gets executed if the returned Promise instance is read or a then-callback is registered on it.
     * Tasks are a lightweight version of dataflow operators, which do not define their communication channels explicitly,
     * but can only exchange data using explicit DataflowVariables and Streams.
     *
     * @param code The task body to run
     * @return A LazyDataflowVariable, which gets assigned the value returned from the supplied code
     */
    public static <T> Promise<T> lazyTask(final Closure<T> code) {
        final PGroup group = retrieveCurrentDFPGroup();
        return group.lazyTask(code);
    }

    /**
     * Creates a new task assigned to a thread from the current parallel group.
     * The task is lazy, since it only gets executed if the returned Promise instance is read or a then-callback is registered on it.
     * Tasks are a lightweight version of dataflow operators, which do not define their communication channels explicitly,
     * but can only exchange data using explicit DataflowVariables and Streams.
     * Registers itself with Dataflow for nested 'whenBound' handlers to use the same group.
     *
     * @param callable The task body to run
     * @return A LazyDataflowVariable, which gets assigned the value returned from the supplied code
     */
    public static <T> Promise<T> lazyTask(final Callable<T> callable) {
        return retrieveCurrentDFPGroup().lazyTask(callable);
    }

    /**
     * Creates an operator using the default dataflow parallel group
     *
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @param code     The operator's body to run each time all inputs have a value to read
     * @return A new active operator instance
     */
    public static DataflowProcessor operator(final Map channels, @DelegatesTo(DataflowOperator.class) final Closure code) {
        return retrieveCurrentDFPGroup().operator(channels, code);
    }

    /**
     * Creates an operator using the current parallel group
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @param code           The operator's body to run each time all inputs have a value to read
     * @return A new active operator instance
     */
    public static DataflowProcessor operator(final List inputChannels, final List outputChannels, @DelegatesTo(DataflowOperator.class) final Closure code) {
        return retrieveCurrentDFPGroup().operator(inputChannels, outputChannels, code);
    }

    /**
     * Creates an operator using the current parallel group
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @param maxForks       Number of parallel threads running operator's body, defaults to 1
     * @param code           The operator's body to run each time all inputs have a value to read
     * @return A new active operator instance
     */
    public static DataflowProcessor operator(final List inputChannels, final List outputChannels, final int maxForks, @DelegatesTo(DataflowOperator.class) final Closure code) {
        return retrieveCurrentDFPGroup().operator(inputChannels, outputChannels, maxForks, code);
    }

    /**
     * Creates an operator using the current parallel group
     *
     * @param input  a dataflow channel to use for input
     * @param output a dataflow channel to use for output
     * @param code   The operator's body to run each time all inputs have a value to read
     * @return A new active operator instance
     */
    public static DataflowProcessor operator(final DataflowReadChannel input, final DataflowWriteChannel output, @DelegatesTo(DataflowOperator.class) final Closure code) {
        return retrieveCurrentDFPGroup().operator(input, output, code);
    }

    /**
     * Creates an operator using the current parallel group
     *
     * @param input    a dataflow channel to use for input
     * @param output   a dataflow channel to use for output
     * @param maxForks Number of parallel threads running operator's body, defaults to 1
     * @param code     The operator's body to run each time all inputs have a value to read
     * @return A new active operator instance
     */
    public static DataflowProcessor operator(final DataflowReadChannel input, final DataflowWriteChannel output, final int maxForks, @DelegatesTo(DataflowOperator.class) final Closure code) {
        return retrieveCurrentDFPGroup().operator(input, output, maxForks, code);
    }

    /**
     * Creates a selector using the default dataflow parallel group
     *
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @param code     The selector's body to run each time a value is available in any of the inputs channels
     * @return A new active selector instance
     */
    public static DataflowProcessor selector(final Map channels, @DelegatesTo(DataflowSelector.class) final Closure code) {
        return retrieveCurrentDFPGroup().selector(channels, code);
    }

    /**
     * Creates a selector using the default dataflow parallel group
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @param code           The selector's body to run each time a value is available in any of the inputs channels
     * @return A new active selector instance
     */
    public static DataflowProcessor selector(final List inputChannels, final List outputChannels, @DelegatesTo(DataflowSelector.class) final Closure code) {
        return retrieveCurrentDFPGroup().selector(inputChannels, outputChannels, code);
    }

    /**
     * Creates a selector using the default dataflow parallel group. Since no body is provided, the selector will simply copy the incoming values to all output channels.
     *
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @return A new active selector instance
     */
    public static DataflowProcessor selector(final Map channels) {
        return retrieveCurrentDFPGroup().selector(channels);
    }

    /**
     * Creates a selector using the default dataflow parallel group. Since no body is provided, the selector will simply copy the incoming values to all output channels.
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @return A new active selector instance
     */
    public static DataflowProcessor selector(final List inputChannels, final List outputChannels) {
        return retrieveCurrentDFPGroup().selector(inputChannels, outputChannels);
    }

    /**
     * Creates a prioritizing selector using the default dataflow parallel group
     * Input with lower position index have higher priority.
     *
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @param code     The selector's body to run each time a value is available in any of the inputs channels
     * @return A new active selector instance
     */
    public static DataflowProcessor prioritySelector(final Map channels, @DelegatesTo(DataflowSelector.class) final Closure code) {
        return retrieveCurrentDFPGroup().prioritySelector(channels, code);
    }

    /**
     * Creates a prioritizing selector using the default dataflow parallel group
     * Input with lower position index have higher priority.
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @param code           The selector's body to run each time a value is available in any of the inputs channels
     * @return A new active selector instance
     */
    public static DataflowProcessor prioritySelector(final List inputChannels, final List outputChannels, @DelegatesTo(DataflowSelector.class) final Closure code) {
        return retrieveCurrentDFPGroup().prioritySelector(inputChannels, outputChannels, code);
    }

    /**
     * Creates a prioritizing selector using the default dataflow parallel group. Since no body is provided, the selector will simply copy the incoming values to all output channels.
     * Input with lower position index have higher priority.
     *
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @return A new active selector instance
     */
    public static DataflowProcessor prioritySelector(final Map channels) {
        return retrieveCurrentDFPGroup().prioritySelector(channels);
    }

    /**
     * Creates a prioritizing selector using the default dataflow parallel group. Since no body is provided, the selector will simply copy the incoming values to all output channels.
     * Input with lower position index have higher priority.
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @return A new active selector instance
     */
    public static DataflowProcessor prioritySelector(final List inputChannels, final List outputChannels) {
        return retrieveCurrentDFPGroup().prioritySelector(inputChannels, outputChannels);
    }

    /**
     * Creates a splitter copying its single input channel into all of its output channels. The created splitter will be part of the default parallel group
     * Input with lower position index have higher priority.
     *
     * @param inputChannel   The channel to  read values from
     * @param outputChannels A list of channels to output to
     * @return A new active splitter instance
     */
    public static DataflowProcessor splitter(final DataflowReadChannel inputChannel, final List<DataflowWriteChannel> outputChannels) {
        return retrieveCurrentDFPGroup().splitter(inputChannel, outputChannels);
    }

    /**
     * Creates a splitter copying its single input channel into all of its output channels. The created splitter will be part of this parallel group
     * Input with lower position index have higher priority.
     *
     * @param inputChannel   The channel to  read values from
     * @param outputChannels A list of channels to output to
     * @param maxForks       Number of threads running the splitter's body, defaults to 1
     * @return A new active splitter instance
     */
    public static DataflowProcessor splitter(final DataflowReadChannel inputChannel, final List<DataflowWriteChannel> outputChannels, final int maxForks) {
        return retrieveCurrentDFPGroup().splitter(inputChannel, outputChannels, maxForks);
    }

    /**
     * Creates a select using the default dataflow parallel group. The returns Select instance will allow the user to
     * obtain values from the supplied dataflow variables or streams as they become available.
     *
     * @param channels Dataflow variables or streams to wait for values on
     * @return A new select instance
     */
    public static Select<?> select(final SelectableChannel<?>... channels) {
        return retrieveCurrentDFPGroup().select(channels);
    }

    /**
     * Creates a select using the default dataflow parallel group. The returns Select instance will allow the user to
     * obtain values from the supplied dataflow variables or streams as they become available.
     *
     * @param channels Dataflow variables or streams to wait for values on
     * @return A new select instance
     */
    public static Select<?> select(final List<? extends SelectableChannel> channels) {
        return retrieveCurrentDFPGroup().select(channels);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promises The promises to wait for
     * @param code     A closure to execute with concrete values for each of the supplied promises
     * @param <T>      The type of the final result
     * @return A promise for the final result
     */
    public static <T> Promise<T> whenAllBound(final List<Promise> promises, final Closure<T> code) {
        return retrieveCurrentDFPGroup().whenAllBound(promises, code);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1 The promises to wait for
     * @param code     A closure to execute with concrete values for each of the supplied promises
     * @param <T>      The type of the final result
     * @return A promise for the final result
     */
    public static <T> Promise<T> whenAllBound(final Promise promise1, final Closure<T> code) {
        return retrieveCurrentDFPGroup().whenAllBound(asList(promise1), code);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1 The promises to wait for
     * @param promise2 The promises to wait for
     * @param code     A closure to execute with concrete values for each of the supplied promises
     * @param <T>      The type of the final result
     * @return A promise for the final result
     */
    public static <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Closure<T> code) {
        return retrieveCurrentDFPGroup().whenAllBound(asList(promise1, promise2), code);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1 The promises to wait for
     * @param promise2 The promises to wait for
     * @param promise3 The promises to wait for
     * @param code     A closure to execute with concrete values for each of the supplied promises
     * @param <T>      The type of the final result
     * @return A promise for the final result
     */
    public static <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Promise promise3, final Closure<T> code) {
        return retrieveCurrentDFPGroup().whenAllBound(asList(promise1, promise2, promise3), code);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1 The promises to wait for
     * @param promise2 The promises to wait for
     * @param promise3 The promises to wait for
     * @param promise4 The promises to wait for
     * @param code     A closure to execute with concrete values for each of the supplied promises
     * @param <T>      The type of the final result
     * @return A promise for the final result
     */
    public static <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Promise promise3, final Promise promise4, final Closure<T> code) {
        return retrieveCurrentDFPGroup().whenAllBound(asList(promise1, promise2, promise3, promise4), code);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promises     The promises to wait for
     * @param code         A closure to execute with concrete values for each of the supplied promises
     * @param errorHandler A closure handling an exception (an instance of Throwable), if it gets bound
     * @param <T>          The type of the final result
     * @return A promise for the final result
     */
    public static <T> Promise<T> whenAllBound(final List<Promise> promises, final Closure<T> code, final Closure<T> errorHandler) {
        return retrieveCurrentDFPGroup().whenAllBound(promises, code, errorHandler);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1     The promises to wait for
     * @param code         A closure to execute with concrete values for each of the supplied promises
     * @param errorHandler A closure handling an exception (an instance of Throwable), if it gets bound
     * @param <T>          The type of the final result
     * @return A promise for the final result
     */
    public static <T> Promise<T> whenAllBound(final Promise promise1, final Closure<T> code, final Closure<T> errorHandler) {
        return retrieveCurrentDFPGroup().whenAllBound(asList(promise1), code, errorHandler);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1     The promises to wait for
     * @param promise2     The promises to wait for
     * @param code         A closure to execute with concrete values for each of the supplied promises
     * @param errorHandler A closure handling an exception (an instance of Throwable), if it gets bound
     * @param <T>          The type of the final result
     * @return A promise for the final result
     */
    public static <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Closure<T> code, final Closure<T> errorHandler) {
        return retrieveCurrentDFPGroup().whenAllBound(asList(promise1, promise2), code, errorHandler);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1     The promises to wait for
     * @param promise2     The promises to wait for
     * @param promise3     The promises to wait for
     * @param code         A closure to execute with concrete values for each of the supplied promises
     * @param errorHandler A closure handling an exception (an instance of Throwable), if it gets bound
     * @param <T>          The type of the final result
     * @return A promise for the final result
     */
    public static <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Promise promise3, final Closure<T> code, final Closure<T> errorHandler) {
        return retrieveCurrentDFPGroup().whenAllBound(asList(promise1, promise2, promise3), code, errorHandler);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1     The promises to wait for
     * @param promise2     The promises to wait for
     * @param promise3     The promises to wait for
     * @param promise4     The promises to wait for
     * @param code         A closure to execute with concrete values for each of the supplied promises
     * @param errorHandler A closure handling an exception (an instance of Throwable), if it gets bound
     * @param <T>          The type of the final result
     * @return A promise for the final result
     */
    public static <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Promise promise3, final Promise promise4, final Closure<T> code, final Closure<T> errorHandler) {
        return retrieveCurrentDFPGroup().whenAllBound(asList(promise1, promise2, promise3, promise4), code, errorHandler);
    }
}
