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
import org.junit.jupiter.api.Test

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
    void ensures_result_generics_available_under_compile_static() {

        // GROOVY-12071: result's declared generics (Map<String, Integer>) must survive into the
        // generated contract closure so that static type checking infers Integer values and
        // no explicit casts are needed in the @Ensures condition.
        def source = '''
            import groovy.contracts.*
            import groovy.transform.CompileStatic

            class A {
                @CompileStatic
                @Ensures({ result.sum <= n * result.max })
                Map<String, Integer> sumMax(int[] a, int n) {
                    int sum = 0, max = 0
                    for (int i = 0; i < n; i++) {
                        if (max < a[i]) max = a[i]
                        sum += a[i]
                    }
                    [sum: sum, max: max]
                }
            }
        '''

        def a = create_instance_of(source)
        assert a.sumMax([1, 10, 20] as int[], 3) == [sum: 31, max: 20]
    }

    @Test
    void ensures_result_generics_in_script_static_method_with_loop_contracts() {

        // GROOVY-12071: the originally reported case: a script-level @CompileStatic static method
        // whose @Ensures references result.sum / result.max with no casts, alongside loop contracts.
        def source = '''
            import groovy.contracts.*
            import groovy.transform.CompileStatic

            @CompileStatic
            @Requires({ 0 <= n && a.length == n && (0..<n).every { a[it] >= 0 } })
            @Ensures({ result.sum <= n * result.max })
            static Map<String, Integer> sumMax(int[] a, int n) {
                int sum = 0, max = 0, i = 0
                @Invariant({ 0 <= i && i <= n && sum <= i * max })
                @Decreases({ n - i })
                while (i < n) {
                    if (max < a[i]) max = a[i]
                    sum += a[i]
                    i++
                }
                [sum: sum, max: max]
            }

            assert sumMax([1, 10, 20] as int[], 3) == [sum: 31, max: 20]
            'ok'
        '''

        def scriptClass = add_class_to_classpath(source)
        assert scriptClass.newInstance().run() == 'ok'
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
