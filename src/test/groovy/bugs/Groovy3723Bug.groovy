package groovy.bugs

class Groovy3723Bug extends GroovyTestCase {
    void testEMCPropertyAccessWitGetPropertySetProperty() {
        assertScript """
            class Dummy3723 {}
            
            Dummy3723.metaClass.id = 1
            
            Dummy3723.metaClass.getProperty = { name ->
               def metaProperty = delegate.metaClass.getMetaProperty(name)
               return metaProperty?.getProperty(delegate)
            }
            
            Dummy3723.metaClass.setProperty = { name, value ->
               def metaProperty = delegate.metaClass.getMetaProperty(name)
               metaProperty?.setProperty(delegate,value)
            }
            
            def d = new Dummy3723()
            // was failing with groovy.lang.GroovyRuntimeException: Cannot set read-only property: id
            d.id = 123
            // was failing with groovy.lang.GroovyRuntimeException: Cannot read write-only property: id
            assert d.id, 123
        """
    }
}
