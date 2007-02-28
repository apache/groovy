package groovy

import java.text.NumberFormat

class PrintTest extends GroovyTestCase {

    void testToString() {
        assertToString("hello", 'hello')

        assertToString([], "[]")
        assertToString([1, 2, "hello"], '[1, 2, hello]')

        // TODO: change toString on Map to produce same as inspect method
        assertToString([1:20, 2:40, 3:'cheese'], '{1=20, 2=40, 3=cheese}')
        assertToString([:], "{}")

        // TODO: change toString on Map to produce same as inspect method
        assertToString([['bob':'drools', 'james':'geronimo']], '[{james=geronimo, bob=drools}]')
        // TODO: change toString on Map to produce same as inspect method
        assertToString([5, ["bob", "james"], ["bob":"drools", "james":"geronimo"], "cheese"], '[5, [bob, james], {james=geronimo, bob=drools}, cheese]')
    }

    void testInspect() {
        assertInspect("hello", '"hello"')
        
        assertInspect([], "[]")
        assertInspect([1, 2, "hello"], '[1, 2, "hello"]')
        
        assertInspect([1:20, 2:40, 3:'cheese'], '[1:20, 2:40, 3:"cheese"]')
        assertInspect([:], "[:]")

        assertInspect([['bob':'drools', 'james':'geronimo']], '[["james":"geronimo", "bob":"drools"]]')
        assertInspect([5, ["bob", "james"], ["bob":"drools", "james":"geronimo"], "cheese"], '[5, ["bob", "james"], ["james":"geronimo", "bob":"drools"], "cheese"]')
    }

    void testCPlusPlusStylePrinting() {
        def endl = "\n"
        System.out << "Hello world!" << endl
    }

    void testSprintf() {
        if (System.properties.'java.version'[2] >= '5') {
            // would be nice to use JDK 1.6 DecimalFormatSymbols
            def decimalSymbol = NumberFormat.instance.format(1.5) - '1' - '5'
            assert sprintf('%5.2f', 12 * 3.5) == "42${decimalSymbol}00"
            assert sprintf('%d + %d = %d' , [1, 2, 1+2] as Integer[]) == '1 + 2 = 3'
            assert sprintf('%d + %d = %d' , [3, 4, 3+4] as int[]) == '3 + 4 = 7'
        }
    }
}
