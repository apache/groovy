package groovy.bugs

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy1465Bug extends GroovyTestCase {
    
    void compileAndVerifyCyclicInheritenceCompilationError(script) {
        try {
            new GroovyShell().parse(script)
            fail('The compilation should have failed as it is a cyclic reference')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains('Cyclic inheritance')
        }
    }
    
    void testInterfaceCyclicInheritenceTC1() {
        compileAndVerifyCyclicInheritenceCompilationError """
            interface G1465Tt extends G1465Tt { }
            def tt = {} as G1465Tt
        """ 
    }

    void testInterfaceCyclicInheritenceTC2() {
        compileAndVerifyCyclicInheritenceCompilationError """
            interface G1465Rr extends G1465Ss { }
            interface G1465Ss extends G1465Rr { }
            def ss = {} as G1465Ss
        """ 
    }

    void testInterfaceCyclicInheritenceTC3() {
        compileAndVerifyCyclicInheritenceCompilationError """
            interface G1465A extends G1465B { }
            interface G1465B extends G1465C { }
            interface G1465C extends G1465B { }
        """ 
    }
}