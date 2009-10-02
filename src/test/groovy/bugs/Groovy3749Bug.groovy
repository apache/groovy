package groovy.bugs

class Groovy3749Bug extends GroovyTestCase {
    void testScriptsProvidingStaticMainMethod() {
        def scriptStr
        
        // test various signatures of main()
        scriptStr = """
            static main(args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")
        
        scriptStr = """
            static def main(args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")
        
        scriptStr = """
            static void main(args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")

        scriptStr = """
            static main(String[] args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")

        scriptStr = """
            static def main(String[] args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")

        scriptStr = """
            static void main(String[] args) {
                throw new RuntimeException('main called')
            }
        """
        verifyScriptRun(scriptStr, "RuntimeException")
        
        // if both main() and the loose statements are provided, then the loose statements should run and not main
        scriptStr = """
            static main(args) {
                throw new RuntimeException('main called')
            }
            throw new Error()
        """
        verifyScriptRun(scriptStr, "Error")
        
        assertScript """
            def main(args) {
                throw new RuntimeException('main called')
            }
        """
    }
    
    void verifyScriptRun(scriptText, expectedFailure) {
        try{
            assertScript(scriptText)
        }catch(Throwable ex) {
            assertTrue ex.class.name.contains(expectedFailure) 
        }
    }
}
