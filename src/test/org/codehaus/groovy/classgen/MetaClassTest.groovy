package org.codehaus.groovy.classgen

class MetaClassTest extends GroovyTestCase {
    
    void testMetaClass() {
        test(this)
        test {i| i.print() }
    }
    
    protected test(object) {
        metaClass = object.metaClass
        assert metaClass != null
        
        metaClass.println()
        
        classNode = metaClass.getClassNode()
        assert classNode != null

        classNode.println()
        
        name = object.getClass().getName()
        assert classNode.name == name
    }
}
