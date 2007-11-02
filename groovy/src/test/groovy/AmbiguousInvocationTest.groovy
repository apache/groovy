package groovy
/**
 * to prove GROOVY-467 is no longer an issue    
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class AmbiguousInvocationTest extends GroovyTestCase {
    def dummy1, dummy2

    void setUp() {
        dummy1 = new groovy.DummyMethodsJava()
        dummy2 = new groovy.DummyMethodsGroovy()
    }

    void testAmbiguousInvocationWithFloats() {
        assert "float args" == dummy1.foo("bar", 1.0f, 2.0f)
        assert "float args" == dummy1.foo("bar", (float) 1, (float) 2)
        assert "float args" == dummy1.foo("bar", (Float) 1, (Float) 2)
        assert "float args" == dummy2.foo("bar", 1.0f, 2.0f)
        assert "float args" == dummy2.foo("bar", (float) 1, (float) 2)
        assert "float args" == dummy2.foo("bar", (Float) 1, (Float) 2)
    }

    void testAmbiguousInvocationWithInts() {
        assert "int args" == dummy1.foo("bar", 1, 2)
        assert "int args" == dummy1.foo("bar", (int) 1, (int) 2)
        assert "int args" == dummy1.foo("bar", (Integer) 1, (Integer) 2)
        assert "int args" == dummy2.foo("bar", 1, 2)
        assert "int args" == dummy2.foo("bar", (int) 1, (int) 2)
        assert "int args" == dummy2.foo("bar", (Integer) 1, (Integer) 2)
    }
} 