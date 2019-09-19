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

class Groovy4006Bug extends GroovyTestCase {
    void testOuterThisReferenceImplicitPassingToInnerClassConstructorNoArg() {
        try {
            assertScript """
                class MyOuterClass4006V1 {
                    def outerName = 'OC1' 
                    def foo() {
                        def ic = new MyInnerClass4006V1()
                        ic.bar()
                    }
                    class MyInnerClass4006V1 {
                        def innerName
                        MyInnerClass4006V1() {
                            this.innerName = 'IC1'
                        }
                        def bar() {
                            assert this.innerName == 'IC1'
                            assert this.outerName == 'OC1'
                            this.outerName = 'OC1New'
                            assert this.outerName == 'OC1New'
                            throw new RuntimeException('V1 - Inner class now successfully refers to implicitly passed outer this reference!')
                        }
                    }
                }
                def oc = new MyOuterClass4006V1()
                oc.foo()
            """
            fail('The script run should have failed with RuntimeException, coming from bar() of inner class')
        } catch (RuntimeException ex) {
            assert ex.message == 'V1 - Inner class now successfully refers to implicitly passed outer this reference!'
        }
    }

    void testOuterThisReferenceImplicitPassingToInnerClassConstructorWithArg() {
        try {
            assertScript """
                class MyOuterClass4006V2 {
                    def outerName = 'OC2' 
                    def foo() {
                        def ic = new MyInnerClass4006V1('IC2')
                        ic.bar()
                    }
                    class MyInnerClass4006V1 {
                        def innerName
                        MyInnerClass4006V1(innerName) {
                            this.innerName = innerName
                        }
                        def bar() {
                            assert this.innerName == 'IC2'
                            assert this.outerName == 'OC2'
                            this.outerName = 'OC2New'
                            assert this.outerName == 'OC2New'
                            throw new RuntimeException('V2 - Inner class now successfully refers to implicitly passed outer this reference!')
                        }
                    }
                }
                def oc = new MyOuterClass4006V2()
                oc.foo()
            """
            fail('The script run should have failed with RuntimeException, coming from bar() of inner class')
        } catch (RuntimeException ex) {
            assert ex.message == 'V2 - Inner class now successfully refers to implicitly passed outer this reference!'
        }
    }

    void testOuterThisReferenceImplicitPassingToInnerClassConstructorWithArgInAProp() {
        try {
            assertScript """
                class MyOuterClass4006V3 {
                    def outerName = 'OC3' 
                    def icField = new MyInnerClass4006V3('IC3');
                    def foo() {
                        icField.bar()
                    }
                    class MyInnerClass4006V3 {
                        def innerName
                        MyInnerClass4006V3(innerName) {
                            this.innerName = innerName
                        }
                        def bar() {
                            assert this.innerName == 'IC3'
                            assert this.outerName == 'OC3'
                            this.outerName = 'OC3New'
                            assert this.outerName == 'OC3New'
                            throw new RuntimeException('V3 - Inner class now successfully refers to implicitly passed outer this reference!')
                        }
                    }
                }
                def oc = new MyOuterClass4006V3()
                oc.foo()
            """
            fail('The script run should have failed with RuntimeException, coming from bar() of inner class')
        } catch (RuntimeException ex) {
            assert ex.message == 'V3 - Inner class now successfully refers to implicitly passed outer this reference!'
        }
    }

    void testOuterThisReferenceImplicitPassingToInnerClassConstructorWithArgInAField() {
        try {
            assertScript """
                class MyOuterClass4006V4 {
                    def outerName = 'OC4' 
                    private def icField = new MyInnerClass4006V4('IC4');
                    def foo() {
                        icField.bar()
                    }
                    class MyInnerClass4006V4 {
                        def innerName
                        MyInnerClass4006V4(innerName) {
                            this.innerName = innerName
                        }
                        def bar() {
                            assert this.innerName == 'IC4'
                            assert this.outerName == 'OC4'
                            this.outerName = 'OC4New'
                            assert this.outerName == 'OC4New'
                            throw new RuntimeException('V4 - Inner class now successfully refers to implicitly passed outer this reference!')
                        }
                    }
                }
                def oc = new MyOuterClass4006V4()
                oc.foo()
            """
            fail('The script run should have failed with RuntimeException, coming from bar() of inner class')
        } catch (RuntimeException ex) {
            assert ex.message == 'V4 - Inner class now successfully refers to implicitly passed outer this reference!'
        }
    }
}
