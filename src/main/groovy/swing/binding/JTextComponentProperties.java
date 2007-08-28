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

import org.codehaus.groovy.binding.FullBinding;
import org.codehaus.groovy.binding.PropertySourceBinding;
import org.codehaus.groovy.binding.SourceBinding;
import org.codehaus.groovy.binding.TargetBinding;
import org.codehaus.groovy.binding.TriggerBinding;
import org.codehaus.groovy.binding.AbstractFullBinding;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class JTextComponentProperties {

    public static Map/*<String, TriggerBinding>*/ getSyntheticProperties() {
        Map/*<String, TriggerBinding>*/ result = new HashMap/*<String, TriggerBinding>*/();
        result.put(JTextComponent.class.getName() + "#text",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new JTextComponentTextBinding((PropertySourceBinding) source, target);
                }
            });
        return result;
    }
}


class JTextComponentTextBinding extends AbstractFullBinding implements PropertyChangeListener, DocumentListener {
    boolean bound;

    public JTextComponentTextBinding(PropertySourceBinding source, TargetBinding target) {
        bound = false;
        setSourceBinding(source);
        setTargetBinding(target);
    }

    public synchronized void bind() {
        if (!bound) {
            JTextComponent tc = (JTextComponent) ((PropertySourceBinding)sourceBinding).getSourceBean();
            try {
                tc.addPropertyChangeListener("document", this);
                tc.getDocument().addDocumentListener(this);
                bound = true;
            } catch (RuntimeException re) {
                try {
                    tc.removePropertyChangeListener("document", this);
                    tc.getDocument().removeDocumentListener(this);
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
            JTextComponent tc = (JTextComponent) ((PropertySourceBinding)sourceBinding).getSourceBean();
            tc.removePropertyChangeListener("document", this);
            tc.getDocument().removeDocumentListener(this);
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
        if (!(source instanceof PropertySourceBinding)) {
            throw new IllegalArgumentException("Only PropertySourceBindings are accepted");
        }

        if (!"text".equals(((PropertySourceBinding)source).getPropertyName())) {
            throw new IllegalArgumentException("PropertyName must be 'text'");
        }
        if (!(((PropertySourceBinding)source).getSourceBean() instanceof JTextComponent)) {
            throw new IllegalArgumentException("SourceBean must be a TextComponent");
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
        forceUpdate();
        ((Document)event.getOldValue()).removeDocumentListener(this);
        ((Document)event.getNewValue()).addDocumentListener(this);
    }

    public void changedUpdate(DocumentEvent event) {
        forceUpdate();
    }

    public void insertUpdate(DocumentEvent event) {
        forceUpdate();
    }

    public void removeUpdate(DocumentEvent event) {
        forceUpdate();
    }

}
