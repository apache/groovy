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
import groovy.lang.GroovyRuntimeException;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime support for nested-path {@code copyWith}. Resolves dotted keys such
 * as {@code 'address.city'} into a flat map of top-level property to its
 * replacement value, by recursively applying {@code copyWith} to the affected
 * nested nodes. Plain (non-dotted) keys pass through unchanged.
 * <p>
 * A nested node whose type does not provide {@code copyWith(Map)} fails with
 * a clear, specific error rather than silent or partial behaviour. Identity
 * is preserved transitively: an unchanged nested node yields its original
 * reference, so an unchanged graph yields the original root.
 *
 * @since 6.0.0
 */
@Incubating
public final class NestedCopyWithSupport {

    private NestedCopyWithSupport() {
    }

    /**
     * Transactional-block form. Runs {@code block} against a recording
     * delegate that captures plain assignments, nested-path navigation, and
     * {@code prop.modify { old -> ... }} updates, then delegates to the
     * (nested-aware) {@code copyWith(Map)}. The block is thus pure sugar over
     * the map form, inheriting its closed-type-domain and identity guarantees.
     */
    public static Object applyBlock(final Object self, final Closure<?> block) {
        Map<Object, Object> sink = new LinkedHashMap<>();
        Closure<?> c = (Closure<?>) block.clone();
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        c.setDelegate(new CopyWithRecorder(self, "", sink));
        c.call();
        if (sink.isEmpty()) return self;
        return InvokerHelper.invokeMethod(self, "copyWith", new Object[]{sink});
    }

    @SuppressWarnings("unchecked")
    public static Map<Object, Object> flatten(final Object self, final Map<Object, Object> raw) {
        if (raw == null) return new LinkedHashMap<>();
        Map<Object, Object> flat = new LinkedHashMap<>();
        Map<String, Map<Object, Object>> nested = new LinkedHashMap<>();

        for (Map.Entry<Object, Object> e : raw.entrySet()) {
            Object key = e.getKey();
            String k = key == null ? null : key.toString();
            int dot = k == null ? -1 : k.indexOf('.');
            if (dot < 0) {
                if (nested.containsKey(k)) {
                    throw conflict(k);
                }
                flat.put(key, e.getValue());
            } else {
                String head = k.substring(0, dot);
                String rest = k.substring(dot + 1);
                if (flat.containsKey(head)) {
                    throw conflict(head);
                }
                nested.computeIfAbsent(head, h -> new LinkedHashMap<>()).put(rest, e.getValue());
            }
        }

        for (Map.Entry<String, Map<Object, Object>> e : nested.entrySet()) {
            String head = e.getKey();
            Object current = InvokerHelper.getProperty(self, head);
            if (current == null) {
                throw new GroovyRuntimeException("copyWith: cannot apply a nested update to '"
                        + head + "' because its current value is null");
            }
            // A nested node must itself expose copyWith(Map); fail clearly otherwise.
            // Probe with the actual nested map so a type that only has
            // copyWith()/copyWith(Closure) does not falsely pass this guard.
            boolean supported = !InvokerHelper.getMetaClass(current)
                    .respondsTo(current, "copyWith", new Object[]{e.getValue()}).isEmpty();
            if (!supported) {
                throw new GroovyRuntimeException("copyWith: nested update of '" + head
                        + "' requires its type (" + current.getClass().getName()
                        + ") to provide a copyWith(Map) method — e.g. an @Immutable/@RecordType "
                        + "declared with copyWith=true. This type is outside the supported "
                        + "nested-copyWith domain.");
            }
            Object updated = InvokerHelper.invokeMethod(current, "copyWith", new Object[]{e.getValue()});
            flat.put(head, updated);
        }
        return flat;
    }

    private static GroovyRuntimeException conflict(final String head) {
        return new GroovyRuntimeException("copyWith: cannot combine a whole-value update and a "
                + "nested update for '" + head + "'");
    }
}
