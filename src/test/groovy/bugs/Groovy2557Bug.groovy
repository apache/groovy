package groovy.bugs

class Groovy2557Bug extends GroovyTestCase{
    void testArray2ListCoercion() {
        String[] args = ['a', 'b']
        List list = args as List
        list.add('c')
        assertEquals(['a', 'b', 'c'], list)
    }
}