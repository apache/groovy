package gls.invocation

import gls.CompilableTestSupport

class DefaultParamTest extends CompilableTestSupport {

    void testDefaultParameterCausingDoubledMethod() {
        //GROOVY-2191
        shouldNotCompile '''
            def foo(String one, String two = "two") {"$one $two"}
            def foo(String one, String two = "two", String three = "three") {"$one $two $three"}
        '''

        shouldNotCompile '''
            def foo(String one, String two = "two", String three = "three") {"$one $two $three"}
            def foo(String one, String two = "two") {"$one $two"}
        '''
            
        shouldNotCompile '''
            def meth(Closure cl = null) {meth([:], cl)}
            def meth(Map args = [:], Closure cl = null) {}
        '''
    } 

    void testDefaultParameters() {
        assertScript '''
            def doSomething(a, b = 'defB', c = 'defC') {
                return a + "-" + b + "-" + c
            }
            assert "X-Y-Z" == doSomething("X", "Y", "Z")
           	assert "X-Y-defC" == doSomething("X", "Y")
    	    assert "X-defB-defC" == doSomething("X")
        '''
        shouldFail {
            assertScript '''
                def doSomething(a, b = 'defB', c = 'defC') {
                    return a + "-" + b + "-" + c
                }
                doSomething()    	
    	    '''
    	}
    }

    void testDefaultTypedParameters() {
        assertScript """
            String doTypedSomething(String a = 'defA', String b = 'defB', String c = 'defC') {
                a + "-" + b + "-" + c
            }
            assert "X-Y-Z" == doTypedSomething("X", "Y", "Z")
            assert "X-Y-defC" == doTypedSomething("X", "Y")
            assert "X-defB-defC" == doTypedSomething("X")
            assert "defA-defB-defC" == doTypedSomething()
        """
    }

    void testDefaultTypedParametersAnother() {
        assertScript """
            String doTypedSomethingAnother(String a = 'defA', String b = 'defB', String c) {
                return a + "-" + b + "-" + c
            }
            assert "X-Y-Z" == doTypedSomethingAnother("X", "Y", "Z")
            assert "X-defB-Z" == doTypedSomethingAnother("X", "Z")
            assert "defA-defB-Z" == doTypedSomethingAnother("Z")            
        """
        shouldFail {
            assertScript """
                String doTypedSomethingAnother(String a = 'defA', String b = 'defB', String c) {
                    return a + "-" + b + "-" + c
                }
                doTypedSomethingAnother()
    	    """
    	}
    }
    
    void testConstructor() {
        assertScript """
            class DefaultParamTestTestClass {
                def j
                DefaultParamTestTestClass(int i = 1){j=i}
            }
            assert DefaultParamTestTestClass.declaredConstructors.size() == 2
            def foo = new DefaultParamTestTestClass()
            assert foo.j == 1
            foo = new DefaultParamTestTestClass(2)
            assert foo.j == 2        
        """
    }
}

