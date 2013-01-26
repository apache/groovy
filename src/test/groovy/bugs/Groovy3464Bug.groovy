package groovy.bugs

import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * GROOVY-3463:
 * Spring/CGLIB proxies throw exception "object is not an instance of declaring class"
 *
 * @author Guillaume Laforge
 */
class Groovy3464Bug extends GroovyTestCase {

    GroovyShell shell
    File targetDir, stubDir

    protected void setUp() {
        super.setUp()

        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = createTempDir()
            jointCompilationOptions = [stubDir: createTempDir()]
        }

        def groovyFile = new File('GroovyThing.groovy', config.targetDirectory)
        def javaFile = new File('JavaThing.java', config.targetDirectory)

        groovyFile << '''
            class GroovyThing {
                String m1() { "thing.m1" }
                String m2() { m1() + " called from thing.m2"}
            }
            '''

        javaFile << '''
            public class JavaThing extends GroovyThing {
                public String m3() {
                    return "javaThing.m3 calling m2 " + m2();
                }
            }
            '''

        def loader = new GroovyClassLoader(this.class.classLoader)
        def cu = new JavaAwareCompilationUnit(config, loader)
        cu.addSources([groovyFile, javaFile] as File[])
        try {
            cu.compile()
        } catch (any) {
            any.printStackTrace()
            assert false, "Compilation of the Groovy and Java files should have succeeded"
        }

        this.shell = new GroovyShell(loader)

    }

    protected void tearDown() {
        targetDir?.deleteDir()
        stubDir?.deleteDir()

        super.tearDown()
    }

    void testScenarioOne() {
        shouldFail(MissingMethodException) {
            shell.evaluate '''
                def t = new GroovyThing()
                assert t.m3() == "javaThing.m3 calling m2 thing.m1 called from thing.m2"

                t = new JavaThing()
                t.m3()
                assert false, "Method m3() should not be found"
            '''
        }
    }

    void testScenarioTwo() {
        shell.evaluate '''
            def t = new GroovyThing()
            assert t.m2() == "thing.m1 called from thing.m2"

            t = new JavaThing()
            assert t.m3() == "javaThing.m3 calling m2 thing.m1 called from thing.m2"

        '''
    }

    static File createTempDir() {
        File tempDirectory = File.createTempDir("Groovy3464Bug", Long.toString(System.currentTimeMillis()))
        return tempDirectory
    }
}

