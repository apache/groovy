// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
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

package groovyx.gpars.actor;

import groovyx.gpars.actor.impl.MessageStream;

import java.io.Serializable;

/**
 * An internal representation of received messages holding both the original message plus the sender actor reference.
 * This class is not intended to be use directly by users.
 *
 * @author Vaclav Pech, Alex Tkachman
 *         Date: Feb 27, 2009
 */
public class ActorMessage implements Serializable {
    private static final long serialVersionUID = -2925547808451571430L;

    private Object payLoad;
    private MessageStream sender;
    //todo what are the values after deserialization?

    /**
     * Creates a new instance
     *
     * @param payLoad The original message
     * @param sender  The sending actor, null, if the message was not sent by an actor
     */
    public ActorMessage(final Object payLoad, final MessageStream sender) {
        this.payLoad = payLoad;
        this.sender = sender;
    }

    /**
     * Constructor for serialization
     */
    protected ActorMessage() {
    }

    public Object getPayLoad() {
        return payLoad;
    }

    public MessageStream getSender() {
        return sender;
    }

    /**
     * Factory method to create instances of ActorMessage with given payload.
     * The sender of the ActorMessage is retrieved from the ReplyRegistry.
     *
     * @param payLoad The original message
     * @return The newly created message
     */
    public static <T> ActorMessage build(final T payLoad) {
        return new ActorMessage(payLoad, Actor.threadBoundActor());
    }

    @Override
    public String toString() {
        return "Message from " + sender + ": " + payLoad;
    }
}
