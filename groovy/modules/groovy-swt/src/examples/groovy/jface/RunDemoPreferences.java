package groovy.jface;

import groovy.lang.GroovyObject;
import groovy.swt.SwtTest;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class RunDemoPreferences extends SwtTest {

    public static void main(String[] args) throws Exception {
        RunDemoPreferences demo = new RunDemoPreferences();
        GroovyObject object = demo.compile("src/examples/groovy/jface/PreferencesDemo.groovy");
        object.invokeMethod("run", null);
    }

}