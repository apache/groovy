package groovy.swt;

import groovy.lang.GroovyObject;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunDemoBrowser extends SwtTest {
    
    public static void main(String[] args) throws Exception {
        RunDemoBrowser demo = new RunDemoBrowser();
        GroovyObject object = demo.compile("src/examples/groovy/swt/BrowserDemo.groovy");
        object.invokeMethod("run", null);
    }
}
