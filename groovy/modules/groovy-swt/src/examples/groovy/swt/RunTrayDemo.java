package groovy.swt;

import groovy.lang.GroovyObject;
import groovy.swt.SwtTest;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunTrayDemo extends SwtTest {
    
    public static void main(String[] args) throws Exception {
        RunTrayDemo demo = new RunTrayDemo();
        GroovyObject object = demo.compile("src/examples/groovy/swt/TrayDemo.groovy");
        object.invokeMethod("run", null);
    }
    
}
