/** 
 * Tests the use of properties in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class PropertyTest extends Test {

    testNormalProperty() {
        
        foo = Foo()
        
        assertEquals(foo.count, 1)
        assertEquals(foo.getCount(), 1)
        assertEquals(foo.blah, 7)
        assertEquals(foo.getBlah(), 7)
        
        assertEquals(foo.name, "James")
        assertEquals(foo.getName(), "James")

		foo.count = 7
		
        assertEquals(foo.count, 7)
        assertEquals(foo.getCount(), 7)
		        
        foo.name = "Bob"
        
        assertEquals(foo.name, "Bob!")
        assertEquals(foo.getName(), "Bob!")
    }

	testCannotSeePrivateProperties() {
	    foo = Foo()
	    
	    // @todo should property access return null for invisible stuff?
	    assertEquals(foo.q, null)
	    
	    // @todo should methods fail on non-existent method calls
	    shouldFail( {
	    	foo.getQ()
	    })
	}

}