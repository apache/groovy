package groovy

/**
 * to prove GROOVY-467 is no longer an issue    
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */

class AmbiguousInvocationTest extends GroovyTestCase { 
    def dummy
    
    void setUp() {
    	dummy = new DummyMethods()
    }
    
    void testAmbiguousInvocationWithFloats() {
   	assert "float args" == dummy.foo("bar",1.0f,2.0f)
   	assert "float args" == dummy.foo("bar",(float)1,(float)2)
   	assert "float args" == dummy.foo("bar",(Float)1,(Float)2)
    }
    void testAmbiguousInvocationWithInts() {
   	assert "int args" == dummy.foo("bar",1,2)
   	assert "int args" == dummy.foo("bar",(int)1,(int)2)
   	assert "int args" == dummy.foo("bar",(Integer)1,(Integer)2)
    }
} 