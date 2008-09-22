package groovy.util

/**
 * Groovy test case, which recreates internal GroovyShell in each setUp ()
 *
 * @author Alex Tkachman
 */
class GroovyShellTestCase extends GroovyTestCase {

    @Delegate protected GroovyShell shell

    protected void setUp() {
        super.setUp();
        shell = createNewShell();
    }

    protected void tearDown() {
        shell = null;
        super.tearDown();
    }

    /**
     * Create new shell instance.
     * Overwrite it to customize
     */
    protected GroovyShell createNewShell() {
        return new GroovyShell()
    }

    /**
     * Executes closure with given binding
     */
    protected def withBinding (Map map, Closure closure) {
        Binding binding = shell.context
        Map bmap = binding.variables
        try {
            Map vars = new HashMap(bmap)
            bmap.putAll map

            return closure.call()
        }
        finally {
            bmap.clear()
            bmap.putAll vars
        }
    }

    /**
     * Evaluates script with given binding
     */
    protected def withBinding (Map map, String script) {
        Binding binding = shell.context
        Map bmap = binding.variables
        try {
            Map vars = new HashMap(bmap)
            bmap.putAll map

            return evaluate(script)
        }
        finally {
            bmap.clear()
            bmap.putAll vars
        }
    }
}