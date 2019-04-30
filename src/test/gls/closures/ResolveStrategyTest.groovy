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

import groovy.transform.CompileStatic
import static groovy.lang.Closure.*

class ResolveStrategyTest extends GroovyTestCase {
    void testDynamicSettingOfResolveStrategy() {
        new MyClass().with {
            assert run(DELEGATE_ONLY) == 12340000 // (*)
            assert run(DELEGATE_FIRST) == 12040030
            assert run(OWNER_ONLY) == 1234 // (*)
            assert run(OWNER_FIRST) == 41230

            assert runOwnerOnly { m1() + m2() + m3() } == 1230
            assert runOwnerOnly { m1() + m2() + m3() + m4() } == 1234 // (*)
            assert runOwnerFirst { m1() + m2() + m3() + m4() } == 41230
            assert runDelegateOnly { m1() + m2() + m3() + m4() } == 12340000 // (*)
            assert runDelegateOnly { m1() + m2() + m4() } == 12040000
            assert runDelegateFirst { m1() + m2() + m3() + m4() } == 12340000 // (**)

            // nested cases
            assert runDelegateFirst { runOwnerFirst { m1() + m2() + m3() + m4() } } == 41230
            assert runOwnerFirst { runDelegateFirst { m1() + m2() + m3() + m4() } } == 12340000 // (**)
            assert runOwnerFirst { runOwnerFirst { m1() + m2() + m3() + m4() } } == 41230
            assert runDelegateFirst { runDelegateFirst { m1() + m2() + m3() + m4() } } == 12340000 // (**)
            // (*) involves methodMissing as forced by ONLY strategy (no equivalent CS case below)
            // (**) involves methodMissing since delegate methodMissing has precedence over explicit owner method
        }
    }

    @CompileStatic
    void testStaticCases() {
        new MyClass().with {
            assert runOwnerOnly { m1() + m2() + m3() } == 1230
            assert runOwnerFirst { m1() + m2() + m3() + m4() } == 41230
            assert runDelegateOnly { m1() + m2() + m4() } == 12040000
            assert runDelegateFirst { m1() + m2() + m3() + m4() } == 12040030 // (*)

            // nested cases (GROOVY-9086)
            assert runDelegateFirst { runOwnerFirst { m1() + m2() + m3() + m4() } } == 12040030 // (*)
            assert runOwnerFirst { runDelegateFirst { m1() + m2() + m3() + m4() } } == 12040030 // (*)
            assert runOwnerFirst { runOwnerFirst { m1() + m2() + m3() + m4() } } == 41230
            assert runDelegateFirst { runDelegateFirst { m1() + m2() + m3() + m4() } } == 12040030 // (*)
            // (*) different to dynamic since static assumes no methodMissing
        }
    }
}

class Delegate {
    int m1() { 10000000 }

    int m2() { 2000000 }

    int m4() { 40000 }

    def methodMissing(String name, args) {
        if (name.size() == 2) return 300000
        else throw new MissingMethodException(name, Delegate, args)
    }
}

class MyClass {
    int m1() { 1000 }

    int m2() { 200 }

    int m3() { 30 }

    def methodMissing(String name, args) { 4 }

    def run(int rs) {
        def answer = -1
        Closure c = { answer = m1() + m2() + m3() + m4() }
        c.resolveStrategy = rs
        c.delegate = new Delegate()
        c()
        answer
    }

    def runDelegateFirst(@DelegatesTo(value = Delegate, strategy = DELEGATE_FIRST) Closure c) {
        c.delegate = new Delegate()
        c.resolveStrategy = DELEGATE_FIRST
        c()
    }

    def runDelegateOnly(@DelegatesTo(value = Delegate, strategy = DELEGATE_ONLY) Closure c) {
        c.delegate = new Delegate()
        c.resolveStrategy = DELEGATE_ONLY
        c()
    }

    def runOwnerFirst(@DelegatesTo(Delegate) Closure c) {
        c.delegate = new Delegate()
        c()
    }

    def runOwnerOnly(@DelegatesTo(value = Delegate, strategy = OWNER_ONLY) Closure c) {
        c.delegate = new Delegate()
        c.resolveStrategy = OWNER_ONLY
        c()
    }
}
