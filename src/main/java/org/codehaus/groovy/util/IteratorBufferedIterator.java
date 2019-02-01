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
package org.codehaus.groovy.util;

import groovy.util.BufferedIterator;

import java.util.Iterator;

/**
 * An implementation for BufferedIterator wraps Iterator.
 *
 * @since 2.5.0
 */
public class IteratorBufferedIterator<T> implements BufferedIterator<T> {

    private final Iterator<T> iter;
    private boolean hasBuffered;
    private T buffered;

    public IteratorBufferedIterator(Iterator<T> iter) {
        this.iter = iter;
        this.hasBuffered = false;
    }

    public boolean hasNext() {
        return hasBuffered || iter.hasNext();
    }

    public T next() {
        if (hasBuffered) {
            T buffered = this.buffered;
            this.buffered = null;
            hasBuffered = false;
            return buffered;
        } else {
            return iter.next();
        }
    }

    public void remove() {
        if (hasBuffered) {
            throw new IllegalStateException("Can't remove from " + this + " when an item is buffered.");
        } else {
            iter.remove();
        }
    }

    /**
     * Return the next element to be returned by next() without consuming it.
     */
    public T head() {
        if (!hasBuffered) {
            buffered = iter.next();
            hasBuffered = true;
        }
        return buffered;
    }
}
