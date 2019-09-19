import groovy.test.GroovyTestCase

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
class PrimitiveTest extends GroovyTestCase {

    void testPrimitiveReferences() {
        assertScript '''
            // tag::primitive_references[]
            class Foo {
              static int i
            }

            assert Foo.class.getDeclaredField('i').type == int.class
            assert Foo.i.class != int.class && Foo.i.class == Integer.class
            // end::primitive_references[]
        '''
    }

    void testPrimitiveWideningVsBoxing() {
        assertScript '''
            // tag::widening_vs_boxing[]
            int i
            m(i)

            void m(long l) {           //<1>
              println "in m(long)"
            }

            void m(Integer i) {        //<2>
              println "in m(Integer)"
            }
            // end::widening_vs_boxing[]
        '''
    }
}
