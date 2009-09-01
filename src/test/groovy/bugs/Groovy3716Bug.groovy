package groovy.bugs

import org.codehaus.groovy.ast.ClassHelper

class Groovy3716Bug extends GroovyTestCase {
    void testVoidAndObjectDerivedFromResults() {
    	assertTrue ClassHelper.VOID_TYPE.isDerivedFrom(ClassHelper.VOID_TYPE)
    	assertFalse ClassHelper.OBJECT_TYPE.isDerivedFrom(ClassHelper.VOID_TYPE)
    	assertFalse ClassHelper.VOID_TYPE.isDerivedFrom(ClassHelper.OBJECT_TYPE)    	
    }
}
