package org.codehaus.groovy.runtime

class NullObjectTest extends GroovyTestCase {
    void testCallingMethod() {
        def foo = null
        try {
          println foo.bar
        } catch (NullPointerException ex) {
          // is successfull
        }
    }

    void testEquals() {
        def a = [1]
        assert a[3] == a[4]
        assert a[2].equals(a[4])
    }
    
    void testAsExpression() {
      assert null as String == null
    }
    
    void testIs(){
      assert null.is(null)
    }
    
}
