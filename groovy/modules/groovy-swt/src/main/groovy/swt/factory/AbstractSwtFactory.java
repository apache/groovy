/*
 * Created on Feb 15, 2004
 *
 */
package groovy.swt.factory;

import groovy.jface.impl.ApplicationWindowImpl;
import groovy.swt.SwtHelper;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public abstract class AbstractSwtFactory {
    public abstract Object newInstance(Map properties, Object parent)
    throws GroovyException;

    /**
     * set the properties
     * 
     * @param bean
     * @param properties
     */
    protected void setBeanProperties(Object bean, Map properties) {
        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String property = entry.getKey().toString();
            Object value = entry.getValue();
            Field field = null;
            try {
                field = bean.getClass().getDeclaredField(property);
                if (value instanceof String) {
                    int style = SwtHelper.parseStyle(SWT.class, (String) value);
                    field.setInt(bean, style);
                }
                else if (value instanceof Boolean) {
                    field.setBoolean(bean, ((Boolean) value).booleanValue());
                }
                else if (value instanceof Integer) {
                    field.setInt(bean, ((Integer) value).intValue());
                }
                else if (value instanceof Double) {
                    field.setDouble(bean, ((Double) value).doubleValue());
                }
                else if (value instanceof Float) {
                    field.setFloat(bean, ((Float) value).floatValue());
                }
                else {
                    InvokerHelper.setProperty(bean, property, value);
                }
            }
            catch (Exception e) {
            }
            if (field == null) {
                InvokerHelper.setProperty(bean, property, value);
            }
        }
    }

    /**
     * return the parent widget
     * 
     * @param parent
     * @return
     */
    protected Widget getParentWidget(Object parent) {
        if (parent instanceof ApplicationWindow) {
            return (Composite) ((ApplicationWindowImpl) parent).getContents();
        }
        else if (parent instanceof Form) {
            return ((Form) parent).getBody();
        }
        else if (parent instanceof ScrolledForm) {
            return ((ScrolledForm) parent).getBody();
        }
        else if (parent instanceof Section) {
            return ((Section) parent).getClient();
        }
        else if (parent instanceof Composite) {
            return (Composite) parent;
        }
        else {
            return null;
        }
    }

    /**
     * return the parent shell
     * 
     * @param parent
     * @return
     */
    protected Shell getParentShell(Object parent) {
        if (parent instanceof ApplicationWindow) {
            return ((ApplicationWindowImpl) parent).getShell();
        }
        else if (parent instanceof Shell) {
            return (Shell) parent;
        }
        else {
            return null;
        }
    }
}
