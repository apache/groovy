/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy

import groovy.io.GroovyPrintStream
import groovy.test.GroovyTestCase

import java.text.NumberFormat

class PrintTest extends GroovyTestCase {
    PrintStream savedSystemOut
    private locale
    
    void setUp() {
        savedSystemOut = System.out
        locale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }
    
    void tearDown() {
        Locale.setDefault(locale)
        System.setOut(savedSystemOut)        
    }

    void testToString() {
        assertToString("hello", 'hello')

        assertToString([], "[]")
        assertToString([1, 2, "hello"], '[1, 2, hello]')

        assertToString([1: 20, 2: 40, 3: 'cheese'], '[1:20, 2:40, 3:cheese]')
        assertToString([:], "[:]")

        assertToString([['bob': 'drools', 'james': 'geronimo']], '[[bob:drools, james:geronimo]]')
        assertToString([5, ["bob", "james"], ["bob": "drools", "james": "geronimo"], "cheese"], '[5, [bob, james], [bob:drools, james:geronimo], cheese]')
    }

    void testInspect() {
        assertInspect("hello", "'hello'")

        assertInspect([], "[]")
        assertInspect([1, 2, "hello"], "[1, 2, 'hello']")

        assertInspect([1: 20, 2: 40, 3: 'cheese'], "[1:20, 2:40, 3:'cheese']")
        assertInspect([:], "[:]")

        assertInspect([['bob': 'drools', 'james': 'geronimo']], "[['bob':'drools', 'james':'geronimo']]")
        assertInspect([5, ["bob", "james"], ["bob": "drools", "james": "geronimo"], "cheese"], "[5, ['bob', 'james'], ['bob':'drools', 'james':'geronimo'], 'cheese']")
    }

    void testCPlusPlusStylePrinting() {
        def endl = "\n"
        System.out << "Hello world!" << endl
    }

    void testSprintf() {
        def decimalSymbol = NumberFormat.instance.format(1.5) - '1' - '5'
        assert sprintf('%5.2f', 12 * 3.5) == "42${decimalSymbol}00"
        assert sprintf('%d + %d = %d', [1, 2, 1 + 2] as Integer[]) == '1 + 2 = 3'
        assert sprintf('%d + %d = %d', [2, 3, 2 + 3] as int[]) == '2 + 3 = 5'
        assert sprintf('%d + %d = %d', [3, 4, 3 + 4] as long[]) == '3 + 4 = 7'
        assert sprintf('%d + %d = %d', [4, 5, 4 + 5] as byte[]) == '4 + 5 = 9'
        assert sprintf('%d + %d = %d', [5, 6, 5 + 6] as short[]) == '5 + 6 = 11'
        def floatExpr = sprintf('%5.2f + %5.2f = %5.2f', [3, 4, 3 + 4] as float[])
        assertEquals " 3${decimalSymbol}00 +  4${decimalSymbol}00 =  7${decimalSymbol}00", floatExpr
        def doubleExpr = sprintf('%5.2g + %5.2g = %5.2g', [3, 4, 3 + 4] as double[])
        // TODO: work out why decimalSymbol is not used here (at least for FR and RU)
        assertEquals "  3.0 +   4.0 =   7.0", doubleExpr
        assert sprintf('hi %s', 'there') == 'hi there'
        assert sprintf('%c', 0x41) == 'A'
        assert sprintf('%x', 0x41) == '41'
        assert sprintf('%o', 0x41) == '101'
        assert sprintf('%h', 0x41) == '41'
        assert sprintf('%b %b', [true, false] as boolean[]) == 'true false'
    }

    void testSprintfExceptionPropagation() {
        shouldFail(IllegalArgumentException) {
            sprintf('%2.4f', [3])
        }
    }

def NEWLINE = System.getProperty("line.separator")

void doTest(def param) {
    StringWriter sw1 = new StringWriter()
    StringWriter sw2 = new StringWriter()
    StringWriter sw3 = new StringWriter()
    StringWriter sw4 = new StringWriter()
    StringWriter sw5 = new StringWriter()
    ByteArrayOutputStream baos1 = new ByteArrayOutputStream()
    ByteArrayOutputStream baos2 = new ByteArrayOutputStream()
    ByteArrayOutputStream baos3 = new ByteArrayOutputStream()
    
    sw1.write(param as String)
    sw2.print(param)
    sw3.withPrintWriter { it.print param }
    new PrintWriter(sw4).print(param)
    sw5.newPrintWriter().print(param)
    new PrintStream(baos1).print(param)
    new GroovyPrintStream(baos2).print(param)
    System.setOut(new PrintStream(baos3))
    print(param)

    def t1 = sw1.toString()
    def t2 = sw2.toString()
    def t3 = sw3.toString()
    def t4 = sw4.toString()
    def t5 = sw5.toString()
    def t6 = baos1.toString()
    def t7 = baos2.toString()
    def t8 = baos3.toString()
    
    assert t1 == t2
    assert t1 == t3
    assert t1 == t4
    assert t1 == t5
    assert t1 == t6
    assert t1 == t7
    assert t1 == t8

    sw1.buffer.length = 0
    sw2.buffer.length = 0
    sw3.buffer.length = 0
    sw4.buffer.length = 0
    sw5.buffer.length = 0
    baos1.reset()
    baos2.reset()
    baos3.reset()

    sw1.write(param as String)
    sw1.write(NEWLINE)
    sw2.println(param)
    sw3.withPrintWriter { it.println param }
    new PrintWriter(sw4).println(param)
    sw5.newPrintWriter().println(param)
    new PrintStream(baos1).println(param)
    new GroovyPrintStream(baos2).println(param)
    System.setOut(new PrintStream(baos3))
    println(param)

    t1 = sw1.toString()
    t2 = sw2.toString()
    t3 = sw3.toString()
    t4 = sw4.toString()
    t5 = sw5.toString()
    t6 = baos1.toString()
    t7 = baos2.toString()
    t8 = baos3.toString()
    
    assert t1 == t2
    assert t1 == t3
    assert t1 == t4
    assert t1 == t5
    assert t1 == t6
    assert t1 == t7
    assert t1 == t8
}

void testGroovy3227() { 
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
    doTest(new Date(107, 12, 31))
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
