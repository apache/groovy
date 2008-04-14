package groovy.bugs

/**
 * @version $Revision$
 */
class BytecodeBug extends GroovyTestCase {
     
    void testTedsBytecodeBug() {
        //def a = ['tom','dick','harry']
        def a = [1, 2, 3, 4]
        doTest(a)
    }
    
    void doTest(args) {
        def m = [:]
        def i = 1
        args.each { 
            talk(it)
            m.put(it, i++)
        }
        assert i == 5
        m.each {
            println(it)
        }
    }
    
    def talk(a) {
        println("hello "+a)
    }
}