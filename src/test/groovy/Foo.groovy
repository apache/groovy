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

    // declare a protected variable
    protected prot = "hide me!"

 	// provide a getter method
 	getCount() {
 	    if (count == null) {
 	        count = 1
 	    }
 	    return count
 	}
     
	getBlah() {
 	    return blah
 	}
	
    // declare a bean with explicit typing
    private String body

    public String getBody() {
        return this.body
    }

    public void setBody(String body) {
        this.body = body
    }

	String toString() {
		return super.toString() + " name: ${name} location: ${location}"
	}
}
