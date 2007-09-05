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

import org.codehaus.groovy.runtime.InvokerHelper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import groovy.lang.MissingMethodException;


/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class PropertyBinding implements SourceBinding, TargetBinding, TriggerBinding {

    Object bean;
    String propertyName;

    public PropertyBinding(Object bean, String propertyName) {
        this.bean = bean;
        this.propertyName = propertyName;
    }

    public void updateTargetValue(Object newValue) {
        InvokerHelper.setProperty(bean, propertyName, newValue);
    }

    public Object getSourceValue() {
        return InvokerHelper.getPropertySafe(bean, propertyName);
    }

    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
        return new PropertyFullBinding
                (source, target);
    }

    class PropertyFullBinding extends AbstractFullBinding implements PropertyChangeListener {

        Object boundBean;
        Object boundProperty;
        boolean bound;

        PropertyFullBinding(SourceBinding source, TargetBinding target) {
            setSourceBinding(source);
            setTargetBinding(target);
        }

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals(boundProperty)) {
                update();
            }
        }

        public void bind() {
            if (!bound) {
                bound = true;
                boundBean = bean;
                boundProperty = propertyName;
                try {
                    InvokerHelper.invokeMethodSafe(boundBean, "addPropertyChangeListener", new Object[] {boundProperty, this});
                } catch (MissingMethodException mme) {
                    try {
                        InvokerHelper.invokeMethodSafe(boundBean, "addPropertyChangeListener", new Object[] {this});
                    } catch (MissingMethodException mme2) {
                        throw new RuntimeException("Properties in beans of type " + bean.getClass().getName() + " are not observable in any capacity (no PropertyChangeListener support).");
                    }
                }
            }
        }

        public void unbind() {
            if (bound) {
                try {
                    InvokerHelper.invokeMethodSafe(boundBean, "removePropertyChangeListener", new Object[] {boundProperty, this});
                } catch (MissingMethodException mme) {
                    mme.printStackTrace();
                    try {
                        InvokerHelper.invokeMethodSafe(boundBean, "removePropertyChangeListener", new Object[] {this});
                    } catch (MissingMethodException mme2) {
                        mme2.printStackTrace();
                    }
                }
                boundBean = null;
                boundProperty = null;
                bound = false;
            }
        }

        public void rebind() {
            if (bound) {
                unbind();
                bind();
            }
        }

    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }


}
