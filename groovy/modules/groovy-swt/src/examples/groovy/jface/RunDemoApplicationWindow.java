package groovy.jface;

import groovy.lang.GroovyObject;
import groovy.swt.SwtTest;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunDemoApplicationWindow extends SwtTest {
    public void testWizardDemo() throws Exception {
        GroovyObject object = compile("src/examples/groovy/jface/ApplicationWindowDemo.groovy");
        object.invokeMethod("run", null);
    }
}
