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

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.runtime.FormatHelper;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a value with name
 *
 * @param <T> the value type
 * @since 4.0.0
 */
@Incubating
public class NamedValue<T> implements Serializable {
    private static final long serialVersionUID = 8853713635573845253L;
    private final String name;
    private final T val;

    public String getName() {
        return name;
    }

    public T getVal() {
        return val;
    }

    public NamedValue(String name, T val) {
        this.name = name;
        this.val = val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedValue<?> that = (NamedValue<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(val, that.val);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, val);
    }

    @Override
    public String toString() {
        return name + "=" + FormatHelper.format(val, true, false, -1, true);
    }

    public String toString(Map<String, Object> options) {
        return name + "=" + FormatHelper.toString(options, val);
    }
}
