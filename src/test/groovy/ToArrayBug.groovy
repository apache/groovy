import java.util.Arrays

class ToArrayBug extends GroovyTestCase {
    
    void testToArrayBug() {
        list = [1, 2, 3, 4]
        array = list.toArray()
        
        assert array != null
        
        /** @todo bug
        list2 = Arrays.asList(array)
        
        assert list2.size() == 4
        assert list2 == list
        */
    }
}
