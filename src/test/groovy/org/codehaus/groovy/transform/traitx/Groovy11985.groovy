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
package org.codehaus.groovy.transform.traitx

import groovy.test.GroovyAssert
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test

final class Groovy11985 {

    // Unqualified and `this.`-qualified public static calls inside a trait
    // method must dispatch through the implementing class so an override on
    // the implementer is visible from trait code.

    @Test
    void testStaticOverrideVisibleFromTraitThisCall() {
        GroovyAssert.assertScript '''
            trait Validateable {
                static boolean defaultNullable() { false }
                static boolean defaultNullableSeenByTrait() { this.defaultNullable() }
            }
            class MyNullableValidateable implements Validateable {
                static boolean defaultNullable() { true }
            }
            class DefaultValidateable implements Validateable {}
            assert MyNullableValidateable.defaultNullable() == true
            assert MyNullableValidateable.defaultNullableSeenByTrait() == true
            assert DefaultValidateable.defaultNullable() == false
            assert DefaultValidateable.defaultNullableSeenByTrait() == false
        '''
    }

    @Test
    void testStaticOverrideVisibleFromTraitUnqualifiedCall() {
        GroovyAssert.assertScript '''
            trait Validateable {
                static boolean defaultNullable() { false }
                static boolean defaultNullableUnqualified() { defaultNullable() }
            }
            class MyNullableValidateable implements Validateable {
                static boolean defaultNullable() { true }
            }
            class DefaultValidateable implements Validateable {}
            assert MyNullableValidateable.defaultNullableUnqualified() == true
            assert DefaultValidateable.defaultNullableUnqualified() == false
        '''
    }

    @Test
    void testStaticOverrideVisibleFromInstanceMethod() {
        GroovyAssert.assertScript '''
            trait T {
                static String which() { 'trait' }
                String greet() { which() }
            }
            class C implements T {
                static String which() { 'class' }
            }
            class D implements T {}
            assert new C().greet() == 'class'
            assert new D().greet() == 'trait'
        '''
    }

    @Test
    void testStaticOverrideUnderCompileStatic() {
        GroovyAssert.assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            trait Validateable {
                static boolean defaultNullable() { false }
                static boolean defaultNullableSeenByTrait() { this.defaultNullable() }
                static boolean defaultNullableUnqualified() { defaultNullable() }
            }
            @CompileStatic
            class MyNullableValidateable implements Validateable {
                static boolean defaultNullable() { true }
            }
            @CompileStatic
            class DefaultValidateable implements Validateable {}
            assert MyNullableValidateable.defaultNullableSeenByTrait() == true
            assert MyNullableValidateable.defaultNullableUnqualified() == true
            assert DefaultValidateable.defaultNullableSeenByTrait() == false
            assert DefaultValidateable.defaultNullableUnqualified() == false
        '''
    }

    @Test
    void testOverloadResolutionStillWorks() {
        GroovyAssert.assertScript '''
            trait T {
                static String foo() { 'no-arg' }
                static String foo(int n) { "int=$n" }
                static String bar() { foo() + ' / ' + foo(42) }
            }
            class C implements T {}
            assert C.bar() == 'no-arg / int=42'
        '''
    }

    @Test
    void testSuperTraitPublicStaticIsPolymorphic() {
        GroovyAssert.assertScript '''
            trait Base { static String hello() { 'base' } }
            trait Mid extends Base { static String greet() { hello() } }
            class C implements Mid {}
            class D implements Mid { static String hello() { 'override' } }
            assert C.greet() == 'base'
            assert D.greet() == 'override'
        '''
    }

    @Test
    void testPrivateStaticStillRoutesToHelper() {
        // Private statics are not composed onto the implementer, so they must
        // continue to dispatch directly to the helper. The override on the
        // implementer (if any) is a different method and intentionally not
        // visible from trait code.
        GroovyAssert.assertScript '''
            trait T {
                boolean passes
                void audit() {
                    if (checkCondition()) { passes = true }
                }
                private static boolean checkCondition() { true }
            }
            class C implements T {}
            def c = new C(); c.audit(); assert c.passes
        '''
    }
}
