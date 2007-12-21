package groovy.bugs

class Groovy1617_Bug extends GroovyTestCase {
   void testCoerceStringIntoStringArray() {
      def expected = ["G","r","o","o","v","y"] as String[]
      def actual = "Groovy" as String[]
      assert expected == actual
   }

   void testCoerceGStringIntoStringArray() {
      def expected = ["G","r","o","o","v","y"] as String[]
      def a = "Gro"
      def b = "ovy"
      // previously returned ["Groovy"]
      def actual = "$a$b" as String[]
      assert expected == actual
   }
}
