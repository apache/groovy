/**
 * @author Pilho Kim
 * @version $Revision$
 */

package groovy.bugs

// todo: GROOVY-1025
class AttributeSetExpressionBug extends GroovyTestCase {
    void testAttributeSetAccess() {
        def a = new HasStaticFieldSomeClass()
        a.name = a.name * 3
        assert( a.name == "bar" * 3 )
        println( a.name )
        assert( a.name == "bar" * 3 )

        println( HasStaticFieldSomeClass.class instanceof Class )
        println( HasStaticFieldSomeClass.class.name )
        println( HasStaticFieldSomeClass.class.getName() )
        println( HasStaticFieldSomeClass.class.@name )

        new HasStaticFieldSomeClass().@name = "changed bar"
        println( HasStaticFieldSomeClass.class.@name )
        assert( HasStaticFieldSomeClass.class.@name == "changed bar" )

        HasStaticFieldSomeClass.class.@name = "changed static bar"
        println( HasStaticFieldSomeClass.class.@name )
        assert( HasStaticFieldSomeClass.class.@name == "changed static bar" )
    }
}

class HasStaticFieldSomeClass {
    public static String name = "bar" 
    static String getName() {
        return "gettter"
    }
}
