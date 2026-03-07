/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.concurrent;

/**
 * Thrown when an {@link AsyncChannel} operation is attempted after the channel
 * has been closed.
 * <p>
 * This exception is raised in the following situations:
 * <ul>
 *   <li>{@link AsyncChannel#send(Object) send()} — the channel was closed before
 *       or during the send attempt.  Pending senders that were waiting for buffer
 *       space when the channel closed also receive this exception.</li>
 *   <li>{@link AsyncChannel#receive() receive()} — the channel was closed and all
 *       buffered values have been drained.  Note that values buffered before
 *       closure are still delivered normally; only once the buffer is exhausted
 *       does this exception appear.</li>
 * </ul>
 * <p>
 * When used with {@code for await}, the loop infrastructure translates
 * {@code ChannelClosedException} into a clean end-of-stream signal (i.e.,
 * the loop exits normally rather than propagating the exception):
 * <pre>
 * def ch = AsyncChannel.create()
 * // ... producer sends values, then calls ch.close()
 * for await (item in ch) {
 *     process(item)       // processes all buffered values
 * }
 * // loop exits cleanly after the channel is closed and drained
 * </pre>
 *
 * @see AsyncChannel#send(Object)
 * @see AsyncChannel#receive()
 * @see AsyncChannel#close()
 * @since 6.0.0
 */
public class ChannelClosedException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a {@code ChannelClosedException} with the specified detail message.
     *
     * @param message the detail message describing which operation failed
     */
    public ChannelClosedException(String message) {
        super(message);
    }

    /**
     * Creates a {@code ChannelClosedException} with the specified detail message
     * and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause (e.g., an {@link InterruptedException}
     *                if the thread was interrupted while waiting on a channel operation)
     */
    public ChannelClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
