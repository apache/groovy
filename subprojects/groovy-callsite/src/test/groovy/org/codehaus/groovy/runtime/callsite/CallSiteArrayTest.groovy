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
package org.codehaus.groovy.runtime.callsite

import org.junit.jupiter.api.Test

/**
 * GROOVY-11158: classic CallSiteArray behaviour after the move to groovy-callsite.
 */
final class CallSiteArrayTest {

    @Test
    void testDefaultCallInvokesInstanceMethod() {
        def csa = new CallSiteArray(CallSiteArrayTest, ['toString'] as String[])
        def site = csa.array[0]
        def result = site.call(42, CallSiteArray.NOPARAM)
        assert result == '42'
    }

    @Test
    void testDefaultCallStatic() {
        def csa = new CallSiteArray(CallSiteArrayTest, ['valueOf'] as String[])
        def site = csa.array[0]
        def result = site.callStatic(String, 'hello')
        assert result == 'hello'
    }

    @Test
    void testDefaultCallConstructor() {
        def csa = new CallSiteArray(CallSiteArrayTest, ['<init>'] as String[])
        def site = csa.array[0]
        def result = site.callConstructor(StringBuilder, 'x')
        assert result instanceof StringBuilder
        assert result.toString() == 'x'
    }

    @Test
    void testNullReceiverUsesNullCallSite() {
        def csa = new CallSiteArray(CallSiteArrayTest, ['toString'] as String[])
        def site = csa.array[0]
        // null.toString() via NullObject
        def result = site.call(null, CallSiteArray.NOPARAM)
        assert result == 'null'
    }

    @Test
    void testCallSiteArrayConstruction() {
        def names = ['a', 'b', 'c'] as String[]
        def csa = new CallSiteArray(String, names)
        assert csa.owner == String
        assert csa.array.length == 3
        assert csa.array[0].name == 'a'
        assert csa.array[1].name == 'b'
        assert csa.array[2].name == 'c'
        assert csa.array[0].index == 0
        assert csa.array[0].array.is(csa)
    }

    @Test
    void testPogoCallCurrent() {
        def script = new GroovyShell().evaluate('''
            class C {
                def m() { 'ok' }
                def run() {
                    def csa = new org.codehaus.groovy.runtime.callsite.CallSiteArray(C, ['m'] as String[])
                    csa.array[0].callCurrent(this, org.codehaus.groovy.runtime.callsite.CallSiteArray.NOPARAM)
                }
            }
            new C().run()
        ''')
        assert script == 'ok'
    }

    /**
     * Linkage surface that Groovy 4/5 classic bytecode depends on.
     * Keep constructor, public fields, and CallSite method names/signatures
     * binary-compatible so precompiled classes keep working with this module.
     */
    @Test
    void testPublicLinkageSurfaceStableForPrecompiledClassicBytecode() {
        def csa = new CallSiteArray(String, ['length', 'valueOf', '<init>', 'name'] as String[])
        assert csa.owner == String
        assert csa.array instanceof CallSite[]
        assert csa.array.length == 4
        assert CallSiteArray.NOPARAM.length == 0

        // Instance / static / constructor / property paths used by CallSiteWriter
        assert csa.array[0].call('hi', CallSiteArray.NOPARAM) == 2
        assert csa.array[1].callStatic(String, 99) == '99'
        def sb = csa.array[2].callConstructor(StringBuilder, 'z')
        assert sb instanceof StringBuilder && sb.toString() == 'z'
        assert csa.array[3].callGetProperty([name: 'via-prop']) == 'via-prop'

        // Warmed sites (second hit) must keep working
        assert csa.array[0].call('ab', CallSiteArray.NOPARAM) == 2
        assert csa.array[1].callStatic(String, 100) == '100'
    }
}
