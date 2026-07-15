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
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassRegistry;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.PackedClosure;

import java.util.Collections;
import java.util.List;

/**
 * The stock metaclass for {@link PackedClosure}, giving packed closures their own MOP
 * standing (the analog of {@link ClosureMetaClass} for generated closure classes, whose
 * per-class metaclasses packed closures deliberately do not have).
 * <p>
 * {@code invokeMethod("call"/"doCall")} dispatches straight to the adapter — a plain
 * virtual call that routes to the hosting class's dispatch tables — with no reflection
 * anywhere on the path, and, matching {@link ClosureMetaClass}, without consulting
 * categories for the closure-invocation names (categories have never applied to closure
 * {@code doCall} dispatch). Each fixed-arity family member class gets its own instance of
 * this metaclass from the registry (a per-class registry can only be per-adapter-class),
 * so class-level metaclass changes affect every packed closure of that member's arity;
 * per-instance {@code setMetaClass} is honoured as usual — the adapter's dispatch guard
 * routes perturbed instances through the replacement's {@code invokeMethod}.
 * <p>
 * {@code respondsTo(Object, String, Object[])} is instance-faithful: the fixed-arity
 * family member's {@code doCall}(s) are erased to {@code Object} parameters (and
 * {@code FixedN}'s is varargs), so the answer for the closure-invocation names is
 * additionally filtered by the instance's declared parameter types (which the adapter
 * carries for exactly this purpose). Purely class-level introspection
 * ({@code pickMethod}, {@code getMetaMethods}) necessarily reflects the shared adapter
 * class, not any single literal.
 *
 * @since 6.0.0
 */
public final class PackedClosureMetaClass extends MetaClassImpl {

    private static final Object[] NO_ARGS = new Object[0];

    public PackedClosureMetaClass(final MetaClassRegistry registry, final Class theClass) {
        super(registry, theClass);
    }

    @Override
    public Object invokeMethod(final Class sender, final Object object, final String methodName,
                               final Object[] originalArguments, final boolean isCallToSuper, final boolean fromInsideClass) {
        if (!isCallToSuper && object instanceof PackedClosure
                && ("call".equals(methodName) || "doCall".equals(methodName))) {
            // reflection-free: a plain virtual call into the adapter, which coerces and routes
            // to the hosting class's dispatch tables; user exceptions propagate unwrapped, as
            // Closure.call's fallback expects. dispatchAll receives the argument array INTACT
            // (a single Object[] argument stays one argument, as on a generated closure class),
            // where MetaMethod selection on a varargs doCall would spread it.
            return ((PackedClosure) object).dispatchAll((originalArguments != null) ? originalArguments : NO_ARGS);
        }
        return super.invokeMethod(sender, object, methodName, originalArguments, isCallToSuper, fromInsideClass);
    }

    @Override
    public List<MetaMethod> respondsTo(final Object obj, final String name, final Object[] argTypes) {
        List<MetaMethod> answer = super.respondsTo(obj, name, argTypes);
        if (!answer.isEmpty() && obj instanceof PackedClosure
                && ("call".equals(name) || "doCall".equals(name))) {
            // the generic varargs doCall answers everything; filter by the instance's
            // declared parameter types so introspection matches a generated closure class
            Class<?>[] classes = MetaClassHelper.castArgumentsToClassArray(argTypes);
            if (!new ParameterTypes(((PackedClosure) obj).getParameterTypes()).isValidMethod(classes)) {
                return Collections.emptyList();
            }
        }
        return answer;
    }
}
