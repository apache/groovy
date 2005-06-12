package groovy.bugs

/**
 * @author John Wilson
 * @version $Revision$
 */
class Groovy278_Bug extends GroovyTestCase {
    
    void testBug() {
        def value = new MyRange()
        println value
        assert value != null
    }
}


class MyRange extends IntRange {
    MyRange() {
        super(1, 2)
    }
}