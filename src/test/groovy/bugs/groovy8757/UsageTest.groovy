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
package groovy.bugs.groovy8757

import groovy.test.GroovyTestCase

class UsageTest extends GroovyTestCase {
    void testAccessingPrecompiledTraitWithMethodGenerics() {
        def c0 = new GroovyShell().evaluate('''
            import groovy.bugs.groovy8757.T0
            class C0 implements T0 {}
            new C0()
        ''')
        assertEquals(42, c0.foo(Integer))
        assertEquals(42L, c0.foo(Long))
    }
}
