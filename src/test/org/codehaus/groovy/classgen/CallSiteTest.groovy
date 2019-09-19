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
package org.codehaus.groovy.classgen

import groovy.test.GroovyTestCase

class CallSiteTest extends GroovyTestCase {
    
    void testChangeMetaClass2() {
        assertScript '''
            def a = new Dummy()
            Closure cl = {a.foo()}

            a.metaClass.foo {1}
            assert cl(1.5G) == 1

            a.metaClass.foo {2}
            assert cl(1.5G) == 2
            class Dummy {}
        '''
    }

    void testChangeMetaClass () {
        def obj = new OBJ()
        assertEquals(6, obj.method(3,3))

        ExpandoMetaClass mc = new ExpandoMetaClass(OBJ)
        mc.mutableMethod = { a, b -> a * b }
        mc.initialize()

        obj.metaClass = mc
        assertEquals(9, obj.method(3,3))

        obj.metaClass = null
        assertEquals(6, obj.method(3,3))

        mc = new ExpandoMetaClass(OBJ)
        mc.mutableMethod = { a, b -> a - b }
        mc.initialize ()

        GroovySystem.metaClassRegistry.setMetaClass(OBJ, mc)

        assertEquals(6, obj.method(3,3))

        final OBJ obj2 = new OBJ()
        assertEquals(0, obj2.method(3,3))

        assertEquals(6, obj.method(3,3))

        mc = new ExpandoMetaClass(Integer)
        mc.plus = { Integer b -> delegate * 10*b }
        mc.initialize ()
        GroovySystem.metaClassRegistry.setMetaClass(Integer, mc)
        try { // use try-finally to ensure mc will be deleted in error case
            assertEquals(150, 5 + 3)
            assertEquals(150, obj.method(5,3))
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(Integer)
        }
        assertEquals(8, 5 + 3)
        assertEquals(6, obj.method(3,3))

        use(TestCategory) {
            assertEquals(3, obj.method(3,3))
        }
    }
}

class OBJ {
    def method (a,b) {
        mutableMethod a, b
    }

    def mutableMethod (a,b) {
        a + b
    }
}

class TestCategory {
    static def mutableMethod(OBJ obj, a, b) {
        2 * a - b
    }
}
