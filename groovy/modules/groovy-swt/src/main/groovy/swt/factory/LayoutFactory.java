/*
 * Created on Feb 15, 2004
 *
 */
package groovy.swt.factory;

import groovy.swt.SwtUtils;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class LayoutFactory extends AbstractSwtFactory implements SwtFactory {

    private Class beanClass;

    /**
     * @param beanClass
     */
    public LayoutFactory(Class beanClass) {
        this.beanClass = beanClass;
    }

    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {

        Layout layout;
        try {
            layout = (Layout) beanClass.newInstance();
        } catch (InstantiationException e) {
            throw new GroovyException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new GroovyException(e.getMessage());
        }

        if (layout != null) {
            setBeanProperties(layout, properties);
        }

        Widget parentComposite = SwtUtils.getParentWidget(parent);
        if (parentComposite != null) {
            ((Composite) parentComposite).setLayout(layout);
        }

        return layout;
    }
}
