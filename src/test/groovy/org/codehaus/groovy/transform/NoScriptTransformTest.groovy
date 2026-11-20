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
package org.codehaus.groovy.transform

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

import static org.junit.jupiter.api.Assertions.assertThrows

/**
 * Tests for the {@code @NoScript} annotation.
 */
final class NoScriptTransformTest {

    private static Class parse(String text, String name = 'Foo.groovy') {
        new GroovyClassLoader().parseClass(text, name)
    }

    private static List<String> declaredMethodNames(Class type) {
        type.declaredMethods.findAll { !it.synthetic }*.name.sort()
    }

    private static void assertPlainClass(Class type) {
        assert type.superclass == Object
        assert !Script.isAssignableFrom(type)
        assert Modifier.isPublic(type.modifiers)
        assert !type.declaredConstructors.any { it.parameterTypes.toList() == [Binding] }
        assert !type.declaredMethods.any { it.name in ['run', 'main'] }
    }

    @Test
    void testSingleMethodBecomesPlainClass() {
        def type = parse '''
            @NoScript
            import groovy.transform.NoScript

            def foo() { 'foo' }
        '''

        assertPlainClass(type)
        assert type.declaredConstructors*.parameterCount == [0]
        assert type.getDeclaredConstructor().newInstance().foo() == 'foo'
    }

    @Test
    void testCompileStaticAndPOJOAlongsideMoveToTheClass() {
        def type = parse '''
            import groovy.transform.CompileStatic
            import groovy.transform.stc.POJO

            @NoScript @CompileStatic @POJO
            import groovy.transform.NoScript

            def foo() { 'foo' }
        '''

        assertPlainClass(type)
        assert !GroovyObject.isAssignableFrom(type)
        assert declaredMethodNames(type) == ['foo']
        assert type.getDeclaredConstructor().newInstance().foo() == 'foo'
    }

    @Test
    void testMethodLevelCompileStatic() {
        def type = parse '''
            @NoScript
            import groovy.transform.NoScript

            @groovy.transform.CompileStatic
            String foo() { 'foo' }
        '''

        assertPlainClass(type)
        assert type.getDeclaredConstructor().newInstance().foo() == 'foo'
    }

    @Test
    void testStaticMethodsOnlyGetPrivateConstructor() {
        def type = parse '''
            @NoScript
            import groovy.transform.NoScript

            static foo() { 'foo' }
        '''

        assertPlainClass(type)
        assert type.declaredConstructors.size() == 1
        assert Modifier.isPrivate(type.declaredConstructors[0].modifiers)
        assert type.foo() == 'foo'
    }

    @Test
    void testStaticMethodWithCompileStaticAndPOJO() {
        def type = parse '''
            import groovy.transform.CompileStatic
            import groovy.transform.stc.POJO

            @NoScript @CompileStatic @POJO
            import groovy.transform.NoScript

            static String foo() { 'foo' }
        '''

        assertPlainClass(type)
        assert !GroovyObject.isAssignableFrom(type)
        assert declaredMethodNames(type) == ['foo']
        assert Modifier.isStatic(type.getDeclaredMethod('foo').modifiers)
        assert type.declaredConstructors.size() == 1
        assert Modifier.isPrivate(type.declaredConstructors[0].modifiers)
        assert type.getDeclaredMethod('foo').invoke(null) == 'foo'
    }

    @Test
    void testDeclarationsBecomeFields() {
        def type = parse '''
            @NoScript
            import groovy.transform.NoScript

            def greeting = 'hello'
            def greet(name) { "$greeting $name" }
        '''

        assertPlainClass(type)
        assert type.getDeclaredField('greeting')
        assert type.getDeclaredConstructor().newInstance().greet('world') == 'hello world'
    }

    // GROOVY-12790
    @Test
    void testMultipleAssignmentWithNonLiteralRightHandSide() {
        def type = parse '''
            @NoScript
            import groovy.transform.NoScript

            def (a, b) = "xy".toList()
            def c = 3
            def foo() { "$a$b$c" }
        '''

        assertPlainClass(type)
        def foo = type.getDeclaredConstructor().newInstance()
        assert foo.a == 'x'
        assert foo.b == 'y'
        assert foo.c == 3
        assert foo.foo() == 'xy3'
    }

    // GROOVY-12790
    @Test
    void testDeclarationAfterMultipleAssignmentUsesTheAssignedFields() {
        def type = parse '''
            @NoScript
            import groovy.transform.NoScript

            def x = 1
            def (a, b) = "xy".toList()
            def c = a + b + x
            def foo() { c }
        '''

        assertPlainClass(type)
        def foo = type.getDeclaredConstructor().newInstance()
        assert foo.c == 'xy1'
        assert foo.foo() == 'xy1'
    }

    // GROOVY-12790
    @Test
    void testMultipleAssignmentWithRestBinding() {
        def type = parse '''
            @NoScript
            import groovy.transform.NoScript

            def (h, *t) = [1, 2, 3]
            def foo() { t }
        '''

        assertPlainClass(type)
        def foo = type.getDeclaredConstructor().newInstance()
        assert foo.h == 1
        assert foo.t == [2, 3]
    }

    // GROOVY-12790
    @Test
    void testMapStyleMultipleAssignment() {
        def type = parse '''
            @NoScript
            import groovy.transform.NoScript

            def (name: n, age: a) = [name: 'Ada', age: 36]
            def foo() { n }
        '''

        assertPlainClass(type)
        def foo = type.getDeclaredConstructor().newInstance()
        assert foo.n == 'Ada'
        assert foo.a == 36
    }

    @Test
    void testOnPackage() {
        def type = parse '''
            @groovy.transform.NoScript
            package foo.bar

            def foo() { 'foo' }
        '''

        assert type.name == 'foo.bar.Foo'
        assertPlainClass(type)
    }

    @Test
    void testOnDeclaration() {
        def type = parse '''
            import groovy.transform.NoScript

            @NoScript def greeting = 'hello'
            def greet() { greeting }
        '''

        assertPlainClass(type)
        assert type.getDeclaredConstructor().newInstance().greet() == 'hello'
    }

    @Test
    void testOnStarImport() {
        def type = parse '''
            @NoScript
            import groovy.transform.*

            def foo() { 'foo' }
        '''

        assertPlainClass(type)
    }

    @Test
    void testOnAliasedImport() {
        def type = parse '''
            @PlainClass
            import groovy.transform.NoScript as PlainClass

            def foo() { 'foo' }
        '''

        assertPlainClass(type)
    }

    @Test
    void testOtherScriptsAreUnaffected() {
        def type = parse '''
            def foo() { 'foo' }
        '''

        assert type.superclass == Script
    }

    @Test
    void testStatementsAreRejected() {
        def err = assertThrows(MultipleCompilationErrorsException) {
            parse '''
                @NoScript
                import groovy.transform.NoScript

                def foo() { 'foo' }
                println foo()
            '''
        }
        assert err.message.contains('Annotation @NoScript cannot be used in a script with statements other than declarations')
    }

    @Test
    void testFileWithoutScriptCodeIsRejected() {
        def err = assertThrows(MultipleCompilationErrorsException) {
            parse '''
                @NoScript
                import groovy.transform.NoScript

                class Bar {}
            '''
        }
        assert err.message.contains('Annotation @NoScript can only be used in a script file')
    }

    @Test
    void testOnClassIsRejected() {
        def err = assertThrows(MultipleCompilationErrorsException) {
            parse '''
                @groovy.transform.NoScript
                class Bar {}
            '''
        }
        assert err.message.contains('is not allowed on element')
    }
}
