/**
 * @author Robert Fuller 
 * @version $Revision$
 */
class ByteIndexBug extends GroovyTestCase {
     
    void testBug() {
		sb = new StringBuffer("<<<FOO\n")
		for (j in 0..127){ // 126 is okay.
			sb.append('$').append("{x}")
		}
		sb.append("\nFOO\n")
		
		new GroovyShell().evaluate(sb.toString(),"foo")
	}
}
