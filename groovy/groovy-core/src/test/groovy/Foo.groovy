import java.io.Serializable

/** 
 * A dummy bean for testing the use of properties in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class Foo implements Serializable {

    // public properties
    name = "James"
    count
    String location = "London"
    
    // declare private variables
	private blah = 9
	private invisible = "invisible"

 	// provide a getter method
    /*
 	getCount() {
 	    if (count == 0) {
 	        count = 1
 	    }
 	    return count
 	}
 	*/
     
	getBlah() {
 	    return blah
 	}
	
 	/* @todo bug
	String toString() {
	    return super.toString() + " name: " + getName() + " location: " + getLocation()
		// return super.toString() + " name: ${name} location: ${location}"
	}
	*/
}