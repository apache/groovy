package groovy.bugs

public class Groovy3175_Bug extends GroovyTestCase {
   void testSyntheticModifier() {
     if (((System.getProperty('java.version') =~ /^\d+\.?\d*/)[0] as BigDecimal) < 1.5)
        return
        
     assertScript """
        class MyService {
            private fio
            def thing
            def something() { }
            def anotherSomething() { assert true }
        }
        def methods = MyService.getDeclaredFields().grep { !it.synthetic }
        println methods
        println methods.size()
        assert methods.size() == 1 
     """
   } 
}
