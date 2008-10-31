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
 * @version $Revision: 10897 $
 * @since Groovy 1.6
 */

public class MutualPropertyBinding extends AbstractFullBinding implements PropertyChangeListener {

    boolean bound;

    Object sourceBoundBean;
    String sourceBoundProperty;
    boolean sourceBoundToProperty;
    Object targetBoundBean;
    String targetBoundProperty;
    boolean targetBoundToProperty;

    MutualPropertyBinding(SourceBinding source, TargetBinding target) {
        setSourceBinding(source);
        setTargetBinding(target);
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() == targetBoundBean) {
            if (sourceBoundToProperty || event.getPropertyName().equals(sourceBoundProperty)) {
                update();
            }
        } else if (event.getSource() == sourceBoundBean) {
            if (targetBoundToProperty || event.getPropertyName().equals(targetBoundProperty)) {
                reverseUpdate();
            }
        }
    }

    public void setSourceBinding(SourceBinding sourceBinding) {
        if (sourceBinding instanceof PropertyBinding) {
            if (bound && sourceBoundBean != null) {
                unbindProperty(sourceBoundBean, sourceBoundToProperty ? sourceBoundProperty : (String) null, this);
                sourceBoundBean = null;
                sourceBoundProperty = null;
            }
            super.setSourceBinding(sourceBinding);
            if (bound) {
                sourceBoundBean = ((PropertyBinding)sourceBinding).bean;
                sourceBoundProperty = bindProperty(sourceBoundBean, ((PropertyBinding)sourceBinding).propertyName, this);
                sourceBoundToProperty = sourceBoundProperty != null;
            }
        } else {
            throw new RuntimeException("Only PropertyBindings can be set in a Mutual Property Binding");
        }
    }

    public void setTargetBinding(TargetBinding targetBinding) {
        if (targetBinding instanceof PropertyBinding) {
            if (bound && targetBoundBean != null) {
                unbindProperty(targetBoundBean, targetBoundToProperty ? targetBoundProperty : (String) null, this);
                targetBoundBean = null;
                targetBoundProperty = null;
            }
            super.setTargetBinding(targetBinding);
            if (bound) {
                targetBoundBean = ((PropertyBinding)targetBinding).bean;
                targetBoundProperty = bindProperty(targetBoundBean, ((PropertyBinding)targetBinding).propertyName, this);
                targetBoundToProperty = targetBoundProperty != null;
            }
        } else if (targetBinding != null) {
            throw new RuntimeException("Only PropertyBindings can be set in a Mutual Property Binding");
        }
    }

    public static String bindProperty(Object bean, String propertyName, PropertyChangeListener that) {
        try {
            InvokerHelper.invokeMethodSafe(bean, "addPropertyChangeListener", new Object[] {propertyName, that});
            return propertyName;
        } catch (MissingMethodException mme) {
            try {
                InvokerHelper.invokeMethodSafe(bean, "addPropertyChangeListener", new Object[] {that});
                return null;
            } catch (MissingMethodException mme2) {
                throw new RuntimeException("Properties in beans of type " + bean.getClass().getName() + " are not observable in any capacity (no PropertyChangeListener support).");
            }
        }
    }

    public static void unbindProperty(Object bean, String propertyName, PropertyChangeListener that) {
        if (propertyName != null) {
            try {
                InvokerHelper.invokeMethodSafe(bean, "removePropertyChangeListener", new Object[] {propertyName, that});
            } catch (MissingMethodException mme) {
                // ignore, too bad so sad they don't follow conventions, we'll just leave the listener attached
            }
        } else {
            try {
                InvokerHelper.invokeMethodSafe(bean, "removePropertyChangeListener", new Object[] {that});
            } catch (MissingMethodException mme2) {
                // ignore, too bad so sad they don't follow conventions, we'll just leave the listener attached
            }
        }
    }

    public void bind() {
        if (!bound) {
            bound = true;
            if (sourceBinding != null) {
                sourceBoundBean = ((PropertyBinding)sourceBinding).bean;
                sourceBoundProperty = bindProperty(sourceBoundBean, ((PropertyBinding)sourceBinding).propertyName, this);
                sourceBoundToProperty = sourceBoundProperty != null;
            }

            if (targetBinding != null) {
                targetBoundBean = ((PropertyBinding)targetBinding).bean;
                targetBoundProperty = bindProperty(targetBoundBean, ((PropertyBinding)targetBinding).propertyName, this);
                targetBoundToProperty = targetBoundProperty != null;
            }
        }
    }

    public void unbind() {
        if (bound) {
            if (sourceBoundBean != null) {
                unbindProperty(sourceBoundBean, sourceBoundToProperty ? sourceBoundProperty : (String) null, this);
                sourceBoundBean = null;
                sourceBoundProperty = null;
            }

            if (targetBoundBean != null) {
                unbindProperty(targetBoundBean, targetBoundToProperty ? targetBoundProperty : (String) null, this);
                targetBoundBean = null;
                targetBoundProperty = null;
            }
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

