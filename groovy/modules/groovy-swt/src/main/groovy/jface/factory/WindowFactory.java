/*
 * Created on Feb 28, 2004
 *
 */
package groovy.jface.factory;

import groovy.swt.InvalidParentException;
import groovy.swt.convertor.PointConverter;
import groovy.swt.factory.SwtFactory;
import groovy.swt.factory.WidgetFactory;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class WindowFactory extends WidgetFactory implements SwtFactory {

    /**
     * @param beanClass
     */
    public WindowFactory(Class beanClass) {
        super(beanClass);
    }

    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent) throws GroovyException {
        if (parent == null){
            parent = new Shell();
        }
        if (!(parent instanceof Shell)){
            throw new InvalidParentException("shell");
        }
        
        Window window = (Window) createWidget(parent);
        if (window != null){
            Shell shell = (Shell) parent;

            // set title of Window
            String title = (String) properties.remove("title");
            if (title != null){
                window.getShell().setText((String) title);
            }

            // set size of Window
            List size = (List) properties.remove("size");
            if (size != null){
                Point point = PointConverter.getInstance().parse(size);
                window.getShell().setSize(point);
            }

            // set location of Window
            List location = (List) properties.remove("location");
            if (location != null){
                Point point = PointConverter.getInstance().parse(location);
                window.getShell().setLocation(point);
            }
        }
        setBeanProperties(window, properties);
        return window;
    }
}
