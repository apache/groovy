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
 * GROOVY-11069: {@code @POJO} on a nested class must keep that class
 * from implementing {@code GroovyObject} (a regression of GROOVY-10691
 * had brought the GroovyObject methods back).
 */
final class Groovy11069 {

    @Test
    void testPojoOnNestedClassDoesNotImplementGroovyObject() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.stc.POJO

            @CompileStatic @POJO
            class Outer {
                static String prop = 'SomeProp'
                @CompileStatic @POJO
                static class Inner {
                    String lowerProp() { prop.toLowerCase() }
                }
            }
            def inner = new Outer.Inner()
            assert inner.lowerProp() == 'someprop'
            assert !(inner instanceof GroovyObject)
            assert inner.getClass().superclass == Object
        '''
    }
}
