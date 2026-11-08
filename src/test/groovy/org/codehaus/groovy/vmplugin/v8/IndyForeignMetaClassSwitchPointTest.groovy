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
package org.codehaus.groovy.vmplugin.v8

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * A proxy or mock subclass whose metaclass is the one of the type it stands in for, as Spock's Mock() and many proxy
 * libraries make it: a change to that metaclass retires the domain of the type it belongs to, so a call site linked
 * through it must re-link even though the receiver's own class did not change. A method call through a
 * {@link DelegatingMetaClass} over that metaclass goes to the metaclass on every call instead of being linked to the
 * selected method, so it is a control that is unaffected.
 */
final class IndyForeignMetaClassSwitchPointTest {

    static class Pricing {
        def previous() { 'real' }

        def plus(int n) { 'real' }
    }

    static class PricingProxy extends Pricing {
    }

    static class RegistryPricingProxy extends Pricing {
        @Override
        MetaClass getMetaClass() {
            GroovySystem.metaClassRegistry.getMetaClass(Pricing)
        }
    }

    enum Receiver {
        /** A subclass given the type's metaclass, as Spock's Mock() makes it. */
        SHARED_METACLASS {
            @Override
            Pricing create(ExpandoMetaClass emc) {
                Pricing proxy = new PricingProxy()
                proxy.metaClass = emc
                proxy
            }
        },
        /** A subclass answering the type's registry metaclass, as a GORM Hibernate proxy does. */
        REGISTRY_METACLASS {
            @Override
            Pricing create(ExpandoMetaClass emc) {
                new RegistryPricingProxy()
            }
        },
        /** A subclass given a DelegatingMetaClass over the type's metaclass. */
        DELEGATING_METACLASS {
            @Override
            Pricing create(ExpandoMetaClass emc) {
                Pricing proxy = new PricingProxy()
                proxy.metaClass = new DelegatingMetaClass(emc)
                proxy
            }
        }

        abstract Pricing create(ExpandoMetaClass emc)
    }

    private ExpandoMetaClass emc

    @BeforeEach
    void installMetaClass() {
        emc = new ExpandoMetaClass(Pricing, true, true)
        emc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(Pricing, emc)
    }

    @AfterEach
    void removeMetaClass() {
        GroovySystem.metaClassRegistry.removeMetaClass(Pricing)
    }

    private static Object callPrevious(Pricing pricing) {
        pricing.previous()
    }

    private static Object addOne(Object value) {
        def result = value
        result += 1
        result
    }

    @ParameterizedTest
    @EnumSource(Receiver)
    void 'a call site sees a change to the metaclass a proxy shares with the type it stands in for'(Receiver receiver) {
        emc.previous = { -> 'first' }
        assertEquals 'first', callPrevious(receiver.create(emc))

        emc.previous = { -> 'second' }
        assertEquals 'second', callPrevious(receiver.create(emc))
    }

    @ParameterizedTest
    @EnumSource(Receiver)
    void 'a call site sees a method replaced on that metaclass after it called the real one'(Receiver receiver) {
        assertEquals 'real', callPrevious(receiver.create(emc))

        emc.previous = { -> 'replaced' }
        assertEquals 'replaced', callPrevious(receiver.create(emc))
    }

    // a compound-assignment site is not guarded by the receiver's metaclass instance, so a DelegatingMetaClass is no
    // control for it
    @ParameterizedTest
    @EnumSource(value = Receiver, names = 'DELEGATING_METACLASS', mode = EnumSource.Mode.EXCLUDE)
    void 'a compound assignment sees a change to the metaclass a proxy shares with the type it stands in for'(Receiver receiver) {
        emc.plus = { int n -> 'first' }
        assertEquals 'first', addOne(receiver.create(emc))

        emc.plus = { int n -> 'second' }
        assertEquals 'second', addOne(receiver.create(emc))
    }

    @ParameterizedTest
    @EnumSource(value = Receiver, names = 'DELEGATING_METACLASS', mode = EnumSource.Mode.EXCLUDE)
    void 'a compound assignment sees a method replaced on that metaclass after it called the real one'(Receiver receiver) {
        assertEquals 'real', addOne(receiver.create(emc))

        emc.plus = { int n -> 'replaced' }
        assertEquals 'replaced', addOne(receiver.create(emc))
    }

    @Test
    void 'the reflective cold tier sees a change to the metaclass a proxy shares with the type it stands in for'() {
        // groovy.indy.cold.reflection is read once at IndyInterface class init, so the method calls run in a fresh JVM
        def javaBin = System.getProperty('java.home') + '/bin/java'
        def cp = System.getProperty('java.class.path')
        def pb = new ProcessBuilder(
                javaBin,
                '-Dgroovy.indy.cold.reflection=true',
                '-cp', cp,
                IndyForeignMetaClassColdTierProbe.name)
        pb.redirectErrorStream(true)
        def proc = pb.start()
        def out = proc.inputStream.text
        assertEquals(0, proc.waitFor(), "cold-tier probe failed: $out")
        assertTrue(out.contains('OK'), out)
    }
}
