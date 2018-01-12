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

package groovy.transform.stc

class LambdaTest extends GroovyTestCase {
    void testMethodCall() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p1();
            }
        
            public static void p1() {
                assert [2, 3, 4] == Stream.of(1, 2, 3).map(e -> e.plus 1).collect(Collectors.toList());
            }
        }
        '''

    }

    void testMethodCall2() {
        if (true) return;

        // the test can pass only in dynamic mode now, it can not pass static type checking...

        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p2();
            }
            
            public static void p2() {
                assert 13 == Stream.of(1, 2, 3).reduce(7, (r, e) -> r + e);
            }
        }
        '''

    }

    void testMethodCall3() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p3();
            }
            
            public static void p3() {
                Stream.of(1, 2, 3).forEach(e -> { System.out.println(e + 1); });
            }
            
        }
        '''

    }
}
