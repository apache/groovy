// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10, 2014  The original author or authors
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

package groovyx.gpars.agent;

import groovy.lang.Closure;
import groovyx.gpars.actor.Actors;
import groovyx.gpars.agent.remote.RemoteAgent;
import groovyx.gpars.serial.RemoteSerialized;
import org.codehaus.groovy.runtime.NullObject;

/**
 * A special-purpose thread-safe non-blocking reference implementation inspired by Agents in Clojure.
 * Agents safe-guard mutable values by allowing only a single agent-managed thread to make modifications to them.
 * The mutable values are not directly accessible from outside, but instead requests have to be sent to the agent
 * and the agent guarantees to process the requests sequentially on behalf of the callers. Agents guarantee sequential
 * execution of all requests and so consistency of the values.
 * An agent wraps a reference to mutable state, held inside a single field, and accepts code (closures / commands)
 * as messages, which can be sent to the Agent just like to any other actor using the '&lt;&lt;' operator
 * or any of the send() methods.
 * After reception of a closure / command, the closure is invoked against the internal mutable field. The closure is guaranteed
 * to be run without intervention from other threads and so may freely alter the internal state of the Agent
 * held in the internal <i>data</i> field.
 * The return value of the submitted closure is sent in reply to the sender of the closure.
 * If the message sent to an agent is not a closure, it is considered to be a new value for the internal
 * reference field. The internal reference can also be changed using the updateValue() method from within the received
 * closures.
 * The 'val' property of an agent will safely return the current value of the Agent, while the valAsync() method
 * will do the same without blocking the caller.
 * The 'instantVal' property will retrieve the current value of the Agent without having to wait in the queue of tasks.
 * The initial internal value can be passed to the constructor. The two-parameter constructor allows to alter the way
 * the internal value is returned from val/valAsync. By default the original reference is returned, but in many scenarios a copy
 * or a clone might be more appropriate.
 *
 * @author Vaclav Pech
 *         Date: Jul 2, 2009
 */
@SuppressWarnings({"MethodNamesDifferingOnlyByCase"})
public class Agent<T> extends AgentBase<T> {

    /**
     * Creates a new Agent with the internal state set to null
     */
    public Agent() {
        super(null);
    }

    /**
     * Creates a new Agent around the supplied modifiable object
     *
     * @param data The object to use for storing the internal state of the variable
     */
    public Agent(final T data) {
        super(data);
    }

    /**
     * Creates a new Agent around the supplied modifiable object
     *
     * @param data The object to use for storing the internal state of the variable
     * @param copy A closure to use to create a copy of the internal state when sending the internal state out
     */
    public Agent(final T data, final Closure copy) {
        super(data, copy);
    }

    /**
     * Dynamically dispatches the method call
     */
    @SuppressWarnings({"unchecked", "ChainOfInstanceofChecks"})
    @Override
    public void handleMessage(final Object message) {
        if (message instanceof Closure) onMessage((Closure) message);
        else if (message instanceof NullObject) onMessage((NullObject) message);
        else onMessage((T) message);
    }

    /**
     * Creates an agent instance initialized with the given state.
     * The instance will use the default thread pool.
     *
     * @param state The initial internal state of the new Agent instance
     * @return The created instance
     */
    @SuppressWarnings({"unchecked"})
    public static <T> Agent<T> agent(final T state) {
        return Actors.defaultActorPGroup.agent(state);
    }

    /**
     * Creates an agent instance initialized with the given state.
     * The instance will use the default thread pool.
     *
     * @param state The initial internal state of the new Agent instance
     * @param copy  A closure to use to create a copy of the internal state when sending the internal state out
     * @return The created instance
     */
    @SuppressWarnings({"unchecked"})
    public static <T> Agent<T> agent(final T state, final Closure copy) {
        return Actors.defaultActorPGroup.agent(state, copy);
    }

    /**
     * Creates an agent instance initialized with the given state, which will cooperate in thread sharing with other Agent instances
     * in a fair manner.
     * The instance will use the default thread pool.
     *
     * @param state The initial internal state of the new Agent instance
     * @return The created instance
     */
    @SuppressWarnings({"unchecked"})
    public static <T> Agent<T> fairAgent(final T state) {
        return Actors.defaultActorPGroup.fairAgent(state);
    }

    /**
     * Creates an agent instance initialized with the given state, which will cooperate in thread sharing with other agents and actors in a fair manner.
     * The instance will use the default thread pool.
     *
     * @param state The initial internal state of the new Agent instance
     * @param copy  A closure to use to create a copy of the internal state when sending the internal state out
     * @return The created instance
     */
    @SuppressWarnings({"unchecked"})
    public static <T> Agent<T> fairAgent(final T state, final Closure copy) {
        return Actors.defaultActorPGroup.fairAgent(state, copy);
    }

    @Override
    public Class<RemoteAgent> getRemoteClass() {
        return RemoteAgent.class;
    }
}
