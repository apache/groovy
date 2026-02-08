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
package org.apache.groovy.contracts.tests.other

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

class GenericTypeTests extends BaseTestClass {

    @Test
    void requires_on_generic_type_parameter() {

        def source = """
           import groovy.contracts.*

           class A {

              @Requires({ ref != null })
              public <T> T m(T ref) { ref }
           }

        """
        def a = create_instance_of(source)
        assert a.m('test') == 'test'
    }

    @Test
    void ensures_on_generic_type_parameter() {

        def source = '''
            import groovy.contracts.*

            class A {
                @Ensures({ result != null })
                public <T> T m(T ref) { ref }
            }
        '''

        def a = create_instance_of(source)
        assert a.m('test') == 'test'
    }

    @Test
    void invariant_on_generic_type_parameter() {

        def source = """
            import groovy.contracts.*

            @groovy.transform.CompileStatic
            @Invariant({ property.size() >= 0 })
            class A<T extends java.util.List> {
                T property = []

                @Ensures({ result != null })
                public <U> U m(U ref) { ref }
//                public <T> T m(T ref) { ref } // TODO investigate generic shadowing of T
            }
        """

        def a = create_instance_of(source)
        assert a.m('test') == 'test'
    }
}
