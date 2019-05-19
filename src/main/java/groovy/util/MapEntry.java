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
package groovy.util;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.Map;

/**
 * A Map.Entry implementation.
 */
public class MapEntry implements Map.Entry {

    private Object key;
    private Object value;

    public MapEntry(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    public boolean equals(Object that) {
        if (that instanceof MapEntry) {
            return equals((MapEntry) that);
        }
        return false;
    }

    public boolean equals(MapEntry that) {
        return DefaultTypeTransformation.compareEqual(this.key, that.key) && DefaultTypeTransformation.compareEqual(this.value, that.value);
    }

    public int hashCode() {
        return hash(key) ^ hash(value);
    }

    public String toString() {
        return "" + key + ":" + value;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public Object setValue(Object value) {
        this.value = value;
        return value;
    }

    /**
     * Helper method to handle object hashes for possibly null values
     */
    protected int hash(Object object) {
        return (object == null) ? 0xbabe : object.hashCode();
    }

}
