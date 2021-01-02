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
package org.apache.groovy.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

/**
 * Represents view of reversed list
 *
 * @since 4.0.0
 */
public class ReversedList<E> extends AbstractList<E> implements RandomAccess, Serializable {
    private static final long serialVersionUID = -1640781973848935560L;
    private final List<E> delegate;

    public ReversedList(List<E> list) {
        this.delegate = list;
    }

    @Override
    public E get(int index) {
        return delegate.get(delegate.size() - 1 - index);
    }

    @Override
    public int size() {
        return delegate.size();
    }
}
