package groovy.swt.scrapbook;

import groovy.lang.Binding;
import groovy.swt.SwtTest;
import groovy.util.GroovyScriptEngine;

import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunDynamicUIDemo extends SwtTest {
    public void testBasic() throws Exception {
        NamedObject namedObject = new NamedObject();
        
//        Shell parent = new Shell();
        DynamicUIBuilder builder  = new DynamicUIBuilder();
        Shell shell = (Shell) builder.createNode("shell");
        
        Binding binding = new Binding();
        binding.setVariable("obj", namedObject);
        binding.setVariable("builder", builder);
        binding.setVariable("parent", shell);
        
        GroovyScriptEngine scriptEngine = new GroovyScriptEngine("src/test/groovy/swt/dynamvc/");
        Object object = scriptEngine.run("NamedObjectUI.groovy", binding);

        shell.open();
        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                shell.getDisplay().sleep();
            }
        }
    }
}
