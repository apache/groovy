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
package org.apache.groovy.runtime;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import org.apache.groovy.lang.annotation.GroovyABI;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runtime deconstruction support for record patterns (GEP-19): the methods
 * the code derived from a record pattern calls, so compiled classes link to
 * them.
 * <p>
 * Record patterns are positional, but component names are not known when the
 * matching code is derived, so it cannot call accessors directly. Instead it
 * calls {@link #components}, which resolves the components at run time: a
 * record is deconstructed through its record components (invoked via the
 * meta-object protocol, so Groovy and Java records behave identically), and
 * any other value through a {@code toList()} method its meta-class provides,
 * which covers emulated Groovy records. The contract of {@code toList()} is
 * that it returns a {@code List} of the components in declaration order; a
 * value whose meta-class has no such method, or whose {@code toList()} returns
 * something else, cannot be deconstructed and fails the match with an
 * exception, while an exception {@code toList()} throws propagates as it is.
 *
 * @since 7.0.0
 */
@Incubating
@GroovyABI(since = "7.0.0")
public final class RecordPatternSupport {

    private static final Object BINDABLE = new Object();

    private RecordPatternSupport() {
    }

    /**
     * Returns the components of the given value, in declaration order.
     *
     * @throws GroovyRuntimeException if the value is {@code null}, is neither a
     *                                record nor responds to {@code toList()}, or
     *                                its {@code toList()} does not return a list
     */
    @SuppressWarnings("unchecked")
    public static List<Object> components(final Object value) {
        if (value == null) {
            throw new GroovyRuntimeException("Cannot deconstruct null");
        }
        RecordComponent[] recordComponents = value.getClass().getRecordComponents();
        if (recordComponents != null) {
            List<Object> result = new ArrayList<>(recordComponents.length);
            for (RecordComponent recordComponent : recordComponents) {
                result.add(InvokerHelper.invokeMethod(value, recordComponent.getName(), InvokerHelper.EMPTY_ARGS));
            }
            return result;
        }
        MetaClass metaClass = InvokerHelper.getMetaClass(value);
        if (metaClass.respondsTo(value, "toList", InvokerHelper.EMPTY_ARGS).isEmpty()) {
            throw new GroovyRuntimeException("Cannot deconstruct an instance of " + value.getClass().getName()
                    + ": it is neither a record nor does it provide a toList() method");
        }
        Object components = metaClass.invokeMethod(value, "toList", InvokerHelper.EMPTY_ARGS);
        if (!(components instanceof List)) {
            throw new GroovyRuntimeException("Cannot deconstruct an instance of " + value.getClass().getName()
                    + ": its toList() method returned " + (components == null ? "null" : "an instance of " + components.getClass().getName())
                    + " rather than a List");
        }
        return (List<Object>) components;
    }

    /**
     * Like {@link #components(Object)} but yields an empty list for {@code null}.
     * Used by the {@code instanceof} record pattern lowering, whose conjuncts are
     * combined without short-circuiting and so may deconstruct the (null) default
     * value of a pattern variable whose type check failed.
     */
    public static List<Object> componentsOrEmpty(final Object value) {
        return value == null ? Collections.emptyList() : components(value);
    }

    /**
     * Bounds-safe component access, yielding {@code null} when the index is out of
     * range. Used by the {@code instanceof} record pattern lowering (see
     * {@link #componentsOrEmpty}) where a failed arity check does not stop the
     * remaining component conjuncts from being evaluated.
     */
    public static Object component(final List<Object> components, final int index) {
        return index < components.size() ? components.get(index) : null;
    }

    /**
     * A non-null token that an {@code instanceof} binding of a {@code var}/{@code def}
     * record pattern component tests, so that the pattern variable is introduced
     * before {@link #bound} assigns it the (possibly null) component value.
     */
    public static Object bindable() {
        return BINDABLE;
    }

    /**
     * Accepts the assignment of a {@code var}/{@code def} record pattern component
     * to its pattern variable and returns {@code true}, keeping the match alive
     * whatever the component value.
     */
    public static boolean bound(final Object ignored) {
        return true;
    }
}
