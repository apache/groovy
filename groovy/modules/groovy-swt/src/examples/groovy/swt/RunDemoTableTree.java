package groovy.swt;

import groovy.lang.GroovyObject;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunDemoTableTree extends SwtTest {
    
    public static void main(String[] args) throws Exception {
        RunDemoTableTree demo = new RunDemoTableTree();
        GroovyObject object = demo.compile("src/examples/groovy/swt/TableTreeDemo.groovy");
        object.invokeMethod("run", null);
    }
    
}
