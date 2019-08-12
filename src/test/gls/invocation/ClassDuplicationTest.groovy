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
package gls.invocation

final class ClassDuplicationTest extends GroovyTestCase {
    void testDuplicationOnMethodSignatureTest() {
        def shell1 = new GroovyShell(this.class.classLoader)
        def obj1 = shell1.evaluate("""
            class A {}
            def foo(A a) {}
            return this
        """)
        def shell2 = new GroovyShell(this.class.classLoader)
        def obj2 = shell2.evaluate("""
            class A {}
            return new A()
        """)
        try {
            obj1.foo(obj2)
            assert false
        } catch (MissingMethodException mme) {
            assert mme.toString().contains("A (defined by")
        }
    }
}
