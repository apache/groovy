package groovy.swt;

import groovy.lang.GroovyObject;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunDemoTree extends SwtTest {
    public void testBasic() throws Exception {
        GroovyObject object = compile("src/examples/groovy/swt/TreeDemo.groovy");
        object.invokeMethod("run", null);
    }
}
