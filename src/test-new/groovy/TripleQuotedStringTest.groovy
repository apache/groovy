class TripleQuotedStringTest extends GroovyTestCase {

    void testTripleQuotedString() {
        s = """
        Lots of 'text' with a variety of ""quoting "" and
   a few lines
    and some escaped \""" quoting and
    an ending""".trim()

        println(s)
        assert s != null
        idx = s.indexOf("quoting and")
        assert idx > 0
    }

    static void main( String[] args ) { 
        o = new TripleQuotedStringTest();
        o.testTripleQuotedString();
    }
}
