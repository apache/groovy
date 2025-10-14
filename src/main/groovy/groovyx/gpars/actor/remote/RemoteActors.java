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

package groovyx.gpars.actor.remote;

import groovyx.gpars.actor.Actor;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.Promise;
import groovyx.gpars.remote.BroadcastDiscovery;
import groovyx.gpars.remote.RemotingContextWithUrls;
import groovyx.gpars.remote.LocalHost;
import groovyx.gpars.remote.message.RemoteActorRequestMsg;
import groovyx.gpars.remote.netty.discovery.DiscoveryClient;
import groovyx.gpars.remote.netty.discovery.DiscoveryServer;

import java.util.Map;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remoting context for Actors. Manages serialization, publishing and retrieval.
 *
 * @author Rafal Slawik
 */
public final class RemoteActors extends LocalHost implements RemotingContextWithUrls {

    private final String contextName;

    /**
     * Stores Actors published in context of this instance of RemoteActors.
     */
    private final Map<String, Actor> publishedActors;

    /**
     * Stores promises to remote instances of Actors.
     */
    private final Map<String, DataflowVariable<Actor>> remoteActors;

    /**
     * Server of broadcast discovery service
     */
    private DiscoveryServer discoveryServer;

    private RemoteActors(String contextName) {
        publishedActors = new ConcurrentHashMap<>();
        remoteActors = new ConcurrentHashMap<>();
        this.contextName = contextName != null ? contextName : getId().toString();
    }

    /**
     * Publishes {@link groovyx.gpars.actor.Actor} under given name.
     * It overrides previously published Actor if the same name is given.
     * @param actor the Actor to be published
     * @param name the name under which Actor is published
     */
    public void publish(Actor actor, String name) {
        if (!RemoteActorsUrlUtils.isValidActorName(name)) {
            throw new IllegalArgumentException("Cannot publish actor under given name: " + name);
        }
        publishedActors.put(name, actor);
    }

    /**
     * Retrieves {@link groovyx.gpars.actor.Actor} published under specified name on remote host.
     * @param host the address of remote host
     * @param port the port of remote host
     * @param name the name under which Actor was published
     * @return promise of {@link groovyx.gpars.actor.remote.RemoteActor}
     */
    public Promise<Actor> get(String host, int port, String name) {
        return getPromise(remoteActors, name, host, port, new RemoteActorRequestMsg(this.getId(), name));
    }

    /**
     * Retrieves {@link groovyx.gpars.actor.Actor} published under specified url
     * @param actorUrl the actor url
     * @return promise of {@link groovyx.gpars.actor.remote.RemoteActor}
     */
    public Promise<Actor> get(String actorUrl) {
        return get(actorUrl, DiscoveryServer.DEFAULT_BROADCAST_PORT);
    }

    public Promise<Actor> get(final String actorUrl, int broadcastPort) {
        DiscoveryClient client = new DiscoveryClient(broadcastPort);

        final DataflowVariable<Actor> result = new DataflowVariable<Actor>();
        Promise<InetSocketAddress> serverSocketAddressPromise = client.ask(actorUrl);
        serverSocketAddressPromise.whenBound(new MessageStream() {
            @Override
            public MessageStream send(Object message) {
                InetSocketAddress serverSocketAddress = (InetSocketAddress) message;
                Promise<Actor> actorPromise = get(serverSocketAddress.getHostName(), serverSocketAddress.getPort(), RemoteActorsUrlUtils.getActorName(actorUrl));
                actorPromise.whenBound(new MessageStream() {
                    @Override
                    public MessageStream send(Object message) {
                        result.bind(((Actor) message));
                        return this;
                    }
                });
                return this;
            }
        });
        return result;
    }

    /**
     * Checks if {@link groovyx.gpars.actor.Actor} under specified url was published within this context
     * @param actorUrl the actor url
     * @return true if url matches some actor within this context
     */
    public boolean has(String actorUrl) {
        String groupName = RemoteActorsUrlUtils.getGroupName(actorUrl);
        String actorName = RemoteActorsUrlUtils.getActorName(actorUrl);
        return publishedActors.containsKey(actorName) && (groupName.isEmpty() || contextName.equals(groupName));
    }

    public static RemoteActors create() {
        return new RemoteActors(null);
    }

    /**
     * Creates remoting context for Actors with specified name
     * @param contextName the name of the context
     * @return Context with specified name
     */
    public static RemoteActors create(String contextName) {
        return new RemoteActors(contextName);
    }

    @Override
    public <T> void registerProxy(Class<T> klass, String name, T object) {
        if (klass == RemoteActor.class) {
            remoteActors.get(name).bind(((Actor) object));
            return;
        }
        throw new IllegalArgumentException("Unsupported proxy type");
    }

    @Override
    public <T> T get(Class<T> klass, String name) {
        if (klass == Actor.class) {
            return klass.cast(publishedActors.get(name));
        }
        throw new IllegalArgumentException("Unsupported type");
    }

    @Override
    public void startServer(String host, int port) {
        startServer(host, port, DiscoveryServer.DEFAULT_BROADCAST_PORT);
    }

    public void startServer(String host, int port, int broadcastPort) {
        super.startServer(host, port);

        final InetSocketAddress serverSocketAddress = new InetSocketAddress(host, port);
        discoveryServer = new DiscoveryServer(broadcastPort, serverSocketAddress, this);
        discoveryServer.start();
    }

    @Override
    public void stopServer() {
        super.stopServer();
        discoveryServer.stop();
    }
}
