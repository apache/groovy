class FieldPropertyMethodDisambiguationTest extends GroovyTestCase {
  String bar = "property"

  String getBar() {
      return "propertyMethod"
  }

  String bar() {
      return "method"
  }


  String bar(param) {
      return "method with param: " + param
  }


  void testCase() {
      def answer = bar()
      assert answer == "method"
      assert this.bar() == "method"

      assert bar(1) == "method with param: 1"
      assert this.bar(1) == "method with param: 1"


      assert getBar() == "propertyMethod"
      assert this.getBar() == "propertyMethod"

      // TODO should these 2 expressions call the getter or return the field?
      assert bar == "property"
      assert this.bar == "property"


      // assert @foo == "field"  // @foo is Java 5 annotation; use this.@foo

      // TODO when the parser can handle this...
      // assert this.@foo == "field"

  }
}