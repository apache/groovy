/**
 * @author John Wilson
 * @version $Revision$
 */
class ForLoopBug extends GroovyTestCase {
    
    void testBug() {
        assertScript( """
list = []
a = 1
b = 5
for (c in a..b) {
    list << c
}
assert list == [1, 2, 3, 4, 5]
""")
    }
    
    void testSeansBug() {
        assertScript( """
for (i in 1..10) {
    println i
}
""")        
    }

    void testNormalMethod() {
        list = []
        a = 1
        b = 5
        for (c in a..b) {
            list << c
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


    void testVisibility() {
        assertScript( """

array = [ true, true, true ];
for( boolean i in array ) {
   1.times {
       assert i == true;
   }
}
""")
    }

}
