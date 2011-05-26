
/**
* Unit test for ArithmeticShell.
* Requires JUnit to be in path, just like any other GroovyTestCase. 
*
* @author Hamlet D'Arcy
*/
class ArithmeticShellTest extends GroovyTestCase {

    private ArithmeticShell shell

    void setUp() {
        shell = new ArithmeticShell()
    }

    Number evaluate(String text) {
        shell.evaluate(text)
    }

    void testEvaluate_SuccessfulPaths() {
        assert 2.9073548971824276E135 == evaluate("((6L / 2f) - 1) ** 4.5e2")
        assert -6.816387600233341 == evaluate("10 * Math.sin(15/-20)")
        assert 1.0 == evaluate("Math.cos(2*Math.PI)")
        assert 74.17310622494026 == shell.evaluate("80*Math.E**(-(+(11++/40)**2))")
        assert 2147483646 == evaluate("Integer.MAX_VALUE - ++2%2")
        assert 6 == evaluate("++(5)")
        assert 0 == evaluate("5 < 4 ? 1 : 0")
        assert 0 == evaluate("5 != 4 ? 0 : 1")
        assert 0 == evaluate("5 < 4 ?: 0 ")
    }

    void testEvaluate_StaticImportOfMath() {
        assert 6.283185307179586 == evaluate("2*PI")
        assert 0.5403023058681398 == evaluate("cos(1)")
        assert 1.0 == evaluate("cos(2*PI)")
    }

    void testEvaluate_Failures() {
        shouldFail(SecurityException) {
            evaluate("Double.valueOf(\"5\")")
        }

        shouldFail(SecurityException) {
            evaluate("import java.text.DateFormat; 5")
        }

        shouldFail(SecurityException) {
            evaluate("import static java.lang.System.*; 6 * out")
        }

        shouldFail(SecurityException) {
            evaluate("def x = 5+3;x.toString()")
        }

        shouldFail(SecurityException) {
            evaluate("new File();Double.valueOf('5')")
        }

        shouldFail(SecurityException) {
             // without statement whitelist set, this causes the arithmetic shell to
             // enter an infinite loop
             evaluate("while (1);")    
        }

        shouldFail(SecurityException) {
             // without statement whitelist set, security exception is still thrown as it should,
             // but with the error message that closures are not allowed, which may be confusing
             evaluate("for (;;);")
        }

        shouldFail(SecurityException) {
             // without statement whitelist set, no exception is thrown
             evaluate("if (1) 12 else 15")    
        }

        shouldFail(SecurityException) {
             // without expression whitelist set, no exception is thrown
             evaluate("[1,2]; 1")
        }

        shouldFail(SecurityException) {
             // without expression whitelist set, no exception is thrown
             evaluate("[1:2]; 1")
        }

        shouldFail(SecurityException) {
             // without expression whitelist set, no exception is thrown
             evaluate("new Object(); 1")
        }
    }
}
