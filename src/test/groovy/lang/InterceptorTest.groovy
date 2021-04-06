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
package groovy.lang

import groovy.test.GroovyTestCase
import org.codehaus.groovy.runtime.StringBufferWriter

/**
 * Test for the Interceptor Interface usage as implemented by the
 * {@link TracingInterceptor}. Makes use of the {@link ProxyMetaClass} and
 * shows the collaboration.
 * <p>
 * As a side effect, the {@code ProxyMetaClass} is also partly tested.
 */
final class InterceptorTest extends GroovyTestCase {

    private final Interceptor interceptor = new TracingInterceptor() // class under test
    private final String interceptable = 'Interceptable String' // the object to observe
    private final ProxyMetaClass proxy = ProxyMetaClass.getInstance(interceptable.class)
    private final StringBuffer log = new StringBuffer('\n')

    void setUp() {
        interceptor.writer = new StringBufferWriter(log)
        proxy.interceptor = interceptor
    }

    void testSimpleInterception() {
        proxy.use {
            assertEquals 20, interceptable.size()
            assertEquals 20, interceptable.length()
            assertTrue interceptable.startsWith('I', 0)
        }
        assertEquals("""
before java.lang.String.size()
after  java.lang.String.size()
before java.lang.String.length()
after  java.lang.String.length()
before java.lang.String.startsWith(java.lang.String, java.lang.Integer)
after  java.lang.String.startsWith(java.lang.String, java.lang.Integer)
""", log.toString())
    }

    void testNoInterceptionWithNullInterceptor() {
        proxy.setInterceptor(null)
        proxy.use {
            interceptable.size()
        }
    }

    void testConstructorInterception() {
        proxy.use {
            new String('some string')
        }
        assertEquals("""
before java.lang.String.ctor(java.lang.String)
after  java.lang.String.ctor(java.lang.String)
""", log.toString())
    }

    void testStaticMethodInterception() {
        proxy.use {
            assertEquals 'true', String.valueOf(true)
        }
        assertEquals("""
before java.lang.String.valueOf(java.lang.Boolean)
after  java.lang.String.valueOf(java.lang.Boolean)
""", log.toString())
    }

    void testInterceptionOfGroovyClasses() {
        def slicer = new groovy.mock.example.CheeseSlicer()
        def proxy = ProxyMetaClass.getInstance(slicer.class)
        proxy.setInterceptor(interceptor)
        proxy.use(slicer) {
            slicer.coffeeBreak('')
        }
        assertEquals("""
before groovy.mock.example.CheeseSlicer.coffeeBreak(java.lang.String)
after  groovy.mock.example.CheeseSlicer.coffeeBreak(java.lang.String)
""", log.toString())
    }

    void testProxyMetaClassUseMethodShouldReturnTheResultOfClosure() {
        assertTrue proxy.use { true }
    }

    // GROOVY-10009
    void testNullArgumentToMethodCall() {
        interceptable.metaClass = proxy
        interceptable.equals(null)

        assertEquals '''
            |before java.lang.String.equals(java.lang.Object)
            |after  java.lang.String.equals(java.lang.Object)
            |'''.stripMargin(), log.toString()
    }
}
