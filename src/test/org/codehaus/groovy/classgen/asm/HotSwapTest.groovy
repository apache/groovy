package org.codehaus.groovy.classgen.asm

/**
 * @author Jochen Theodorou
 */
class HotSwapTest extends AbstractBytecodeTestCase {
    
    void testHotSwapMethodExistsAndCallsGetCallSiteArray() {
        assert compile(method: '__$swapInit', '''
            double d = 1d
        ''').hasSequence([
                'INVOKESTATIC script.$getCallSiteArray ()[Lorg/codehaus/groovy/runtime/callsite/CallSite;'
        ])
    }
    
    void testClinitCallingHotSwapMethod() {
        assert compile(method: '<clinit>', '''
            double d = 1d
        ''').hasSequence([
                'INVOKESTATIC script.__$swapInit ()V'
        ])
    }
}