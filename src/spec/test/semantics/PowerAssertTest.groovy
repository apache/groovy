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
package semantics

import asciidoctor.Utils
import groovy.test.GroovyTestCase

class PowerAssertTest extends GroovyTestCase {
    void testPowerAssert() {
        def msg = shouldFail {
            //tag::assert_code_1[]
            assert 1+1 == 3
            //end::assert_code_1[]
        }
        assert msg == Utils.stripAsciidocMarkup('''
//tag::assert_error_1[]
assert 1+1 == 3
        |  |
        2  false
//end::assert_error_1[]
''')
    }

    void testPowerAssert2() {
        def msg = shouldFail {
            //tag::assert_code_2[]
            def x = 2
            def y = 7
            def z = 5
            def calc = { a,b -> a*b+1 }
            assert calc(x,y) == [x,z].sum()
            //end::assert_code_2[]
        }
        assert msg == Utils.stripAsciidocMarkup('''
//tag::assert_error_2[]
assert calc(x,y) == [x,z].sum()
       |    | |  |   | |  |
       15   2 7  |   2 5  7
                 false
//end::assert_error_2[]
''')
    }

    void testCustomAssertMessage() {
        def msg = shouldFail {
            //tag::assert_code_3[]
            def x = 2
            def y = 7
            def z = 5
            def calc = { a,b -> a*b+1 }
            assert calc(x,y) == z*z : 'Incorrect computation result'
            //end::assert_code_3[]
        }
        assert msg == Utils.stripAsciidocMarkup('''
//tag::assert_error_3[]
Incorrect computation result. Expression: (calc.call(x, y) == (z * z)). Values: z = 5, z = 5
//end::assert_error_3[]
''')
    }
}
