/*
 * TextArgWidgetFactory.java
 * 
 * Created on May 23, 2007, 8:19:22 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package groovy.swing.factory;

import groovy.swing.SwingBuilder;
import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 *
 * @author Danno Ferrin
 */
public class TextArgWidgetFactory implements Factory {
    
    Class klass;
    
    public TextArgWidgetFactory(Class klass) {
        this.klass = klass;
    }
    
    public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        if (SwingBuilder.checkValueIsTypeNotString(value, name, klass)) {
            return value;
        }
        
        Object widget = klass.newInstance();
        
        if (value instanceof String) {
            // this does not create property setting order issues, since the value arg preceeds all properties in the builder element
            InvokerHelper.setProperty(widget, "text", value);
        }
        
        return widget;
    }    

}