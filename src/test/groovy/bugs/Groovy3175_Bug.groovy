package groovy.bugs

public class Groovy3175_Bug extends GroovyTestCase {

   def getJavaVersionMajorMinor() { (System.getProperty('java.version') =~ /^\d+\.?\d*/)[0] as BigDecimal }

   void testSyntheticModifier() {
     if (getJavaVersionMajorMinor() < 1.5)
        return
        
     assertScript """
        class MyService {
            private fio
            def thing
            def something() { }
            def anotherSomething() { assert true }
        }
        def fields = MyService.getDeclaredFields().grep { !it.synthetic }
        assert fields.size() == 2 
        def methods = MyService.getDeclaredMethods().grep { !it.synthetic }
        assert methods.size() == 4 
     """
   } 
}
