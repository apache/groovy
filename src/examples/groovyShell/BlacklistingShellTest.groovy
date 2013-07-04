/**
* Unit test for BlacklistingShell.
* Requires JUnit to be in path, just like any other GroovyTestCase. 
*
* @author Jim Driscoll jamesgdriscoll@gmail.com
*/
class BlacklistingShellTest extends GroovyTestCase {

    private BlacklistingShell shell

    void setUp() {
        shell = new BlacklistingShell()
    }

    Object evaluate(String text) {
        shell.evaluate(text)
    }

    void testEvaluate_SuccessfulPaths() {
        assert 6 == evaluate("++(5)")
        assert 0 == evaluate("5 < 4 ? 1 : 0")
        assert 0 == evaluate("5 != 4 ? 0 : 1")
        assert 0 == evaluate("5 < 4 ?: 0 ")
    }

    void testEvaluate_Failures() {
        shouldFail(SecurityException) {
            evaluate('def c = System.class; c.forName("java.lang.System").exit(0)')
        }
        shouldFail(SecurityException) {
            evaluate('Class.forName("java.lang.System").exit(0)')
        }
        shouldFail(SecurityException) {
            evaluate('System.exit(0)')
        }
        shouldFail(SecurityException) {
            evaluate('def e = System.&exit; e.call(0)')
        }
        shouldFail(SecurityException) {
            evaluate('System.&exit.call(0)')
        }
        shouldFail(SecurityException) {
            evaluate('System.getMetaClass().invokeMethod("exit",0)')
        }
        shouldFail(SecurityException) {
            evaluate('evaluate("System.exit(0)")')
        }
        shouldFail(SecurityException) {
            evaluate('(new GroovyShell()).evaluate("System.exit(0)")')
        }
        shouldFail(SecurityException) {
            evaluate('def sh = new GroovyShell(); sh.evaluate("System.exit(0)")')
        }
        shouldFail(SecurityException) {
            evaluate('Eval.me("System.exit(0)")')
        }
        shouldFail(SecurityException) {
            evaluate('def s = System; s.exit(0)')
        }
        shouldFail(SecurityException) {
            evaluate('Script t = this; t.evaluate("System.exit(0)")')
        }
    }
}