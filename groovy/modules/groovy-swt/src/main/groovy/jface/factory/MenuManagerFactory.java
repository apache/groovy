package groovy.jface.factory;

import groovy.lang.MissingPropertyException;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;
import java.util.Map;
import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class MenuManagerFactory extends AbstractSwtFactory implements SwtFactory {

    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
    throws GroovyException {
        
        String text = (String) properties.remove("text");
        if (text == null) {
            throw new MissingPropertyException("text", 
            String.class);
        }
        
        MenuManager menuManager = new MenuManager(text);
        if (parent instanceof ApplicationWindow) {
            ApplicationWindow window = (ApplicationWindow) parent;
            if (window != null) {
                window.getMenuBarManager().add(menuManager);
            }
        }
        
        return menuManager;
    }
}
