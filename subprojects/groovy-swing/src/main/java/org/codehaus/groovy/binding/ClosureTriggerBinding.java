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
package org.codehaus.groovy.binding;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Reference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClosureTriggerBinding implements TriggerBinding, SourceBinding {
    private static final BindPath[] EMPTY_BINDPATH_ARRAY = new BindPath[0];
    Map<String, TriggerBinding> syntheticBindings;
    Closure closure;

    public ClosureTriggerBinding(Map<String, TriggerBinding> syntheticBindings) {
        this.syntheticBindings = syntheticBindings;
    }

    public Closure getClosure() {
        return closure;
    }

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

    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
        if (source != this) {
            throw new RuntimeException("Source binding must the Trigger Binding as well");
        }
        final BindPathSnooper delegate = new BindPathSnooper();
        try {
            // create our own local copy of the closure
            final Class closureClass = closure.getClass();

            // do in privileged block since we may be looking at private stuff
            Closure closureLocalCopy = java.security.AccessController.doPrivileged(new PrivilegedAction<Closure>() {
                public Closure run() {
                    // assume closures have only 1 constructor, of the form (Object, Reference*)
                    Constructor constructor = closureClass.getConstructors()[0];
                    int paramCount = constructor.getParameterTypes().length;
                    Object[] args = new Object[paramCount];
                    args[0] = delegate;
                    for (int i = 1; i < paramCount; i++) {
                        args[i] = new Reference(new BindPathSnooper());
                    }
                    try {
                        boolean acc = constructor.isAccessible();
                        constructor.setAccessible(true);
                        Closure localCopy = (Closure) constructor.newInstance(args);
                        if (!acc) { constructor.setAccessible(false); }
                        localCopy.setResolveStrategy(Closure.DELEGATE_ONLY);
                        for (Field f:closureClass.getDeclaredFields()) {
                            acc = f.isAccessible();
                            f.setAccessible(true);
                            if (f.getType() == Reference.class) {
                                delegate.fields.put(f.getName(),
                                        (BindPathSnooper) ((Reference) f.get(localCopy)).get());
                            }
                            if (!acc) { f.setAccessible(false); }
                        }
                        return localCopy;
                    } catch (Exception e) {
                        throw new RuntimeException("Error snooping closure", e);
                    }
                }
            });
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

    public Object getSourceValue() {
        return closure.call();
    }
}

class DeadEndException extends RuntimeException {
    DeadEndException(String message) { super(message); }
}

class DeadEndObject {
    public Object getProperty(String property) {
        throw new DeadEndException("Cannot bind to a property on the return value of a method call");
    }
    public Object invokeMethod(String name, Object args) {
        return this;
    }
}

class BindPathSnooper extends GroovyObjectSupport {
    static final DeadEndObject DEAD_END = new DeadEndObject();

    Map<String, BindPathSnooper> fields = new LinkedHashMap<>();

    public Object getProperty(String property) {
        if (fields.containsKey(property)) {
            return fields.get(property);
        } else {
            BindPathSnooper snooper = new BindPathSnooper();
            fields.put(property, snooper);
            return snooper;
        }
    }

    public Object invokeMethod(String name, Object args) {
        return DEAD_END;
    }
}
