package groovy.bugs

class Groovy4078Bug extends GroovyTestCase {
    void testInfiniteLoopDetectionInStepUsage() {
        (2..2).step 0, {assert it != null} //IntRange
        
        ('b'..'b').step 0, {assert it != null} //ObjectRange
        
        5.step( 5, 1 ) { assert it != null } // DGM.step(), int
        
        5.0.step (5.0, 1 ) { assert it != null } // DGM.step(), BigDecimal
        
        def from = new BigInteger(5)
        def to = new BigInteger(5)
        from.step (to, 1 ) { assert it != null }  // DGM.step(), BigInteger

        try{
            (1..2).step 0, {assert it != null} //IntRange
            fail('Should have failed as step size 0 causes infinite loop')
        } catch(ex) {
            assert ex.message.contains('Infinite loop detected due to step size of 0')
        }
                      
        try{
            ('a'..'b').step 0, {assert it != null} // ObjectRange
            fail('Should have failed as step size 0 causes infinite loop')
        } catch(ex) {
            assert ex.message.contains('Infinite loop detected due to step size of 0')
        }
    }
}
