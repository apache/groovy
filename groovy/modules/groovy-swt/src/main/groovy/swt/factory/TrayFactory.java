/*
 * Created on Feb 15, 2004
 *  
 */
package groovy.swt.factory;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class TrayFactory extends AbstractSwtFactory implements SwtFactory {

    /*
     * @see groovy.swt.impl.Factory#newInstance(java.util.Map, java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {
        return Display.getCurrent().getSystemTray();
    }

}