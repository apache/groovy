/**
 * ConstructorThisCallBug.groovy
 *
 *     Test Script for the Jira issue: GROOVY-994.
 *
 * @author    Pilho Kim
 * @date      2005.08.05.06.21
 */

package groovy.bugs

public class ConstructorThisCallBug extends GroovyTestCase {
    public void testCallA() {
        println "Testing for a class without call()"
        def a1 = new ConstructorCallA("foo") 
        def a2 = new ConstructorCallA(9) 
        def a3 = new ConstructorCallA() 
    }
}

public class ConstructorCallA { 
    public ConstructorCallA() {
        this(19)               // call another constructor
        println "(1) no argument consructor"
    } 

    public ConstructorCallA(String a) {
        println "(2) String value a = $a"
    } 

    public ConstructorCallA(int a) {
        this("" + (a*a))       // call another constructor
        println "(3) int value a = $a"
    } 
} 
