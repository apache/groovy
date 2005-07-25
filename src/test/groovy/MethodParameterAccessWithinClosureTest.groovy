/**
 * To test access to method scoped variable within closure
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class MethodParameterAccessWithinClosureTest extends GroovyTestCase { 
    def cheese
    def shop
       
    void setUp() {
        cheese = null
        shop = ["wensleydale"]
    }              
    void testSimpleMethodParameterAccess() { 
        assert "wensleydale" == vendor1("wensleydale") 
    }
    void testMethodParameterWithDifferentNameToPropertyUsingClosure() {
        assert "wensleydale" == vendor2("wensleydale")
    }
    void testMethodParameterWithSameNameAsPropertyUsingClosure() {
        //@todo fails in 1.0b6   
        println vendor3("wensleydale")
        // assert "wensleydale" == vendor3("wensleydale")
    }
    
    private String vendor1(cheese) {
        cheese
    }
    
    private String vendor2(aCheese) {
        shop.find() {it == aCheese}
    }
    
    private String vendor3(cheese) {
        // problem is that cheese here refers to def 'cheese'
        // and not the method parameter 'cheese'
        println "shop = $shop"
        println "cheese = $cheese"
        def a  = shop.find() {println (it == cheese)}
        println ([1, 2, 3].find() {it == 2})
        println (["wensleydale"].find() {it == "wensleydale"})
        println (shop.find() {it == "wensleydale"})
        println (shop.find() {it == cheese})
        println "a = $a"
    }
} 
