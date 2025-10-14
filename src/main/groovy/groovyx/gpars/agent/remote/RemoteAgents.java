// GPars - Groovy Parallel Systems
//
// Copyright © 2014  The original author or authors
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

package groovyx.gpars.agent.remote;

import groovyx.gpars.agent.Agent;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.Promise;
import groovyx.gpars.remote.LocalHost;
import groovyx.gpars.remote.message.RemoteAgentRequestMsg;
import groovyx.gpars.remote.netty.NettyTransportProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Remoting context for Agents. Manages serialization, publishing and retrieval.
 *
 * @author Rafal Slawik
 */
public final class RemoteAgents extends LocalHost {

    /**
     * Stores Agents published in context of this instance of RemoteAgents.
     */
    private final Map<String, Agent<?>> publishedAgents;

    /**
     * Stores promises to remote instances of Agents.
     */
    private final Map<String, DataflowVariable<RemoteAgent>> remoteAgents;

    private RemoteAgents() {
        publishedAgents = new ConcurrentHashMap<>();
        remoteAgents = new ConcurrentHashMap<>();
    }

    /**
     * Publishes {@link groovyx.gpars.agent.Agent} under given name.
     * It overrides previously published Agent if the same name is given.
     * @param agent the Agent to be published
     * @param name the name under which Agent is published
     */
    public void publish(Agent<?> agent, String name) {
        publishedAgents.put(name, agent);
    }

    /**
     * Retrieves {@link groovyx.gpars.agent.Agent} published under specified name on remote host.
     * @param host the address of remote host
     * @param port the port of remote host
     * @param name the name under which Agent was published
     * @return promise of {@link groovyx.gpars.agent.remote.RemoteAgent}
     */
    public Promise<RemoteAgent> get(String host, int port, String name) {
        return getPromise(remoteAgents, name, host, port, new RemoteAgentRequestMsg(this.getId(), name));
    }

    public static RemoteAgents create() {
        return new RemoteAgents();
    }

    @Override
    public <T> void registerProxy(Class<T> klass, String name, T object) {
        if (klass == RemoteAgent.class) {
            remoteAgents.get(name).bind(((RemoteAgent) object));
            return;
        }
        throw new IllegalArgumentException("Unsupported proxy type");
    }

    @Override
    public <T> T get(Class<T> klass, String name) {
        if (klass == Agent.class) {
            return klass.cast(publishedAgents.get(name));
        }
        throw new IllegalArgumentException("Unsupported type");
    }
}
