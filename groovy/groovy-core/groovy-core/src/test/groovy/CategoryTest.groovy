package groovy

class CategoryTest extends GroovyTestCase {

    void setUp() {
        def dummy = null
        CategoryTestPropertyCategory.setSomething(dummy, 'hello')
        CategoryTestHelperPropertyReplacer.setAProperty(dummy, 'anotherValue')
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
}

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
    static getAProperty(CategoryTestHelper self) { return aVal }
    static void setAProperty(CategoryTestHelper self, newValue) { aVal = newValue }
}