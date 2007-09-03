/*
 * Copyright 2007 the original author or authors.
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
package groovy.swing.binding;

import org.codehaus.groovy.binding.AbstractFullBinding;
import org.codehaus.groovy.binding.FullBinding;
import org.codehaus.groovy.binding.PropertyBinding;
import org.codehaus.groovy.binding.SourceBinding;
import org.codehaus.groovy.binding.TargetBinding;
import org.codehaus.groovy.binding.TriggerBinding;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class AbstractButtonProperties {
    public static Map/*<String, TriggerBinding>*/ getSyntheticProperties() {
        Map/*<String, TriggerBinding>*/ result = new HashMap/*<String, TriggerBinding>*/();
        result.put(AbstractButton.class.getName() + "#selected",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new AbstractButtonSelectedBinding((PropertyBinding) source, target);
                }
            });
        return result;
    }
}


class AbstractButtonSelectedBinding extends AbstractFullBinding implements PropertyChangeListener, ItemListener {
    boolean bound;

    public AbstractButtonSelectedBinding(PropertyBinding source, TargetBinding target) {
        bound = false;
        setSourceBinding(source);
        setTargetBinding(target);
    }

    public synchronized void bind() {
        if (!bound) {
            AbstractButton ab = (AbstractButton) ((PropertyBinding) sourceBinding).getBean();
            try {
                ab.addPropertyChangeListener("model", this);
                ab.getModel().addItemListener(this);
                bound = true;
            } catch (RuntimeException re) {
                try {
                    ab.removePropertyChangeListener("model", this);
                    ab.getModel().removeItemListener(this);
                } catch (Exception e) {
                    // ignore as we are re-throwing the original cause
                }
                throw re;
            }
        }
    }

    public synchronized void unbind() {
        if (bound) {
            bound = false;
            // fail dirty, no checks
            AbstractButton ab = (AbstractButton) ((PropertyBinding)sourceBinding).getBean();
            ab.removePropertyChangeListener("model", this);
            ab.getModel().removeItemListener(this);
        }
    }

    public void rebind() {
        unbind();
        bind();
    }

    public void setSourceBinding(SourceBinding source) {
        if (bound) {
            throw new IllegalStateException("Cannot change source while binding is bound");
        }
        if (!(source instanceof PropertyBinding)) {
            throw new IllegalArgumentException("Only PropertySourceBindings are accepted");
        }

        if (!"selected".equals(((PropertyBinding)source).getPropertyName())) {
            throw new IllegalArgumentException("PropertyName must be 'selected'");
        }
        if (!(((PropertyBinding)source).getBean() instanceof AbstractButton)) {
            throw new IllegalArgumentException("SourceBean must be an AbstractButton");
        }
        super.setSourceBinding(source);
    }

    public void setTargetBinding(TargetBinding target) {
        if (bound) {
            throw new IllegalStateException("Cannot change target while binding is bound");
        }
        super.setTargetBinding(target);
    }

    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((ButtonModel)event.getOldValue()).removeItemListener(this);
        ((ButtonModel)event.getNewValue()).addItemListener(this);
    }

    public void itemStateChanged(ItemEvent e) {
        update();
    }
}
