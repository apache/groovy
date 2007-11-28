package groovy.bugs

class Groovy1593 extends GroovyTestCase {
   void testPropertyAccessInSubClassOfHashMap() {
      def subclass = new SubClassOfHashMap()
      // any of the following caused a MPE previously
      assertNull subclass.property
      subclass.property = "value"
      assert "value" == subclass.property
   }

}

class SubClassOfHashMap extends HashMap {}
