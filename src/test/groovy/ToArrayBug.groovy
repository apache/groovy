import java.util.Arrays

class ToArrayBug extends GroovyTestCase {
    
    void testToArrayBug() {
        
        array = getArray()

        callArrayMethod(array)
    }
    
    protected def getArray() {
        list = [1, 2, 3, 4]
        array = list.toArray()
        
        assert array != null
        
        return array
    }
    
    protected def callArrayMethod(array) {
        System.out.println("Called method with ${array}")
        
        list = Arrays.asList(array)
        
        assert list.size() == 4
        assert list == [1, 2, 3, 4]
    }
}
