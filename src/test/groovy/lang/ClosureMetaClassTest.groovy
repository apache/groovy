package groovy.lang

import groovy.util.GroovyTestCase
import org.codehaus.groovy.runtime.metaclass.ClosureMetaClass

class ClosureMetaClassTest extends GroovyTestCase {
    
    void testClosureMetaClass() {
        Object[] myObjectArray = ['a', 'b'] as Object[]
        def closure = {1}
        assert closure.metaClass.getClass() == ClosureMetaClass.class
        assert closure(myObjectArray) == 1
    }    

    void testMetaClassImpl() {
        Object[] myObjectArray = ['a', 'b'] as Object[]
        def closure = {1}
        closure.metaClass == new MetaClassImpl(closure.getClass())
        assert closure(myObjectArray) == 1
    }
    
    void testEMC() {
        Object[] myObjectArray = ['a', 'b'] as Object[]
        def closure = {1}
        closure.metaClass == new ExpandoMetaClass(closure.getClass())
        assert closure(myObjectArray) == 1
    }
}