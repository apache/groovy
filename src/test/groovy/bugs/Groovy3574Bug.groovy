package groovy.bugs

class Groovy3574Bug extends GroovyTestCase {
    void testToStringCallDelegationToConvertedClosureProxy() {
        Closure failing1 = { 
            throw new RuntimeException("Call to this closure fails.") 
        }
        
        Closure failing2 = { a, b ->
            assert a == "a"
            assert b == "b"
            throw new RuntimeException("Call to this closure fails.") 
        }
        
        MyType3574A instance1 = failing1 as MyType3574A

        // test call without args
        try{
            instance1.m()
            fail("The call m() should have failed - 1")
        } catch (ex) {
            // ok, if it failed
        }
        
        // this call was getting delegated to the closure earlier
        assert instance1.toString() != null
        
        // test call with args
        MyType3574B instance2 = failing2 as MyType3574B
        try{
            instance2.m("a", "b")
            fail("The call m() should have failed - 2")
        } catch (ex) {
            // ok, if it failed
        }

        // this call was getting delegated to the closure earlier
        assert instance2.toString() != null
    }
}

interface MyType3574A { def m()}

interface MyType3574B { def m(a, b)}