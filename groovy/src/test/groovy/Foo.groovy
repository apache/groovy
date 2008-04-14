package groovy

/** 
 * A dummy bean for testing the use of properties in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class Foo implements Serializable {

    // public properties
    def name = "James"
    def count
    String location = "London"
    
    // declare private variables
    private blah = 9
    private invisible = "invisible"

    // declare a protected variable
    protected prot = "hide me!"

    // declare a bean with explicit typing
    private String body

    static void main(args) {
        def f = new Foo()
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
        return this.body ? this.body : 'null'
    }

    public void setBody(String body) {
        this.body = body
    }

    String toString() {
        return super.toString() + " name: ${name} location: ${location}"
    }
}
