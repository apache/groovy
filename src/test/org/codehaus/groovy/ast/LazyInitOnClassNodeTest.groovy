import org.codehaus.groovy.ast.*

class LazyInitOnClassNodeTest extends GroovyTestCase {
    void testLazyInit() {
        def script = """
          class ClassNodeMethodsListGrowth {
            // Setting the type to ArrayList triggers a growth in methodsList
            ArrayList array1=new ArrayList()
        
            // ... using dynamic typing does not
            // def array2=new ArrayList()
            
            // Test method as a sanity check
            def get(o) { return "ok" }
          } 
        """
        def listType = ClassHelper.make(List.class)
        def last=0
        5.times {
          def loader = new GroovyClassLoader(this.class.classLoader)
          assert loader.parseClass(script).newInstance().get("default")=="ok"
          def size = listType.redirect().getMethods().size()
          if (last!=0) assert last==size 
          if (last==0) last = size
        }
    }
}

