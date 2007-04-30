package groovy.bugs

/**
 * @version $Revision$
 */
class Bytecode2Bug extends GroovyTestCase {

    Integer count = 0
    
    void testBytecodeBug() {
        getCollection().each { count += it }
    }
    
    void testTedsBytecodeBug() {
        //doTest(getCollection())
        def a = [1, 2, 3, 4]
        doTest(a)

    }
    
    void doTest(args) {
        def m = [:]
        def i = 1
        args.each { m.put(it, i++) }     
        
        assert m[1] == 1
        assert m[2] == 2
        assert m[3] == 3
        assert m[4] == 4
        
        println("created: ${m}")
        
        assert i == 5
    }
    
    
    void testTedsBytecode2Bug() {
        def m = [:]
        def i = 1
        getCollection().each { m.put(it, i++) }     
        
        assert m[1] == 1
        assert m[2] == 2
        assert m[3] == 3
        assert m[4] == 4
        
        println("created: ${m}")
        
        assert i == 5
    }
    
    def getCollection() {
        [1, 2, 3, 4]
    }
}