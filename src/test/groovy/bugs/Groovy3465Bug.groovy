package groovy.bugs

import static groovy.bugs.Groovy3465Helper.*

class Groovy3465Bug extends GroovyTestCase {
    void testCallingAStaticImportedMethodWithNamedParamaters() {

    	func text: 'Some text', value: 1
    	
    	func(text: 'Some text', value: 1)
    	
    	func([text: 'Some text', value: 1])
    }
}
