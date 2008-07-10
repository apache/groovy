package org.codehaus.groovy.classgen

class MetaClassTest extends GroovyTestCase {
    
    void testMetaClass() {
        test(this)
        test { print(it) }
    }
    
    protected def test(object) {
        def metaClass = object.metaClass
        assert metaClass != null
        
        println(metaClass)
        
        def classNode = metaClass.getClassNode()
        assert classNode != null

        println(classNode)
        
        def name = object.getClass().getName()
        assert classNode.name == name
    }
    
	void testMetaClassDefinition() {
		assertScript """
			class Foo {
		    	MetaClass metaClass
			} 
			def foo = new Foo()
			assert foo.@metaClass != null
			"""
	}
}
