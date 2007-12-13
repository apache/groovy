/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.runtime.iterator;

import java.util.LinkedList;
import java.util.Map;

public class QueuedIterator extends AbstractIterator {

    private final LinkedList queue = new LinkedList();

    public QueuedIterator(Map vars) {
        super(vars);
    }

    public void put(Object object) {
        queue.addLast(object);
    }

    public Object get() {
        return queue.removeFirst();
    }

    protected void doCheckNext() {
        if (!queue.isEmpty()) {
            hasNext = true;
            lastAcquired = get();
        }
    }

    protected boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    protected void clearQueue() {
        queue.clear();
    }

    protected int getQueueSize() {
        return queue.size();
    }
}
