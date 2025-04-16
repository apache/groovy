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
package gls.closures

import groovy.test.GroovyTestCase
import groovy.transform.CompileStatic

import static groovy.lang.Closure.*

final class ResolveStrategyTest extends GroovyTestCase {

    void testDynamicResolveStrategy() {
        new ClassRST().with {
            assert run(OWNER_ONLY) == 1234
            assert runOwnerOnly { m1() + m2() + m3() + m4() } == 1234
            shouldFail(MissingMethodException) {
                runOwnerOnly { m1() + m2() + m3() + m6() }
            }

            assert run(DELEGATE_ONLY) == 12340000
            assert runDelegateOnly { m1() + m2() + m3() + m4() } == 12340000
            shouldFail(MissingMethodException) {
                runDelegateOnly { m1() + m2() + m4() + m5() }
            }

            assert run(OWNER_FIRST) == 1234
            assert runOwnerFirst { m1() + m2() + m3() + m4() } == 1234
            assert runOwnerFirst { m1() + m2() + m3() + m5() } == 1234
            assert runOwnerFirst { m1() + m2() + m3() + m6() } == 301230 // delegate: m6

            assert run(DELEGATE_FIRST) == 12340000
            assert runDelegateFirst { m1() + m2() + m3() + m4() } == 12340000
            assert runDelegateFirst { m1() + m2() + m3() + m5() } == 12300004 // owner: m5
            assert runDelegateFirst { m1() + m2() + m4() + m6() } == 12340000

            // nested cases
            assert runOwnerFirst { runOwnerFirst { m1() + m2() + m3() + m4() } } == 1234
            assert runOwnerFirst { runDelegateFirst { m1() + m2() + m3() + m4() } } == 12340000
            assert runDelegateFirst { runOwnerFirst { m1() + m2() + m3() + m4() } } == 12340000
            assert runDelegateFirst { runDelegateFirst { m1() + m2() + m3() + m4() } } == 12340000
            assert runOwnerFirst { runOwnerFirst { m1() + m2() + m3() + m4() + m5() + m6() } } == 301238 // owner: m4 m5, delegate: m6
            assert runOwnerFirst { runDelegateFirst { m1() + m2() + m3() + m4() + m5() + m6() } } == 12640004 // owner: m5, delegate: m3 m6
            assert runDelegateFirst { runOwnerFirst { m1() + m2() + m3() + m4() + m5() + m6() } } == 12640004 // owner: m5, delegate: m3 m6
            assert runDelegateFirst { runDelegateFirst { m1() + m2() + m3() + m4() + m5() + m6() } } == 12640004 // owner: m5, delegate: m3 m6
        }
    }

    @CompileStatic
    void testStaticResolveStrategy() {
        new ClassRST().with {
            assert runOwnerOnly { m1() + m2() + m3() } == 1230
            assert runOwnerFirst { m1() + m2() + m3() + m4() } == 41230
            assert runDelegateOnly { m1() + m2() + m4() } == 12040000
            assert runDelegateFirst { m1() + m2() + m3() + m4() } == 12040030

            // GROOVY-9086: nested cases
            assert runOwnerFirst { runOwnerFirst { m1() + m2() + m3() + m4() } } == 41230
            assert runDelegateFirst { runOwnerFirst { m1() + m2() + m3() + m4() } } == 12040030
            assert runOwnerFirst { runDelegateFirst { m1() + m2() + m3() + m4() } } == 12040030
            assert runDelegateFirst { runDelegateFirst { m1() + m2() + m3() + m4() } } == 12040030
        }
    }
}

class DelegateRST {
    int m1() { 10000000 }

    int m2() { 2000000 }

    int m4() { 40000 }

    def methodMissing(String name, args) {
        if (name.size() != 2 || 'm5' == name) {
            throw new MissingMethodException(name, DelegateRST, args)
        }
        return 300000
    }
}

class ClassRST {
    int m1() { 1000 }

    int m2() { 200 }

    int m3() { 30 }

    def methodMissing(String name, args) {
        if (name.size() != 2 || 'm6' == name) {
            throw new MissingMethodException(name, ClassRST)
        }
        return 4
    }

    def run(int rs) {
        def answer = -1
        Closure c = { answer = m1() + m2() + m3() + m4() }
        c.resolveStrategy = rs
        c.delegate = new DelegateRST()
        c()
        answer
    }

    def runDelegateFirst(@DelegatesTo(value = DelegateRST, strategy = DELEGATE_FIRST) Closure c) {
        c.delegate = new DelegateRST()
        c.resolveStrategy = DELEGATE_FIRST
        c()
    }

    def runDelegateOnly(@DelegatesTo(value = DelegateRST, strategy = DELEGATE_ONLY) Closure c) {
        c.delegate = new DelegateRST()
        c.resolveStrategy = DELEGATE_ONLY
        c()
    }

    def runOwnerFirst(@DelegatesTo(DelegateRST /* strategy = OWNER_FIRST  */) Closure c) {
        c.delegate = new DelegateRST()
        c()
    }

    def runOwnerOnly(@DelegatesTo(value = DelegateRST, strategy = OWNER_ONLY) Closure c) {
        c.delegate = new DelegateRST()
        c.resolveStrategy = OWNER_ONLY
        c()
    }
}
