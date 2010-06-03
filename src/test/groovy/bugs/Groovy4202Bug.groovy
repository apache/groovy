package groovy.bugs

class Groovy4202Bug extends GroovyTestCase {
    void testSuccessiveMCModificationFirstClassThenInstance() {
		def inst0 = new Foo4202()
		def inst1 = new Foo4202()

		Foo4202.metaClass.addedMethod0 = { 'hello0'}

		inst0.metaClass.addedMethod1 = { 'hello10'}
		inst0.metaClass.addedMethod2 = { 'hello20'}
		
		inst1.metaClass.addedMethod1 = { 'hello11'}
		inst1.metaClass.addedMethod2 = { 'hello21'}
    }
}

class Foo4202 { }
