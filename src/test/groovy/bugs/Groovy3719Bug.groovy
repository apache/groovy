package groovy.bugs

class Groovy3719Bug extends GroovyTestCase {
    void testScriptThrowingNoSuchMethodException() {
        try {
            GroovyShell shell = new GroovyShell();
            String[] args = new String[0];
            shell.run(new File("src/test/groovy/bugs/Groovy3719Bug_script.groovy"), args);
        } catch(ex) {
            assert ex instanceof NoSuchMethodException
        }
    }
}
