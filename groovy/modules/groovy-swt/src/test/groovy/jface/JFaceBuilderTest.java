/*
 * Created on Mar 6, 2004
 *
 */
package groovy.jface;

import groovy.lang.GroovyObject;
import groovy.swt.SwtTest;
import junit.framework.TestCase;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> 
 * $Id$
 */
public class JFaceBuilderTest extends TestCase {
    public void testBasic() throws Exception {
        SwtTest demo = new SwtTest();
        GroovyObject object = demo.compile("src/test/groovy/jface/JFaceBuilderTest1.groovy");
        object.invokeMethod("run", null);
    }
}
