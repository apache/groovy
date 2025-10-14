// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2010, 2013  The original author or authors
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

package groovyx.gpars.util;

import java.util.LinkedList;


/**
 * An implementation of the message queue for actor and agent messaging.
 * It leverages the fact that in any moment there's only one reading thread accessing the queue
 * and that potential read thread swap at the actor or agent thread pool synchronizes thread memory.
 * <p>
 * We also count on writers not to call the isEmpty() method
 * </p>
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"SynchronizedMethod", "ArithmeticOnVolatileField", "FieldAccessedSynchronizedAndUnsynchronized"})
public final class DefaultMessageQueue implements MessageQueue {

    private LinkedList<Object> outside = new LinkedList<Object>();
    private LinkedList<Object> inside = new LinkedList<Object>();
    private volatile boolean empty = true;

    @Override
    public boolean isEmpty() {
        return inside.isEmpty() && empty;
    }

    @Override
    public Object poll() {
        if (!inside.isEmpty()) {
            return inside.removeFirst();
        }
        final LinkedList<Object> localQueue = inside;
        inside = outside;
        swap(localQueue);
        if (!inside.isEmpty()) {
            return inside.removeFirst();
        }
        return null;
    }

    private synchronized void swap(final LinkedList<Object> localQueue) {
        outside = localQueue;
        empty = true;
    }

    @Override
    public synchronized void add(final Object element) {
        outside.add(element);
        empty = false;
    }
}
