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
package groovy.bugs

import gls.CompilableTestSupport

class Groovy8090Bug extends CompilableTestSupport {
    void testGroovy8090() {
        assertScript '''
        import static java.util.Arrays.asList
        
        @groovy.transform.CompileStatic
        class Main {
            final <T> Iterable<T> foo(T instance) { asList(instance) }
            final <U> Iterable<U> bar(U instance) { asList(instance) }
            final Iterable<String> baz(String instance) { asList(instance) }
        }
        
        new Main().with {
            assert foo('A') + bar('B') + baz('C') == ['A', 'B', 'C']
        }
        '''
    }

    void test2() {
        assertScript '''
        @groovy.transform.CompileStatic
        class Main {
            final <U> Iterable<U> bar(U instance) { Arrays.asList(instance) }
        }
        
        assert new Main().bar('B') == ['B']
        '''
    }

    void test3() {
        assertScript '''
        @groovy.transform.CompileStatic
        class Arraysx {
            static <T> List<T> asList(T a) {
                return [a]
            }
        }
        
        @groovy.transform.CompileStatic
        class Main {
            final <U> Iterable<U> bar(U instance) { Arraysx.asList(instance) }
        }
        
        assert new Main().bar('B') == ['B']
        '''
    }
}
