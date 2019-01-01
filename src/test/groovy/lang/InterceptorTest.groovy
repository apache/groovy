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

import org.codehaus.groovy.runtime.StringBufferWriter

/**
 * Test for the Interceptor Interface usage as implemented by the
 * TracingInterceptor. Makes also use of the ProxyMetaClass and
 * shows the collaboration.
 * As a side Effect, the ProxyMetaClass is also partly tested.
 */
class InterceptorTest extends GroovyTestCase {

    def Interceptor logInterceptor
    def StringBuffer log
    def interceptable   // the object to intercept method calls on
    def proxy

    void setUp() {
        logInterceptor = new TracingInterceptor()
        log = new StringBuffer("\n")
        logInterceptor.writer = new StringBufferWriter(log)
        // we intercept calls from Groovy to the java.lang.String object
        interceptable = 'Interceptable String'
        proxy = ProxyMetaClass.getInstance(interceptable.class)
        proxy.setInterceptor(logInterceptor)
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
        proxy.setInterceptor(logInterceptor)
        proxy.use(slicer) {
            slicer.coffeeBreak('')
        }
        assertEquals("""
before groovy.mock.example.CheeseSlicer.coffeeBreak(java.lang.String)
after  groovy.mock.example.CheeseSlicer.coffeeBreak(java.lang.String)
""", log.toString())
    }

    void testProxyMetaClassUseMethodShouldReturnTheResultOfClosure() {
        assertEquals true, proxy.use { true }
    }
}
