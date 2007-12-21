package groovy

/** 
 * was: Tests the use of new def methods in Groovy: eachProperty(), eachPropertyName(), and
 * allProperties().
 * New Method: getMetaPropertyValues
 * Method name has changed: getProperties
 * Remove: eachProperty(), eachPropertyName() use properties.each {key,value -> } instead
 *
 * @author john stump
 * @author dierk koenig
 * @version $Revision$
 */
class Property2Test extends GroovyTestCase {

    void testEachPropertyName() {
        def foo = new Foo()
		
		// these are the properties that should be there
		def props = ['name', 'count', 'location', 'blah']
		foo.properties.each { name, value ->
			//println "looking for ${prop} in ${props}"

			// todo: GROOVY-996
                                    // We should see protected properties, but not  private ones.
			assert name != "invisible"

			// remove this one from the list
			props = props - [name]
		}

		// make sure there are none left over
		//println "count left in props list is ${props.count()}"
		assert props.count() == 0
    }

    void testMetaPropertyValuesFromObject() {
        def foo = new Foo()
		def metaProps = foo.metaPropertyValues
		assert metaProps[0] instanceof PropertyValue
		assertNotNull metaProps[0].name
		assertNotNull metaProps[0].value
		assertNotNull metaProps[0].type
    }

	void testEachProperty() {
        def foo = new Foo()

		// these are the properties and their values that should be there
		def props = ['name':'James', 'count':1, 'location':'London', 'blah':9]
		foo.properties.each { name, value ->
			//println "looking for ${prop.name} in ${props}"
			
			// todo: GROOVY-996
                                    // We should see protected properties, but not  private ones.
			assert name != "invisible"
			
			def pvalue = props[name]
			if(pvalue != null)
				assert pvalue == value
			
			// remove this one from the map
			props.remove(name)
		}
		
		// make sure there are none left over
		//println "count left in props map is ${props.size()}"
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
		foo.properties.each { name, value -> props -= [name] }
		
		// there should be none left
		//println props
		assert props.size() == 0
    }
}

