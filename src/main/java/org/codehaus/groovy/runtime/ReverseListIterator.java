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
package org.codehaus.groovy.runtime;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A reverse iterator over a list. Utilizes the {@link ListIterator}
 * obtained from the provided {@link List} and converts it to an
 * {@link Iterator} that efficiently traverses the <code>List</code> in
 * reverse. The fail-fast semantics of this iterator are the same as the
 * semantics of the underlying <code>ListIterator</code>.
 */
public class ReverseListIterator<T> implements Iterator<T> {
    private final ListIterator<T> delegate;

    /**
     * Constructs a new <code>ReverseListIterator</code> for the provided list.
     * @param list the list to iterate over in reverse
     */
    public ReverseListIterator(List<T> list) {
        this.delegate = list.listIterator(list.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return delegate.hasPrevious();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T next() {
        return delegate.previous();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        delegate.remove();
    }
}
