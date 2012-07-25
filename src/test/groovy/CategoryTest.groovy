package groovy

class CategoryTest extends GroovyTestCase {

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

}

class X{ def bar(){1}}
class XCat{ static bar(X x){2}}
class XCat2{ static bar(X x){3}}

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
