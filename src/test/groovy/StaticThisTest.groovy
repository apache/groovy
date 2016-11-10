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
package groovy

import gls.CompilableTestSupport

class StaticThisTest extends CompilableTestSupport {

    void testThisFail() {
        staticMethod()
    }

    static staticMethod() {
        def foo = this
        assert foo != null
        assert foo.name.endsWith("StaticThisTest")

        def s = super
        assert s != null
        assert s.name.endsWith("CompilableTestSupport")
    }

    void testThisPropertyInStaticMethodShouldNotCompile() {
        shouldNotCompile """
            class A {
                def prop
                static method(){
                    this.prop
                }
            }
            """
    }

    void testSuperPropertyInStaticMethodShouldNotCompile() {
        try {
            assertScript """
            class A { def prop }
            class B extends A {
                static method(){
                    super.prop
                }
            }
            B.method()
            """
        } catch (MissingPropertyException mpe) {
            assert mpe.message.contains('No such property: prop for class: A')
        }
    }

    void testQualifiedThisShouldBeNested() {
        shouldNotCompile """
            class A {
                def foo() {
                    A.this.class
                }
            }
            """
    }

    void testQualifiedSuperShouldBeNested() {
        shouldNotCompile """
            class A {
                def foo() {
                    A.super.class
                }
            }
            """
    }

    void testQualifiedThisShouldReferenceOuterClass() {
        shouldNotCompile """
            class A {
                class Inner {
                    def foo() {
                        String.this.class
                    }
                }
            }
            """
    }

    void testQualifiedSuperShouldReferenceOuterClass() {
        shouldNotCompile """
            class A {
                class Inner {
                    def foo() {
                        String.super.class
                    }
                }
            }
            """
    }

    void testQualifiedThisForNestedClassShouldNotBeStaticContext() {
        shouldNotCompile """
            class A {
                static class Inner {
                    static foo() {
                        A.this.class
                    }
                }
            }
            """
    }

    void testQualifiedSuperForNestedClassShouldNotBeStaticContext() {
        shouldNotCompile """
            class A {
                static class Inner {
                    static foo() {
                        A.super.class
                    }
                }
            }
            """
    }

    /**
     * GROOVY-7047: Static inner class crashes compiler when it references parent's this
     */
    void testParentThisShouldNotBeReferredInsideStaticClass() {
        shouldNotCompile """
            class Foo {
                static class Bar {
                    def parent = Foo.this
                }
            }
        """
    }

}
