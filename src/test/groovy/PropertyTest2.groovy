/** 
 * Tests the use of new def methods in Groovy: eachProperty(), eachPropertyName(), and
 * allProperties()
 * 
 * @author john stump
 * @version $Revision$
 */
class PropertyTest2 extends GroovyTestCase {

    void testEachPropertyName() {
        def foo = new Foo()
		
		// these are the properties that should be there
		def props = ['name', 'count', 'location', 'blah']
		foo.eachPropertyName { prop ->
			//println "looking for ${prop} in ${props}"

			// we should not see private or protected properties
			assert prop != "invisible"
			assert prop != "prot"

			// remove this one from the list
			props = props - [prop]
		}

		// make sure there are none left over
		//println "count left in props list is ${props.count()}"
		assert props.count() == 0
    }

	void testEachProperty() {
        def foo = new Foo()

		// these are the properties and their values that should be there
		def props = ['name':'James', 'count':1, 'location':'London', 'blah':9]
		foo.eachProperty { prop ->
			//println "looking for ${prop.name} in ${props}"
			
			// we should not see private or protected properties
			assert prop.name != "invisible"
			assert prop.name != "prot"
			
			def value = props[prop.name]
			if(value != null)
				assert prop.value == value
			
			// remove this one from the map
			props.remove(prop.name)
		}
		
		// make sure there are none left over
		//println "count left in props map is ${props.size()}"
		assert props.size() == 0
	}
	
	void testAllProperties() {
        def foo = new Foo()
		
		// these are the properties that should be there
		def props = ['name', 'count', 'location', 'blah']
		
		foo.allProperties().each { props -= [it.name] }
		
		// there should be none left
		//println props
		assert props.size() == 0
	}
	
	// make sure allProperties() works with expando objects too
    void testAllPropertiesExpando() {
        def foo = new Expando()
		
		foo.name = 'John'
		foo.location = 'Colorado'
		foo.count = 23
		foo.blah = true
		
		// these are the properties that should be there
		def props = ['name', 'count', 'location', 'blah']
		foo.allProperties().each { props -= [it.name] }
		
		// there should be none left
		//println props
		assert props.size() == 0
    }
}

