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
 * @version $Revision: 7046 $
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

    class PropertyListener implements PropertyChangeListener, FullBinding {
        SourceBinding source;
        TargetBinding target;

        PropertyListener(SourceBinding source, TargetBinding target) {
            this.source = source;
            this.target = target;
        }

        public void propertyChange(PropertyChangeEvent event) {
            if ((event == null) || event.getPropertyName().equals(propertyName)) {
                target.updateTargetValue(source.getSourceValueClosure().call());
            } 
        }

        public void bind() {
            try {
                InvokerHelper.invokeMethodSafe(bean, "addPropretyChangeListener", new Object[] {propertyName, this});
            } catch (MissingMethodException mme) {
                InvokerHelper.invokeMethodSafe(bean, "addPropertyChangeListener", new Object[] {this});
                //don't catch this MissingMethodException, we will throw it out in this case
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
                //don't catch this MissingMethodException, we will throw it out in this case
            }
        }

        public void rebind() {
            unbind();
            bind();
        }

        public void forceUpdate() {
            propertyChange(null);
        }

        public SourceBinding getSourceBinding() {
            return source;
        }

        public void setSourceBinding(SourceBinding source) {
            this.source = source;
        }

        public TargetBinding getTargetBinding() {
            return target;
        }

        public void setTargetBinding(TargetBinding target) {
            this.target = target;
        }
    }
}
