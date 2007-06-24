package groovy

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
        assert "wensleydale" == vendor3("wensleydale")
    }
    
    void testOptionalMethodParameterUsedInClosure() {
        assert "wensleydale" == vendor4("wensleydale")
        assert null == vendor4()
    }
    
    void testDoubleParameterAndsingleParameterUsedInClosure() {
         assert vendor5(5.0d,2) == 7.0d
    }
    
    private String vendor1(cheese) {
        cheese
    }
    
    private String vendor2(aCheese) {
        shop.find() {it == aCheese}
    }
    
    private String vendor3(cheese) {
        shop.find() {it == cheese}
    }
    
    /** note: cheese is a field, that is intended **/
    private vendor4(aCheese=cheese) {
        shop.find() {it == aCheese}
    }
    
    private vendor5(double a, int b) {
        b.times {a++}
        return a
    }
}