/*
 * Created on Feb 25, 2004
 *
 */
package groovy.swt;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Shell;


/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class SwtTest extends TestCase {
    public GroovyObject compile(String fileName) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader();
        Class groovyClass = loader.parseClass(new File(fileName));
        GroovyObject object = (GroovyObject) groovyClass.newInstance();
        assertTrue(object != null);
        return object;
    }
    
    public void testSwt() {
        Shell shell = new Shell();
        shell.dispose();
    }
    
    public void testJFace() {
        Shell shell = new Shell();
        ApplicationWindow window = new ApplicationWindow(shell);
        shell.dispose();
    }
}
