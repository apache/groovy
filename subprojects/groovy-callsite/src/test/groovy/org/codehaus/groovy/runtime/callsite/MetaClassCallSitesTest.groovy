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

import groovy.lang.ExpandoMetaClass
import groovy.lang.GroovySystem
import groovy.lang.MetaClassImpl
import org.codehaus.groovy.runtime.metaclass.ClosureMetaClass
import org.junit.jupiter.api.Test

/**
 * GROOVY-11158: MetaClassCallSites factory fidelity after extraction from MetaClassImpl.
 */
final class MetaClassCallSitesTest {

    private static CallSite dummySite(String name = 'toString') {
        new CallSiteArray(MetaClassCallSitesTest, [name] as String[]).array[0]
    }

    @Test
    void testCreatePojoCallSite() {
        def mc = (MetaClassImpl) GroovySystem.metaClassRegistry.getMetaClass(Integer)
        def site = MetaClassCallSites.createPojoCallSite(mc, dummySite('toString'), 7, CallSiteArray.NOPARAM)
        assert site.call(7, CallSiteArray.NOPARAM) == '7'
    }

    @Test
    void testCreateStaticSite() {
        def mc = (MetaClassImpl) GroovySystem.metaClassRegistry.getMetaClass(String)
        def site = MetaClassCallSites.createStaticSite(mc, dummySite('valueOf'), ['z'] as Object[])
        assert site.callStatic(String, 'z') == 'z'
    }

    @Test
    void testCreateConstructorSite() {
        def mc = (MetaClassImpl) GroovySystem.metaClassRegistry.getMetaClass(StringBuilder)
        def site = MetaClassCallSites.createConstructorSite(mc, dummySite('<init>'), ['ab'] as Object[])
        def sb = site.callConstructor(StringBuilder, 'ab')
        assert sb instanceof StringBuilder
        assert sb.toString() == 'ab'
    }

    @Test
    void testCreatePogoCallSite() {
        def obj = new GroovyShell().evaluate('''
            class P { def hi() { 'hi' } }
            new P()
        ''')
        def mc = (MetaClassImpl) GroovySystem.metaClassRegistry.getMetaClass(obj.getClass())
        def site = MetaClassCallSites.createPogoCallSite(mc, dummySite('hi'), CallSiteArray.NOPARAM)
        assert site.call(obj, CallSiteArray.NOPARAM) == 'hi'
    }

    @Test
    void testExpandoCustomInvokeMethodUsesMetaClassSite() {
        def mc = new ExpandoMetaClass(String, true, true)
        mc.initialize()
        mc.invokeMethod = { String name, Object args -> "x-$name" }
        try {
            assert mc.hasCustomInvokeMethod()
            def site = MetaClassCallSites.createPojoCallSite(mc, dummySite('foo'), 'recv', CallSiteArray.NOPARAM)
            assert site instanceof PojoMetaClassSite
            assert site.call('recv', CallSiteArray.NOPARAM) == 'x-foo'
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(String)
        }
    }

    @Test
    void testClosureMetaClassPogoSiteDoesNotSpecialize() {
        def closure = { -> 'ok' }
        def mc = (MetaClassImpl) closure.metaClass
        // Groovy may wrap; force ClosureMetaClass path when present
        if (mc instanceof ClosureMetaClass || mc.getClass().name.contains('Closure')) {
            def site = MetaClassCallSites.createPogoCallSite(mc, dummySite('call'), CallSiteArray.NOPARAM)
            assert site instanceof PogoMetaClassSite
            assert site.call(closure, CallSiteArray.NOPARAM) == 'ok'
        } else {
            // Fallback: ClosureMetaClass via registry after explicit set
            def cmc = new ClosureMetaClass(GroovySystem.metaClassRegistry, closure.getClass())
            cmc.initialize()
            def site = MetaClassCallSites.createPogoCallSite(cmc, dummySite('call'), CallSiteArray.NOPARAM)
            assert site instanceof PogoMetaClassSite
        }
    }

    @Test
    void testClosureMetaClassPojoSiteThrows() {
        def cmc = new ClosureMetaClass(GroovySystem.metaClassRegistry, Object)
        cmc.initialize()
        try {
            MetaClassCallSites.createPojoCallSite(cmc, dummySite('toString'), new Object(), CallSiteArray.NOPARAM)
            assert false: 'expected UnsupportedOperationException'
        } catch (UnsupportedOperationException expected) {
            // ok
        }
    }

    @Test
    void testFunctorCallSelectsDoCall() {
        def obj = new GroovyShell().evaluate('''
            class F extends groovy.lang.Closure {
                F() { super(null) }
                def doCall() { 'from-doCall' }
            }
            new F()
        ''')
        def mc = (MetaClassImpl) GroovySystem.metaClassRegistry.getMetaClass(obj.getClass())
        def site = MetaClassCallSites.createPogoCallSite(mc, dummySite('call'), CallSiteArray.NOPARAM)
        assert site.call(obj, CallSiteArray.NOPARAM) == 'from-doCall'
    }
}
