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

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * A proxy or mock subclass whose metaclass is the one of the type it stands in for, as Spock's Mock() and many proxy
 * libraries make it: a change to that metaclass retires the domain of the type it belongs to, so a call site linked
 * through it must re-link even though the receiver's own class did not change.
 */
final class IndyForeignMetaClassSwitchPointTest {

    static class Pricing {
        def previous() { 'real' }

        def plus(int n) { 'real' }
    }

    static class PricingProxy extends Pricing {
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

    private PricingProxy proxy() {
        PricingProxy proxy = new PricingProxy()
        proxy.metaClass = emc
        proxy
    }

    private static Object callPrevious(Pricing pricing) {
        pricing.previous()
    }

    private static Object addOne(Object value) {
        def result = value
        result += 1
        result
    }

    @Test
    void 'a call site sees a change to the metaclass a proxy shares with the type it stands in for'() {
        emc.previous = { -> 'first' }
        assertEquals 'first', callPrevious(proxy())

        emc.previous = { -> 'second' }
        assertEquals 'second', callPrevious(proxy())
    }

    @Test
    void 'a compound assignment sees a change to the metaclass a proxy shares with the type it stands in for'() {
        emc.plus = { int n -> 'first' }
        assertEquals 'first', addOne(proxy())

        emc.plus = { int n -> 'second' }
        assertEquals 'second', addOne(proxy())
    }
}
