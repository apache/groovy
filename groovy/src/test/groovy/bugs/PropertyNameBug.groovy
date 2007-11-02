package groovy.bugs

/**
 * Test to fix the issue GROOVY-843.
 *
 * @author Pilho Kim
 * @version $Revision$
 */

public class PropertyNameBug extends GroovyTestCase {
    void testNonJavaIdentifierChacactersWithJavaSyntax() {
        Map map = new HashMap()
        map.put("foo.bar", "FooBar")
        map.put("foo.bar-bar", "FooBar-Bar")
        map.put("foo.=;&|^*-+-/\\'?.*:arbitrary()[]{}%#@!", "Any character")

        println("foo.bar1 = ${map.get("foo.bar1")}")
        println("foo.bar-bar = ${map.get("foo.bar-bar")}")
        println("Specical Character Test: ${map.get("foo.=;&|^*-+-/\\'?.*:arbitrary()[]{}%#@!")}")
    }

    void testNonJavaIdentifierChacactersWithGroovySyntax() {
        def map = [:]
        map."foo.bar" = "FooBar"
        map."foo.bar-bar" = "FooBar-Bar"
        map."foo.=;&|^*-+-/\\'?.*:arbitrary()[]{}%#@!" = "Any character"

        println("foo.bar1 = ${map."foo.bar1"}")
        println("foo.bar-bar = ${map."foo.bar-bar"}")
        println("Specical Character Test: ${map."foo.=;&|^*-+-/\\'?.*:arbitrary()[]{}%#@!"}")
    }
}


