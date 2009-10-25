package groovy.bugs

class Groovy3831Bug extends GroovyTestCase {
    void testClosureDefinitionInSpecialCallsInConstructorsV1() {
        def test = new Test3831V1('hello', ["world"])
        assert test.string == 'hello'
        assert test.uris.size() == 1
    }

    void testClosureDefinitionInSpecialCallsInConstructorsV2() {
        // just loading of class is test enough as it was giving VerifyError earlier.
        new Test3831V2()
    }
}

class Test3831V1 {
    String string
    URI[] uris
 
    public Test3831V1(String string, URI[] uris) {
        this.string = string
        this.uris = uris
    }

    public Test3831V1(String string, List uris) {
        this(string, uris.collect { new URI(it) } as URI[])
    }
}

class Test3831V2 {
    public Test3831V2(cl) {}

    public Test3831V2() {
        this({1})
    }
}