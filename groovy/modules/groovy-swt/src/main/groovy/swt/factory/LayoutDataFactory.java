/*
 * Created on Feb 16, 2004
 *
 */
package groovy.swt.factory;

import groovy.swt.SwtHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.swt.widgets.Control;


/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster</a>
 * $Id$
 */
public class LayoutDataFactory extends AbstractSwtFactory implements SwtFactory{

    private Class beanClass;

    /**
     * @param class1
     */
    public LayoutDataFactory(Class beanClass) {
        this.beanClass = beanClass;
    }

    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *           java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {

        Object bean = createWidget(properties, parent);

        if (bean != null) {
            setBeanProperties(bean, properties);
        }

        if (parent instanceof Control) {
            Control control = (Control) parent;
            control.setLayoutData(bean);
        }

        return bean;
    }

    private Object createWidget(Map properties, Object parent)
            throws GroovyException {
        Object bean = null;

        String styleText = (String) properties.remove("style");
        if (styleText != null) {
            int style = SwtHelper.parseStyle(beanClass, styleText);

            // now lets try invoke a constructor
            Class[] types = { int.class};

            try {
                Constructor constructor = beanClass.getConstructor(types);
                if (constructor != null) {
                    Object[] values = { new Integer(style)};
                    bean = constructor.newInstance(values);
                }
            } catch (NoSuchMethodException e) {
                throw new GroovyException(e.getMessage());
            } catch (InstantiationException e) {
                throw new GroovyException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new GroovyException(e.getMessage());
            } catch (InvocationTargetException e) {
                throw new GroovyException(e.getMessage());
            }
        } else {
            try {
                bean = beanClass.newInstance();
            } catch (InstantiationException e) {
                throw new GroovyException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new GroovyException(e.getMessage());
            }
        }

        if (bean != null) {
            setBeanProperties(bean, properties);
        }

        return bean;
    }
}
