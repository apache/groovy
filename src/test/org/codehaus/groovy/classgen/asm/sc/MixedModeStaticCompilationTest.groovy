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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

final class MixedModeStaticCompilationTest extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    @Override
    protected void setUp() {
        super.setUp()
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer }
    }

    void testDynamicMethodCall() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            int bar() {
                foo() + baz()
            }
            int baz() {
                456
            }
            this.metaClass.foo = { 123 }
            assert bar() == 579
        '''
    }

    void testDynamicMethodCallInsideClosure() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            int bar() {
                def cl = { foo() + baz() }
                cl()
            }
            int baz() {
                456
            }
            this.metaClass.foo = { 123 }
            assert bar() == 579
        '''
    }

    void testDynamicMethodCallWithStaticCallArgument() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            int bar() {
                twice(baz())
            }
            int baz() {
                456
            }
            this.metaClass.twice = { 2*it }
            assert bar() == 912
        '''
    }

    void testDynamicMethodCallOnField() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            class Holder {
                def delegate
                int bar() {
                    2*delegate.baz()
                }
            }
            class Baz {
                int baz() { 456 }
            }
            def holder = new Holder(delegate: new Baz())
            assert holder.bar() == 912
        '''
    }

    void testDynamicProperty() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            int value(String str) {
                str.val
            }
            @Category(String)
            class StringCategory {
                int getVal() { this.length() }
            }
            use (StringCategory) {
                assert value('abc') == 3
            }
        '''
    }

    void testDynamicPropertyMixedWithStatic() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            class Holder {
                int offset() { 2 }
                int value(String str) {
                    str.val + offset()
                }
            }
            @Category(String)
            class StringCategory {
                int getVal() { this.length() }
            }
            def holder = new Holder()
            use (StringCategory) {
                assert holder.value('abc') == 5
            }
        '''
    }

    void testDynamicPropertyAsStaticArgument() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            class Holder {
                int twice(int v) { 2*v }
                int value(String str) {
                    twice(str.val)
                }
            }
            @Category(String)
            class StringCategory {
                int getVal() { this.length() }
            }
            def holder = new Holder()
            use (StringCategory) {
                assert holder.value('abc') == 6
            }
        '''
    }

    void testDynamicVariable() {
        shell.setVariable('myVariable', 123)
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            int value() {
                myVariable
            }
            assert value() == 123
        '''
    }

    void testDynamicVariableMixedWithStaticCall() {
        shell.setVariable('myVariable', 123)
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            class Holder {
                def binding
                def propertyMissing(String name) { binding.getVariable(name) }
                int value() {
                    myVariable + offset()
                }
                int offset() { 123 }
            }
            def h = new Holder(binding:binding)
            assert h.value() == 246
        '''
    }

    void testDynamicVariableAsStaticCallParameter() {
        shell.setVariable('myVariable', 123)
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            class Holder {
                def binding
                def propertyMissing(String name) { binding.getVariable(name) }
                int value() {
                    twice(myVariable)
                }
                int twice(int x) { 2*x }
            }
            def h = new Holder(binding:binding)
            assert h.value() == 246
        '''
    }

    void testAllowMetaClass() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            void test() {
                assert String.metaClass != Class.metaClass
                assert String.metaClass != Class.getMetaClass()
                String.metaClass.up = { -> (delegate as String).toUpperCase() }
            }
            try {
                test()
                assert 'aaa'.up() == 'AAA'
            } finally {
                GroovySystem.getMetaClassRegistry().removeMetaClass(String)
            }
        '''
    }

    void testRecognizeStaticMethodCall() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic(extensions='groovy/transform/sc/MixedMode2.groovy')
            Map<String, Integer> foo() {
                String.map()
            }
            @CompileStatic(extensions='groovy/transform/sc/MixedMode2.groovy')
            List<Integer> bar() {
                Date.list()
            }
            try {
                String.metaClass.static.map = { [a: 1, b:2 ]}
                Date.metaClass.static.list = { [1,2] }
                assert foo().values().sort() == bar()
            } finally {
                GroovySystem.getMetaClassRegistry().removeMetaClass(Date)
                GroovySystem.getMetaClassRegistry().removeMetaClass(String)
            }
        '''
    }

    void testDynamicBuilder() {
        assertScript '''import groovy.transform.CompileStatic
            import groovy.xml.MarkupBuilder

            @CompileStatic(extensions='groovy/transform/sc/MixedModeDynamicBuilder.groovy')
            String render(List<String> items) {
                def sw = new StringWriter()
                def mb = new MarkupBuilder(sw)
                mb.html {
                    body {
                        ul {
                            items.each { String item ->
                                li("Item ${item.toUpperCase()}")
                            }
                        }
                    }
                }

                sw
            }
            def list = ['Chocolate','Milk','Butter']
            def rendered = render(list).replaceAll(/[\r\n]|[ ]{2,}/,'')
            assert rendered == '<html><body><ul><li>Item CHOCOLATE</li><li>Item MILK</li><li>Item BUTTER</li></ul></body></html>'
        '''
    }

    void testDynamicClassWithStaticConstructorAndInitialization() {
        shouldFailWithMessages '''
            class A {
            }
            class B {
                A a = new A() // may require dynamic support...
                @groovy.transform.CompileStatic
                B() {
                }
            }
        ''',
        'Cannot statically compile constructor implicitly including non-static elements from fields, properties or initializers'
    }

    void testSCClosureCanAccessPrivateFieldsOfNonSCEnclosingClass() {
        assertScript '''
            class Test {
                private String str = "hi"

                @groovy.transform.CompileStatic
                String strInSCClosure() {
                    Closure c = { str }
                    c()
                }
            }
            assert new Test().strInSCClosure() == 'hi'
        '''
    }

    void testSCClosureCanAccessPrivateMethodsOfNonSCEnclosingClass() {
        assertScript '''
            class Test {
                private String str() { 'hi' }

                @groovy.transform.CompileStatic
                String strInSCClosure() {
                    Closure c = { str() }
                    c()
                }
            }
            assert new Test().strInSCClosure() == 'hi'
        '''
    }

    void testSCInnerClassCanAccessPrivateFieldsOfNonSCOuterClass() {
        assertScript '''
            class Test {
                private String str = "hi"

                @groovy.transform.CompileStatic
                class Inner {
                    String outerStr() { str }
                }

                String strInSCInner() { new Inner().outerStr() }
            }
            assert new Test().strInSCInner() == 'hi'
        '''
    }

    void testSCInnerClassCanAccessPrivateMethodsOfNonSCOuterClass() {
        assertScript '''
            class Test {
                private String str() { 'hi' }

                @groovy.transform.CompileStatic
                class Inner {
                    String outerStr() { str() }
                }

                String strInSCInner() { new Inner().outerStr() }
            }
            assert new Test().strInSCInner() == 'hi'
        '''

        // GROOVY-9328
        assertScript '''
            class Test {
                private String str() { 'hi' }

                class Inner {
                    @groovy.transform.CompileStatic
                    String outerStr() { str() }
                }

                String strInSCInner() { new Inner().outerStr() }
            }
            assert new Test().strInSCInner() == 'hi'
        '''
    }

    void testSCAICCanAccessPrivateFieldsOfNonSCOuterClass() {
        assertScript '''
            class Test {
                private String str = "hi"

                @groovy.transform.CompileStatic
                String strInSCAIC() {
                    new Object() {
                        String outerStr() { str }
                    }.outerStr()
                }
            }
            assert new Test().strInSCAIC() == 'hi'
        '''
    }

    void testSCAICCanAccessPrivateMethodsOfNonSCOuterClass() {
        assertScript '''
            class Test {
                private String str() { 'hi' }

                @groovy.transform.CompileStatic
                String strInSCAIC() {
                    def callable = new java.util.concurrent.Callable<String>() {
                        @Override String call() { str() }
                    }
                    callable.call()
                }
            }
            assert new Test().strInSCAIC() == 'hi'
        '''

        // GROOVY-9328
        assertScript '''
            class Test {
                private String str() { 'hi' }

                def strInSCAIC() {
                    def callable = new java.util.concurrent.Callable<String>() {
                        @groovy.transform.CompileStatic
                        @Override String call() { str() }
                    }
                    callable.call()
                }
            }
            assert new Test().strInSCAIC() == 'hi'
        '''
    }
}
