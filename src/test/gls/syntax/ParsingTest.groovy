package gls.syntax

public class ParsingTest extends gls.CompilableTestSupport {
    void testExpressionParsingWithCastingInFrontOfAClosure() {
        int[] numbers = new int[3]

        shouldCompile """
            (String) {-> print ""}.call()
        """
    
        shouldCompile """
            (String[]) {-> print ""}.call()
        """

        shouldCompile """
            (short) {-> print numbers[0]}.call()
        """
        
        shouldCompile """
            (short[]) {-> print numbers}.call()
        """
        def testObj = new Groovy2605()

        def val1 = (Groovy2605) {-> return testObj}.call()
        assert val1 instanceof Groovy2605
        
        def val2 = (String){-> return testObj}.call()
        assert val2 instanceof String
        assert val2 == "[A Groovy2605 object]"

        def val3 = (short) {-> return numbers[0]}.call()
        assert val3 instanceof Short

        def val4 = (short[]) {-> return numbers}.call()
        assert val4.class.componentType == short
    }
}

class Groovy2605 {
    String toString(){
        return "[A Groovy2605 object]"
    }
}
