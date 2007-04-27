/**
 * @author Pilho Kim
 * @version $Revision$
 */

package groovy.bugs

class AttributeSetExpressionBug extends GroovyTestCase {
    void testAttributeSetAccess() {
        def a = new HasStaticFieldSomeClass()
        a.name = a.name * 3
        assert a.@name == "gettter" * 3 
        assert a.name == "gettter"

        new HasStaticFieldSomeClass().@name = "changed bar"
        assert( HasStaticFieldSomeClass.class.@name == "changed bar" )

        HasStaticFieldSomeClass.class.@name = "changed static bar"
        assert( HasStaticFieldSomeClass.class.@name == "changed static bar" )
    }
}

class HasStaticFieldSomeClass {
    static String name = "bar" 
    static String getName() {
        return "gettter"
    }
}
