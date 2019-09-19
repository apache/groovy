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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.test.GroovyTestCase

class Groovy7883Bug extends GroovyTestCase {
    void testGroovy7883() {
        assertScript '''
        @groovy.transform.CompileStatic
        void doIt() {
            throw new AssertionError("abc")
        }
        
        try {
            doIt()
            assert false: "should never reach here"
        } catch (AssertionError e) {
            assert 'abc' == e.message
        }
        '''
    }

    void test2() {
        def errMsg = shouldFail '''
        @groovy.transform.CompileStatic
        class A {
            private void doIt() {}
        }
        
        @groovy.transform.CompileStatic
        class B {
            public void m() { new A().doIt() }
        }
        
        new B().m()
        '''

        assert errMsg.contains('[Static type checking] - Cannot find matching method A#doIt()')
    }

    void test4() {
        def errMsg = shouldFail '''
        @groovy.transform.CompileStatic
        class A {
            private void doIt() {}
        }
        
        @groovy.transform.CompileStatic
        class B extends A {
            public void m() { doIt() }
        }
        
        new B().m()
        '''

        assert errMsg.contains('[Static type checking] - Cannot find matching method B#doIt()')
    }

    /**
     * ensure the filtering logic does not break any code
     */
    void test5() {
        assertScript '''
        @groovy.transform.CompileStatic
        class A {
            protected void doIt() { doIt2() }
            private void doIt2() {}
            public void doIt3() { doIt() }
        }
        
        @groovy.transform.CompileStatic
        class B extends A {
            public void m() { doIt() }
        }
        
        new B().m()
        new B().doIt3()
        '''
    }

    void test6() {
        assertScript '''
        @groovy.transform.CompileStatic
        class A {
            protected void doIt(ArrayList al) { doIt2(al) }
            private void doIt2(List list, String x = "abc") {}
        }
        
        @groovy.transform.CompileStatic
        class B extends A {
            public void m() { doIt(new ArrayList()) }
        }
        
        new B().m()
        '''
    }

    void test7() {
        assertScript '''
        @groovy.transform.CompileStatic
        class A {
            protected void doIt(ArrayList al) { doIt2(al) }
            private void doIt2(List list, String x = "abc") {}
            public void doIt3() { }
        }
        
        @groovy.transform.CompileStatic
        class B extends A {
            class C {
                public void m() {
                    doIt(new ArrayList());
                    doIt3();
                }
            }
            
        }
        
        assert true
        '''
    }

    void test8() {
        def errMsg = shouldFail  '''
        @groovy.transform.CompileStatic
        class A {
            protected void doIt(ArrayList al) { doIt2(al) }
            private void doIt2(List list, String x = "abc") {}
        }
        
        @groovy.transform.CompileStatic
        class B extends A {
            class C {
                public void m() { doIt2(new ArrayList()) }
            }
            
        }
        
        '''

        assert errMsg.contains('[Static type checking] - Cannot find matching method B$C#doIt2(java.util.ArrayList)')
    }
}
