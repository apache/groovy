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
}
