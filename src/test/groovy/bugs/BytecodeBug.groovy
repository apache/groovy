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
            //talk(it)
            m.put(it, i++)
        }
        //println(i)
        //assert i == 5
        /*
        l.each {
            println(it)
        }
        */
    }
    
    talk(a) {
        println("hello "+a)
    }
}