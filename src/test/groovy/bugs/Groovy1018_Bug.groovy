package groovy.bugs

/**
 * Test to fix the Jira issues GROOVY-1018 and GROOVY-732.
 * Access to a static field member by a class name:
 *      ClassName.fieldName or ClassName.@fieldName.
 *
 * @author Pilho Kim
 * @version $Revision$
 */

class Groovy1018_Bug extends GroovyTestCase { 

    public static Object Class = "bar" 

    // todo: GROOVY-1018
    void testGetPublicStaticField() {
        Groovy1018_Bug.Class = 'bar'
        def a = new Groovy1018_Bug()
        println( a.Class )
        println( a.@Class )
        println( Groovy1018_Bug.Class )
        println( Groovy1018_Bug.@Class )
        assert a.Class == "bar" && a.@Class == "bar"
        assert Groovy1018_Bug.Class == "bar" && Groovy1018_Bug.@Class == "bar"
    }

    // todo: GROOVY-732
    void testSetPublicStaticField() {
        Groovy1018_Bug.Class = 'bar-'
        assert Groovy1018_Bug.Class == "bar-" && Groovy1018_Bug.@Class == "bar-"
    }

} 
