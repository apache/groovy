class GStringTest extends GroovyTestCase {

    void testWithOneVariable() {
        
        name = "Bob"
        
        template = "hello ${name} how are you?"
				
        assert template instanceof GString

        count = template.getValueCount()
        assert count == 1
        assert template.getValue(0) == "Bob"

        string = template.toString()
        assert string == "hello Bob how are you?"
    }
    
    void testWithVariableAtEnd() {
        name = "Bob"
        template = "hello ${name}"
        string = template.toString()
        
        assert string == "hello Bob"
    }
    
    void testWithVariableAtBeginning() {
        name = "Bob"
        template = "${name} hey"
        string = template.toString()
        
        assert string == "Bob hey"
    }

    void testWithJustVariable() {
        name = "Bob"
        template = "${name}"
        string = template.toString()
        
        assert string == "Bob"
    }
    
    void testWithTwoVariables() {
        name = "Bob"
        template = "${name}${name}"
        string = template.toString()
        
        assert string == "BobBob"
    }
    
    void testWithTwoVariablesWithSpace() {
        name = "Bob"
        template = "${name} ${name}"
        string = template.toString()
        
        assert string == "Bob Bob"
    }
    
    void testAppendString() {
        a = "dog" 
        b = "a ${a}"
        
        c = b + " cat"

        println("Created ${c}")
        
        assert c.toString() == "a dog cat" : c
        
        b += " cat"
        
        assert b.toString() == "a dog cat" : b
    }
    
    void testAppendGString() {
        a = "dog" 
        b = "a ${a}" 
        b += " cat${a}"
        
        assert b.toString() == "a dog catdog" : b
        
        println("Created ${b}")
    }
    
    void testReturnString() {
        value = dummyMethod()
        assert value == "Hello Gromit!"
    }
    
    String dummyMethod() {
        name = "Gromit"
        return "Hello ${name}!"
    }
    
    void testCoerce() {
        it = "US-ASCII"
        value = "test".getBytes("${it}")
        
        println "Created ${value}"
        assert value != null
    }
    
    void testGroovy441() {
        arg = "test"
        content = "${arg} ="

        if (arg != "something")
        {
            content += "?"
        }

        content += "= ${arg}."

        assert content == "test =?= test."
    }

    void testTwoStringsInMiddle() {
        a = "---"
        b = "${a} :"
        b += "<<"
        b += ">>"
        b += ": ${a}"
        assert b == "--- :<<>>: ---"
    }

    void testAlternatingGStrings() {
        a = "---"
        b = "${a} :"
        b += "<<"
        b += " [[${a}]] "
        b += ">>"
        b += ": ${a}"
        assert b == "--- :<< [[---]] >>: ---"
    }
 }
