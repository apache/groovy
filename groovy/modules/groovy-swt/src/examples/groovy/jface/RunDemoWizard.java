package groovy.jface;

import groovy.lang.GroovyObject;
import groovy.swt.SwtTest;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunDemoWizard extends SwtTest {
    
    public static void main(String[] args) throws Exception {
        RunDemoWizard demo = new RunDemoWizard();
        GroovyObject object = demo.compile("src/examples/groovy/jface/WizardDemo.groovy");
        object.invokeMethod("run", null);
    }
    
}
