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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.assertThrows

final class CategoryTest {

    static class StringCategory {
        static String lower(String s) {
            s.toLowerCase()
        }
    }

    static class IntegerCategory {
        static Integer inc(Integer i) {
            i + 1
        }
    }

    @Test
    void testNestingOfCategories() {
        use(StringCategory) {
            assert 'Sam'.lower() == 'sam'
            use(IntegerCategory) {
                assert 9.inc() == 10
                assert 'Sam'.lower() == 'sam'
            }
            assertThrows(MissingMethodException) { 1.inc() }
        }
        assertThrows(MissingMethodException) { 'Sam'.lower() }
    }

    @Test
    void testReturnValueWithUseClass() {
        def returnValue = use(StringCategory) {
            'Green Eggs And Ham'.lower()
        }
        assert returnValue == 'green eggs and ham'
    }

    @Test
    void testReturnValueWithUseClassList() {
        def returnValue = use([StringCategory, IntegerCategory]) {
            'Green Eggs And Ham'.lower() + 5.inc()
        }
        assert returnValue == 'green eggs and ham6'
    }

    //--------------------------------------------------------------------------

    @Test
    void testCategoryDefinedProperties() {
        assertScript '''
            class CategoryTestPropertyCategory {
                private static aVal = 'hello'
                static getSomething(Object self) { return aVal }
                static void setSomething(Object self, newValue) { aVal = newValue }
            }

            use(CategoryTestPropertyCategory) {
                assert getSomething() == 'hello'
                assert something == 'hello'
                something = 'nihao'
                assert something == 'nihao'
            }
            // test the new value again in a new block
            use(CategoryTestPropertyCategory) {
                assert something == 'nihao'
            }
        '''
    }

    // GROOVY-10133
    @Test
    void testCategoryDefinedProperties2() {
        assertScript '''
            class Cat {
                static boolean isAbc(self) { true }
                static boolean getAbc(self) { false }
            }

            use(Cat) {
                assert abc // should select isAbc()
            }
        '''
    }

    // GROOVY-5245
    @Test
    void testCategoryDefinedProperties3() {
        assertScript '''
            class Isser {
                boolean isWorking() { true }
            }
            class IsserCat {
                static boolean getWorking2(Isser b) { true }
                static boolean isNotWorking(Isser b) { true }
            }

            use(IsserCat) {
                assert new Isser().working
                assert new Isser().working2
                assert new Isser().notWorking // MissingPropertyException
            }
        '''
    }

    @Test
    void testCategoryMethodReplacesPropertyAccessMethod() {
        assertScript '''
            class CategoryTestHelper {
                def aProperty = 'aValue'
            }
            class CategoryTestHelperPropertyReplacer {
                private static aVal = 'anotherValue'
                static getaProperty(CategoryTestHelper self) { return aVal }
                static void setaProperty(CategoryTestHelper self, newValue) { aVal = newValue }
            }

            def cth = new CategoryTestHelper()
            cth.aProperty = 'aValue'
            assert cth.aProperty == 'aValue'
            use(CategoryTestHelperPropertyReplacer) {
                assert cth.aProperty == 'anotherValue'
                cth.aProperty = 'this is boring'
                assert cth.aProperty == 'this is boring'
            }
            assert cth.aProperty == 'aValue'
        '''
    }

    // GROOVY-11820
    @Test
    void testCategoryMethodHiddenByPropertyAccessMethod() {
        assertScript '''import java.lang.reflect.Field
            class Pogo {
                public float field
            }

            Field field = Pogo.fields.first()
            assert field.getType() == float
            assert field.type == float

            @Category(Field)
            class FieldCat {
                def getType() { 'override' }
            }

            use(FieldCat) {
                assert field.getType() == 'override'
                assert field.type == 'override'
            }

            @Category(Object)
            class ObjectCat {
                def getType() { 'override' }
            }

            use(ObjectCat) { // class method is closer than category method
                assert field.getType() == float
              //assert field.type == float TODO
            }
        '''
    }

    @Test
    void testCategoryHiddenByClassMethod() {
        assertScript '''
            class A {
            }
            class B extends A {
                def m() { 1 }
            }
            class C {
                static m(A a) { 2 }
            }

            def b = new B()
            use(C) {
                assert b.m() == 1
            }
        '''
    }

    @Test
    void testCategoryOverridingClassMethod() {
        assertScript '''
            class A {
                def m() { 1 }
            }
            class C {
                static m(A a) { 2 }
            }

            def a = new A()
            use(C) {
                assert a.m() == 2
            }
        '''
    }

    @Test
    void testCategoryOverridingClassMethod2() {
        assertScript '''
            class A {
                def m() { 1 }
            }
            class B extends A {
            }
            class C {
                static m(A a) { 2 }
            }

            def a = new B()
            use(C) {
                assert a.m() == 2
            }
        '''
    }

    @Test
    void testCategoryWithMixedOverriding() {
        assertScript '''
            class A {
                def m() { 0 }
            }
            class B extends A {
                def m() { 1 }
            }
            class C {
                static m(A a) { 2 }
            }

            def b = new B()
            use(C) {
                assert b.m() == 1
            }
        '''
    }

    @Test
    void testCategoryInheritance() {
        assertScript '''
            class Foo {
                static Object foo(Object obj) {
                    'Foo.foo()'
                }
            }
            class Bar extends Foo {
                static Object bar(Object obj) {
                    'Bar.bar()'
                }
            }

            def obj = new Object()
            use(Foo) {
                assert obj.foo() == 'Foo.foo()'
            }
            use(Bar) {
                assert obj.bar() == 'Bar.bar()'
                assert obj.foo() == 'Foo.foo()'
            }
        '''
    }

    // GROOVY-8433
    @Test
    void testCategoryAnnotationAndAIC() {
        assertScript '''
            @Category(Number)
            class NumberCategory {
                def m() {
                    String variable = 'works'
                    new Object() { // "Cannot cast object '1' with class 'java.lang.Integer' to class 'NumberCategory'" due to implicit "this"
                        String toString() { variable }
                    }
                }
            }

            use(NumberCategory) {
                String result = 1.m()
                assert result == 'works'
            }
        '''
    }

    // GROOVY-5248
    @Test
    void testNullReceiverChangeForPOJO() {
        // this test will call a method using a POJO while a category is active
        // in call site caching this triggers the usage of POJOMetaClassSite,
        // which was missing a null check for the receiver. The last foo call
        // uses null to exactly check that path. I use multiple calls with foo(1)
        // before to ensure for example indy will do the right things as well,
        // since indy may need more than one call here.
        assertScript '''
            class C {
                static findAll(Integer x, Closure c) { 1 }
            }
            def foo(x) {
                x.findAll { -> }
            }

            use(C) {
                assert foo(1) == 1
                assert foo(1) == 1
                assert foo(1) == 1
                assert foo() == []
                assert foo(1) == 1
                assert foo(1) == 1
                assert foo(1) == 1
            }
        '''
    }

    @Test
    void testCallToPrivateMethod1() {
        assertScript '''
            class A {
                private foo() { 1 }
                def baz() { foo() }
            }
            class B extends A {
            }
            class C {
            }

            use(C) {
                assert new B().baz() == 1
            }
        '''
    }

    // GROOVY-6263
    @Test
    void testCallToPrivateMethod2() {
        assertScript '''
            class A {
                private foo(a) { 1 }
                def baz() { foo() }
            }
            class B extends A {
            }
            class C {
            }

            use(C) {
                assert new B().baz() == 1
            }
        '''
    }

    // GROOVY-5453
    @Test
    void testOverloadedGetterMethod1() {
        assertScript '''
            class C {
                static getFoo(String s) { 'String' }
                static getFoo(CharSequence s) { 'CharSequence' }
            }

            use(C) {
                assert 'abc'.getFoo() == 'String'
                assert 'abc'.foo      == 'String'
            }
        '''
    }

    // GROOVY-10214
    @Test
    void testOverloadedGetterMethod2() {
        assertScript '''
            class C {
                static String getFoo(Boolean self) {
                    'Boolean'
                }
                static String getFoo(Byte    self) {
                    'Byte'
                }
                static String getFoo(Short   self) {
                    'Short'
                }
                static String getFoo(Integer self) {
                    'Integer'
                }
                static String getFoo(Long    self) {
                    'Long'
                }
                static String getFoo(Float   self) {
                    'Float'
                }
                static String getFoo(Double  self) {
                    'Double'
                }
            }

            use(C) {
                assert 123.foo == 'Integer'
                assert 4.5d.foo == 'Double'
            }
        '''
    }

    // GROOVY-10743
    @Test
    void testStaticMethodOnInterface() {
        assertScript '''
            use(java.util.stream.Stream) {
                assert [1, 1].iterate(f -> [f[1], f.sum()]).limit(8).toList()*.head() == [1, 1, 2, 3, 5, 8, 13, 21]
                assert 16.iterate(n -> n < 500, n -> n * 2).toList() == [16, 32, 64, 128, 256]
            }
        '''
    }

    // GROOVY-11813
    @Test
    void testCategoryOperatorMethodAndCustomMetaClass() {
        assertScript '''
            import groovy.MetaClassCreator
            import groovy.time.TimeCategory

            GroovySystem.metaClassRegistry.metaClassCreationHandle = new MetaClassCreator()

            use(TimeCategory) {
                def date = new Date()
                def duration = 7.months
                return (date - duration)
            }
        '''
    }

    //--------------------------------------------------------------------------

    static class X     { def bar() {1} }
    static class XCat  { static bar(X x) {2} }
    static class XCat2 { static bar(X x) {3} }
    static class XCat3 { static methodMissing(X x, String name, args) {4} }
    static class XCat4 { static propertyMissing(X x, String name) {'works'} }

    def foo(x) { x.bar() }

    @Test
    void testMethodHiding1() {
        def x = new X()
        assert foo(x) == 1
        use(XCat) {
            assert foo(x) == 2
            def t = Thread.start {assert foo(x)==1}
            t.join()
        }
        assert foo(x) == 1
        def t = Thread.start {use(XCat2){assert foo(x)==3}}
        t.join()
        assert foo(x) == 1
    }

    @Test
    void testMethodHiding2() {
        def x = new X()
        assert foo(x) == 1
        use(XCat) {
            assert foo(x) == 2
            def t = Thread.start {use(XCat2){assert foo(x)==3}}
            t.join()
            assert foo(x) == 2
            t = Thread.start {assert foo(x)==1}
            t.join()
        }
        assert foo(x) == 1
        def t = Thread.start {use(XCat2){assert foo(x)==3}}
        t.join()
        assert foo(x) == 1
    }

    // GROOVY-3867
    @Test
    void testMethodMissing() {
        def x = new X()
        assert foo(x) == 1
        use(XCat3) {
            assert foo(x) == 1 // regular foo() is not affected by methodMissing in category
            assert x.baz() == 4 // XCat3.methodMissing is called
        }
        assert foo(x) == 1
        def t = Thread.start {use(XCat3){assert x.baz()==4}}
        t.join()
        assert foo(x) == 1
        assertThrows(MissingMethodException) {
            x.baz()
        }
    }

    // GROOVY-3867
    @Test
    void testMethodMissingNoStatic() {
        def x = new X()
        use(XCat3) {
            assert x.baz() == 4 // XCat3.methodMissing is called for instance
            assertThrows(MissingMethodException) {
                assert X.baz() != 4 // XCat3.methodMissing should not be called for static method of X
            }
        }
    }

    // GROOVY-3867, GROOVY-10783
    @Test
    void testPropertyMissing() {
        def x = new X()
        assertThrows(MissingPropertyException) {
            assert x.baz != 'works' // accessing x.baz should throw MPE
        }
        use(XCat4) {
            assert x.baz == 'works'
        }
        assertThrows(MissingPropertyException) {
            assert x.baz != 'works' // accessing x.baz should throw MPE
        }
    }
}
