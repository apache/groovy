/**
 * @author John Wilson
 * @version $Revision$
 */
class Groovy239_Bug extends GroovyTestCase {
    
    void testBug() {
		a = { it() }
		b = { it() }
		c = { it() }
		
		a.call() {
			b.call() {
				c.call() {
				}
			}
		}
		
		/** @todo fixme!
		a() {
			b() {
				c() {
				}
			}
		}
		*/
	}
   
}