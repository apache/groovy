package org.codehaus.groovy.runtime

class NullObjectTest extends GroovyTestCase {
    void testCallingMethod() {
        def foo = null
        try {
          println foo.bar
          assert false  // should throw exception
        } catch (NullPointerException ex) {
          // is successfull
        }
    }
    
    void testtoStringMethod() {
        def foo = null
        assert foo.toString() == "null"
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
    
    void testCategory() {
        def n = null

        assert "a $n b" == "a null b"
            assert n.toString() == "null"
            assert n + " is a null value" == "null is a null value"
            assert "this is a null value " + null == "this is a null value null"

            use (MyCategory) {
                assert "a $n b" == "a  b"
                assert n.toString() == ""
                assert n + " is a null value" == " is a null value"
                assert "this is a null value " + null == "this is a null value "
            }
        } 
}

class MyCategory {
    public static String toString(NullObject obj) {
        return ""
    }
}

