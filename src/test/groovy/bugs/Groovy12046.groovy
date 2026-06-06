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

/**
 * Regression tests for GROOVY-12046.
 *
 * <p>A mocking framework (e.g. Spock, Mockito) typically creates a runtime subclass ("proxy") and
 * installs a per-instance {@link groovy.lang.MetaClassImpl} whose {@code theClass} points to the
 * <em>parent</em> class.  When an unresolved method reaches
 * {@code MetaClassImpl.invokeMissingMethod}, the thrown {@link groovy.lang.MissingMethodException}
 * must carry the <em>receiver's runtime class</em> as its {@code type}, not {@code theClass}.
 * The indy call-site handler in
 * {@code IndyGuardsFiltersAndSignatures.invokeGroovyObjectInvoker} guards
 * {@code receiver.getClass() == e.getType()} before delegating to
 * {@link groovy.lang.GroovyObject#invokeMethod(String, Object)};  if the type is wrong the
 * {@code invokeMethod} MOP fallback is silently skipped.</p>
 */
final class Groovy12046 {

    // ---------------------------------------------------------------------------
    // Shared proxy infrastructure (inlined per assertScript isolation requirement)
    // ---------------------------------------------------------------------------

    private static final String PROXY_SETUP = '''
        import groovy.lang.GroovySystem
        import groovy.lang.MetaClass
        import groovy.lang.MetaClassImpl
        import groovy.lang.MissingMethodException

        // Simulates the subclass a mocking framework generates at runtime.
        // The per-instance metaclass intentionally targets the *parent* ObjClass.
        class Outer {
            static class ObjClass {
                String test(int a, int b) { "real:${a + b}" }
            }
            static class Proxy extends ObjClass {
                private final MetaClass metaClass
                Proxy() {
                    metaClass = new MetaClassImpl(GroovySystem.metaClassRegistry, ObjClass)
                    metaClass.initialize()
                }
                @Override MetaClass getMetaClass() { metaClass }
                @Override Object invokeMethod(String name, Object args) { "FALLBACK(${name})" }
            }
        }
    '''

    /**
     * The {@link groovy.lang.MissingMethodException#getType()} must equal the receiver's
     * <em>runtime</em> class ({@code Proxy}), not the metaclass {@code theClass} ({@code ObjClass}).
     * This is the contract consumed by {@code invokeGroovyObjectInvoker}'s type guard.
     */
    @Test
    void testMissingMethodExceptionTypeIsReceiverRuntimeClass() {
        assertScript PROXY_SETUP + '''
            Outer.ObjClass client = new Outer.Proxy()
            Object[] args = [123d, false] as Object[]

            // Pre-condition: metaclass is deliberately bound to the parent class.
            assert client.metaClass.theClass == Outer.ObjClass

            // Calling through the metaclass should propagate a MissingMethodException whose
            // type matches the actual runtime class, so that the indy guard can recognise it.
            try {
                client.metaClass.invokeMethod(Outer.ObjClass, client, 'test', args, false, false)
                assert false : 'expected MissingMethodException'
            } catch (MissingMethodException mme) {
                assert mme.type == Outer.Proxy : "expected Proxy but got ${mme.type}"
            }
        '''
    }

    /**
     * An end-to-end call on a typed variable compiled with an indy call site must activate the
     * {@link groovy.lang.GroovyObject#invokeMethod(String, Object)} MOP fallback when the
     * target method is absent from the per-instance metaclass.
     */
    @Test
    void testInvokeMethodMOPFallbackHonoredViaIndyCallSite() {
        assertScript PROXY_SETUP + '''
            Outer.ObjClass client = new Outer.Proxy()

            // client.test(double, boolean) is not defined on ObjClass; the only match is the
            // int,int overload.  The indy site must fall through to Proxy.invokeMethod().
            assert client.test(123d, false) == 'FALLBACK(test)'
        '''
    }

    /**
     * If the missing-method path is already rethrowing an existing {@link groovy.lang.MissingMethodException},
     * it must preserve that original exception instead of synthesizing a replacement.
     */
    @Test
    void testExistingMissingMethodExceptionIsPreserved() {
        assertScript '''
            import groovy.lang.MissingMethodException

            class FailingDelegate {
                final MissingMethodException failure = new MissingMethodException('missingCall', FailingDelegate, [] as Object[])
                Object invokeMethod(String name, Object args) {
                    throw failure
                }
            }

            class DelegatingClosure extends Closure {
                DelegatingClosure(Object owner) { super(owner) }
                Object doCall() { missingCall() }
            }

            def delegate = new FailingDelegate()
            def closure = new DelegatingClosure(new Object())
            closure.delegate = delegate
            closure.resolveStrategy = Closure.DELEGATE_FIRST

            try {
                closure()
                assert false : 'expected MissingMethodException'
            } catch (MissingMethodException mme) {
                assert mme.is(delegate.failure)
                assert mme.type == FailingDelegate
            }
        '''
    }

    /**
     * Negative case: when the metaclass {@code theClass} <em>equals</em> the receiver's runtime
     * class (the ordinary, non-proxy scenario) the exception type must still be {@code theClass}.
     */
    @Test
    void testMissingMethodExceptionTypeIsTheClassForOrdinaryReceiver() {
        assertScript '''
            import groovy.lang.MissingMethodException

            class Foo {}

            Foo foo = new Foo()
            try {
                foo.metaClass.invokeMethod(Foo, foo, 'noSuchMethod', [] as Object[], false, false)
                assert false : 'expected MissingMethodException'
            } catch (MissingMethodException mme) {
                assert mme.type == Foo : "expected Foo but got ${mme.type}"
            }
        '''
    }
}
