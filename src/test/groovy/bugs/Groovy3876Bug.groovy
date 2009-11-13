package groovy.bugs

class Groovy3876Bug extends GroovyTestCase {
    void testGStringToNumberConversion() {
        def a

        assert "-1" as Integer == -1
        a = '-1'
        assert "$a" as Integer == -1

        assert "-1000" as Integer == -1000
        a = "-1000"
        assert "$a" as Integer == -1000
    }
}