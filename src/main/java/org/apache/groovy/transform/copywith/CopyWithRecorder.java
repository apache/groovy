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
package org.apache.groovy.transform.copywith;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Map;

/**
 * Recording delegate for the transactional {@code copyWith { }} block. It does
 * not mutate anything: property assignments and {@code prop.modify { }} calls
 * are recorded as a (possibly dotted) path-keyed map which is then handed to
 * the nested-aware {@code copyWith(Map)} for reconstruction.
 * <p>
 * Reads of a property return a child recorder bound to the extended path, so
 * {@code address.city = 'NYC'} records {@code 'address.city'}.
 * <p>
 * The reserved name {@code old} resolves to the original (root) object —
 * aligning with the {@code old} of {@code @Ensures}/{@code @Contract} in
 * design-by-contract — so a value may be derived from the pre-state:
 * {@code address.city = old.address.city.reverse()} or, cross-field,
 * {@code name = old.company.name}. The concise single-field shorthand
 * {@code address.city.modify { it.reverse() }} remains for the common
 * transform-this-same-field case.
 * <p>
 * On a navigated path the recorder intercepts {@code modify}, so a same-named
 * real method on the value type is shadowed there. {@code old} is the escape
 * hatch: past {@code old} the RHS is the real object, so a genuine
 * {@code modify(Closure)} (or any other domain method) runs and its result is
 * recorded — e.g. {@code balance = old.balance.modify { it + 50 }}. {@code old}
 * may equally be preferred purely for style, when {@code .modify} on a path
 * could be misread, even if no real method exists.
 *
 * @since 6.0.0
 */
@Incubating
public class CopyWithRecorder extends GroovyObjectSupport {

    private final Object self;
    private final String prefix;
    private final Map<Object, Object> sink;

    public CopyWithRecorder(final Object self, final String prefix, final Map<Object, Object> sink) {
        this.self = self;
        this.prefix = prefix;
        this.sink = sink;
    }

    private String path(final String name) {
        return prefix.isEmpty() ? name : prefix + "." + name;
    }

    @Override
    public Object getProperty(final String name) {
        // 'old' is the pre-state root (cf. @Ensures/@Contract old), so RHS
        // expressions can derive new values from the original object.
        if ("old".equals(name)) return self;
        return new CopyWithRecorder(self, path(name), sink);
    }

    @Override
    public void setProperty(final String name, final Object value) {
        sink.put(path(name), value);
    }

    @Override
    public Object invokeMethod(final String name, final Object args) {
        if ("modify".equals(name) && !prefix.isEmpty()) {
            Object[] a = args instanceof Object[] ? (Object[]) args : new Object[]{args};
            if (a.length == 1 && a[0] instanceof Closure) {
                sink.put(prefix, ((Closure<?>) a[0]).call(currentValue()));
                return null;
            }
        }
        // Unknown call: let the closure fall back to its owner (DELEGATE_FIRST)
        return super.invokeMethod(name, args);
    }

    private Object currentValue() {
        Object o = self;
        for (String seg : prefix.split("\\.")) {
            o = InvokerHelper.getProperty(o, seg);
        }
        return o;
    }
}
