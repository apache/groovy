package examples.astbuilder

/**
 * Exists to test the @Main annotation. At compile time, there will be a main()
 * method added to this class that has the same body as the greet() method. 
 * It can be invoked either by using either Java or Groovy to run the class. 
 *
 * @author Hamlet D'Arcy
 */
class MainExample {

    @Main
    public void greet() {
        println "Hello from the greet() method!"
    }
}
