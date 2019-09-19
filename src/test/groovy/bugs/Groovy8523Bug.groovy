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

import groovy.test.GroovyTestCase

class Groovy8523Bug extends GroovyTestCase {
    void testInstanceofNot1() {
        assertScript '''
        import groovy.transform.CompileStatic
        @CompileStatic
        class Test1 {
            static int checkRes = 0

            static void f1(Object var1) {
                if (!(var1 instanceof Runnable)){
                    checkRes = 3
                    return
                }
                f2(var1)
            }

            static void f2(Runnable var2) {
                checkRes = 4
            }
        }

        Runnable r = {}
        Test1.f1(r)
        assert Test1.checkRes == 4
        Test1.f1(42)
        assert Test1.checkRes == 3
        '''
    }

    void testNotInstanceof1() {
        assertScript '''
        import groovy.transform.CompileStatic
        @CompileStatic
        class Test1 {
            static int checkRes = 0

            static void f1(Object var1) {
                if (var1 !instanceof Runnable){
                    checkRes = 3
                    return
                }
                f2(var1)
            }

            static void f2(Runnable var2) {
                checkRes = 4
            }
        }

        Runnable r = {}
        Test1.f1(r)
        assert Test1.checkRes == 4
        Test1.f1(42)
        assert Test1.checkRes == 3
        '''
    }


    void testInstanceofNot2() {
        assertScript '''
        import groovy.transform.CompileStatic
        @CompileStatic
        class Test1 {
            static int checkRes = 0

            static void f1(Object var1) {
                if (!(var1 instanceof Runnable)){
                    checkRes = 3
                    return
                }
                if (!(var1 instanceof List)){
                    checkRes = 5
                    return
                }
                f2(var1)
            }

            static void f2(Runnable var2) {
                checkRes = 4
            }
        }

        Runnable r = {}
        Test1.f1(r)
        assert Test1.checkRes == 5
        '''
    }


    void testInstanceofNot3() {
        assertScript '''
        import groovy.transform.CompileStatic
        @CompileStatic
        class Test1 {
            static int checkRes = 0

            static void f1(Object var1) {
                if (!(var1 instanceof Runnable)){
                    checkRes = 3
                    return
                }
                if (!(var1 instanceof Thread)){
                    checkRes = 5
                    return
                }
                f2(var1)
            }

            static void f2(Runnable var2) {
                checkRes = 4
            }
        }

        Runnable r = {}
        Test1.f1(r)
        assert Test1.checkRes == 5
        '''
    }


}
