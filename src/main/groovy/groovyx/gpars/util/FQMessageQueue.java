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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


/**
 * An implementation of the message queue for actor and agent messaging using functional queues.
 * It leverages the fact that in any moment there's only one reading thread accessing the queue
 * and that potential read thread swap at the actor or agent thread pool synchronizes thread memory.
 * <p>
 * We also count on writers not to call the isEmpty() method
 * </p>
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"rawtypes", "RawUseOfParameterizedType", "unchecked"})
public final class FQMessageQueue implements MessageQueue {

    @SuppressWarnings({"FieldMayBeFinal"})
    private volatile Node outside = new EmptyNode();
    private Node inside = new EmptyNode();
    private final AtomicReferenceFieldUpdater outsideUpdater = AtomicReferenceFieldUpdater.newUpdater(FQMessageQueue.class, Node.class, "outside");

    @Override
    public boolean isEmpty() {
        return inside.isEmpty() && outside.isEmpty();
    }

    @Override
    public Object poll() {
        if (!inside.isEmpty()) {
            return pollFromInside();
        }
        while (true) {
            final Node newOutside = new EmptyNode();
            Node copy = outside;
            if (outsideUpdater.compareAndSet(this, copy, newOutside)) {
                while (!copy.isEmpty()) {
                    final Node current = copy;
                    copy = copy.next;
                    current.next = inside;
                    inside = current;
                }
                break;
            }
        }
        if (!inside.isEmpty()) {
            return pollFromInside();
        }
        return null;
    }

    private Object pollFromInside() {
        final Object result = inside.value;
        inside = inside.next;
        return result;
    }

    @Override
    public void add(final Object element) {
        while (true) {
            final Node currentNode = outside;
            final Node newNode = new Node(currentNode, element);
            if (outsideUpdater.compareAndSet(this, currentNode, newNode)) break;
        }
    }

    @SuppressWarnings({"PackageVisibleField"})
    static class Node {
        Node next;
        final Object value;

        Node(final Node next, final Object value) {
            this.next = next;
            this.value = value;
        }

        boolean isEmpty() {
            return false;
        }
    }

    static final class EmptyNode extends Node {
        EmptyNode() {
            super(null, null);
        }

        @Override
        boolean isEmpty() {
            return true;
        }
    }
}
