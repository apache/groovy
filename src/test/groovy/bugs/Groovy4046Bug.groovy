package groovy.bugs

class Groovy4046Bug extends GroovyTestCase {
    void testComparableOnLeftObjectOnRight() {
        assertFalse MyEnum4046.A == new Object()
        
        assertFalse 1 == new Object()
    }
}

enum MyEnum4046 { A, B, C }
