package groovy.bugs

import groovy.util.GroovyTestCase

public class Groovy2706Bug extends GroovyTestCase {
    static counter = 0

    void testStaticAccessWithIncAndDec() {
        ++Groovy2706Bug.counter
        assert counter == 1
        Groovy2706Bug.counter++
        assert counter == 2
        --Groovy2706Bug.counter
        assert counter == 1
        Groovy2706Bug.counter--
        assert counter == 0
        --counter
        assert counter == -1
        ++counter
        assert counter == 0
        counter++
        assert counter == 1
        counter--
        assert counter == 0
    }

}
