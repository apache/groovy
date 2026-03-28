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
package org.apache.groovy.util.concurrent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Helper for starting threads that prefers virtual threads when running
 * on Java 21+, falling back to platform threads on older runtimes.
 *
 * @since 6.0.0
 */
public final class ThreadHelper {

    private static final MethodHandle VIRTUAL_START;

    static {
        MethodHandle h = null;
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            // Thread.ofVirtual() returns Thread.Builder.OfVirtual (Java 21+)
            Class<?> ofVirtualReturnType = Class.forName("java.lang.Thread$Builder$OfVirtual");
            MethodHandle ofVirtual = lookup.findStatic(Thread.class, "ofVirtual",
                    MethodType.methodType(ofVirtualReturnType));
            // start(Runnable) is declared on Thread.Builder
            Class<?> builderClass = Class.forName("java.lang.Thread$Builder");
            MethodHandle start = lookup.findVirtual(builderClass, "start",
                    MethodType.methodType(Thread.class, Runnable.class));
            // Adapt ofVirtual return type so collectArguments can combine them
            ofVirtual = ofVirtual.asType(MethodType.methodType(builderClass));
            // Combine: start(ofVirtual(), runnable)
            h = MethodHandles.collectArguments(start, 0, ofVirtual);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignore) {
            // Java < 21 — virtual threads not available
        }
        VIRTUAL_START = h;
    }

    private ThreadHelper() { }

    /**
     * Starts a new thread to execute the given task.
     * Uses a virtual thread on Java 21+, otherwise a platform thread.
     *
     * @param task the runnable to execute
     * @return the started thread
     */
    public static Thread startThread(Runnable task) {
        if (VIRTUAL_START != null) {
            try {
                return (Thread) VIRTUAL_START.invoke(task);
            } catch (Throwable ignore) {
                // unexpected failure — fall back to platform thread
            }
        }
        Thread t = new Thread(task);
        t.start();
        return t;
    }
}
