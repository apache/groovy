/*
 * Created on Feb 16, 2004
 *  
 */
package groovy.swt.factory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.GroovyException;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> $Id
 *         LayoutDataFactory.java,v 1.2 2004/03/18 08:51:47 ckl Exp $
 */
public class FormLayoutDataFactory extends AbstractSwtFactory implements
        SwtFactory {

    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {
        // get attachment properties
        List left = (List) properties.remove("left");
        List right = (List) properties.remove("right");
        List top = (List) properties.remove("top");
        List bottom = (List) properties.remove("bottom");

        // build new formdata
        FormData formData = new FormData();
        if (left != null) {
            formData.left = getFormAttachment(left);
        }
        if (right != null) {
            formData.right = getFormAttachment(right);
        }
        if (top != null) {
            formData.top = getFormAttachment(top);
        }
        if (bottom != null) {
            formData.bottom = getFormAttachment(bottom);
        }

        // set layout data
        if (parent instanceof Control) {
            Control control = (Control) parent;
            control.setLayoutData(formData);
        }

        // set remaining properties
        setBeanProperties(formData, properties);

        // return formdata
        return formData;
    }

    /**
     * @param list
     * @return @throws
     *         GroovyException
     */
    private FormAttachment getFormAttachment(List list) throws GroovyException {
        FormAttachment formAttachment = null;
        try {
            // get constructor
            Class[] types = new Class[list.size()];
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getClass() == Integer.class) {
                    types[i] = int.class;
                } else if (list.get(i) instanceof Control) {
                    types[i] = Control.class;
                } else {
                    throw new GroovyException(
                            "list element must be of type 'int' or 'Control': "
                                    + list.get(i));
                }
            }
            Constructor constructor = FormAttachment.class
                    .getConstructor(types);

            // invoke constructor
            if (constructor != null) {
                Object[] values = new Object[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    values[i] = list.get(i);
                }
                formAttachment = (FormAttachment) constructor
                        .newInstance(values);
            }
        } catch (Exception e) {
            throw new GroovyException(e.getMessage());
        }
        return formAttachment;
    }
}