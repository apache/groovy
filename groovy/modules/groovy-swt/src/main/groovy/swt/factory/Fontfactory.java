/*
 * Created on Jun 15, 2004
 *  
 */
package groovy.swt.factory;

import groovy.swt.InvalidParentException;
import groovy.swt.SwtUtils;
import groovy.swt.UnKnownStyleException;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> 
 * $Id$
 */
public class Fontfactory extends AbstractSwtFactory implements SwtFactory {

    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent) throws GroovyException {

        Object parentWidget = SwtUtils.getParentWidget(parent, properties);
        if (!(parentWidget instanceof Control)) {
            throw new InvalidParentException("control");
        }

        Control parentControl = (Control) parentWidget;

        String styleProperty = (String) properties.remove("style");

        if (styleProperty != null) {
            int style;
            try {
                style = SwtUtils.parseStyle(SWT.class, styleProperty);
            } catch (GroovyException e) {
                throw new UnKnownStyleException(styleProperty);
            }

            Font initialFont = parentControl.getFont();
            FontData[] fontData = initialFont.getFontData();
            for (int i = 0; i < fontData.length; i++) {
                fontData[i].setStyle(style);
            }

            Font newFont = new Font(parentControl.getDisplay(), fontData);
            parentControl.setFont(newFont);
        }

        return parentControl;
    }

}