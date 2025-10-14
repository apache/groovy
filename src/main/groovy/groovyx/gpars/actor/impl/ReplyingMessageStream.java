// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
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

package groovyx.gpars.actor.impl;

import groovyx.gpars.actor.Actor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Tkachman, Vaclav Pech
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public abstract class ReplyingMessageStream extends Actor {
    private static final long serialVersionUID = -4660316352077009411L;
    /**
     * A list of senders for the currently processed messages
     */
    private MessageStream sender = null;

    @SuppressWarnings({"ReturnOfCollectionOrArrayField"})
    protected final MessageStream getSender() {
        return sender;
    }

    protected final void setSender(final MessageStream sender) {
        this.sender = sender;
    }

    /**
     * Sends a reply to all currently processed messages. Throws ActorReplyException if some messages
     * have not been sent by an actor. For such cases use replyIfExists().
     *
     * @param message reply message
     * @throws groovyx.gpars.actor.impl.ActorReplyException
     *          If some of the replies failed to be sent.
     */
    protected final void reply(final Object message) {
        if (sender == null) throw new ActorReplyException(CANNOT_SEND_REPLIES_NO_SENDER_HAS_BEEN_REGISTERED);
        final List<Exception> exceptions = new ArrayList<Exception>();
        try {
            sender.send(message);
        } catch (IllegalStateException e) {
            exceptions.add(e);
        }
        if (!exceptions.isEmpty()) {
            throw new ActorReplyException("Failed sending some replies. See the issues field for details", exceptions);
        }
    }

    /**
     * Sends a reply to all currently processed messages, which have been sent by an actor.
     * Ignores potential errors when sending the replies, like no sender or sender already stopped.
     *
     * @param message reply message
     */
    protected final void replyIfExists(final Object message) {
        if (sender == null) return;
        try {
            sender.send(message);
        } catch (IllegalStateException ignore) {
        }
    }
}
