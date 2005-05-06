package groovy.bugs

/**
 * Test to fix the Jira issues GROOVY-810, GROOVY-811 and GROOVY-812.
 *
 * @author Pilho Kim
 * @version $Revision$
 */

public class AsBoolBug extends GroovyTestCase {

    void testMapAsBool() {
        def a = ["A":123]
        println ("$a : ${a as Boolean}")
        assert a as Boolean == true
        a = [:]
        println ("$a : ${a as Boolean}")
        assert a as Boolean == false
    }

    void testListAsBool() {
        def b = [123]
        println ("$b : ${b as Boolean}")
        assert b as Boolean == true
        b = []
        println ("$b : ${b as Boolean}")
        assert b as Boolean == false
    }

    /**
     * void testStringAsBool() <br><br>
     *
     * "string as Boolean" should have the same meaning
     *  as "java.lang.Boolean.getBoolean(string)" of Java.
     */
    void testStringAsBool() {
        def c = "true"
        println ("$c : ${c as Boolean}")
        assert c as Boolean == true
        boolean z = c
        println ("$z")
        assert z == true
        if (c)
           println "It is true!!"
        else
           println "It is false!!"

        c = "123"
        println ("$c : ${c as Boolean}")
        assert c as Boolean == false

        c = "True"
        println ("$c : ${c as Boolean}")
        assert c as Boolean == true
        if (c)
           println "It is true!!"
        else
           println "It is false!!"
        z = c
        println ("$z")
        assert z
    }
}
