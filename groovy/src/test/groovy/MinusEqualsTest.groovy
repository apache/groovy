package groovy

class MinusEqualsTest extends GroovyTestCase {

    void testIntegerMinusEquals() {
        def x = 4
        def y = 2
        x -= y
        
        assert x == 2
        
        y -= 1
        
        assert y == 1
    }

    void testCharacterMinusEquals() {
        Character x = 4
        Character y = 2
        x -= y
        
        assert x == 2
        
        y -= 1
        
        assert y == 1
    }
    
    void testNumberMinusEquals() {
        def x = 4.2
        def y = 2
        x -= y
        
        assert x == 2.2
        
        y -= 0.1
        
        assert y == 1.9
    }
    
    void testStringMinusEquals() {
        def foo = "nice cheese"
        foo -= "cheese"
        
        assert foo == "nice "
    }


    void testSortedSetMinusEquals() {
        def sortedSet = new TreeSet()
        sortedSet.add('one')
        sortedSet.add('two')
        sortedSet.add('three')
        sortedSet.add('four')
        sortedSet -= 'one'
        sortedSet -= ['two', 'three']
        assertTrue 'sortedSet should have been a SortedSet',
                   sortedSet instanceof SortedSet
        assertEquals 'sortedSet had the wrong number of elements', 1, sortedSet.size()
        assertTrue 'sortedSet should have contained the word four', sortedSet.contains('four')
    }
}
