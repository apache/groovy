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
package org.codehaus.groovy.classgen.asm

import org.junit.jupiter.api.Test

/**
 * Checks how the static initializer of an enum creates its constants.
 * <p>
 * A constant that supplies no arguments of its own is created with a direct call to the
 * {@code (String,int)} constructor of the enum.  Every other constant keeps going through
 * the synthetic {@code $INIT} helper, which spreads an {@code Object[]} over the
 * constructors of the enum and so has the meta class select one reflectively.
 */
final class EnumConstantInitBytecodeTest extends AbstractBytecodeTestCase {

    private List<String> staticInitializerOf(final String source) {
        compile(method: '<clinit>', classNamePattern: 'E', source)
        bodyOf('static <clinit>()V')
    }

    private List<String> bodyOf(final String header) {
        def all = sequence.instructions
        int start = all.findIndexOf { it == header }
        assert start >= 0: "method not found: $header\n$sequence"
        int end = all.findIndexOf(start) { it.startsWith('MAXSTACK') }
        all[(start + 1)..<end]
    }

    private static void assertDirectConstructorCall(final List<String> code) {
        assert code.join('\n').contains([
            'NEW E',
            'DUP',
            'LDC "ONE"',
            'ICONST_0',
            'INVOKESPECIAL E.<init> (Ljava/lang/String;I)V',
            'PUTSTATIC E.ONE : LE;'
        ].join('\n'))
        assert !code.any { it.contains('$INIT') }
        assert !code.any { it.contains('selectConstructorAndTransformArguments') }
    }

    private static void assertInitHelperCall(final List<String> code) {
        assert code.any { it.contains('$INIT') }
        assert !code.any { it.startsWith('NEW E') }
    }

    @Test
    void testConstantsWithoutArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E { ONE, TWO }
        '''
        assertDirectConstructorCall(code)
    }

    @Test
    void testInitHelperIsStillGenerated() {
        staticInitializerOf '''
            enum E { ONE, TWO }
        '''
        def header = sequence.instructions.find { it.contains(' $INIT([Ljava/lang/Object;)LE;') }
        assert header != null
        assert bodyOf(header).any { it.contains('selectConstructorAndTransformArguments') }
    }

    @Test
    void testExplicitNoArgConstructorCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE, TWO
                private final String tag
                E() { tag = 'x' }
            }
        '''
        assertDirectConstructorCall(code)
    }

    @Test
    void testConstructorWithDefaultsCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE, TWO
                final String tag
                E(String tag = 'x') { this.tag = tag }
            }
        '''
        assertDirectConstructorCall(code)
    }

    @Test
    void testCompileStaticConstantsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            @groovy.transform.CompileStatic
            enum E { ONE, TWO }
        '''
        assertDirectConstructorCall(code)
    }

    @Test
    void testConstantsWithArgumentsUseInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(1), TWO(2)
                final int value
                E(int value) { this.value = value }
            }
        '''
        assertInitHelperCall(code)
    }

    @Test
    void testConstantsWithNamedArgumentsUseInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(value: 1), TWO(value: 2)
                int value
            }
        '''
        assertInitHelperCall(code)
    }

    @Test
    void testConstantsWithABodyUseInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE { int twice() { 2 } },
                TWO { int twice() { 4 } }
                abstract int twice()
            }
        '''
        assertInitHelperCall(code)
    }

    @Test
    void testMixOfConstantsWithAndWithoutArgumentsUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE, TWO(2)
                final int value
                E(int value = 0) { this.value = value }
            }
        '''
        assertInitHelperCall(code)
    }

    // the transform leaves the enum without a constructor that takes just the name and
    // the ordinal, so the direct call is not available and $INIT is emitted as before
    @Test
    void testMissingNameAndOrdinalConstructorUsesInitHelper() {
        def code = staticInitializerOf '''
            @groovy.transform.TupleConstructor(defaults = false)
            enum E {
                ONE
                String value
            }
        '''
        assertInitHelperCall(code)
    }
}
