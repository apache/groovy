package groovy.bugs

class Groovy4206Bug extends GroovyTestCase {
    void testIsNamesForBooleanProps() {
        assert Bar4206.isValid()
        assert Bar4206.valid
        assert '1.1E2'.isBigDecimal()
        assert '1.1E2'.bigDecimal
        assert '    '.isAllWhitespace()
        assert '    '.allWhitespace
    }
}

class Bar4206 {
    static Boolean isValid() { true }
}
