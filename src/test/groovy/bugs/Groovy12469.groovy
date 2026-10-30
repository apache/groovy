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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Under static compilation, a local variable declared with an explicit type
 * was cast to (and stored as) the narrower type inferred for its initializer,
 * even when that type came from generics and so is not guaranteed by the
 * erased return type. Like javac, only the declared type should be enforced.
 */
final class Groovy12469 {

    @Test
    void testDeclaredTypeWiderThanInferredGenericType() {
        assertScript '''
            import java.lang.annotation.Annotation
            import java.lang.annotation.Retention
            import java.lang.annotation.Target
            import groovy.transform.CompileStatic

            @CompileStatic
            <T extends Annotation> T getRetention(Class<T> clazz) {
                Target.getAnnotation(Target)
            }

            @CompileStatic
            def foo() {
                Annotation x = getRetention(Retention)
            }

            assert foo() instanceof Target
        '''
    }

    @Test
    void testDeclaredTypeWiderThanInferredCollectionElementType() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            def foo(List<String> list) {
                CharSequence cs = list.get(0)
            }

            def polluted = [new StringBuilder('sb')]
            assert foo(polluted) instanceof StringBuilder
        '''
    }

    @Test
    void testFlowTypedUseOfVariableStillCompiles() {
        assertScript '''
            import java.lang.annotation.Annotation
            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import groovy.transform.CompileStatic

            @CompileStatic
            <T extends Annotation> T find(Class<T> clazz) {
                Retention.getAnnotation(clazz)
            }

            @CompileStatic
            def foo() {
                Annotation x = find(Retention)
                x.value()
            }

            assert foo() == RetentionPolicy.RUNTIME
        '''
    }

    @Test
    void testMethodReferenceOnFlowTypedVariable() {
        assertScript '''
            import java.util.function.Supplier
            import groovy.transform.CompileStatic

            @CompileStatic
            def foo(List<String> list) {
                CharSequence cs = list.get(0)
                Supplier<Integer> s = cs::length
                s.get()
            }

            assert foo(['abc']) == 3
        '''
    }

    @Test
    void testClosureSharedVariable() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            def foo(List<String> list) {
                CharSequence cs = list.get(0)
                def c = { -> cs.length() }
                [c(), cs]
            }

            def (len, cs) = foo([new StringBuilder('abc')])
            assert len == 3
            assert cs instanceof StringBuilder
        '''
    }

    @Test
    void testMembersOfDeclaredTypeDoNotNeedNarrowerType() {
        assertScript '''
            import java.util.function.Supplier
            import groovy.transform.CompileStatic

            @CompileStatic
            def foo(List<String> list) {
                CharSequence cs = list.get(0)
                Object o = list.get(0)
                Supplier<Integer> length = cs::length
                [cs.length(), cs.charAt(0), cs.subSequence(0, 1).toString(), cs.chars().count(), cs.empty,
                 cs.toString(), o.toString(), length.get()]
            }

            def name = 'ab'
            List<String> polluted = ["$name"] // GString elements, as built by dynamic code
            assert foo(polluted) == [2, (char) 'a', 'a', 2L, false, 'ab', 'ab', 2]
            assert foo(['ab']) == [2, (char) 'a', 'a', 2L, false, 'ab', 'ab', 2]
        '''
    }

    @Test
    void testGStringConvertedWhereOnlyStringWillDo() {
        assertScript '''
            import java.util.function.Supplier
            import groovy.transform.CompileStatic

            @CompileStatic
            def foo(List<String> list) {
                CharSequence cs = list.get(0)
                Comparable c = list.get(0)
                Supplier<String> upper = cs::toUpperCase
                [cs.toUpperCase(), cs.bytes.length, c.compareTo('ab'), upper.get(), list.get(0).toUpperCase(),
                 list.get(0)?.toUpperCase(), list.collect { it.toUpperCase() }[0], cs]
            }

            def name = 'ab'
            List<String> polluted = ["$name"] // GString elements, as built by dynamic code
            def result = foo(polluted)
            assert result[0..-2] == ['AB', 2, 0, 'AB', 'AB', 'AB', 'AB']
            assert result[-1] instanceof GString // the variable still holds the GString
            assert foo(['ab'])[0..-2] == ['AB', 2, 0, 'AB', 'AB', 'AB', 'AB']
        '''
    }

    @Test
    void testGStringConvertedWhenHeldAsCommonTypeOfSeveralTypes() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            String foo(List<String> list) {
                def obj = 1
                obj = list.get(0)
                obj.toUpperCase()
            }

            def name = 'ab'
            List<String> polluted = ["$name"] // GString elements, as built by dynamic code
            assert foo(polluted) == 'AB'
            assert foo(['ab']) == 'AB'
        '''
    }

    @Test
    void testMemberOfNarrowerTypeStillCastsAtPointOfUse() {
        def err = shouldFail ClassCastException, '''
            import groovy.transform.CompileStatic

            @CompileStatic
            def foo(List<String> list) {
                CharSequence cs = list.get(0)
                cs.toUpperCase()
            }

            assert foo(['ab']) == 'AB'
            foo([new StringBuilder('ab')])
        '''
        assert err.message.contains('StringBuilder')
    }
}
