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
 * The packed-closure dispatcher linkage (GROOVY-12227): the hosting class's compiler-emitted
 * {@code $packedDispatchersFactory$} builds the bundle from bytecode-level
 * {@code LambdaMetafactory} sites, invoked once through
 * {@link GeneratedDispatcher#bootstrap}. Exercises every dispatch shape through that linkage,
 * including the transparent propagation of checked exceptions the dispatch interfaces do not
 * declare.
 */
final class PackedDispatcherFactoryTest {

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

    private static Class parsePacked() {
        withPacking {
            def loader = new GroovyClassLoader()
            def host = loader.parseClass(SRC, 'Host.groovy')
            assert host.declaredMethods.any { it.name == '$packedDispatch$' } : 'packing did not engage'
            assert host.declaredMethods.any { it.name == '$packedDispatchersFactory$' } : 'factory not emitted'
            host
        }
    }

    private static <T> T withPacking(Closure<T> work) {
        String previous = System.getProperty(CompilerConfiguration.CLOSURE_PACKING)
        System.setProperty(CompilerConfiguration.CLOSURE_PACKING, 'true')
        try {
            work.call()
        } finally {
            if (previous != null) {
                System.setProperty(CompilerConfiguration.CLOSURE_PACKING, previous)
            } else {
                System.clearProperty(CompilerConfiguration.CLOSURE_PACKING)
            }
        }
    }

    @Test
    void 'every dispatch shape links and dispatches through the emitted factory'() {
        assertEquals(['42', '42', '42', '[2, 3, 4]'], parsePacked().run())
    }

    @Test
    void 'undeclared checked exceptions propagate unchanged through packed dispatch'() {
        def thrown = assertThrows(IOException) { parsePacked().boom() }
        assertEquals('checked, undeclared', thrown.message)
    }
}
