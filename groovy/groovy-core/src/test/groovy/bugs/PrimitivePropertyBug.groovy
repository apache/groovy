package groovy.bugs

/**
 * @version $Revision$
 *
 * Fix Bug GROOVY-683
 * @author Pilho Kim
 */
class PrimitivePropertyBug extends GroovyTestCase {
     
    double x1
    float x2
    long x3
    int x4
    short x5
    byte x6
    char x7

    void testBug() {
        def y = new PrimitivePropertyBug()
        y.x1 = 10.0
        y.x2 = 10.0
        y.x3 = 10.0
        y.x4 = 10.0
        y.x5 = 10.0
        y.x6 = 10.0
        y.x7 = 10.0
        
        assert y.x1 == 10.0
        assert y.x2 == 10.0
        assert y.x3 == 10.0
        assert y.x4 == 10.0
        assert y.x5 == 10.0
        assert y.x6 == 10.0
        assert y.x1.class == Double.class
        assert y.x2.class == Float.class
        assert y.x3.class == Long.class
        assert y.x4.class == Integer.class
        assert y.x5.class == Short.class
        assert y.x6.class == Byte.class
        assert y.x7.class == Character.class
        assert y.x1 + y.x1 == y.x1 * 2
        assert y.x2 - 1 == 9.0f
        assert y.x3 * 2 == 20L
        assert y.x4 == 10
        assert y.x5 == 10
        assert y.x6 + 3 == 13
        assert "Hello" + y.x7 + "World!" == "Hello\nWorld!"
    }
}
