/*
 * Created on Feb 28, 2004
 *
 */
package groovy.jface.factory;

import groovy.swt.factory.SwtFactory;
import groovy.swt.factory.WidgetFactory;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ViewerFactory extends WidgetFactory implements SwtFactory {

    /**
     * @param beanClass
     * @param style
     */
    public ViewerFactory(Class beanClass, int style) {
        super(beanClass, style);
    }

    /**
     * @param class1
     */
    public ViewerFactory(Class beanClass) {
        super(beanClass);
    }

    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent) throws GroovyException {

        if (beanClass.equals(TableViewer.class) && (parent instanceof Table)) {
            return new TableViewer((Table) parent, style);
        }

        return newInstance(properties, parent);
    }
}