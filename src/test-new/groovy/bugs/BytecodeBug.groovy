/**
 * @version $Revision$
 */
class BytecodeBug extends GroovyTestCase {
     
    void testTedsBytecodeBug() {
        //a = ['tom','dick','harry']
        a = [1, 2, 3, 4]
        doTest(a)
    }
    
    void doTest(args) {
        m = [:]
        i = 1
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