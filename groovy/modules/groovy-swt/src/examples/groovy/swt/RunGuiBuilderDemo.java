package groovy.swt;

import groovy.lang.GroovyObject;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunGuiBuilderDemo extends SwtTest {
    
    public static void main(String[] args) throws Exception {
        RunGuiBuilderDemo demo = new RunGuiBuilderDemo();
        GroovyObject object = demo.compile("src/examples/groovy/swt/GuiBuilderDemo.groovy");
        object.invokeMethod("run", null);
    }
    
}
