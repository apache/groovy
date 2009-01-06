package groovy.bugs

public class Groovy3237Bug extends GroovyTestCase {

def NEWLINE = System.getProperty("line.separator")

void doTest(def param) {
    StringWriter sw1 = new StringWriter()
    StringWriter sw2 = new StringWriter()
    StringWriter sw3 = new StringWriter()

    sw1.write(param.toString())
    sw2.print(param)
    sw3.newPrintWriter().print(param)

    def t1 = sw1.toString()
    def t2 = sw2.toString()
    def t3 = sw3.toString()
    
    assert t1 == t2
    assert t1 == t3

    sw1 = new StringWriter()
    sw2 = new StringWriter()
    sw3 = new StringWriter()

    sw1.write(param.toString())
    sw1.write(NEWLINE)
    sw2.println(param)
    sw3.withPrintWriter { it.println(param) }

    t1 = sw1.toString()
    t2 = sw2.toString()
    t3 = sw3.toString()
    
//    println "${param.getClass()} - ${param.toString()} - ${String.valueOf(param)}"
    
    assert t2 == t3
    assert t1 == t2
    assert t1 == t3
}

void testGroovy3237ValueOf() {
    def bi = new BigInteger("123456789012345678901234567890")
    def bts = bi.toString()
    def bvs = String.valueOf(bi)
    
    assert bts == bvs
}

void testGroovy3237() { 
    doTest(null)
    doTest("foo")
    doTest(true)
    doTest(false)
    doTest((byte)123)
    doTest((short)1234)
    doTest(new Integer(1234))
    doTest(new Long(9999999999))
    doTest(new Float(1234.5678))
    doTest(new Double(1234.5678))
    doTest(new BigInteger("123456789012345678901234567890"))
    doTest(new BigDecimal("12345678901234567890.1234567890123456789"))
    doTest(new Date())
    doTest(new StringBuffer("bar"))
    doTest([null, "foo", true, false, new Integer(1234)])
    doTest(["foo" : "bar", "true": true, "int": new Integer(1234)])
    doTest([null, "foo", true, false, new Integer(1234)] as Object[])
    doTest(["foo",new Integer(1234)] as String[])
    doTest([true, false] as Boolean[])
    doTest([true, false] as boolean[])
    doTest([1, 2, 3] as int[])
    doTest([1, 2, 3] as Integer[])
    doTest(['a', 'b', 'c'] as char[])
    doTest(['a', 'b', 'c'] as Character[])
}

}
