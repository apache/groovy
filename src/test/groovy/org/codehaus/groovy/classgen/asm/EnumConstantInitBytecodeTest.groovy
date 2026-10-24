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
 * {@code (String,int)} constructor of the enum, and so is a constant whose arguments are
 * literals that the only constructor able to take them accepts as they are.  Every other
 * constant, and every constant of an enum with such a constant, keeps going through the
 * synthetic {@code $INIT} helper, which spreads an {@code Object[]} over the constructors
 * of the enum and so has the meta class select one reflectively.
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

    private static void assertContiguous(final List<String> code, final List<String> pattern) {
        def body = new InstructionSequence(instructions: code.findAll {
            !(it ==~ /L\d+/ || it.startsWith('LINENUMBER') || it.startsWith('FRAME'))
        })
        assert body.hasStrictSequence(pattern)
    }

    private static void assertDirectConstructorCall(final List<String> code) {
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', [], '(Ljava/lang/String;I)V')
    }

    private static void assertDirectConstructorCall(final List<String> code, final String name, final String ordinal,
                                                    final List<String> arguments, final String descriptor) {
        assertContiguous(code, [
            'NEW E',
            'DUP',
            'LDC "' + name + '"',
            ordinal,
            *arguments,
            'INVOKESPECIAL E.<init> ' + descriptor,
            'PUTSTATIC E.' + name + ' : LE;'
        ])
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
    void testTraitImplementingEnumCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            trait Named {
                String describe() { "constant $name" }
            }
            enum E implements Named { ONE, TWO }
        '''
        assertDirectConstructorCall(code)
    }

    @Test
    void testLargeOrdinalsSelectCorrectPushInstruction() {
        def code = staticInitializerOf """
            enum E { ${(0..129).collect { 'C' + it }.join(', ')} }
        """
        assertContiguous(code, ['LDC "C5"', 'ICONST_5'])
        assertContiguous(code, ['LDC "C6"', 'BIPUSH 6'])
        assertContiguous(code, ['LDC "C127"', 'BIPUSH 127'])
        assertContiguous(code, ['LDC "C128"', 'SIPUSH 128'])
        assert !code.any { it.contains('$INIT') }
    }

    @Test
    void testConstantsWithArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE(1), TWO(2)
                final int value
                E(int value) { this.value = value }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['ICONST_1'], '(Ljava/lang/String;II)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['ICONST_2'], '(Ljava/lang/String;II)V')
    }

    @Test
    void testConstantsWithStringArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE('one'), TWO('two')
                final String keyword
                E(String keyword) { this.keyword = keyword }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "one"'], '(Ljava/lang/String;ILjava/lang/String;)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['LDC "two"'], '(Ljava/lang/String;ILjava/lang/String;)V')
    }

    @Test
    void testCompileStaticConstantsWithArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            @groovy.transform.CompileStatic
            enum E {
                ONE('one')
                final String keyword
                E(String keyword) { this.keyword = keyword }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "one"'], '(Ljava/lang/String;ILjava/lang/String;)V')
    }

    // every kind of literal, each passed to a parameter that the runtime hands it to
    // unchanged: of its own type, of its primitive type, or of a type it extends or implements
    @Test
    void testLiteralArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE('s', 1, true, 2L, 1.5f, 2.5d, 3.5, 4G, -5, null, String, 6, 'x')
                E(String s, int i, boolean b, long l, float f, double d, BigDecimal bd, BigInteger bi,
                  int negative, Integer nothing, Class c, Number n, CharSequence cs) { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', [
            'LDC "s"',
            'ICONST_1',
            'ICONST_1',
            'LDC 2L',
            'LDC 1.5F',
            'LDC 2.5D',
            'NEW java/math/BigDecimal',
            'DUP',
            'LDC "3.5"',
            'INVOKESPECIAL java/math/BigDecimal.<init> (Ljava/lang/String;)V',
            'NEW java/math/BigInteger',
            'DUP',
            'LDC "4"',
            'INVOKESPECIAL java/math/BigInteger.<init> (Ljava/lang/String;)V',
            'BIPUSH -5',
            'ACONST_NULL',
            'LDC Ljava/lang/String;.class',
            'BIPUSH 6',
            'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;',
            'LDC "x"'
        ], '(Ljava/lang/String;ILjava/lang/String;IZJFDLjava/math/BigDecimal;Ljava/math/BigInteger;' +
            'ILjava/lang/Integer;Ljava/lang/Class;Ljava/lang/Number;Ljava/lang/CharSequence;)V')
    }

    // the number of arguments picks the constructor when no other one can take as many
    @Test
    void testConstructorsOfDifferentArityCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE('one'), TWO('two', 2)
                E(String s) { }
                E(String s, int i) { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "one"'], '(Ljava/lang/String;ILjava/lang/String;)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['LDC "two"', 'ICONST_2'], '(Ljava/lang/String;ILjava/lang/String;I)V')
    }

    @Test
    void testOverloadedConstructorsOfTheSameArityUseInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(1), TWO('two')
                E(int i) { }
                E(String s) { }
            }
        '''
        assertInitHelperCall(code)
    }

    @Test
    void testNullArgumentWithOverloadedConstructorsUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(null)
                E(String s) { }
                E(Integer i) { }
            }
        '''
        assertInitHelperCall(code)
    }

    @Test
    void testVarargsConstructorUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE('a', 'b')
                E(String... names) { }
            }
        '''
        assertInitHelperCall(code)
    }

    // the runtime would weigh the varargs constructor against the one of matching arity
    @Test
    void testVarargsConstructorBesideMatchingConstructorUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE('a')
                E(String name) { }
                E(String name, String... more) { }
            }
        '''
        assertInitHelperCall(code)
    }

    @Test
    void testGStringArgumentUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE("o${'n'}e")
                E(String s) { }
            }
        '''
        assertInitHelperCall(code)
    }

    // the runtime converts each of these arguments on the way to the constructor (a
    // Boolean parameter goes through asBoolean), or rejects it: a null passed to a
    // primitive, a class literal passed to a String
    @Test
    void testArgumentsThatTheRuntimeDoesNotPassUnchangedUseInitHelper() {
        [
            long   : '1',
            double : '1.5',
            Long   : '1',
            Boolean: 'true',
            char   : "'c'",
            int    : 'null',
            String : 'String'
        ].each { type, argument ->
            def code = staticInitializerOf """
                enum E {
                    ONE($argument)
                    E($type value) { }
                }
            """
            assertInitHelperCall(code)
        }
    }

    @Test
    void testNonLiteralArgumentUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE('one'.toUpperCase())
                E(String s) { }
            }
        '''
        assertInitHelperCall(code)
    }

    // a constant that does not qualify keeps the other constants of the enum on $INIT too
    @Test
    void testMixOfQualifyingAndOtherConstantsUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE('one'), TWO('two'.toUpperCase())
                E(String s) { }
            }
        '''
        assertInitHelperCall(code)
    }

    @Test
    void testConstantsWithArgumentsAndABodyUseInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(1) { int twice() { 2 } }
                E(int i) { }
                int twice() { 0 }
            }
        '''
        assertInitHelperCall(code)
    }

    // the explicit empty argument list keeps its meaning as the opt-out next to constants
    // that have arguments of their own
    @Test
    void testEmptyArgumentListBesideConstantsWithArgumentsUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(), TWO('two')
                E(String s = 'none') { }
            }
        '''
        assertInitHelperCall(code)
    }

    // an explicit empty argument list means the same [name, ordinal] arguments but keeps
    // the meta-class-mediated $INIT path: the per-enum opt-out from the direct call for
    // anyone intercepting constant creation through a meta class registered before init
    @Test
    void testConstantsWithEmptyArgumentListUseInitHelper() {
        def code = staticInitializerOf '''
            enum E { ONE(), TWO }
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
    void testMixOfConstantsWithAndWithoutArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE, TWO(2)
                final int value
                E(int value = 0) { this.value = value }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', [], '(Ljava/lang/String;I)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['ICONST_2'], '(Ljava/lang/String;II)V')
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

    // the $INIT call that is emitted instead of the direct call must be the one the static
    // compilation transformer produced, not the untransformed call it was given
    @Test
    void testMissingNameAndOrdinalConstructorKeepsStaticInitHelperCall() {
        def code = staticInitializerOf '''
            @groovy.transform.CompileStatic
            @groovy.transform.TupleConstructor(defaults = false)
            enum E {
                ONE
                String[] value
            }
        '''
        assertInitHelperCall(code)
        assert code.any { it.contains('INVOKESTATIC E.$INIT ([Ljava/lang/Object;)LE;') }
        assert !code.any { it.contains('ScriptBytecodeAdapter') }
    }

    // likewise for constants whose arguments do not qualify for the direct call
    @Test
    void testIneligibleArgumentsKeepStaticInitHelperCall() {
        def code = staticInitializerOf '''
            @groovy.transform.CompileStatic
            enum E {
                ONE(1), TWO('two')
                E(int i) { }
                E(String s) { }
            }
        '''
        assertInitHelperCall(code)
        assert code.any { it.contains('INVOKESTATIC E.$INIT ([Ljava/lang/Object;)LE;') }
        assert !code.any { it.contains('ScriptBytecodeAdapter') }
    }
}
