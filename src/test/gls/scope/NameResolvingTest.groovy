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
package gls.scope

import gls.CompilableTestSupport

class NameResolvingTest extends CompilableTestSupport {
    void testVariableNameEqualsToAClassName() {
        Object String = ""
        assert String == ""
        assert String.class == java.lang.String
    }

    void testVariableNameEqualsCurrentClassName() {
        Object NameResolvingTest = ""
        assert NameResolvingTest == ""
        assert NameResolvingTest.class == java.lang.String.class
    }

    void testClassNoVariableInStaticMethod() {
        assertScript """
            static def foo() {
                Class.forName('java.lang.Integer')
            }
            assert foo() == Integer
        """
    }

    void testInAsDefAllowedInPackageNames() {
        shouldCompile """
            package as.in.def
            class X {}
        """
    }

    void testAssignmentToNonLocalVariableWithSameNameAsClass() {
        shouldNotCompile """
            String = 1
        """
    }

    void testClassUsageInSuper() {
        shouldCompile """
            class A {A(x){}}
            class B extends A {
                B(x){super(Thread)}
            }
        """
    }
    
    void testSuperClassVariableAccess() {
        assertScript """
            class U {
                public static final int uint
            }
        
            class A extends U {
                def foo ( ) {
                    id  "A" + ( ( uint < 7 ) ? "B" : "C" ) + '\\n'
                }
                def id(x){x}
            }
            def a = new A()
            assert a.foo() == "AB\\n"
        """
    }
}