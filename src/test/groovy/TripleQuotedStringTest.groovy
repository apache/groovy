package groovy

class TripleQuotedStringTest extends GroovyTestCase {

    void testTripleQuotedString() {
        def s = """
        Lots of 'text' with a variety of ""quoting "" and
   a few lines
    and some escaped \""" quoting and
    an ending""".trim()

        println(s)
        assert s != null
        def idx = s.indexOf("quoting and")
        assert idx > 0
    }

    static void main( String[] args ) { 
        def o = new TripleQuotedStringTest();
        o.testTripleQuotedString();
    }
}
