

/** 
 * A dummy class used for testing Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class Foo {

	// declare private variables
	private p = 9
	private q = "invisible"
	
	Foo() {
	    this.count = 0
	    this.name = "James"
	}
 
 	// provide a getter method
 	getCount() {
 	    if count == 0 {
 	        count = 1
 	    }
 	    return count
 	}
 	
 	setCount(value) {
 	    this.count = value
 	}
 	
 	getName() {
 	    return this.name
 	}
 	
 	// provide a setter method
 	setName(value) {
 	    this.name = value + "!"
 	}
 	
 	// custom access to private stuff
 	getBlah() {
 	    return p
 	}
}