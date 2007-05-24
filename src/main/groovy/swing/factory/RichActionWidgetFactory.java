/*
 * ButtonLikeWidgetFactory.java
 * 
 * Created on May 23, 2007, 7:48:27 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package groovy.swing.factory;

import groovy.swing.SwingBuilder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 *
 * @author shemnon
 */
public class RichActionWidgetFactory implements Factory {
    static final Class[] ACTION_ARGS = {Action.class};
    static final Class[] ICON_ARGS = {Icon.class};
    static final Class[] STRING_ARGS = {String.class};
    
    Constructor actionCtor;
    Constructor iconCtor;
    Constructor stringCtor;
    Class klass;
    
    public RichActionWidgetFactory(Class klass) {
        try {
            actionCtor = klass.getConstructor(ACTION_ARGS);
            iconCtor = klass.getConstructor(ICON_ARGS);
            stringCtor = klass.getConstructor(STRING_ARGS);
            this.klass = klass;
        }
        catch (NoSuchMethodException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
        catch (SecurityException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }
    
    public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        try {
            if (value == null) {
                return klass.newInstance();
            } else if (value instanceof Action) {
                return actionCtor.newInstance(new Object[] {value});
            } else if (value instanceof Icon) {
                return iconCtor.newInstance(new Object[] {value});
            } else if (value instanceof String) {
                return stringCtor.newInstance(new Object[] {value});
            } else if (klass.isAssignableFrom(value.getClass())) {
                return value;
            } else {
                throw new RuntimeException(name + " can only have a value argument of type javax.swing.Action, javax.swing.Icon, java.lang.String, or " + klass.getName());
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to create component for '" + name + "' reason: " + e, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to create component for '" + name + "' reason: " + e, e);
        }
    }

}
