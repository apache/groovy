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
package org.apache.groovy.swing.binding;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Reference;
import org.codehaus.groovy.reflection.ReflectionUtils;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates bindings that track the property path referenced by a closure expression.
 */
public class ClosureTriggerBinding implements TriggerBinding, SourceBinding {
    private static final BindPath[] EMPTY_BINDPATH_ARRAY = new BindPath[0];
    /**
     * Synthetic trigger bindings available while resolving closure paths.
     */
    Map<String, TriggerBinding> syntheticBindings;
    /**
     * The closure used to evaluate the source value and discover observed properties.
     */
    Closure closure;

    /**
     * Creates a closure trigger binding with the supplied synthetic trigger registry.
     *
     * @param syntheticBindings synthetic trigger bindings keyed by class and property
     */
    public ClosureTriggerBinding(Map<String, TriggerBinding> syntheticBindings) {
        this.syntheticBindings = syntheticBindings;
    }

    /**
     * Returns the closure evaluated by this trigger binding.
     *
     * @return the source closure
     */
    public Closure getClosure() {
        return closure;
    }

    /**
     * Replaces the closure evaluated by this trigger binding.
     *
     * @param closure the new source closure
     */
    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    private BindPath createBindPath(String propertyName, BindPathSnooper snooper) {
        BindPath bp = new BindPath();
        bp.propertyName = propertyName;
        bp.updateLocalSyntheticProperties(syntheticBindings);
        List<BindPath> childPaths = new ArrayList<>();
        for (Map.Entry<String, BindPathSnooper> entry : snooper.fields.entrySet()) {
            childPaths.add(createBindPath(entry.getKey(), entry.getValue()));
        }
        bp.children = childPaths.toArray(EMPTY_BINDPATH_ARRAY);
        return bp;
    }

    /**
     * Creates a property-path-aware binding by snooping the closure's property accesses.
     *
     * @param source the source binding, which must be this trigger binding
     * @param target the target binding
     * @return a property-path-aware full binding
     */
    @Override
    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
        if (source != this) {
            throw new RuntimeException("Source binding must the Trigger Binding as well");
        }
        final BindPathSnooper delegate = new BindPathSnooper();
        try {
            // create our own local copy of the closure
            final Class<?> closureClass = closure.getClass();

            // assume closures have only 1 constructor, of the form (Object, Reference*)
            Constructor<?> constructor = closureClass.getConstructors()[0];
            int paramCount = constructor.getParameterTypes().length;
            Object[] args = new Object[paramCount];
            args[0] = delegate;
            for (int i = 1; i < paramCount; i++) {
                args[i] = new Reference<Object>(new BindPathSnooper());
            }
            Closure closureLocalCopy;
            try {
                ReflectionUtils.trySetAccessible(constructor);
                closureLocalCopy = (Closure) constructor.newInstance(args);
                closureLocalCopy.setResolveStrategy(Closure.DELEGATE_ONLY);
                for (Field f:closureClass.getDeclaredFields()) {
                    ReflectionUtils.trySetAccessible(f);
                    if (f.getType() == Reference.class) {
                        delegate.fields.put(f.getName(),
                                (BindPathSnooper) ((Reference) f.get(closureLocalCopy)).get());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error snooping closure", e);
            }
            try {
                closureLocalCopy.call();
            } catch (DeadEndException e) {
                // we want this exception exposed.
                throw e;
            } catch (Exception e) {
                //LOGME
                // ignore it, likely failing because we are faking out properties
                // such as a call to Math.min(int, BindPathSnooper)
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new RuntimeException("A closure expression binding could not be created because of " + e.getClass().getName() + ":\n\t" + e.getMessage());
        }
        List<BindPath> rootPaths = new ArrayList<>();
        for (Map.Entry<String, BindPathSnooper> entry : delegate.fields.entrySet()) {
            BindPath bp =createBindPath(entry.getKey(), entry.getValue());
            bp.currentObject = closure;
            rootPaths.add(bp);
        }
        PropertyPathFullBinding fb = new PropertyPathFullBinding();
        fb.setSourceBinding(new ClosureSourceBinding(closure));
        fb.setTargetBinding(target);
        fb.bindPaths = rootPaths.toArray(EMPTY_BINDPATH_ARRAY);
        return fb;
    }

    /**
     * Evaluates the configured closure to obtain the current source value.
     *
     * @return the current source value
     */
    @Override
    public Object getSourceValue() {
        return closure.call();
    }
}

/**
 * Signals that closure snooping reached a method-return boundary that cannot be traversed.
 */
class DeadEndException extends RuntimeException {
    /**
     * Creates an exception with the supplied message.
     *
     * @param message the detail message
     */
    DeadEndException(String message) { super(message); }
}

/**
 * Placeholder object returned from snooped method calls to prevent deeper property traversal.
 */
class DeadEndObject {
    /**
     * Rejects attempts to continue binding through a method return value.
     *
     * @param property the requested property
     * @return never returns normally
     */
    public Object getProperty(String property) {
        throw new DeadEndException("Cannot bind to a property on the return value of a method call");
    }

    /**
     * Treats further method calls as additional dead-end placeholders.
     *
     * @param name the method name
     * @param args the method arguments
     * @return this placeholder
     */
    public Object invokeMethod(String name, Object args) {
        return this;
    }
}

/**
 * Records the property graph visited while snooping a binding closure.
 */
class BindPathSnooper extends GroovyObjectSupport {
    /**
     * Shared placeholder returned when a snooped method call terminates traversal.
     */
    static final DeadEndObject DEAD_END = new DeadEndObject();

    /**
     * Child property snoopers keyed by property name.
     */
    Map<String, BindPathSnooper> fields = new LinkedHashMap<>();

    /**
     * Returns or creates a child snooper for the requested property.
     *
     * @param property the property being observed
     * @return the child snooper for that property
     */
    @Override
    public Object getProperty(String property) {
        if (fields.containsKey(property)) {
            return fields.get(property);
        } else {
            BindPathSnooper snooper = new BindPathSnooper();
            fields.put(property, snooper);
            return snooper;
        }
    }

    /**
     * Treats any method call as a traversal dead end.
     *
     * @param name the method name
     * @param args the method arguments
     * @return the shared dead-end placeholder
     */
    @Override
    public Object invokeMethod(String name, Object args) {
        return DEAD_END;
    }
}
