package groovy.bugs

class SubscriptOnPrimitiveTypeArrayBug extends TestSupport {

    void testBug() {
        array = getIntArray()
        
        value = array[2]
        
        assert value == 3
        
        array[2] = 8

        value = array[2]
        assert value == 8
        
        // lets test a range
        range = array[1..3]
        
        assert range == [2, 8]
    }
}