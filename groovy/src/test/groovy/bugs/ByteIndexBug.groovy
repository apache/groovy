package groovy.bugs

/**
 * @author Robert Fuller 
 * @version $Revision$
 */
class ByteIndexBug extends GroovyTestCase {
    // TODO: this tests a string with 128 nulls - is that what is intended?
    void testBug() {
        def sb = new StringBuffer("\"\"\"\n")
        for (j in 0..127){ // 126 is okay.
            sb.append('$').append("{x}")
        }
        sb.append("\n\"\"\"\n")
        def b = new Binding(x:null)
        new GroovyShell(b).evaluate(sb.toString(),"foo")
    }
}
