
import groovy.lang.GroovyShell;
import junit.framework.TestCase;

/**
 * @author Guillaume Laforge
 */
public class ScriptTest extends TestCase
{
    public static final String script =
        "import org.codehaus.groovy.scriptom.ActiveXProxy\n" +
        "import com.jacob.com.*\n" +
        "def sc = new ActiveXProxy(\"ScriptControl\")\n" +
        "sc.events.Error = { println \"!!! ERROR !!!\" }\n" +
        "sc.events.listen()\n" +
        "sc.Language = \"VBScript\"\n" +
        "sc.AllowUI = true\n" +
        "sc.Eval(\"+\")";

    public static final String ieScript =
        "import org.codehaus.groovy.scriptom.ActiveXProxy\n" +
        "def explorer = new ActiveXProxy(\"InternetExplorer.Application\")\n" +
        "explorer.events.OnQuit =           { println \"quiting\" }\n" +
        "explorer.events.DocumentComplete = { println \"document complete\" }\n" +
        "explorer.events.StatusTextChange = { println \"status text changed\" }\n" +
        "explorer.events.listen()\n" +
        "explorer.Visible = true\n" +
        "explorer.AddressBar = true\n" +
        "explorer.Navigate(\"http://glaforge.free.fr/weblog/\")\n" +
        "Thread.sleep(5000)\n" +
        "explorer.Quit()";

    public void testExceptionThrownAndEventTriggered()
    {
        GroovyShell sh = new GroovyShell();
        try
        {
            sh.evaluate(script);
            fail("The expression should not have been evaluated properly");
        }
        catch (Throwable e)
        {
            System.out.println(e.getMessage());
            System.out.println(e.getClass());
        }
    }
}
