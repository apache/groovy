

/** 
 * Tests the use of properties in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class PropertyTest extends Test {

    static class ClassWithConciseProperties {
        property count = 1
        property blah = "abc"
    }
	
    static class ClassWithMediumConciseProperties {
        property count {
            get() {
                return this.count
            }
            set() {
                this.count = value
            }
        } = 1
            
        property blah { 
            get() {
                return this.blah
            }
            set() {
                this.blah = value
            }
        } = "abc"
    }
	
    static class ClassWithJavaVerboseProperties {
        count = 1
        blah = "abc"
        
        getCount() {
            return this.count
        }
        
        setCount(value) {
            this.count = value
        }
        
        getBlah() {
            return this.blah
        }
        
        setBlah(value) {
            this.blah = value
        }
    }
	
	
    testConciseProperties() { 
        testProperties(ClassWithConciseProperties())
    }
	
    testConciseProperties() { 
        testProperties(ClassWithMediumConciseProperties())
    }
	
    testJavaVerboseProperties() { 
        testProperties(ClassWithJavaVerboseProperties())
    }
	
	
    protected testProperties(foo) {
        assertEquals(foo.count, 1)
        assertEquals(foo.getCount(), 1)
        assertEquals(foo.blah, "abc")
        assertEquals(foo.getBlah(), "abc")
        
		foo.count = 7
		
        assertEquals(foo.count, 7)
        assertEquals(foo.getCount(), 7)

		foo.setCount(12)
		
        assertEquals(foo.count, 12)
        assertEquals(foo.getCount(), 12)
		        
        foo.name = "Bob"
        
        assertEquals(foo.name, "Bob")
        assertEquals(foo.getName(), "Bob")

        foo.setName("James")
        
        assertEquals(foo.name, "James")
        assertEquals(foo.getName(), "James")
    }
}