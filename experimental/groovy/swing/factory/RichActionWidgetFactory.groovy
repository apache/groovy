/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                return actionCtor.newInstance([value]);
            } else if (value instanceof Icon) {
                return iconCtor.newInstance([value]);
            } else if (value instanceof String) {
                return stringCtor.newInstance([value]);
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
