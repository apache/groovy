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
package groovy.lang;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a list of Objects.
 */
public class Tuple<E> extends AbstractList<E> implements Serializable {
    private static final long serialVersionUID = -6707770506387821031L;
    private final E[] contents;

    @SafeVarargs
    public Tuple(E... contents) {
        if (contents == null) throw new NullPointerException();
        this.contents = contents;
    }

    @Override
    public E get(int index) {
        return contents[index];
    }

    @Override
    public int size() {
        return contents.length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        int size = toIndex - fromIndex;
        E[] newContent = (E[]) new Object[size];
        System.arraycopy(contents, fromIndex, newContent, 0, size);
        return new Tuple<>(newContent);
    }

    public Tuple<E> subTuple(int fromIndex, int toIndex) {
        return (Tuple<E>) subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;

        Tuple that = (Tuple) o;
        int size = size();
        if (size != that.size()) return false;
        for (int i = 0; i < size; i++) {
            if (!DefaultTypeTransformation.compareEqual(get(i), that.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contents);
    }
}
