package groovy.swt;

import groovy.lang.GroovyObject;
import groovy.swt.SwtTest;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunSashFormDemo extends SwtTest {
    
    public static void main(String[] args) throws Exception {
        RunSashFormDemo demo = new RunSashFormDemo();
        GroovyObject object = demo.compile("src/examples/groovy/swt/SashFormDemo.groovy");
        object.invokeMethod("run", null);
    }
    
}
