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
 * GROOVY-8912: chaining {@code getJavaClass().getMetaClass().getMetaProperty(...)}
 * must not throw GroovyCastException (Class cannot be cast to GroovyObject).
 */
final class Groovy8912 {

    @Test
    void testChainedGetMetaClassOnClassDoesNotCastToGroovyObject() {
        assertScript '''
            class Person { String id }
            def getJavaClass() { Person }
            MetaProperty idProp = getJavaClass().getMetaClass().getMetaProperty('id')
            assert idProp.name == 'id'
        '''
    }

    @Test
    void testChainedGetMetaClassOnClassUnderCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic

            class Person { String id }

            @CompileStatic
            class C {
                Class getJavaClass() { Person }
                def test() {
                    MetaProperty idProp = getJavaClass().getMetaClass().getMetaProperty('id')
                    return idProp.name
                }
            }
            assert new C().test() == 'id'
        '''
    }
}
