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

package groovyx.gpars.group;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovyx.gpars.MessagingRunnable;
import groovyx.gpars.actor.Actor;
import groovyx.gpars.actor.BlockingActor;
import groovyx.gpars.actor.DefaultActor;
import groovyx.gpars.actor.DynamicDispatchActor;
import groovyx.gpars.actor.ReactiveActor;
import groovyx.gpars.actor.StaticDispatchActor;
import groovyx.gpars.actor.impl.RunnableBackedBlockingActor;
import groovyx.gpars.agent.Agent;
import groovyx.gpars.dataflow.Dataflow;
import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.DataflowWriteChannel;
import groovyx.gpars.dataflow.LazyDataflowVariable;
import groovyx.gpars.dataflow.Promise;
import groovyx.gpars.dataflow.Select;
import groovyx.gpars.dataflow.SelectableChannel;
import groovyx.gpars.dataflow.operator.DataflowOperator;
import groovyx.gpars.dataflow.operator.DataflowPrioritySelector;
import groovyx.gpars.dataflow.operator.DataflowProcessor;
import groovyx.gpars.dataflow.operator.DataflowProcessorAtomicBoundAllClosure;
import groovyx.gpars.dataflow.operator.DataflowSelector;
import groovyx.gpars.scheduler.Pool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;

/**
 * Provides a common super class of pooled parallel groups.
 *
 * @author Vaclav Pech, Alex Tkachman
 *         Date: May 8, 2009
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
public abstract class PGroup {

    protected static final String A_SPLITTER_NEEDS_AN_INPUT_CHANNEL_AND_AT_LEAST_ONE_OUTPUT_CHANNEL_TO_BE_CREATED = "A splitter needs an input channel and at least one output channel to be created.";

    /**
     * Stores the group actors' thread pool
     */
    private final Pool threadPool;

    public Pool getThreadPool() {
        return threadPool;
    }

    /**
     * Creates a group for actors, agents, tasks and operators. The actors will share a common daemon thread pool.
     *
     * @param threadPool The thread pool to use by the group
     */
    protected PGroup(final Pool threadPool) {
        this.threadPool = threadPool;
    }

    /**
     * Creates a new instance of DefaultActor, using the passed-in runnable/closure as the body of the actor's act() method.
     * The created actor will belong to the pooled parallel group.
     *
     * @param handler The body of the newly created actor's act method.
     * @return A newly created instance of the DefaultActor class
     */
    public final DefaultActor actor(@DelegatesTo(DefaultActor.class) final Runnable handler) {
        final DefaultActor actor = new DefaultActor(handler);
        actor.setParallelGroup(this);
        actor.start();
        return actor;
    }

    /**
     * Creates a new instance of BlockingActor, using the passed-in closure as the body of the actor's act() method.
     * The created actor will be part of the default actor group.
     *
     * @param handler The body of the newly created actor's act method.
     * @return A newly created instance of the BlockingActor class
     */
    public final BlockingActor blockingActor(@DelegatesTo(BlockingActor.class) final Runnable handler) {
        final BlockingActor actor = new RunnableBackedBlockingActor(handler);
        actor.setParallelGroup(this);
        actor.start();
        return actor;
    }

    /**
     * Creates a new instance of DefaultActor, using the passed-in runnable/closure as the body of the actor's act() method.
     * The actor will cooperate in thread sharing with other actors sharing the same thread pool in a fair manner.
     * The created actor will belong to the pooled parallel group.
     *
     * @param handler The body of the newly created actor's act method.
     * @return A newly created instance of the DefaultActor class
     */
    public final DefaultActor fairActor(@DelegatesTo(DefaultActor.class) final Runnable handler) {
        final DefaultActor actor = new DefaultActor(handler);
        actor.setParallelGroup(this);
        actor.makeFair();
        actor.start();
        return actor;
    }

    /**
     * Creates a reactor around the supplied code.
     * When a reactor receives a message, the supplied block of code is run with the message
     * as a parameter and the result of the code is send in reply.
     *
     * @param code The code to invoke for each received message
     * @return A new instance of ReactiveEventBasedThread
     */
    public final Actor reactor(@DelegatesTo(Actor.class) final Closure code) {
        final Actor actor = new ReactiveActor(code);
        actor.setParallelGroup(this);
        actor.start();
        return actor;
    }

    /**
     * Creates a reactor around the supplied code, which will cooperate in thread sharing with other actors sharing the same thread pool
     * When a reactor receives a message, the supplied block of code is run with the message
     * as a parameter and the result of the code is send in reply.
     *
     * @param code The code to invoke for each received message
     * @return A new instance of ReactiveEventBasedThread
     */
    public final Actor fairReactor(@DelegatesTo(Actor.class) final Closure code) {
        final ReactiveActor actor = new ReactiveActor(code);
        actor.setParallelGroup(this);
        actor.makeFair();
        actor.start();
        return actor;
    }

    /**
     * Creates an instance of DynamicDispatchActor.
     *
     * @param code The closure specifying individual message handlers.
     * @return The new started actor
     */
    public final Actor messageHandler(@DelegatesTo(Actor.class) final Closure code) {
        final DynamicDispatchActor actor = new DynamicDispatchActor().become(code);
        actor.setParallelGroup(this);
        actor.start();
        return actor;
    }

    /**
     * Creates an instance of DynamicDispatchActor, which will cooperate in thread sharing with other actors sharing the same thread pool
     *
     * @param code The closure specifying individual message handlers.
     * @return The new started actor
     */
    public final Actor fairMessageHandler(@DelegatesTo(Actor.class) final Closure code) {
        final DynamicDispatchActor actor = new DynamicDispatchActor().become(code);
        actor.setParallelGroup(this);
        actor.makeFair();
        actor.start();
        return actor;
    }

    /**
     * Creates an instance of StaticDispatchActor.
     *
     * @param code The closure specifying the only statically dispatched message handler.
     * @return The new started actor
     */
    public final Actor staticMessageHandler(@DelegatesTo(Actor.class) final Closure code) {
        final StaticDispatchActor<Object> actor = new StaticDispatchActor<Object>() {
            @Override
            public void onMessage(final Object message) {
                code.call(message);
            }
        };
        code.setDelegate(actor);
        code.setResolveStrategy(Closure.DELEGATE_FIRST);
        actor.setParallelGroup(this);
        actor.start();
        return actor;
    }

    /**
     * Creates an instance of StaticDispatchActor, which will cooperate in thread sharing with other actors sharing the same thread pool.
     *
     * @param code The closure specifying the only statically dispatched message handler.
     * @return The new started actor
     */
    public final Actor fairStaticMessageHandler(@DelegatesTo(Actor.class) final Closure code) {
        final StaticDispatchActor<Object> actor = new StaticDispatchActor<Object>() {
            @Override
            public void onMessage(final Object message) {
                code.call(message);
            }
        };
        code.setDelegate(actor);
        code.setResolveStrategy(Closure.DELEGATE_FIRST);
        actor.setParallelGroup(this);
        actor.makeFair();
        actor.start();
        return actor;
    }

    /**
     * Creates an agent instance initialized with the given state
     *
     * @param state The initial internal state of the new Agent instance
     * @return The created instance
     */
    public final <T> Agent<T> agent(final T state) {
        final Agent<T> safe = new Agent<T>(state);
        safe.attachToThreadPool(threadPool);
        return safe;
    }

    /**
     * Creates an agent instance initialized with the given state
     *
     * @param state The initial internal state of the new Agent instance
     * @param copy  A closure to use to create a copy of the internal state when sending the internal state out
     * @return The created instance
     */
    public final <T> Agent<T> agent(final T state, final Closure copy) {
        final Agent<T> safe = new Agent<T>(state, copy);
        safe.attachToThreadPool(threadPool);
        return safe;
    }

    /**
     * Creates an agent instance initialized with the given state, which will cooperate in thread sharing with other agents and actors in a fair manner.
     *
     * @param state The initial internal state of the new Agent instance
     * @return The created instance
     */
    public final <T> Agent<T> fairAgent(final T state) {
        final Agent<T> safe = agent(state);
        safe.makeFair();
        return safe;
    }

    /**
     * Creates an agent instance initialized with the given state, which will cooperate in thread sharing with other agents and actors in a fair manner.
     *
     * @param copy  A closure to use to create a copy of the internal state when sending the internal state out
     * @param state The initial internal state of the new Agent instance
     * @return The created instance
     */
    public final <T> Agent<T> fairAgent(final T state, final Closure copy) {
        final Agent<T> safe = agent(state, copy);
        safe.makeFair();
        return safe;
    }

    /**
     * Creates a new task assigned to a thread from the current parallel group.
     * Tasks are a lightweight version of dataflow operators, which do not define their communication channels explicitly,
     * but can only exchange data using explicit DataflowVariables and Streams.
     * Registers itself with Dataflow for nested 'whenBound' handlers to use the same group.
     *
     * @param code The task body to run
     * @return A DataflowVariable, which gets assigned the value returned from the supplied code
     */
    public final <T> Promise<T> task(final Closure<T> code) {
        final Closure<T> clonedCode = (Closure<T>) code.clone();
        return task(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return clonedCode.call();
            }
        });
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
    public final <T> Promise<T> task(final Callable<T> callable) {
        final DataflowVariable result = new DataflowVariable();
        threadPool.execute(new Runnable() {
            @SuppressWarnings("OverlyBroadCatchBlock")
            @Override
            public void run() {
                Dataflow.activeParallelGroup.set(PGroup.this);
                try {
                    //noinspection OverlyBroadCatchBlock
                    try {
                        result.bind(callable.call());
                    } catch (Throwable e) {
                        result.bind(e);
                    }
                } finally {
                    Dataflow.activeParallelGroup.remove();
                }
            }
        });
        return result;
    }

    /**
     * Creates a new task assigned to a thread from the current parallel group.
     * Tasks are a lightweight version of dataflow operators, which do not define their communication channels explicitly,
     * but can only exchange data using explicit DataflowVariables and Streams.
     * Registers itself with Dataflow for nested 'whenBound' handlers to use the same group.
     *
     * @param code The task body to run
     * @return A DataflowVariable, which gets bound to null once the supplied code finishes
     */
    public final Promise<Object> task(final Runnable code) {
        if (code instanceof Closure) return task((Closure) code);
        final DataflowVariable result = new DataflowVariable();
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                Dataflow.activeParallelGroup.set(PGroup.this);
                try {
                    try {
                        code.run();
                        result.bind(null);
                    } catch (Throwable e) {
                        result.bind(e);
                    }
                } finally {
                    Dataflow.activeParallelGroup.remove();
                }
            }
        });
        return result;
    }

    /**
     * Creates a new task assigned to a thread from the current parallel group.
     * The task is lazy, since it only gets executed if the returned Promise instance is read or a then-callback is registered on it.
     * Tasks are a lightweight version of dataflow operators, which do not define their communication channels explicitly,
     * but can only exchange data using explicit DataflowVariables and Streams.
     * Registers itself with Dataflow for nested 'whenBound' handlers to use the same group.
     *
     * @param code The task body to run
     * @return A LazyDataflowVariable, which gets assigned the value returned from the supplied code
     */
    public final <T> Promise<T> lazyTask(final Closure<T> code) {
        final Closure<T> clonedCode = (Closure<T>) code.clone();
        return new LazyDataflowVariable<T>(this, clonedCode);
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
    public final <T> Promise<T> lazyTask(final Callable<T> callable) {
        if (callable instanceof Closure) return lazyTask((Closure<T>) callable);
        return new LazyDataflowVariable<T>(this, new Closure<T>(this) {
            @Override public int getMaximumNumberOfParameters() {
                return 0;
            }

            @Override public T call() {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Creates an operator using the current parallel group
     *
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @param code     The operator's body to run each time all inputs have a value to read
     * @return A new started operator instance with all the channels set
     */
    public final DataflowProcessor operator(final Map channels, @DelegatesTo(DataflowOperator.class) final Closure code) {
        return new DataflowOperator(this, channels, code).start();
    }

    /**
     * Creates an operator using the current parallel group
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @param code           The operator's body to run each time all inputs have a value to read
     * @return A new started operator instance with all the channels set
     */
    public final DataflowProcessor operator(final List inputChannels, final List outputChannels, @DelegatesTo(DataflowOperator.class) final Closure code) {
        final HashMap<String, List> params = new HashMap<String, List>(5);
        params.put(DataflowProcessor.INPUTS, inputChannels);
        params.put(DataflowProcessor.OUTPUTS, outputChannels);
        return new DataflowOperator(this, params, code).start();
    }

    /**
     * Creates an operator using the current parallel group
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @param maxForks       Number of parallel threads running operator's body, defaults to 1
     * @param code           The operator's body to run each time all inputs have a value to read
     * @return A new started operator instance with all the channels set
     */
    public final DataflowProcessor operator(final List inputChannels, final List outputChannels, final int maxForks, @DelegatesTo(DataflowOperator.class) final Closure code) {
        final HashMap<String, Object> params = new HashMap<String, Object>(5);
        params.put(DataflowProcessor.INPUTS, inputChannels);
        params.put(DataflowProcessor.OUTPUTS, outputChannels);
        params.put(DataflowProcessor.MAX_FORKS, maxForks);
        return new DataflowOperator(this, params, code).start();
    }

    /**
     * Creates an operator using the current parallel group
     *
     * @param input  a dataflow channel to use for input
     * @param output a dataflow channel to use for output
     * @param code   The operator's body to run each time all inputs have a value to read
     * @return A new started operator instance with all the channels set
     */
    public final DataflowProcessor operator(final DataflowReadChannel input, final DataflowWriteChannel output, @DelegatesTo(DataflowOperator.class) final Closure code) {
        final HashMap<String, List> params = new HashMap<String, List>(5);
        params.put(DataflowProcessor.INPUTS, asList(input));
        params.put(DataflowProcessor.OUTPUTS, asList(output));
        return new DataflowOperator(this, params, code).start();
    }

    /**
     * Creates an operator using the current parallel group
     *
     * @param input    a dataflow channel to use for input
     * @param output   a dataflow channel to use for output
     * @param maxForks Number of parallel threads running operator's body, defaults to 1
     * @param code     The operator's body to run each time all inputs have a value to read
     * @return A new started operator instance with all the channels set
     */
    public final DataflowProcessor operator(final DataflowReadChannel input, final DataflowWriteChannel output, final int maxForks, @DelegatesTo(DataflowOperator.class) final Closure code) {
        final HashMap<String, Object> params = new HashMap<String, Object>(5);
        params.put(DataflowProcessor.INPUTS, asList(input));
        params.put(DataflowProcessor.OUTPUTS, asList(output));
        params.put(DataflowProcessor.MAX_FORKS, maxForks);
        return new DataflowOperator(this, params, code).start();
    }

    /**
     * Creates a selector using this parallel group
     *
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @param code     The selector's body to run each time a value is available in any of the inputs channels
     * @return A new started selector instance with all the channels set
     */
    public final DataflowProcessor selector(final Map channels, @DelegatesTo(DataflowSelector.class) final Closure code) {
        return new DataflowSelector(this, channels, code).start();
    }

    /**
     * Creates a selector using this parallel group
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @param code           The selector's body to run each time a value is available in any of the inputs channels
     * @return A new started selector instance with all the channels set
     */
    public final DataflowProcessor selector(final List inputChannels, final List outputChannels, @DelegatesTo(DataflowSelector.class) final Closure code) {
        final HashMap<String, List> params = new HashMap<String, List>(5);
        params.put(DataflowProcessor.INPUTS, inputChannels);
        params.put(DataflowProcessor.OUTPUTS, outputChannels);
        return new DataflowSelector(this, params, code).start();
    }

    /**
     * Creates a selector using this parallel group. Since no body is provided, the selector will simply copy the incoming values to all output channels.
     *
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @return A new started selector instance with all the channels set
     */
    public final DataflowProcessor selector(final Map channels) {
        return new DataflowSelector(this, channels, new DataflowProcessorAtomicBoundAllClosure()).start();
    }

    /**
     * Creates a selector using this parallel group. Since no body is provided, the selector will simply copy the incoming values to all output channels.
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @return A new started selector instance with all the channels set
     */
    public final DataflowProcessor selector(final List inputChannels, final List outputChannels) {
        final HashMap<String, List> params = new HashMap<String, List>(5);
        params.put(DataflowProcessor.INPUTS, inputChannels);
        params.put(DataflowProcessor.OUTPUTS, outputChannels);
        return new DataflowSelector(this, params, new DataflowProcessorAtomicBoundAllClosure()).start();
    }

    /**
     * Creates a prioritizing selector using the default dataflow parallel group
     * Input with lower position index have higher priority.
     *
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @param code     The selector's body to run each time a value is available in any of the inputs channels
     * @return A new started selector instance with all the channels set
     */
    public final DataflowProcessor prioritySelector(final Map channels, @DelegatesTo(DataflowSelector.class) final Closure code) {
        return new DataflowPrioritySelector(this, channels, code).start();
    }

    /**
     * Creates a prioritizing selector using the default dataflow parallel group
     * Input with lower position index have higher priority.
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @param code           The selector's body to run each time a value is available in any of the inputs channels
     * @return A new started selector instance with all the channels set
     */
    public final DataflowProcessor prioritySelector(final List inputChannels, final List outputChannels, @DelegatesTo(DataflowSelector.class) final Closure code) {
        final HashMap<String, Object> params = new HashMap<String, Object>(5);
        params.put(DataflowProcessor.INPUTS, inputChannels);
        params.put(DataflowProcessor.OUTPUTS, outputChannels);
        return new DataflowPrioritySelector(this, params, code).start();
    }

    /**
     * Creates a prioritizing selector using the default dataflow parallel group. Since no body is provided, the selector will simply copy the incoming values to all output channels.
     * Input with lower position index have higher priority.
     *
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataflowQueue or DataflowVariable classes) to use for inputs and outputs
     * @return A new started selector instance with all the channels set
     */
    public final DataflowProcessor prioritySelector(final Map channels) {
        return new DataflowPrioritySelector(this, channels, new DataflowProcessorAtomicBoundAllClosure()).start();
    }

    /**
     * Creates a prioritizing selector using the default dataflow parallel group. Since no body is provided, the selector will simply copy the incoming values to all output channels.
     * Input with lower position index have higher priority.
     *
     * @param inputChannels  dataflow channels to use for input
     * @param outputChannels dataflow channels to use for output
     * @return A new started selector instance with all the channels set
     */
    public final DataflowProcessor prioritySelector(final List inputChannels, final List outputChannels) {
        final HashMap<String, Object> params = new HashMap<String, Object>(5);
        params.put(DataflowProcessor.INPUTS, inputChannels);
        params.put(DataflowProcessor.OUTPUTS, outputChannels);
        return new DataflowPrioritySelector(this, params, new DataflowProcessorAtomicBoundAllClosure()).start();
    }

    /**
     * Creates a splitter copying its single input channel into all of its output channels. The created splitter will be part of this parallel group
     *
     * @param inputChannel   The channel to  read values from
     * @param outputChannels A list of channels to output to
     * @return A new started splitter instance with all the channels set
     */
    public final DataflowProcessor splitter(final DataflowReadChannel inputChannel, final List<DataflowWriteChannel> outputChannels) {
        if (inputChannel == null || outputChannels == null || outputChannels.isEmpty())
            throw new IllegalArgumentException(A_SPLITTER_NEEDS_AN_INPUT_CHANNEL_AND_AT_LEAST_ONE_OUTPUT_CHANNEL_TO_BE_CREATED);
        final HashMap<String, List> params = new HashMap<String, List>(5);
        params.put(DataflowProcessor.INPUTS, asList(inputChannel));
        params.put(DataflowProcessor.OUTPUTS, outputChannels);
        return new DataflowOperator(this, params, new DataflowProcessorAtomicBoundAllClosure()).start();
    }

    /**
     * Creates a splitter copying its single input channel into all of its output channels. The created splitter will be part of this parallel group
     *
     * @param inputChannel   The channel to  read values from
     * @param outputChannels A list of channels to output to
     * @param maxForks       Number of threads running the splitter's body, defaults to 1
     * @return A new started splitter instance with all the channels set
     */
    public final DataflowProcessor splitter(final DataflowReadChannel inputChannel, final List<DataflowWriteChannel> outputChannels, final int maxForks) {
        if (inputChannel == null || outputChannels == null || outputChannels.isEmpty())
            throw new IllegalArgumentException(A_SPLITTER_NEEDS_AN_INPUT_CHANNEL_AND_AT_LEAST_ONE_OUTPUT_CHANNEL_TO_BE_CREATED);
        final HashMap<String, Object> params = new HashMap<String, Object>(5);
        params.put(DataflowProcessor.INPUTS, asList(inputChannel));
        params.put(DataflowProcessor.OUTPUTS, outputChannels);
        params.put(DataflowProcessor.MAX_FORKS, maxForks);
        return new DataflowOperator(this, params, new DataflowProcessorAtomicBoundAllClosure()).start();
    }

    /**
     * Creates a select using the current parallel group. The returns Select instance will allow the user to
     * obtain values from the supplied dataflow variables or streams as they become available.
     *
     * @param channels Dataflow variables or streams to wait for values on
     */
    public final Select select(final SelectableChannel... channels) {
        return new Select(this, channels);
    }

    /**
     * Creates a select using the current parallel group. The returns Select instance will allow the user to
     * obtain values from the supplied dataflow variables or streams as they become available.
     *
     * @param channels Dataflow variables or streams to wait for values on
     */
    public final Select select(final List<? extends SelectableChannel> channels) {
        return new Select(this, channels);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promises The promises to wait for
     * @param code     A closure to execute with concrete values for each of the supplied promises
     * @param <T>      The type of the final result
     * @return A promise for the final result
     */
    public final <T> Promise<T> whenAllBound(final List<Promise> promises, final Closure<T> code) {
        return whenAllBound(promises, code, null);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1 The promises to wait for
     * @param code     A closure to execute with concrete values for each of the supplied promises
     * @param <T>      The type of the final result
     * @return A promise for the final result
     */
    public final <T> Promise<T> whenAllBound(final Promise promise1, final Closure<T> code) {
        return whenAllBound(asList(promise1), code, null);
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
    public final <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Closure<T> code) {
        return whenAllBound(asList(promise1, promise2), code, null);
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
    public final <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Promise promise3, final Closure<T> code) {
        return whenAllBound(asList(promise1, promise2, promise3), code, null);
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
    public final <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Promise promise3, final Promise promise4, final Closure<T> code) {
        return whenAllBound(asList(promise1, promise2, promise3, promise4), code, null);
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
    public final <T> Promise<T> whenAllBound(final List<Promise> promises, final Closure<T> code, final Closure<T> errorHandler) {
        if (promises.size() != code.getMaximumNumberOfParameters() && !isListAccepting(code)) {
            throw new IllegalArgumentException("Cannot run whenAllBound(), since the number of promises does not match the number of arguments to the supplied closure.");
        }
        for (final Promise promise : promises) {
            promise.touch();
        }
        final DataflowVariable result = new DataflowVariable();
        whenAllBound(promises, 0, new ArrayList<Object>(promises.size()), result, code, errorHandler);
        return result;
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1     The promises to wait for
     * @param code         A closure to execute with concrete values for each of the supplied promises
     * @param errorHandler A closure handling an exception (an instance of Throwable), if if it gets bound
     * @param <T>          The type of the final result
     * @return A promise for the final result
     */
    public final <T> Promise<T> whenAllBound(final Promise promise1, final Closure<T> code, final Closure<T> errorHandler) {
        return whenAllBound(asList(promise1), code, errorHandler);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1     The promises to wait for
     * @param promise2     The promises to wait for
     * @param code         A closure to execute with concrete values for each of the supplied promises
     * @param errorHandler A closure handling an exception (an instance of Throwable), if if it gets bound
     * @param <T>          The type of the final result
     * @return A promise for the final result
     */
    public final <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Closure<T> code, final Closure<T> errorHandler) {
        return whenAllBound(asList(promise1, promise2), code, errorHandler);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1     The promises to wait for
     * @param promise2     The promises to wait for
     * @param promise3     The promises to wait for
     * @param code         A closure to execute with concrete values for each of the supplied promises
     * @param errorHandler A closure handling an exception (an instance of Throwable), if if it gets bound
     * @param <T>          The type of the final result
     * @return A promise for the final result
     */
    public final <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Promise promise3, final Closure<T> code, final Closure<T> errorHandler) {
        return whenAllBound(asList(promise1, promise2, promise3), code, errorHandler);
    }

    /**
     * Without blocking the thread waits for all the promises to get bound and then passes them to the supplied closure.
     *
     * @param promise1     The promises to wait for
     * @param promise2     The promises to wait for
     * @param promise3     The promises to wait for
     * @param promise4     The promises to wait for
     * @param code         A closure to execute with concrete values for each of the supplied promises
     * @param errorHandler A closure handling an exception (an instance of Throwable), if if it gets bound
     * @param <T>          The type of the final result
     * @return A promise for the final result
     */
    public final <T> Promise<T> whenAllBound(final Promise promise1, final Promise promise2, final Promise promise3, final Promise promise4, final Closure<T> code, final Closure<T> errorHandler) {
        return whenAllBound(asList(promise1, promise2, promise3, promise4), code, errorHandler);
    }

    /**
     * Waits for the promise identified by the index to be bound and then passes on to the next promise in the list
     *
     * @param promises A list of all promises that need to be waited for
     * @param index    The index of the current promise to wait for
     * @param values   A list of values the so-far processed promises were bound tpo
     * @param result   The promise for the final result of the calculation
     * @param code     The calculation to execute on the values once they are all bound
     * @param <T>      The type of the final result
     */
    private <T> void whenAllBound(final List<Promise> promises, final int index, final List<Object> values, final DataflowVariable<T> result, final Closure<T> code, final Closure<T> errorHandler) {
        if (index == promises.size()) {
            try {
                if (isListAccepting(code)) {
                    result.leftShift(code.call(values));
                } else {
                    result.leftShift(code.call(values.toArray()));
                }
            } catch (Throwable e) {
                result.bindError(e);
            }
        } else promises.get(index).then(this, new MessagingRunnable<Object>() {
                    @Override
                    protected void doRun(final Object argument) {
                        values.add(argument);
                        whenAllBound(promises, index + 1, values, result, code, errorHandler);
                    }
                }, new MessagingRunnable() {
                    @Override
                    protected void doRun(final Object argument) {
                        if (errorHandler != null && shallHandle(errorHandler, (Throwable) argument)) {
                            try {
                                result.leftShift(errorHandler.getMaximumNumberOfParameters() == 1 ? errorHandler.call(argument) : errorHandler.call());
                            } catch (Throwable e) {
                                result.bindError(e);
                            }
                        } else {
                            result.bindError((Throwable) argument);
                        }

                    }
                }
        );
    }

    private <T> boolean shallHandle(final Closure<T> errorHandler, final Throwable e) {
        final Class[] types = errorHandler.getParameterTypes();
        if (types.length == 0) return true;
        return types[0].isAssignableFrom(e.getClass());
    }

    private static <T> boolean isListAccepting(final Closure<T> code) {
        return code.getMaximumNumberOfParameters() == 1 && List.class.isAssignableFrom(code.getParameterTypes()[0]);
    }

    /**
     * Shutdown the thread pool gracefully
     */
    @SuppressWarnings({"FinalizeDeclaration"})
    @Override
    protected void finalize() throws Throwable {
        this.threadPool.shutdown();
        super.finalize();
    }

    /**
     * Resizes the thread pool to the specified value
     *
     * @param poolSize The new pool size
     */
    public void resize(final int poolSize) {
        threadPool.resize(poolSize);
    }

    /**
     * Sets the pool size to the default
     */
    public void resetDefaultSize() {
        threadPool.resetDefaultSize();
    }

    /**
     * Retrieves the current thread pool size
     *
     * @return The pool size
     */
    public int getPoolSize() {
        return threadPool.getPoolSize();
    }

    /**
     * schedules a new task for processing with the pool
     *
     * @param task The task to schedule
     */
    public void execute(final Runnable task) {
        threadPool.execute(task);
    }

    /**
     * Gently stops the pool
     */
    public void shutdown() {
        threadPool.shutdown();
    }
}
