/*
 * Created on Feb 15, 2004
 *  
 */
package groovy.swt.factory;

import groovy.swt.SwtUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class WidgetFactory extends AbstractSwtFactory implements SwtFactory {

    protected Class beanClass;

    protected int style = SWT.NONE;

    public WidgetFactory(Class beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * @param beanClass2
     * @param style
     */
    public WidgetFactory(Class beanClass, int style) {
        this.beanClass = beanClass;
        this.style = style;
    }

    /*
     * @see groovy.swt.impl.Factory#newInstance(java.util.Map, java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {
        String styleProperty = (String) properties.remove("style");
        if (styleProperty != null) {
            style = SwtUtils.parseStyle(SWT.class, styleProperty);
        }

        Object parentWidget = SwtUtils.getParentWidget(parent);
        Object bean = createWidget(parentWidget);
        if (bean != null) {
            setBeanProperties(bean, properties);
        }

        setControl(bean, parent);

        return bean;
    }

    /**
     * @param parent
     * @param bean
     * @return @throws
     *         GroovyException
     */
    protected Object createWidget(Object parent) throws GroovyException {
        if (beanClass == null) { throw new GroovyException(
                "No Class available to create the new widget"); }
        try {
            if (parent == null) {
                // lets try call a constructor with a single style
                Class[] types = { int.class};
                Constructor constructor = beanClass.getConstructor(types);
                if (constructor != null) {
                    Object[] arguments = { new Integer(style)};
                    return constructor.newInstance(arguments);
                }
            } else {
                // lets try to find the constructor with 2 arguments with the
                // 2nd argument being an int
                Constructor[] constructors = beanClass.getConstructors();
                if (constructors != null) {
                    for (int i = 0, size = constructors.length; i < size; i++) {
                        Constructor constructor = constructors[i];
                        Class[] types = constructor.getParameterTypes();
                        if (types.length == 2
                                && types[1].isAssignableFrom(int.class)) {
                            if (types[0].isAssignableFrom(parent.getClass())) {
                                Object[] arguments = { parent,
                                        new Integer(style)};
                                return constructor.newInstance(arguments);
                            }
                            // lets try to find the constructor with 1
                            // arguments
                        } else if (types.length == 1
                                && types[0].isAssignableFrom(parent.getClass())) {
                            Object[] arguments = { parent};
                            return constructor.newInstance(arguments);
                        }
                    }
                }
            }
            return beanClass.newInstance();
        } catch (NoSuchMethodException e) {
            throw new GroovyException(e.getMessage());
        } catch (InstantiationException e) {
            throw new GroovyException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new GroovyException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new GroovyException(e.getTargetException()
                    .getLocalizedMessage());
        }
    }

    protected void setControl(Object bean, Object parent) {
        if (parent instanceof CTabItem) {
            CTabItem tabItem = (CTabItem) parent;
            tabItem.setControl((Control) bean);
        } else if (parent instanceof TabItem) {
            TabItem tabItem = (TabItem) parent;
            tabItem.setControl((Control) bean);
        } else if (bean instanceof Menu && parent instanceof Shell) {
            Menu menu = (Menu) bean;
            Shell shell = (Shell) parent;
            if (style == SWT.BAR) {
                shell.setMenuBar(menu);
            } else if (style == SWT.POP_UP) {
                shell.setMenu(menu);
            }
        }
    }

}
