
/** 
 * A dummy bean for testing the use of properties in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class Foo {

    // public properties
    name = "James"
    count
    String location = "London"
    
    // declare private variables
	private blah = 9
	private invisible = "invisible"

 	// provide a getter method
 	getCount() {
 	    if (count == 0) {
 	        count = 1
 	    }
 	    return count
 	}
     
	getBlah() {
 	    return blah
 	}
}