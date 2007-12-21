package groovy

class PlusEqualsTest extends GroovyTestCase {

    void testIntegerPlusEquals() {
        def x = 1
        def y = 2
        x += y
        
        assert x == 3
        
        y += 10
        
        assert y == 12
    }

    void testCharacterPlusEquals() {
        Character x = 1
        Character y = 2
        x += y
        
        assert x == 3
        
        y += 10
        
        assert y == 12
    }
    
    void testNumberPlusEquals() {
        def x = 1.2
        def y = 2
        x += y
        
        assert x == 3.2
        
        y += 10.1
        
        assert y == 12.1
    }
    
    void testStringPlusEquals() {
        def x = "bbc"
        def y = 2
        x += y
        
        assert x == "bbc2"
        
        def foo = "nice cheese"
        foo += " gromit"
        
        assert foo == "nice cheese gromit"
    }

    void testSortedSetPlusEquals() {
        def sortedSet = new TreeSet()
        sortedSet += 'abc'
        assertTrue 'sortedSet should have been a SortedSet',
                   sortedSet instanceof SortedSet
        sortedSet += ['def', 'ghi']
        assertTrue 'sortedSet should have been a SortedSet',
                   sortedSet instanceof SortedSet
        assertEquals 'sortedSet had wrong number of elements',
                     3, sortedSet.size()
    }
}
