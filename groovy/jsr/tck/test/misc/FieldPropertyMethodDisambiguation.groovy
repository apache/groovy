class FieldPropertyMethodDisambiguation extends GroovyTestCase {
  String bar = "field"

  String getBar() {
      return "property"
  }

  String bar() {
      return "method"
  }


  String bar(param) {
      return "method with param: " + param
  }


  void testCase() {
      assert bar == "property"
      assert this.bar == "property"

      assert bar() == "method"
      assert this.bar() == "method"

      // assert @foo == "field"  // @foo is Java 5 annotation; use this.@foo
      assert this.@foo == "field"

      assert bar(1) == "method with param: 1"

  }
}