package org.codehaus.groovy.classgen.asm

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config

/**
 * @author Jochen Theodorou
 */
class HotSwapTest extends AbstractBytecodeTestCase {
    
    void testHotSwapMethodExistsAndCallsGetCallSiteArray() {
        if (config.optimizationOptions.indy) return;
        assert compile(method: '__$swapInit', '''
            double d = 1d
        ''').hasSequence([
                'ACONST_NULL',
                'PUTSTATIC script.$callSiteArray : Ljava/lang/ref/SoftReference;'
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
