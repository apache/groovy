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

package groovyx.gpars.dataflow.remote;

import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.*;
import groovyx.gpars.remote.LocalHost;
import groovyx.gpars.remote.message.RemoteDataflowBroadcastRequestMsg;
import groovyx.gpars.remote.message.RemoteDataflowQueueRequestMsg;
import groovyx.gpars.remote.message.RemoteDataflowVariableRequestMsg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remoting context for Dataflows. Manages serialization, publishing and retrieval.
 *
 * @author Rafal Slawik
 */
public final class RemoteDataflows extends LocalHost {

    /**
     * Stores DataflowVariables published in context of this instance of RemoteDataflows.
     */
    private final Map<String, DataflowVariable<?>> publishedVariables;

    /**
     * Stores promises to remote instances of DataflowVariables.
     */
    private final Map<String, DataflowVariable<DataflowVariable>> remoteVariables;

    /**
     * Stores DataflowBroadcasts published in context of this instance of RemoteDataflows.
     */
    private final Map<String, DataflowBroadcast> publishedBroadcasts;

    /**
     * Stores promises to remote instances of DataflowBroadcasts.
     */
    private final Map<String, DataflowVariable<RemoteDataflowBroadcast>> remoteBroadcasts;

    /**
     * Stores DataflowQueues published in context of this instance of RemoteDataflows.
     */
    private final Map<String, DataflowQueue<?>> publishedQueues;

    /**
     * Stores promises to remote instances of DataflowQueues.
     */
    private final Map<String, DataflowVariable<RemoteDataflowQueue<?>>> remoteQueues;

    RemoteDataflows() {
        publishedVariables = new ConcurrentHashMap<>();
        remoteVariables = new ConcurrentHashMap<>();

        publishedBroadcasts = new ConcurrentHashMap<>();
        remoteBroadcasts = new ConcurrentHashMap<>();

        publishedQueues = new ConcurrentHashMap<>();
        remoteQueues = new ConcurrentHashMap<>();
    }

    /**
     * Publishes {@link groovyx.gpars.dataflow.DataflowVariable} under given name.
     * It overrides previously published variable if the same name is given.
     * @param variable the variable to be published
     * @param name the name under which variable is published
     */
    public void publish(DataflowVariable<?> variable, String name) {
        publishedVariables.put(name, variable);
    }

    /**
     * Retrieves {@link groovyx.gpars.dataflow.DataflowVariable} published under specified name on remote host.
     * @param host the address of remote host
     * @param port the the port of remote host
     * @param name the name under which variable was published
     * @return promise of {@link groovyx.gpars.dataflow.DataflowVariable}
     */
    public Promise<DataflowVariable> getVariable(String host, int port, String name) {
        return getPromise(remoteVariables, name, host, port, new RemoteDataflowVariableRequestMsg(this.getId(), name));
    }

    /**
     * Publishes {@link groovyx.gpars.dataflow.DataflowBroadcast} under given name.
     * It overrides previously published broadcast if the same name is given.
     * @param broadcastStream the stream to be published
     * @param name the name under which stream is published
     */
    public void publish(DataflowBroadcast broadcastStream, String name) {
        publishedBroadcasts.put(name, broadcastStream);
    }

    /**
     * Retrieves {@link groovyx.gpars.dataflow.DataflowReadChannel} corresponding to
     * {@link groovyx.gpars.dataflow.DataflowBroadcast} published under given name on remote host.
     * @param host the address of remote host
     * @param port the port of remote host
     * @param name the name under which broadcast is published
     * @return promise of {@link groovyx.gpars.dataflow.DataflowReadChannel}
     */
    public Promise<DataflowReadChannel> getReadChannel(String host, int port, String name) {
        DataflowVariable<RemoteDataflowBroadcast> broadcastPromise = getPromise(remoteBroadcasts, name, host, port, new RemoteDataflowBroadcastRequestMsg(this.getId(), name));
        DataflowVariable<DataflowReadChannel> promise = new DataflowVariable<>();
        broadcastPromise.whenBound(new MessageStream() {
            @Override
            public MessageStream send(Object message) {
                promise.bind(((RemoteDataflowBroadcast) message).createReadChannel());
                return this;
            }
        });
        return promise;
    }

    /**
     * Publishes {@link groovyx.gpars.dataflow.DataflowQueue} under given name.
     * It overrides previously published queue if the same name is given.
     * @param queue the queue to be published
     * @param name the name under which queue is published
     */
    public void publish(DataflowQueue<?> queue, String name) {
        publishedQueues.put(name, queue);
    }

    /**
     * Retrieves {@link groovyx.gpars.dataflow.DataflowQueue} published under specified name on remote host.
     * @param host the address of remote host
     * @param port the port of remote host
     * @param name the name under which queue was published
     * @return promise of {@link groovyx.gpars.dataflow.remote.RemoteDataflowQueue}
     */
    public Promise<RemoteDataflowQueue<?>> getDataflowQueue(String host, int port, String name) {
        return getPromise(remoteQueues, name, host, port, new RemoteDataflowQueueRequestMsg(this.getId(), name));
    }

    /**
     * Factory method for creating RemoteDataflows
     * @return new instance of RemoteDataflows
     */
    public static RemoteDataflows create() {
        return new RemoteDataflows();
    }

    @Override
    public <T> void registerProxy(Class<T> klass, String name, T object) {
        if (klass == RemoteDataflowVariable.class) {
            remoteVariables.get(name).bind((DataflowVariable) object);
            return;
        }
        if (klass == RemoteDataflowBroadcast.class) {
            remoteBroadcasts.get(name).bind((RemoteDataflowBroadcast) object);
            return;
        }
        if (klass == RemoteDataflowQueue.class) {
            remoteQueues.get(name).bind((RemoteDataflowQueue) object);
            return;
        }
        throw new IllegalArgumentException("Unsupported proxy type");
    }

    @Override
    public <T> T get(Class<T> klass, String name) {
        if (klass == DataflowVariable.class) {
            return klass.cast(publishedVariables.get(name));
        }
        if (klass == DataflowBroadcast.class) {
            return klass.cast(publishedBroadcasts.get(name));
        }
        if (klass == DataflowQueue.class) {
            return klass.cast(publishedQueues.get(name));
        }
        throw new IllegalArgumentException("Unsupported type");
    }
}
