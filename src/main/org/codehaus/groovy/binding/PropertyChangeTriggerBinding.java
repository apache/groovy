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
package org.codehaus.groovy.binding;

import groovy.lang.MissingMethodException;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class PropertyChangeTriggerBinding implements TriggerBinding {

    Object bean;
    String propertyName;

    public PropertyChangeTriggerBinding(Object bean, String propertyName) {
        this.bean = bean;
        this.propertyName = propertyName;
    }

    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
        return new PropertyListener(source, target);
    }

    class PropertyListener extends AbstractFullBinding implements PropertyChangeListener {

        PropertyListener(SourceBinding source, TargetBinding target) {
            setSourceBinding(source);
            setTargetBinding(target);
        }

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals(propertyName)) {
                forceUpdate();
            } 
        }

        public void bind() {
            try {
                InvokerHelper.invokeMethodSafe(bean, "addPropretyChangeListener", new Object[] {propertyName, this});
            } catch (MissingMethodException mme) {
                try {
                    InvokerHelper.invokeMethodSafe(bean, "addPropertyChangeListener", new Object[] {this});
                } catch (MissingMethodException mme2) {
                    throw new RuntimeException("Properties in beans of type" + bean.getClass().getName() + " are not observable in any capacity (no PropertyChangeListener support).");
                }
            }
        }

        public void unbind() {
            try {
                InvokerHelper.invokeMethodSafe(bean, "removePropertyChangeListener", new Object[] {propertyName, this});
            } catch (MissingMethodException mme) {
                mme.printStackTrace();
                try {
                    InvokerHelper.invokeMethodSafe(bean, "removePropertyChangeListener", new Object[] {this});
                } catch (MissingMethodException mme2) {
                    mme2.printStackTrace();
                }
            }
        }

        public void rebind() {
            unbind();
            bind();
        }

    }
}
