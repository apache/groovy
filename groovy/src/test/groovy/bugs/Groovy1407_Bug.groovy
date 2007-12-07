package groovy.bugs

class Groovy1407_Bug extends GroovyTestCase {
   void testGPathOnMultiKeyMap(){
      // each key is a two-element String list
      // each value is a two-element integer list
      def map = [['a','b']:[2,34],['c','d']:[2,16],['e','f']:[3,97],['g','h']:[4,48]]
      def expected = [["a", "b"],["c", "d"],["e", "f"],["g", "h"]]
      // previous returned value was [a, b, g, h, e, f, c, d]
      // i.e, expanded
      def actual = map.entrySet().key
      assert expected == actual
   }

   void testGPathOnMultiValueMap(){
      // each key is a two-element String list
      // each value is a two-element integer list
      def map = [['a','b']:[2,34],['c','d']:[2,16],['e','f']:[3,97],['g','h']:[4,48]]
      def expected = [[2, 34],[2, 16],[3, 97],[4, 48]]
      // previous returned value was [2, 34, 4, 48, 3, 97, 2, 16]
      // i.e, expanded
      def actual = map.entrySet().value
      assert expected == actual
   }
}
