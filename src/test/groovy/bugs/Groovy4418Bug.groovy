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

class Groovy4418Bug extends CompilableTestSupport {
    void testStaticFieldAccess() {
        assertScript '''
            class Base {
                static String field = 'foo'
            }
            class Subclass extends Base {
                static method() {
                    field
                }
            }
            assert new Subclass().method() == 'foo'
        '''
    }

    // additional test for GROOVY-6183
    void testStaticAttributeAccess() {
        assertScript '''
        class A {
            static protected int x
            public static void reset() { this.@x = 2 }
        }
        assert A.x == 0
        assert A.@x == 0
        A.reset()
        assert A.x == 2
        assert A.@x == 2
        '''
    }

    // GROOVY-8385
    void testParentClassStaticAttributeSetAccessShouldNotCallSetter() {
        assertScript '''
            class A {
                static protected p
                static setP(){def val}
                static getP(){this.@p}
            }
            class B extends A {
              def m(){this.@p = 1}
            }
            def x = new B()
            assert A.@p == null
            x.m()
            assert A.@p == 1
        '''

        assertScript '''
            class A {
                static protected p
                static setP(){def val}
                static getP(){this.@p}
            }
            class B extends A {
              def m(){super.@p = 1}
            }
            def x = new B()
            assert A.@p == null
            x.m()
            assert A.@p == 1
        '''

        assertScript '''
            class A {
                static protected p
                static setP(){def val}
                static getP(){this.@p}
            }
            class AA extends A {}
            class B extends AA {
              def m(){super.@p = 1}
            }
            def x = new B()
            assert A.@p == null
            x.m()
            assert A.@p == 1
        '''
    }

    // GROOVY-8385
    void testParentClassNonStaticAttributeSetAccessShouldNotCallSetter() {
        assertScript '''
            class A {
                protected p
                void setP(def val){}
                def getP(){p}
            }
            class B extends A {
              def m(){this.@p = 1}
            }
            def x = new B()
            assert x.@p == null
            x.m()
            assert x.@p == 1
        '''

        assertScript '''
            class A {
                protected p
                void setP(def val){}
                def getP(){p}
            }
            class B extends A {
              def m(){super.@p = 1}
            }
            def x = new B()
            assert x.@p == null
            x.m()
            assert x.@p == 1
        '''

        assertScript '''
            class A {
                protected p
                void setP(def val){}
                def getP(){p}
            }
            class AA extends A {}
            class B extends AA {
              def m(){super.@p = 1}
            }
            def x = new B()
            assert x.@p == null
            x.m()
            assert x.@p == 1
        '''
    }

    // GROOVY-8385
    void testParentClassPrivateStaticAttributeSetAccessShouldCallSetter() {
        shouldFail(MissingFieldException, '''
            class A {
                static private p
                static setP(){def val}
                static getP(){this.@p}
            }
            class B extends A {
              def m(){this.@p = 1}
            }
            def x = new B()
            assert A.@p == null
            x.m()
        ''')
    }

    // GROOVY-8385
    void testParentClassPrivateNonStaticAttributeSetAccessShouldNotCallSetter() {
        shouldFail(MissingFieldException, '''
            class A {
                private p
                void setP(def val){}
                def getP(){p}
            }
            class B extends A {
              def m(){this.@p = 1}
            }
            def x = new B()
            assert x.@p == null
            x.m()
        ''')
    }


}
