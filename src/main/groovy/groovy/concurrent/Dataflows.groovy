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
package groovy.concurrent

import groovy.transform.CompileStatic
import org.apache.groovy.runtime.async.AsyncSupport

import java.util.concurrent.ConcurrentHashMap

/**
 * A dynamic map of {@link DataflowVariable} instances, providing a
 * concise syntax for dataflow-style programming.
 * <p>
 * Property reads block until the variable is bound; property writes
 * bind the variable. Variables are created on demand.
 *
 * <pre>{@code
 * def df = new Dataflows()
 *
 * async { df.z = df.x + df.y }
 * async { df.x = 10 }
 * async { df.y = 5 }
 *
 * println "Result: ${df.z}"  // 15
 * }</pre>
 * <p>
 * Inspired by GPars' {@code Dataflows} class.
 *
 * @see DataflowVariable
 * @since 6.0.0
 */
@CompileStatic
class Dataflows {

    private final ConcurrentHashMap<String, DataflowVariable> variables = new ConcurrentHashMap<>()

    /**
     * Reading a property awaits the corresponding DataflowVariable's value.
     * Creates the variable on demand if it doesn't exist yet.
     */
    def propertyMissing(String name) {
        AsyncSupport.await(getOrCreate(name))
    }

    /**
     * Writing a property binds the corresponding DataflowVariable.
     * Creates the variable on demand if it doesn't exist yet.
     */
    void propertyMissing(String name, value) {
        getOrCreate(name).bind(value)
    }

    /**
     * Returns the underlying {@link DataflowVariable} for the given name
     * without blocking. Useful for passing the variable itself
     * (e.g., to {@code await} or {@code Awaitable.all}).
     *
     * @param name the variable name
     * @return the DataflowVariable (created on demand)
     */
    DataflowVariable getVariable(String name) {
        getOrCreate(name)
    }

    /**
     * Returns {@code true} if the named variable has been bound.
     */
    boolean isBound(String name) {
        def v = variables.get(name)
        v != null && v.isBound()
    }

    private DataflowVariable getOrCreate(String name) {
        variables.computeIfAbsent(name) { new DataflowVariable() }
    }
}
