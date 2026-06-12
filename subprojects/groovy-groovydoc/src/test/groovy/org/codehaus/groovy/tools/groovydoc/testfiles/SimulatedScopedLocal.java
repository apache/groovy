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
package org.codehaus.groovy.tools.groovydoc.testfiles;

import java.util.function.Supplier;

/**
 * A test fixture that mimics the nested-carrier link shape used by ScopedLocal.
 *
 * <ul>
 *   <li>{@link Carrier#run(Runnable)}, {@link Carrier#call(Supplier)} -
 *       execute code with the bindings active.</li>
 * </ul>
 */
public final class SimulatedScopedLocal {

    private SimulatedScopedLocal() {
    }

    /**
     * Creates a {@link Carrier} that binds {@code key} to {@code value}.
     * The binding takes effect when {@link Carrier#run(Runnable)} or
     * {@link Carrier#call(Supplier)} is invoked.
     *
     * @param key the scoped-local to bind
     * @param value the value to bind
     * @return a carrier holding the binding
     */
    public static Carrier where(Object key, Object value) {
        return new Carrier();
    }

    /**
     * Simple nested carrier used only for groovydoc regression coverage.
     */
    public static final class Carrier {
        public void run(Runnable action) {
            action.run();
        }

        public <T> T call(Supplier<T> supplier) {
            return supplier.get();
        }
    }
}
