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

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An implementation for BufferedIterator wraps ListIterator.  This version
 * provides an implementation for remove().
 *
 * @since 2.5.0
 */
public class ListBufferedIterator<T> implements BufferedIterator<T> {

    private final List<T> list;
    private final ListIterator<T> iter;

    public ListBufferedIterator(List<T> list) {
        this.list = list;
        this.iter = list.listIterator();
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public T next() {
        return iter.next();
    }

    @Override
    public void remove() {
        iter.remove();
    }

    /**
     * Return the next element to be returned by next() without consuming it.
     */
    @Override
    public T head() {
        int index = iter.nextIndex();
        if (index >= list.size()) {
            throw new NoSuchElementException();
        } else {
            return list.get(index);
        }
    }
}
