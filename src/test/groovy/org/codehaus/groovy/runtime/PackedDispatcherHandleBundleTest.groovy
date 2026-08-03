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
package org.codehaus.groovy.runtime

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows

/**
 * Parity between the two ways {@link GeneratedDispatcher#bootstrap} adapts a class's dispatch
 * tables: the default {@code LambdaMetafactory} hidden classes, and the method-handle wrappers
 * used where classes cannot be defined at run time (GraalVM native image). The wrapper path is
 * forced on a regular JVM with {@code -Dgroovy.packed.dispatch.handles=true}; linkage happens
 * once per loaded class, so each compilation below (a fresh class in a fresh loader) observes
 * the property's value at its own first dispatch.
 */
final class PackedDispatcherHandleBundleTest {

    private static final String FORCE = 'groovy.packed.dispatch.handles'

    /** Exercises every dispatch shape: array (3 values), arity-1, arity-2, and a checked throw. */
    private static final String SRC = '''
        class Host {
            static List<String> run() {
                def results = []
                def one = { int a -> a * 2 }                          // arity-1 table
                def two = { int a, int b -> a + b }                   // arity-2 table
                def three = { int a, int b, int c -> a + b + c }      // array table
                results << one(21).toString()
                results << two(20, 22).toString()
                results << three(10, 14, 18).toString()
                results << [1, 2, 3].collect { it + 1 }.toString()    // through the GDK
                results
            }
            static void boom() {
                def thrower = { throw new java.io.IOException('checked, undeclared') }
                thrower()
            }
        }
    '''

    private static List<String> runPacked(boolean forceHandles) {
        withProperty(CompilerConfiguration.CLOSURE_PACKING, 'true') {
            withProperty(FORCE, forceHandles ? 'true' : null) {
                def loader = new GroovyClassLoader()
                def host = loader.parseClass(SRC, 'Host.groovy')
                assert host.declaredMethods.any { it.name == '$packedDispatch$' } : 'packing did not engage'
                host.run()
            }
        }
    }

    private static <T> T withProperty(String name, String value, Closure<T> work) {
        String previous = System.getProperty(name)
        if (value != null) System.setProperty(name, value) else System.clearProperty(name)
        try {
            work.call()
        } finally {
            if (previous != null) System.setProperty(name, previous) else System.clearProperty(name)
        }
    }

    @Test
    void 'handle bundles produce the same results as hidden-class bundles'() {
        def viaHiddenClasses = runPacked(false)
        def viaHandles = runPacked(true)
        assertEquals(viaHiddenClasses, viaHandles)
        assertEquals(['42', '42', '42', '[2, 3, 4]'], viaHandles)
    }

    @Test
    void 'undeclared checked exceptions propagate unchanged through handle bundles'() {
        withProperty(CompilerConfiguration.CLOSURE_PACKING, 'true') {
            withProperty(FORCE, 'true') {
                def loader = new GroovyClassLoader()
                def host = loader.parseClass(SRC, 'Host.groovy')
                def thrown = assertThrows(IOException) { host.boom() }
                assertEquals('checked, undeclared', thrown.message)
            }
        }
    }
}
