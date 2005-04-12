package org.codehaus.groovy.classgen

class MetaClassTest extends GroovyTestCase {
    
    void testMetaClass() {
        test(this)
        test { print(it) }
    }
    
    protected test(object) {
        metaClass = object.metaClass
        assert metaClass != null
        
        println(metaClass)
        
        classNode = metaClass.getClassNode()
        assert classNode != null

        println(classNode)
        
        name = object.getClass().getName()
        assert classNode.name == name
    }
}
