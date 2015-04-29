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

import java.util.AbstractList;
import java.util.List;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * Represents a list of Objects.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class Tuple extends AbstractList {
    private final Object[] contents;
    private int hashCode;

    public Tuple(Object[] contents) {
        if (contents == null) throw new NullPointerException();
        this.contents = contents;
    }

    public Object get(int index) {
        return contents[index];
    }

    public int size() {
        return contents.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Tuple)) return false;

        Tuple that = (Tuple) o;
        if (size() != that.size()) return false;
        for (int i = 0; i < contents.length; i++) {
            if (!DefaultTypeTransformation.compareEqual(contents[i], that.contents[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            for (int i = 0; i < contents.length; i++ ) {
                Object value = contents[i];
                int hash = (value != null) ? value.hashCode() : 0xbabe;
                hashCode ^= hash;
            }
            if (hashCode == 0) {
                hashCode = 0xbabe;
            }
        }
        return hashCode;
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        int size = toIndex - fromIndex;
        Object[] newContent = new Object[size];
        System.arraycopy(contents, fromIndex, newContent, 0, size);
        return new Tuple(newContent);
    }
}
