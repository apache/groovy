/**
 * @author John Wilson
 * @version $Revision$
 */
class Groovy239_Bug extends GroovyTestCase {
    
    void testBug() {
		a = makeClosure()
		b = makeClosure()
		c = makeClosure()
		
		a() {
			println("A")
			b() {
				println("B")
				c() {
					println("C")
				}
			}
		}
	}

	def makeClosure() {
		return { it() }
	}
		
    void testBug2() {
		a = { it() }
		b = { it() }
		c = { it() }
		
		a() {
			b() {
				c() {
				}
			}
		}
	}
   
}