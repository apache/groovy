class Groovy3175_Bug extends GroovyTestCase {
   void testSyntheticModifier() {
     assertScript """
        class MyService {
            private fio
            def thing
            def something() { }
            def anotherSomething() { assert true }
        }
        assert MyService.getDeclaredFields().grep {!it.synthetic}.size() == 1 
     """
   } 
}