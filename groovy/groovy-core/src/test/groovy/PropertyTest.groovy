/** 
 * Tests the use of properties in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class PropertyTest extends GroovyTestCase {

    void testNormalPropertyGettersAndSetters() {
        
        println("About to create Foo")
        
        foo = new Foo()

        println("created ${foo}")
        
        value = foo.getMetaClass()
        
        println("metaClass ${value}")
        
        println(foo.inspect())
        
        println("name ${foo.name}, blah ${foo.blah}")
        
        assert foo.name == "James"
        assert foo.getName() == "James"
        
        assert foo.location == "London"
        assert foo.getLocation() == "London"
        
        assert foo.blah == 9
        assert foo.getBlah() == 9
        
        foo.name = "Bob"
        foo.location = "Atlanta"
        
        assert foo.name == "Bob"
        assert foo.getName() == "Bob"
        
        assert foo.location == "Atlanta"
        assert foo.getLocation() == "Atlanta"
    }
    
    void testOverloadedGetter() {
        
        foo = new Foo()

        println("count ${foo.count}")
        
        /* @todo
        assert foo.getCount() == 1
        assert foo.count == 1
        */
        
        foo.count = 7
        
        assert foo.count == 7
        assert foo.getCount() == 7
    }

    void testNoSetterAvailableOnPrivateProperty() {
        foo = new Foo()
        
        // methods should fail on non-existent method calls
        shouldFail { foo.blah = 4 }
        shouldFail { foo.setBlah(4) }
    }
    
    void testCannotSeePrivateProperties() {
	    foo = new Foo()
	    
	    // property access fails on non-existent property
		shouldFail { x = foo.invisible }														
	    
	    // methods should fail on non-existent method calls
	    shouldFail { foo.getQ() }
	}

}