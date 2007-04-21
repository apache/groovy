/*
 * $Id:  $
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
 *
 */

package groovy.swing.factory;

import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;
import groovy.swing.SwingBuilder;
import groovy.swing.impl.DefaultAction;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 *
 * @author Danno Ferrin
 */
public class ActionFactory implements Factory {
    
    public Object newInstance(SwingBuilder builder, Object name, Object value, Map properties) throws InstantiationException, IllegalAccessException {
        Action action = null;
        if (value instanceof Action) {
            action = (Action) value;
        } else if (properties.get(name) instanceof Action) {
            action = (Action) properties.remove(name);
        } else {
            action = new DefaultAction();
        }
        
        if ((properties.get("closure") instanceof Closure) && (action instanceof DefaultAction)){
            Closure closure = (Closure) properties.remove("closure");
            ((DefaultAction)action).setClosure(closure);
        }

        Object accel = properties.remove("accelerator");
        if (accel != null) {
            KeyStroke stroke = null;
            if (accel instanceof KeyStroke) {
                stroke = (KeyStroke) accel;
            } else {
                stroke = KeyStroke.getKeyStroke(accel.toString());
            }
            action.putValue(Action.ACCELERATOR_KEY, stroke);
        }

        Object mnemonic = properties.remove("mnemonic");
        if (mnemonic != null) {
            if (!(mnemonic instanceof Number)) {
                mnemonic = new Integer(mnemonic.toString().charAt(0));
            }
            action.putValue(Action.MNEMONIC_KEY, mnemonic);
        }
        
        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String propertyName = (String) entry.getKey();
            // first attempt to set as a straight proeprty
            try {
                InvokerHelper.setProperty(action, propertyName, entry.getValue());
            } catch (MissingPropertyException mpe) {
                // failing that store them in the action values list
                // typically standard Action names start with upper case, so lets upper case it
                propertyName = SwingBuilder.capitalize(propertyName);
                action.putValue(propertyName, entry.getValue());
            }

        }
        properties.clear();
        
        return action;
    }    
}
