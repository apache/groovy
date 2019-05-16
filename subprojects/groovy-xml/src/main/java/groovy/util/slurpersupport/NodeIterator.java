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
package groovy.util.slurpersupport;

import java.util.Iterator;

/**
 * Helper class for iterating through nodes.
 */
@Deprecated
public abstract class NodeIterator implements Iterator {
    private static final Object DELAYED_INIT = new Object();
    private final Iterator iter;
    private Object nextNode;

    public NodeIterator(final Iterator iter) {
        this.iter = iter;
        this.nextNode = DELAYED_INIT;
    }

    private void initNextNode(){
        if (nextNode==DELAYED_INIT) nextNode = getNextNode(iter);
    }

    public boolean hasNext() {
        initNextNode();
        return this.nextNode != null;
    }

    public Object next() {
        initNextNode();
        try {
            return this.nextNode;
        } finally {
            this.nextNode = getNextNode(this.iter);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected abstract Object getNextNode(final Iterator iter);
}
