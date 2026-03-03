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
import org.junit.jupiter.api.Test

final class MixedModeStaticCompilationTest extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    @Override
    protected void configure() {
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer }
    }

    @Test
    void testDynamicMethodCall() {
        assertScript '''
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

    @Test
    void testDynamicMethodCallInsideClosure() {
        assertScript '''
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

    @Test
    void testDynamicMethodCallWithCheckedArgument() {
        assertScript '''
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

    @Test
    void testDynamicMethodCallOnField() {
        assertScript '''
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

    @Test
    void testDynamicProperty() {
        assertScript '''
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

    @Test
    void testDynamicPropertyMixedWithCheckedMethodCall() {
        assertScript '''
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

    @Test
    void testDynamicPropertyAsArgumentToCheckedMethodCall() {
        assertScript '''
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

    @Test
    void testDynamicVariable() {
        shell.setVariable('myVariable', 123)
        assertScript '''
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            int value() {
                myVariable
            }
            assert value() == 123
        '''
    }

    @Test
    void testDynamicVariableMixedWithCheckedMethodCall() {
        shell.setVariable('myVariable', 123)
        assertScript '''
            int offset() { 321 }
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            int value() {
                myVariable + offset()
            }
            assert value() == 444
        '''
    }

    @Test
    void testDynamicVariableAsArgumentToCheckedMethodCall() {
        shell.setVariable('myVariable', 123)
        assertScript '''
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

    @Test
    void testMetaClassUsage() {
        assertScript '''
            @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
            void test() {
                assert String.metaClass !== Class.metaClass
                assert String.metaClass !== Class.getMetaClass()
                assert String.getMetaClass() !== Class.getMetaClass()
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

    @Test
    void testDynamicClassMethod1() {
        assertScript '''
            @CompileStatic(extensions='groovy/transform/sc/MixedMode2.groovy')
            Map<String, Integer> foo() {
                String.map()
            }
            try {
                String.metaClass.static.map = { -> [a:1,b:2] }
                assert foo().values().sort() == [1,2]
            } finally {
                GroovySystem.getMetaClassRegistry().removeMetaClass(String)
            }
        '''
    }

    @Test
    void testDynamicClassMethod2() {
        assertScript '''
            @CompileStatic(extensions='groovy/transform/sc/MixedMode2.groovy')
            List<Integer> foo() {
                Date.list()
            }
            try {
                Date.metaClass.static.list = { -> [1,2] }
                assert foo() == [1,2]
            } finally {
                GroovySystem.getMetaClassRegistry().removeMetaClass(Date)
            }
        '''
    }

    // GROOVY-11817
    @Test
    void testDynamicClassMethod3() {
        assertScript '''
            @CompileStatic(extensions='groovy/transform/sc/MixedMode2.groovy')
            def foo() {
                def list_of_integer = Date.list()
                def integer = list_of_integer[0]
            }
            try {
                Date.metaClass.static.list = { -> [1,2] }
                assert foo() == 1
            } finally {
                GroovySystem.getMetaClassRegistry().removeMetaClass(Date)
            }
        '''
    }

    @Test
    void testDynamicBuilder() {
        assertScript '''import groovy.xml.MarkupBuilder
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

    @Test
    void testDynamicClassWithStaticConstructorAndInitialization() {
        shouldFailWithMessages '''
            class A {
            }
            class B {
                A a = new A() // may require dynamic support...
                @CompileStatic
                B() {
                }
            }
        ''',
        'Cannot statically compile constructor implicitly including non-static elements from fields, properties or initializers'
    }

    @Test
    void testSCClosureCanAccessPrivateFieldsOfNonSCEnclosingClass() {
        assertScript '''
            class Test {
                private String str = 'hi'
                @CompileStatic
                String strInSCClosure() {
                    Closure c = { str }
                    c()
                }
            }
            assert new Test().strInSCClosure() == 'hi'
        '''
    }

    @Test
    void testSCClosureCanAccessPrivateMethodsOfNonSCEnclosingClass() {
        assertScript '''
            class Test {
                private String str() { 'hi' }
                @CompileStatic
                String strInSCClosure() {
                    Closure c = { str() }
                    c()
                }
            }
            assert new Test().strInSCClosure() == 'hi'
        '''
    }

    @Test
    void testSCInnerClassCanAccessPrivateFieldsOfNonSCOuterClass() {
        assertScript '''
            class Test {
                private String str = 'hi'
                @CompileStatic
                class Inner {
                    String outerStr() { str }
                }
                String strInSCInner() { new Inner().outerStr() }
            }
            assert new Test().strInSCInner() == 'hi'
        '''
    }

    @Test
    void testSCInnerClassCanAccessPrivateMethodsOfNonSCOuterClass() {
        assertScript '''
            class Test {
                private String str() { 'hi' }
                @CompileStatic
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
                    @CompileStatic
                    String outerStr() { str() }
                }
                String strInSCInner() { new Inner().outerStr() }
            }
            assert new Test().strInSCInner() == 'hi'
        '''
    }

    @Test
    void testSCAICCanAccessPrivateFieldsOfNonSCOuterClass() {
        assertScript '''
            class Test {
                private String str = "hi"
                @CompileStatic
                String strInSCAIC() {
                    new Object() {
                        String outerStr() { str }
                    }.outerStr()
                }
            }
            assert new Test().strInSCAIC() == 'hi'
        '''
    }

    @Test
    void testSCAICCanAccessPrivateMethodsOfNonSCOuterClass() {
        assertScript '''
            class Test {
                private String str() { 'hi' }
                @CompileStatic
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
                        @CompileStatic @Override String call() { str() }
                    }
                    callable.call()
                }
            }
            assert new Test().strInSCAIC() == 'hi'
        '''
    }
}
