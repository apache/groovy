package groovy

class ToArrayBugTest extends GroovyTestCase {
    
    void testToArrayBug() {
        
        def array = getArray()

        callArrayMethod(array)
    }
    
    protected def getArray() {
        def list = [1, 2, 3, 4]
        def array = list.toArray()
        
        assert array != null
        
        return array
    }
    
    protected def callArrayMethod(array) {
        System.out.println("Called method with ${array}")
        
        def list = Arrays.asList(array)
        
        assert list.size() == 4
        assert list == [1, 2, 3, 4]
    }
}
