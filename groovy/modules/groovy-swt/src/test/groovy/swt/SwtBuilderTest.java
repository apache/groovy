/*
 * Created on Mar 6, 2004
 *
 */
package groovy.swt;

import groovy.lang.GroovyObject;
import junit.framework.TestCase;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> 
 * $Id$
 */
public class SwtBuilderTest extends TestCase {
    public void testBasic() throws Exception {
        SwtTest demo = new SwtTest();
        GroovyObject object = demo.compile("src/test/groovy/swt/SwtBuilderTest1.groovy");
        object.invokeMethod("run", null);
    }
}
