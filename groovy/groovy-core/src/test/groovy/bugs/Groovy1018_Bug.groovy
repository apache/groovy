// Test to fix the issue GROOVY-1018

package groovy.bugs

class Groovy1018_Bug extends GroovyTestCase { 

    public static Object Class = "bar" 

    void testAccessPublicStaticField() {
        def a = new Groovy1018_Bug()
        println( a.Class )
        println( a.@Class )
        println( Groovy1018_Bug.@Class )
        assert a.Class == "bar" && a.@Class == "bar"
        assert Groovy1018_Bug.@Class == "bar"
    }

} 
