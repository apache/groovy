/** 
 * Tests the use of GroovyLog
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class LogTest extends GroovyTestCase {

	void testUseLog() {
	    file = "something.txt"
	    
        log = GroovyLog.newInstance(getClass())
        
        log.starting("Hey I'm starting up...")
        
	    log.openFile("Am about to open file ${file}")
	    
	    // ...
	    
	    log.closeFile("Have closed the file ${file}")

        log.stopping("..Finished")
	}
}
