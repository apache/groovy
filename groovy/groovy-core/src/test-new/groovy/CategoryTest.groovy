class CategoryTest extends GroovyTestCase {

  void testCategories() {
    use (StringCategory) {
      assert "Sam".lower() == "sam";
      use (IntegerCategory) {
        assert "Sam".lower() == "sam";
        assert 1.inc() == 2;
      }
        shouldFail(MissingMethodException, { 1.inc() });
    }
    shouldFail(MissingMethodException, { "Sam".lower() });
  }

  static void main(args) {
    t = new CategoryTest();
    t.testCategories();
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