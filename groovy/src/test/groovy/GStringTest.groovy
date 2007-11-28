package groovy

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
         check(/hello $name/, teststr)
         check(/hello ${name}/, teststr)
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
         check("${assert name == "Bob"; name}", teststr)
         // Put punctuation after the variable name:
         check("$name.", "Bob.")
         check("$name...", "Bob...")
         check("$name?", "Bob?")

         check(/$name/, teststr)
         check(/${name}/, teststr)
         check(/${assert name == "Bob"; name}/, teststr)
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
         assert c.toString() == "a dog cat", c
         b += " cat"
         assert b.toString() == "a dog cat", b
     }

     void testAppendGString() {
         def a = "dog"
         def b = "a ${a}"
         b += " cat${a}"
         assert b.toString() == "a dog catdog", b
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
         def enc = "US-ASCII"
         def value = "test".getBytes("${enc}")
         assert value == [116, 101, 115, 116]
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

     // Test case for GROOVY-599
     void testGStringInStaticMethod() {
         int value = 2
         String str = "1${value}3"
         int result = Integer.parseInt(str)
         assert result == 123
         result = Integer.parseInt("1${value}3")
         assert result == 123
     }

     // Test case for GROOVY-2275
     void testGetAtWithRange() {
         def number = 1234567
         def numberString = "${number}"
         def realString = "1234567"
         assert numberString[0..-1] == '1234567'
         assert realString[0..-1] == '1234567'
     }

     void testEmbeddedClosures() {
         def c1 = {-> "hello"}
         def c2 = {out -> out << "world"}
         def c3 = {a, b -> b << a}
         def c4 = c3.curry(5)

         def g1 = "${-> "hello"} ${out -> out << "world"}"
         def g2 = "$c1 $c2"
         def g3 = "${-> c1} ${-> c2}"
         def g4 = "$c4"
         def g5 = "$c3"

         def w = new StringWriter()
         w << g1
         assertEquals(w.buffer.toString(), "hello world")
         assertEquals(g1.toString(), "hello world")
         w = new StringWriter()
         w << g2
         assertEquals(w.buffer.toString(), "hello world")
         assertEquals(g2.toString(), "hello world")
         w = new StringWriter()
         w << g3
         assert w.buffer.toString().contains("closure")
         assert g3.toString().contains("closure")
         w = new StringWriter()
         w << g4
         assertEquals(w.buffer.toString(), "5")
         assertEquals(g4.toString(), "5")
         try {
             println g5
             fail("should throw a GroovyRuntimeException")
         } catch (GroovyRuntimeException e) {
         }
         try {
             println g5.toString()
             fail("should throw a GroovyRuntimeException")
         } catch (GroovyRuntimeException e) {
         }
     }
 }
