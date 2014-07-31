package org.codehaus.groovy.classgen.asm

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config

/**
 * @author Jochen Theodorou
 */
class HotSwapTest extends AbstractBytecodeTestCase {
    
    void testHotSwapMethodExistsAndCallsGetCallSiteArray() {
        if (config.optimizationOptions.indy) return;
        assert compile(method: '__$swapInit', '''
            Long d = 123456L
        ''').hasSequence([
                'ACONST_NULL',
                'PUTSTATIC script.$callSiteArray : Ljava/lang/ref/SoftReference;'
        ])
    }
    
    void testClinitCallingHotSwapMethod() {
        assert compile(method: '<clinit>', '''
             Long d = 123456L
        ''').hasSequence([
                'INVOKESTATIC script.__$swapInit ()V'
        ])
    }
}
