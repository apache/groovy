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
import groovy.lang.MissingMethodException;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Runtime deconstruction support for record patterns (GEP-19) &mdash; the emit
 * target of the pattern switch lowering in the parser.
 * <p>
 * Record patterns are positional, but component names are not known at parse
 * time, so the lowering cannot emit direct accessor calls. Instead it emits
 * {@code RecordPatternSupport.components(value)} calls; this class resolves the
 * components at runtime: native records are deconstructed through their record
 * components (invoked via the meta-object protocol, so Groovy and Java records
 * behave identically), and other values are deconstructed through a
 * {@code toList()} method if one exists (which covers emulated Groovy records).
 *
 * @since 6.0.0
 */
@Incubating
public final class RecordPatternSupport {

    private RecordPatternSupport() {
    }

    /**
     * Returns the components of the given value, in declaration order.
     *
     * @throws GroovyRuntimeException if the value is neither a record nor deconstructable via {@code toList()}
     */
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
        try {
            Object components = InvokerHelper.invokeMethod(value, "toList", InvokerHelper.EMPTY_ARGS);
            if (components instanceof List) {
                return (List<Object>) components;
            }
        } catch (MissingMethodException ignore) {
        }
        throw new GroovyRuntimeException("Cannot deconstruct an instance of " + value.getClass().getName() + ": it is neither a record nor does it provide a toList() method");
    }
}
