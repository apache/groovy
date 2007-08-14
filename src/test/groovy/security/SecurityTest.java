package groovy.security;

import groovy.lang.GroovyCodeSource;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Security;
import java.util.PropertyPermission;

/**
 * Test the effects of enabling security in Groovy.  Some tests below check for proper framework
 * behavior (e.g. ensuring that GroovyCodeSources may only be created for which proper permissions exist).
 * Other tests run .groovy scripts under a secure environment and ensure that the proper permissions
 * are required for success.
 * <p/>
 * Todo: find out why the marked tests are environment specific and why security tests are not
 * running on the build server.
 *
 * @author Steve Goetze
 */
public class SecurityTest extends SecurityTestSupport {

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(SecurityTest.class);
    }

    public void testForbiddenProperty() {
        String script = "System.getProperty(\"user.home\")";
        assertExecute(script, null, new PropertyPermission("user.home", "read"));
    }

    public void testForbiddenPackage() {
        String script = "import sun.net.*; s = new NetworkClient()";
        assertExecute(script, "/groovy/security/testForbiddenPackage", new RuntimePermission("accessClassInPackage.sun.*"));
    }

    public void testForbiddenCodebase() {
        assertExecute(new File("src/test/groovy/security/forbiddenCodeBase.gvy"), new GroovyCodeSourcePermission("/groovy/security/forbiddenCodeBase"));
    }

    public void testForbiddenCodebaseWithActions() {
        assertExecute(new File("src/test/groovy/security/forbiddenCodeBase.gvy"), new GroovyCodeSourcePermission("/groovy/security/forbiddenCodeBase", "unused actions string"));
    }

    //Check that the Security package.access control works.
    public void testPackageAccess() {
        String script = "new javax.print.PrintException();";
        Security.setProperty("package.access", "javax.print");
        //This should throw an ACE because its codeBase does not allow access to javax.print
        assertExecute(script, "/groovy/security/javax/print/deny", new RuntimePermission("accessClassInPackage.javax.print"));
        //This should not throw an ACE because groovy.policy grants the codeBase access to javax.print
        assertExecute(script, "/groovy/security/javax/print/allow", null);
    }

    public void testBadScriptNameBug() {
        assertExecute(new File("src/test/groovy/bugs/BadScriptNameBug.groovy"), null);
    }

    public void testClosureListenerTest() {
        //if (System.getProperty("java.version").startsWith("1.5") && notYetImplemented()) return;
        if (System.getProperty("java.version").startsWith("1.5")) return;
        assertExecute(new File("src/test/groovy/ClosureListenerTest.groovy"), null);
    }

    public void testClosureMethodTest() {
        assertExecute(new File("src/test/groovy/ClosureMethodTest.groovy"), null);
    }

    public void testGroovyMethodsTest_FAILS() {
        if (notYetImplemented()) return;
        assertExecute(new File("src/test/groovy/GroovyMethodsTest.groovy"), null);
    }

    public void testClosureWithDefaultParamTest() {
        assertExecute(new File("src/test/groovy/ClosureWithDefaultParamTest.groovy"), null);
    }

    public void testGroovy303_Bug() {
        assertExecute(new File("src/test/groovy/bugs/Groovy303_Bug.groovy"), null);
    }

    public void testScriptTest() {
        assertExecute(new File("src/test/groovy/script/ScriptTest.groovy"), null);
    }

    //In addition to requiring several permissions, this test is an example of the case
    //where the groovy class loader is required at script invocation time as well as
    //during compilation.
    public void testSqlCompleteWithoutDataSourceTest() {
        assertExecute(new File("src/test/groovy/sql/SqlCompleteWithoutDataSourceTest.groovy"), null);
    }

    //Test to prevent scripts from invoking the groovy compiler.  This is done by restricting access
    //to the org.codehaus.groovy packages.
    public void testMetaClassTest() {
        //Security.setProperty("package.access", "org.codehaus.groovy");
        //assertExecute(new File("src/test/org/codehaus/groovy/classgen/MetaClassTest.groovy"), new RuntimePermission("accessClassInPackage.org.codehaus.groovy"));
    }

    //Mailing list post by Richard Hensley reporting a CodeSource bug.  A GroovyCodeSource created
    //with a URL was causing an NPE.
    public void testCodeSource() throws IOException, CompilationFailedException {
        URL script = loader.getResource("groovy/ArrayTest.groovy");
        try {
            new GroovyCodeSource(script);
        } catch (RuntimeException re) {
            assertEquals("Could not construct a GroovyCodeSource from a null URL", re.getMessage());
        }
    }

}
