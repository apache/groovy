/**
 * To test access to method scoped variable within closure
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class MethodParameterAccessWithinClosureTest extends GroovyTestCase { 
    property cheese
    property shop
       
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
        //assert "wensleydale" == vendor3("wensleydale")
    }
    
    private String vendor1(cheese) {
        cheese
    }
    
    private String vendor2(aCheese) {
        shop.find() {it == aCheese}
    }
    
    private String vendor3(cheese) {
        // problem is that cheese here refers to property 'cheese'
        // and not the method parameter 'cheese'
        shop.find() {it == cheese}      
    }
} 
