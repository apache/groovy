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

    void testCallB() {
        println "Testing for a class with call()"
        def b1 = new ConstructorCallB('bar') 
        def b2 = new ConstructorCallB(9) 
        def b3 = new ConstructorCallB() 
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

public class ConstructorCallB { 
    public ConstructorCallB() {
        println '1: no argument consructor'
        this(19)              // call the method call()
    } 

    public ConstructorCallB(String b) {
        println """2: String value b = $b"""
    } 

    public ConstructorCallB(int b) {
        println """3: int value b = $b"""
        this('' + (b + b))     // call the method call()
    } 

    void call(Object o) {
        println "Hello, $o"
    } 
} 
