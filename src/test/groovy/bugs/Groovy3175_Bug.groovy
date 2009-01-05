class Groovy3175_Bug extends GroovyTestCase {
   void testSyntheticModifier() {
     assertScript """
        class MyService {
            private fio
            def thing
            def something() { }
            def anotherSomething() { assert true }
        }
        def isNotSynthetic(o) {
          return (o.modifiers & 0x1000) == 0
        }
        assert MyService.getDeclaredFields().grep {isNotSynthetic(it)}.size() == 1 
     """
   } 
}