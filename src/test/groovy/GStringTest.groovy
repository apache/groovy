class GStringTest extends GroovyTestCase {

    void check(template, teststr) {
        assert template instanceof GString

        count = template.getValueCount()
        assert count == 1
        assert template.getValue(0) == "Bob"

        string = template.toString()
        assert string == teststr
    }

    void testWithOneVariable() {
        name = "Bob"
        teststr = "hello Bob how are you?"


    check("hello $name how are you?", teststr)
    check("hello ${name} how are you?", teststr)
    check("hello ${println "feep"; name} how are you?", teststr)
    check(/hello $name how are you?/, teststr)
    check(/hello ${name} how are you?/, teststr)
    check(/hello ${println "feep"; name} how are you?/, teststr)
    }

    void testWithVariableAtEnd() {
        name = "Bob"
        teststr = "hello Bob"

        check("hello $name", teststr)
        check("hello ${name}", teststr)
        check("hello ${def name = "Bob"; name}", teststr)
        check(/hello $name/, teststr)
        check(/hello ${name}/, teststr)
        check(/hello ${def name = "Bob"; name}/, teststr)
    }
    
    void testWithVariableAtBeginning() {
        name = "Bob"
        teststr = "Bob hey"
        check("$name hey", teststr)
        check("${name} hey", teststr)
        name = ""
        check("${name += "Bob"; name} hey", teststr)
        assert name == "Bob"
        check(/$name hey/, teststr)
        check(/${name} hey/, teststr)
        name = ""
        check(/${name += "Bob"; name} hey/, teststr)
    }

    void testWithJustVariable() {
        name = teststr = "Bob"
        check("$name", teststr)
        check("${name}", teststr)
        check("${assert name=="Bob"; name}", teststr)
        // Put punctuation after the variable name:
        check("$name.", "Bob.")
        check("$name...", "Bob...")
        check("$name?", "Bob?")

        check(/$name/, teststr)
        check(/${name}/, teststr)
        check(/${assert name=="Bob"; name}/, teststr)
        // Put punctuation after the variable name:
        check(/$name./, "Bob.")
        check(/$name.../, "Bob...")
        check(/$name?/, "Bob?")
        check(/$name\?/, "Bob\\?")
        check(/$name$/, "Bob\$")

        guy = [name: name]
        check("${guy.name}", "Bob")
        check("$guy.name", "Bob")
        check("$guy.name.", "Bob.")
        check("$guy.name...", "Bob...")
        check("$guy.name?", "Bob?")
        check(/$guy.name/, "Bob")
        check(/$guy.name./, "Bob.")
        check(/$guy.name.../, "Bob...")
        check(/$guy.name?/, "Bob?")
        check(/$guy.name\?/, "Bob\\?")
        check(/$guy.name$/, "Bob\$")
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
        
        assert c.toString() == "a dog cat" , c
        
        b += " cat"
        
        assert b.toString() == "a dog cat" , b
    }
    
    void testAppendGString() {
        a = "dog" 
        b = "a ${a}" 
        b += " cat${a}"
        
        assert b.toString() == "a dog catdog" , b
        
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

        if (arg != "something") {
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
