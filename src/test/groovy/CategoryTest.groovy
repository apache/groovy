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

import groovy.test.GroovyTestCase

final class CategoryTest extends GroovyTestCase {

    void setUp() {
        def dummy = null
        CategoryTestPropertyCategory.setSomething(dummy, 'hello')
        CategoryTestHelperPropertyReplacer.setaProperty(dummy, 'anotherValue')
    }

    void testCategories() {
        use (StringCategory) {
            assert "Sam".lower() == "sam";
            use (IntegerCategory.class) {
                assert "Sam".lower() == "sam";
                assert 1.inc() == 2;
            }
            shouldFail(MissingMethodException, { 1.inc() });
        }
        shouldFail(MissingMethodException, { "Sam".lower() });
    }

    void testReturnValueWithUseClass() {
        def returnValue = use(StringCategory) {
            "Green Eggs And Ham".lower()
        }
        assert "green eggs and ham" == returnValue
    }

    void testReturnValueWithUseList() {
        def returnValue = use([StringCategory, IntegerCategory]) {
            "Green Eggs And Ham".lower() + 5.inc()
        }
        assert "green eggs and ham6" == returnValue
    }

    void testCategoryDefinedProperties() {
        use(CategoryTestPropertyCategory) {
            assert getSomething() == "hello"
            assert something == "hello"
            something = "nihao"
            assert something == "nihao"
        }

        // test the new value again in a new block
        use(CategoryTestPropertyCategory) {
            assert something == "nihao"
        }
    }

    void testCategoryReplacedPropertyAccessMethod() {
        def cth = new CategoryTestHelper()
        cth.aProperty = "aValue"
        assert cth.aProperty == "aValue"
        use (CategoryTestHelperPropertyReplacer) {
            assert cth.aProperty == "anotherValue"
            cth.aProperty = "this is boring"
            assert cth.aProperty == "this is boring"
        }
        assert cth.aProperty == "aValue"
    }

    void testCategoryHiddenByClassMethod() {
      assertScript """
         class A{}
         class B extends A{def m(){1}}
         class Category{ static m(A a) {2}}
         def b = new B()
         use (Category) {
           assert b.m() == 1
         }
      """
    }

    void testCategoryOverridingClassMethod() {
      assertScript """
         class A {def m(){1}}
         class Category{ static m(A a) {2}}
         def a = new A()
         use (Category) {
           assert a.m() == 2
         }
      """
      assertScript """
         class A {def m(){1}}
         class B extends A{}
         class Category{ static m(A a) {2}}
         def a = new B()
         use (Category) {
           assert a.m() == 2
         }
      """
    }

    void testCategoryWithMixedOverriding() {
      assertScript """
         class A{def m(){0}}
         class B extends A{def m(){1}}
         class Category{ static m(A a) {2}}
         def b = new B()
         use (Category) {
           assert b.m() == 1
         }
      """
    }

    void testCategoryInheritance() {
      assertScript """
        public class Foo {
          static Object foo(Object obj) {
            "Foo.foo()"
          }
        }

        public class Bar extends Foo{
          static Object bar(Object obj) {
            "Bar.bar()"
          }
        }

        def obj = new Object()

        use(Foo){
          assert obj.foo() == "Foo.foo()"
        }

        use(Bar){
          assert obj.bar() == "Bar.bar()"
          assert obj.foo() == "Foo.foo()"
        }
      """
    }

    void testNullReceiverChangeForPOJO() {
        // GROOVY-5248
        // this test will call a method using a POJO while a category is active
        // in call site caching this triggers the usage of POJOMetaClassSite,
        // which was missing a null check for the receiver. The last foo call
        // uses null to exaclty check that path. I use multiple calls with foo(1)
        // before to ensure for example indy will do the right things as well,
        // since indy may need more than one call here.
        assertScript """
            class Cat {
              public static findAll(Integer x, Closure cl) {1}
            }

             def foo(x) {
                 x.findAll {}
             }

             use (Cat) {
                 assert foo(1) == 1
                 assert foo(1) == 1
                 assert foo(1) == 1
                 assert foo(null) == []
                 assert foo(1) == 1
                 assert foo(1) == 1
                 assert foo(1) == 1
             }
        """
    }


    def foo(x){x.bar()}
    void testMethodHiding1() {
        def x = new X()
        assert foo(x) == 1
        use (XCat) {
	        assert foo(x) == 2
	        def t = Thread.start {assert foo(x)==1}
	        t.join()
        }
        assert foo(x) == 1
        def t = Thread.start {use (XCat2){assert foo(x)==3}}
        t.join()
        assert foo(x) == 1
    }

    void testMethodHiding2() {
        def x = new X()
        assert foo(x) == 1
        use (XCat) {
	        assert foo(x) == 2
        	def t = Thread.start {use (XCat2){assert foo(x)==3}}
        	t.join()
        	assert foo(x) == 2
	        t = Thread.start {assert foo(x)==1}
	        t.join()
        }
        assert foo(x) == 1
        def t = Thread.start {use (XCat2){assert foo(x)==3}}
        t.join()
        assert foo(x) == 1
    }

    void testCallToPrivateMethod1() {
        assertScript '''
            class A {
                private foo() { 1 }
                def baz() { foo() }
            }

            class B extends A {}

            class C {}

            use(C) {
                assert new B().baz() == 1
            }
        '''
    }

    // GROOVY-6263
    void testCallToPrivateMethod2() {
        assertScript '''
            class A {
                private foo(a) { 1 }
                def baz() { foo() }
            }

            class B extends A {}

            class C {}

            use(C) {
                assert new B().baz() == 1
            }
        '''
    }

    // GROOVY-3867
    void testPropertyMissing() {
        def x = new X()

        shouldFail(MissingPropertyException) {
            assert x.baz != "works" // accessing x.baz should throw MPE
        }

        use(XCat4) {
            assert x.baz == "works"
        }

        shouldFail(MissingPropertyException) {
            assert x.baz != "works" // accessing x.baz should throw MPE
        }
    }

    // GROOVY-3867
    void testMethodMissing() {
        def x = new X()
        assert foo(x) == 1
        use (XCat3) {
            assert foo(x) == 1 // regular foo() is not affected by methodMissing in category
            assert x.baz() == 4 // XCat3.methodMissing is called
        }
        assert foo(x) == 1
        def t = Thread.start {use (XCat3){assert x.baz()==4}}
        t.join()
        assert foo(x) == 1
        shouldFail(MissingMethodException) {
            x.baz()
        }
    }

    // GROOVY-3867
    void testMethodMissingNoStatic() {
        def x = new X()
        use (XCat3) {
            assert x.baz() == 4 // XCat3.methodMissing is called for instance
            shouldFail(MissingMethodException) {
                assert X.baz() != 4 // XCat3.methodMissing should not be called for static method of X
            }
        }
    }
}

class X{ def bar(){1}}
class XCat{ static bar(X x){2}}
class XCat2{ static bar(X x){3}}
class XCat3{ static methodMissing(X x, String name, args) {4}}
class XCat4{ static propertyMissing(X x, String name) {"works"}}

class StringCategory {
    static String lower(String string) {
        return string.toLowerCase();
    }
}

class IntegerCategory {
    static Integer inc(Integer i) {
        return i + 1;
    }
}

class CategoryTestPropertyCategory {
    private static aVal = "hello"
    static getSomething(Object self) { return aVal }
    static void setSomething(Object self, newValue) { aVal = newValue }
}

class CategoryTestHelper {
    def aProperty = "aValue"
}

class CategoryTestHelperPropertyReplacer {
    private static aVal = "anotherValue"
    static getaProperty(CategoryTestHelper self) { return aVal }
    static void setaProperty(CategoryTestHelper self, newValue) { aVal = newValue }
}
