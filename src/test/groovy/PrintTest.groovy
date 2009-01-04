package groovy

import java.text.NumberFormat

class PrintTest extends GroovyTestCase {

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
        assertInspect("hello", '"hello"')

        assertInspect([], "[]")
        assertInspect([1, 2, "hello"], '[1, 2, "hello"]')

        assertInspect([1: 20, 2: 40, 3: 'cheese'], '[1:20, 2:40, 3:"cheese"]')
        assertInspect([:], "[:]")

        assertInspect([['bob': 'drools', 'james': 'geronimo']], '[["bob":"drools", "james":"geronimo"]]')
        assertInspect([5, ["bob", "james"], ["bob": "drools", "james": "geronimo"], "cheese"], '[5, ["bob", "james"], ["bob":"drools", "james":"geronimo"], "cheese"]')
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
}
