// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2011, 2013  The original author or authors
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

import groovyx.gpars.dataflow.stream.DataflowStreamWriteAdapter;
import groovyx.gpars.dataflow.stream.StreamCore;
import groovyx.gpars.dataflow.stream.SyncDataflowStream;

/**
 * Offers a synchronous deterministic one-to-many and many-to-many messaging alternative to SyncDataflowQueue.
 * Internally it wraps a SyncDataflowStream class with a DataflowStreamWriteAdapter and so
 * synchronizes all writes to the underlying stream allowing multiple threads accessing the stream concurrently.
 * On demand through the createReadChannel() method it will return an DataflowReadChannel through which the reader will receive
 * all messages written to the channel since then.
 * <p>
 * Typical use:
 * </p>
 * <pre>
 * DataflowWriteChannel broadcastStream = new DataflowBroadcast()
 * DataflowReadChannel stream1 = broadcastStream.createReadChannel()
 * DataflowReadChannel stream2 = broadcastStream.createReadChannel()
 * broadcastStream &lt;&lt; 'Message'
 * assert stream1.val == stream2.val
 * </pre>
 *
 * @param <T> The type of messages to pass through the stream
 * @author Vaclav Pech
 */
public final class SyncDataflowBroadcast<T> extends DataflowStreamWriteAdapter<T> {

    /**
     * Creates a new instance
     */
    public SyncDataflowBroadcast() {
        super(new SyncDataflowStream<T>(0));
    }

    @SuppressWarnings({"SynchronizedMethod"})
    @Override
    public synchronized String toString() {
        return "SyncDataflowBroadcast around " + super.toString();
    }

    /**
     * Retrieves an implementation of DataflowReadChannel to read all messages submitted to the broadcast chanel.
     * Since multiple parties (threads/tasks/actors/...) may ask for read channels independently, the submitted messages are effectively
     * broadcast to all the subscribers.
     *
     * @return A read channel to receive messages submitted to the broadcast channel from now on.
     */
    public synchronized DataflowReadChannel<T> createReadChannel() {
        final StreamCore<T> head = getHead();
        head.incrementParties();
        return new SyncDataflowStreamReadAdapter<T>(head);
    }

    /**
     * Un-registers the supplied read channel from the broadcast. The number of parties that have to meet at data exchange is reduced by one.
     *
     * @param channel The channel to unsubscribe. The channel won't be able to read further messages.
     * @throws InterruptedException If the thread got interrupted
     */
    public synchronized void unsubscribeReadChannel(final DataflowReadChannel<T> channel) throws InterruptedException {
        if (!(channel instanceof SyncDataflowStreamReadAdapter))
            throw new IllegalArgumentException("The supplied channel has not been subscribed to this synchronous broadcast - " + channel);
//        final StreamCore<T> head = getHead();
//        head.decrementParties();
        ((SyncDataflowStreamReadAdapter<T>) channel).close();
    }
}
