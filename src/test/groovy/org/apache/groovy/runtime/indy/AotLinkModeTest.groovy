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
package org.apache.groovy.runtime.indy

import org.codehaus.groovy.vmplugin.v8.CacheableCallSite
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.invoke.SwitchPoint

import static org.junit.jupiter.api.Assertions.assertThrows

/**
 * AOT link mode (GROOVY-12234) exercised on a regular JVM through the
 * {@link AotDispatch#FORCE_PROPERTY} diagnostic knob: sites link once to a constant
 * cache-consulting target, are never retargeted, and carry cache freshness in the global
 * invalidation stamp instead of SwitchPoints.
 * <p>
 * The property is captured per site at link time, so every test evaluates freshly compiled
 * scripts inside the property window; their call sites all link in AOT mode. Meta classes are
 * only ever mutated on script-local classes — while the window is open, real SwitchPoint
 * invalidation is suppressed, so mutating a shared class's meta class could leave sites
 * outside the window stale (the documented mid-flip hazard of {@code FORCE_PROPERTY}).
 */
@ResourceLock(Resources.SYSTEM_PROPERTIES)
final class AotLinkModeTest {

    private static <T> T withAotLink(Closure<T> work) {
        String previous = System.getProperty(AotDispatch.FORCE_PROPERTY)
        System.setProperty(AotDispatch.FORCE_PROPERTY, 'true')
        try {
            work.call()
        } finally {
            if (previous != null) {
                System.setProperty(AotDispatch.FORCE_PROPERTY, previous)
            } else {
                System.clearProperty(AotDispatch.FORCE_PROPERTY)
            }
        }
    }

    private static Object evaluateAotLinked(String script) {
        withAotLink {
            new GroovyShell().evaluate(script)
        }
    }

    /**
     * Every dispatch shape links and runs on AOT-linked sites. The hot loop runs past
     * {@code groovy.indy.fallback.threshold} and {@code groovy.indy.optimize.threshold}
     * (both default 1000): any missed retarget gate on the promotion or reset paths would
     * surface as the fail-fast {@code IllegalStateException} from
     * {@code CacheableCallSite.setTarget}.
     */
    @Test
    void 'dispatch gauntlet runs correctly on AOT-linked sites'() {
        def result = evaluateAotLinked '''
            class Calc {
                int base = 40
                int add(int x) { base + x }
                static String greet(String who) { "hi $who" }
            }
            def out = []
            def c = new Calc()
            out << c.add(2)                          // instance method
            out << Calc.greet('aot')                 // static method
            out << c.base                            // property get
            c.base = 1
            out << c.base                            // property set
            out << new Calc().add(41)                // constructor
            out << [1, 2, 3].collect { it * 2 }      // GDK + closure
            def sum = 0
            for (i in 1..2500) { sum += c.add(i) }   // hot: past both thresholds
            out << sum
            out
        '''
        assert result == [42, 'hi aot', 40, 1, 81, [2, 4, 6], 3128750]
    }

    /** Polymorphic receivers churn one site's PIC without ever needing a retarget. */
    @Test
    void 'polymorphic dispatch runs correctly on one AOT-linked site'() {
        def result = evaluateAotLinked '''
            class A { String id() { 'a' } }
            class B { String id() { 'b' } }
            class C { String id() { 'c' } }
            def call = { it.id() }                   // single call site
            def receivers = [new A(), new B(), new C()]
            (1..300).collect { call(receivers[it % 3]) }.unique().sort()
        '''
        assert result == ['a', 'b', 'c']
    }

    /**
     * The stamp is the AOT replacement for SwitchPoint guards: a meta class change after a
     * site has linked and cached its selection must be observed on the next call through
     * that same site. With the property forced, real SwitchPoint invalidation is skipped,
     * so this passing proves the stamp flush alone carries the change. Each call dispatches
     * on a fresh receiver: an instance that dispatched before the change keeps the meta
     * class captured in its instance field on either path (plain-JVM parity, verified),
     * which would test instance-capture semantics rather than the site's cache.
     */
    @Test
    void 'meta class change is observed through the stamp on an already-hot site'() {
        def result = evaluateAotLinked '''
            class Subject { String speak() { 'original' } }
            def call = { -> new Subject().speak() }  // the one site under test
            def first = (1..50).collect { call() }.unique()
            Subject.metaClass.speak = { -> 'intercepted' }
            [first, call()]
        '''
        assert result == [['original'], 'intercepted']
    }

    /**
     * A per-instance meta class is not class-keyed-cacheable: the sentinel must force
     * re-selection on every later hit, keeping plain and per-instance receivers correct
     * through the same AOT-linked site in any order.
     */
    @Test
    void 'per-instance meta class re-selects through the sentinel'() {
        def result = evaluateAotLinked '''
            class Duo { String name() { 'plain' } }
            def call = { Duo d -> d.name() }         // the one site under test
            def a = new Duo()
            def b = new Duo()
            call(a)                                  // cache the plain selection
            b.metaClass.name = { -> 'special' }
            [call(a), call(b), call(a), call(b)]
        '''
        assert result == ['plain', 'special', 'plain', 'special']
    }

    @Test
    void 'setTarget fails fast on an AOT-linked site and works on a normal one'() {
        withAotLink {
            def aotSite = new CacheableCallSite(MethodType.methodType(Object, Object[]), MethodHandles.lookup())
            def e = assertThrows(IllegalStateException) {
                aotSite.setTarget(MethodHandles.empty(aotSite.type()))
            }
            assert e.message.contains('AOT link mode')
        }
        def normalSite = new CacheableCallSite(MethodType.methodType(Object, Object[]), MethodHandles.lookup())
        normalSite.setTarget(MethodHandles.empty(normalSite.type())) // no throw
    }

    @Test
    void 'invalidation always advances the stamp and suppresses real invalidation only in AOT mode'() {
        def suppressed = new SwitchPoint()
        withAotLink {
            long before = AotDispatch.stamp()
            AotDispatch.invalidateAll([suppressed] as SwitchPoint[])
            assert AotDispatch.stamp() == before + 1
            assert !suppressed.hasBeenInvalidated()
        }
        def invalidated = new SwitchPoint()
        long before = AotDispatch.stamp()
        AotDispatch.invalidateAll([invalidated] as SwitchPoint[])
        assert AotDispatch.stamp() == before + 1
        assert invalidated.hasBeenInvalidated()
    }
}
