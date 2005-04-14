class GStringTest extends GroovyTestCase {

    void check(template, teststr) {
        assert template instanceof GString

        def count = template.getValueCount()
        assert count == 1
        assert template.getValue(0) == "Bob"

        def string = template.toString()
        assert string == teststr
    }

    void testWithOneVariable() {
        def name = "Bob"
        def teststr = "hello Bob how are you?"


    check("hello $name how are you?", teststr)
    check("hello ${name} how are you?", teststr)
    check("hello ${println "feep"; name} how are you?", teststr)
    check(/hello $name how are you?/, teststr)
    check(/hello ${name} how are you?/, teststr)
    check(/hello ${println "feep"; name} how are you?/, teststr)
    }

    void testWithVariableAtEnd() {
        def name = "Bob"
        def teststr = "hello Bob"

        check("hello $name", teststr)
        check("hello ${name}", teststr)
        check("hello ${def name = "Bob"; name}", teststr)
        check(/hello $name/, teststr)
        check(/hello ${name}/, teststr)
        check(/hello ${def name = "Bob"; name}/, teststr)
    }
    
    void testWithVariableAtBeginning() {
        def name = "Bob"
        def teststr = "Bob hey"
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
        def teststr
        def name = teststr = "Bob"
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

        def guy = [name: name]
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
        def name = "Bob"
        def template = "${name}${name}"
        def string = template.toString()
        
        assert string == "BobBob"
    }
    
    void testWithTwoVariablesWithSpace() {
        def name = "Bob"
        def template = "${name} ${name}"
        def string = template.toString()
        
        assert string == "Bob Bob"
    }
    
    void testAppendString() {
        def a = "dog" 
        def b = "a ${a}"
        
        def c = b + " cat"

        println("Created ${c}")
        
        assert c.toString() == "a dog cat" , c
        
        b += " cat"
        
        assert b.toString() == "a dog cat" , b
    }
    
    void testAppendGString() {
        def a = "dog" 
        def b = "a ${a}" 
        b += " cat${a}"
        
        assert b.toString() == "a dog catdog" , b
        
        println("Created ${b}")
    }
    
    void testReturnString() {
        def value = dummyMethod()
        assert value == "Hello Gromit!"
    }
    
    String dummyMethod() {
        def name = "Gromit"
        return "Hello ${name}!"
    }
    
    void testCoerce() {
        def it = "US-ASCII"
        def value = "test".getBytes("${it}")
        
        println "Created ${value}"
        assert value != null
    }
    
    void testGroovy441() {
        def arg = "test"
        def content = "${arg} ="

        if (arg != "something") {
            content += "?"
        }

        content += "= ${arg}."

        assert content == "test =?= test."
    }

    void testTwoStringsInMiddle() {
        def a = "---"
        def b = "${a} :"
        b += "<<"
        b += ">>"
        b += ": ${a}"
        assert b == "--- :<<>>: ---"
    }

    void testAlternatingGStrings() {
        def a = "---"
        def b = "${a} :"
        b += "<<"
        b += " [[${a}]] "
        b += ">>"
        b += ": ${a}"
        assert b == "--- :<< [[---]] >>: ---"
    }
 }
