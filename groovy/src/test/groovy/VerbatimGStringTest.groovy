package groovy

class VerbatimGStringTest extends GroovyTestCase {

    void testWithOneVariable() {
        
        def name = "Bob"
        
        def template = """
hello ${name} how are you?
"""

        assert template instanceof GString

        def count = template.getValueCount()
        assert count == 1

        def value = template.getValue(0)
        assert value == "Bob"
        assert template.getValue(0) == "Bob"

        def string = template.toString().trim()
        assert string == "hello Bob how are you?"
    }
    
    void testWithVariableAtEnd() {
        def name = "Bob"

        def template = """
hello ${name}
"""

        def string = template.toString().trim()
        
        assert string == "hello Bob"
    }
    
    void testWithVariableAtBeginning() {
        def name = "Bob"

        def template = """
${name} hey,
hello
"""
        def string = template.toString().trim()
        
        assert fixEOLs(string) == "Bob hey,\nhello"
    }

    void testWithJustVariable() {
        def name = "Bob"

        def template = """
${name}
"""
        def string = template.toString().trim()
        
        assert string == "Bob"
    }
}
