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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * {@link org.codehaus.groovy.runtime.metaclass.PackedClosureMetaClass} gives packed
 * closures their own MOP standing: it is the registered stock metaclass for the shared
 * {@code PackedClosure} adapter class, dispatches {@code call}/{@code doCall} without
 * reflection, matches {@code ClosureMetaClass}'s category stance for those names, and
 * answers {@code respondsTo} faithfully to the instance's declared parameter types
 * (which purely class-level introspection cannot, since every packed closure shares
 * one adapter class).
 */
final class PackedClosureMetaClassTest {

    @Test
    void packedClosuresGetTheDedicatedStockMetaclass() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Probe {
                static String fieldMetaClassOf(Closure c) { c.getMetaClass().getClass().name }
            }
            @groovy.transform.PackedClosures
            class P { def m() { def c = { it * 2 }; [Probe.fieldMetaClassOf(c), c(3), [3].collect(c)] } }
            def (mc, dyn, gdk) = new P().m()
            assert mc == 'org.codehaus.groovy.runtime.metaclass.PackedClosureMetaClass'
            assert dyn == 6      // dynamic path through the metaclass
            assert gdk == [6]    // Java/GDK path through the guard + tables
        '''
    }

    @Test
    void invokeMethodDispatchesWithoutReflection() {
        assertScript '''
            @groovy.transform.PackedClosures
            class P {
                def m() {
                    def c = { int a, int b -> a + b }
                    [c.invokeMethod('doCall', [2, 3] as Object[]),
                     c.invokeMethod('call', [4, 5] as Object[])]
                }
            }
            assert new P().m() == [5, 9]
        '''
    }

    @Test
    void respondsToIsInstanceFaithful() {
        assertScript '''
            @groovy.transform.PackedClosures
            class P {
                def m() {
                    def c = { Integer x -> x * 2 }
                    def mc = c.metaClass
                    [mc.respondsTo(c, 'doCall', [Integer] as Object[]).empty,
                     mc.respondsTo(c, 'doCall', [Date] as Object[]).empty,
                     mc.respondsTo(c, 'doCall', [Integer, Integer] as Object[]).empty]
                }
            }
            def (matching, wrongType, wrongArity) = new P().m()
            assert !matching             // declared shape answers
            assert wrongType             // a Date is not an Integer
            assert wrongArity            // two args to a one-param closure
        '''
    }

    @Test
    void categoryStanceMatchesClosureClasses() {
        // ClosureMetaClass has never consulted categories for doCall dispatch; the packed
        // metaclass must agree, so packed and classed closures behave identically in use{}
        assertScript '''
            class ShoutCategory {
                static Object doCall(Closure self, Object arg) { 'category' }
            }
            @groovy.transform.PackedClosures
            class P { def m() { def c = { it * 2 }; use(ShoutCategory) { [c(3), [3].collect(c)] } } }
            def plain = { it * 2 }
            def plainResults = use(ShoutCategory) { [plain(3), [3].collect(plain)] }
            assert new P().m() == [6, [6]]
            assert plainResults == [6, [6]]
        '''
    }
}
