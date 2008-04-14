package groovy.bugs

import groovy.util.Dummy

/**
  * Fixes GROOVY-558 and GROOVY-616.
  * A fully qualified class name ending with .class or not were not recognized properly.
  *
  * @author Jochen Theodorou
  * @author Guillaume Laforge
  */
class Groovy558_616_Bug extends GroovyTestCase {

    void testListClass() {
        assert java.util.ArrayList.class == ArrayList.class
        assert java.util.ArrayList.class == ArrayList
        assert ArrayList != Class
        def list = new ArrayList()
        assert list.class == ArrayList
    }

    void testStringClass() {
        assert java.lang.String.class == String.class
        assert java.lang.String.class == String
        assert String != Class
        def st = ""
        assert st.class == String
    }

    void testDummyClass() {
        assert groovy.util.Dummy.class == Dummy.class
        assert groovy.util.Dummy.class == Dummy
        assert Dummy != Class
        def dum = new Dummy()
        assert dum.class == Dummy
    }

    void testFooClass() {
        assert groovy.bugs.Groovy558_616_Bug.class == Groovy558_616_Bug
        assert Groovy558_616_Bug != Class
        def f = new Groovy558_616_Bug()
        assert f.class == Groovy558_616_Bug
    }
}

