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

package org.apache.groovy.internal.util;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.GroovyBugError;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * This class represents a reference to the most actual incarnation of a Metaclass.
 * INTERNAL USE ONLY.
 */
@Incubating
public class ReevaluatingReference<T> {
    private static final MethodHandle FALLBACK_HANDLE;
    static {
        try {
            //TODO Jochen: move the findSpecial to a central place together with others to easy security configuration
            FALLBACK_HANDLE = AccessController.doPrivileged((PrivilegedExceptionAction<MethodHandle>) () -> MethodHandles.lookup().findSpecial(
                    ReevaluatingReference.class, "replacePayLoad",
                    MethodType.methodType(Object.class),
                    ReevaluatingReference.class));
        } catch (PrivilegedActionException e) {
            throw new GroovyBugError(e);
        }
    }

    private final Supplier<T> valueSupplier;
    private final Function<T, SwitchPoint> validationSupplier;
    private final WeakReference<Class<T>> clazzRef;
    private MethodHandle returnRef;


    public ReevaluatingReference(Class clazz, Supplier<T> valueSupplier, Function<T, SwitchPoint> validationSupplier) {
        this.valueSupplier = valueSupplier;
        this.validationSupplier = validationSupplier;
        clazzRef = new WeakReference<Class<T>>(clazz);
        replacePayLoad();
    }

    private T replacePayLoad() {
        T payload = valueSupplier.get();
        MethodHandle ref = MethodHandles.constant(clazzRef.get(), payload);
        SwitchPoint sp = validationSupplier.apply(payload);
        returnRef = sp.guardWithTest(ref, FALLBACK_HANDLE);
        return payload;
    }

    public T getPayload() {
        T ref = null;
        try {
            ref = (T) returnRef.invokeExact();
        } catch (Throwable throwable) {
            UncheckedThrow.rethrow(throwable);
        }
        return ref;
    }
}
