package groovy.bugs

import org.codehaus.groovy.GroovyBugError

class Groovy2666Bug extends GroovyTestCase{

    private void ex () {
        throw new GroovyBugError ("ERR")
    }

    void testMe () {

        try {
            ex ()
        } catch (org.codehaus.groovy.GroovyBugError e) {
            println "caught"
            return
        } catch (NullPointerException e) {
        }

        fail ()
    }
}