package groovy.lang

class StripMarginTest extends GroovyTestCase {
    void testStripMarginOnSingleLineString() {
        def expected = "the quick brown fox jumps over the lazy dog"
        def actual = "     |the quick brown fox jumps over the lazy dog".stripMargin()
        assert expected == actual

        actual = "     ||the quick brown fox jumps over the lazy dog".stripMargin()
        assert "|" + expected == actual

        actual = "     #the quick brown fox jumps over the lazy dog".stripMargin('#')
        assert expected == actual
    }

    void testStripMarginOnMultiLineString() {
        def expected = "the quick brown fox\njumps over the lazy dog"
        def actual = """     |the quick brown fox
     |jumps over the lazy dog""".stripMargin()
        assert expected == actual

        actual = """     #the quick brown fox
     #jumps over the lazy dog""".stripMargin('#')
        assert expected == actual

        expected = "the quick brown fox\n|jumps over the lazy dog"
        actual = """     |the quick brown fox
     ||jumps over the lazy dog""".stripMargin()
        assert expected == actual
    }

    void testStripIndent() {
        def actual   = """
                return 'foo'
            }

            def method() {
                return 'bar'
            }
        """.stripIndent()

        def expected = """
    return 'foo'
}

def method() {
    return 'bar'
}
"""

        assert expected == actual
    }

    void testStripIndentWithFirstLineBackslash() {
        def actual   = """\
                return 'foo'
            }

            def method() {
                return 'bar'
            }
        """.stripIndent()
        
        def expected = """\
    return 'foo'
}

def method() {
    return 'bar'
}
"""

        assert expected == actual
    }
}
