/**
 * @author John Wilson
 * @version $Revision$
 */
class ForLoopBug extends GroovyTestCase {
    
    void testBug() {
        assertScript( <<<EOF
list = []
a = 1
b = 5
for (a in a..b) {
    list << a
}
assert list == [1, 2, 3, 4, 5]
EOF)        
    }
    
    void testNormalMethod() {
        list = []
        a = 1
        b = 5
        for (a in a..b) {
            list << a
        }
        assert list == [1, 2, 3, 4, 5]
    }
    
     void testBytecodeGenBug() {
        a = 1
        b = 5

        for (i in a..b) {
            println i
        }
        a = i
        
		assert a == 5
    }
}