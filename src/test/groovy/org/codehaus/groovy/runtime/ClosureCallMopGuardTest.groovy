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
 * {@code Closure.call}'s cached direct-dispatch path must be semantically invisible:
 * it may only be taken when nothing MOP-relevant is in play. These tests pin the
 * guard — a per-instance metaclass whose {@code invokeMethod} intercepts
 * {@code doCall} must be honoured on the Java/GDK entry ({@code closure.call(...)}
 * from DGM), not just the dynamic path, for statically compiled closure classes
 * (which declare typed doCall/call methods the cache serves). Without the guard,
 * the cache invokes the target directly and the interception is silently skipped
 * on one path but not the other.
 */
final class ClosureCallMopGuardTest {

    private static final String INTERCEPTOR = '''
        def intercept = { Closure target ->
            def mc = new DelegatingMetaClass(target.metaClass) {
                Object invokeMethod(Object object, String methodName, Object[] args) {
                    if (methodName == 'doCall' || methodName == 'call') return 'intercepted'
                    super.invokeMethod(object, methodName, args)
                }
            }
            mc.initialize()
            target.metaClass = mc
            target
        }
    '''

    @Test
    void perInstanceMetaclassInterceptsStaticallyCompiledClosureOnGdkPath() {
        assertScript INTERCEPTOR + '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class D { Closure maker() { return { Integer x -> x * 2 } } }
            def c = new D().maker()
            assert [3].collect(c) == [6]           // unperturbed: fast path, real body
            intercept(c)
            assert c(3) == 'intercepted'           // dynamic path
            assert [3].collect(c) == ['intercepted'] // Java/GDK path: the guard must route via the MOP
        '''
    }

    @Test
    void unperturbedInstancesAreUnaffectedByAnotherInstancesMetaclass() {
        assertScript INTERCEPTOR + '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class D2 { Closure maker() { return { Integer x -> x + 1 } } }
            def a = new D2().maker()
            def b = new D2().maker()
            intercept(a)
            assert [1].collect(a) == ['intercepted']
            assert [1].collect(b) == [2]           // b keeps the fast path
        '''
    }

    @Test
    void behaviourInsideCategoryBlocksIsPreserved() {
        // A category on the current thread conservatively disables the direct path (matching
        // AbstractCallSite); results must be identical either way. Note ClosureMetaClass has
        // never consulted categories for doCall dispatch (verified 3.x-5.x), so the pin here
        // is "unchanged behaviour", not interception.
        assertScript '''
            class ShoutCategory {
                static String doCall(Closure self, Object arg) { 'category' }
            }
            def c = { it * 2 }
            assert [3].collect(c) == [6]
            use(ShoutCategory) {
                assert c(3) == 6                  // dynamic path: unchanged (as in 3.x-5.x)
                assert [3].collect(c) == [6]      // Java/GDK path via the MOP route: unchanged
            }
            assert [3].collect(c) == [6]          // fast path restored after the block
        '''
    }
}
