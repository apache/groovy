import java.io.Serializable

/** 
 * A dummy bean for testing the use of properties in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class Foo implements Serializable {

    // public properties
    @Property def name = "James"
    @Property def count
    @Property String location = "London"
    
    // declare private variables
    private def blah = 9
    private def invisible = "invisible"

    // declare a protected variable
    protected def prot = "hide me!"

    // declare a bean with explicit typing
    private String body

    static void main(args) {
        f = new Foo()
        println f
    }
    
    // provide a getter method
    def getCount() {
         if (count == null) {
             count = 1
         }
         return count
    }
     
    def getBlah() {
         return blah
    }

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
