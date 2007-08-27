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

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision: 7046 $
 * @since Groovy 1.1
 */
public class PropertySourceBinding extends Closure implements SourceBinding {

    Object sourceBean;
    String propertyName;

    public PropertySourceBinding(Object owner, Object sourceBean, String propertyName) {
        super(owner);
        this.sourceBean = sourceBean;
        this.propertyName = propertyName;
    }


    public Closure getSourceValueClosure() {
        return this;
    }

    public Object getSourceBean() {
        return sourceBean;
    }

    public void setSourceBean(Object sourceBean) {
        this.sourceBean = sourceBean;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }


    public Object doCall() {
        return InvokerHelper.getPropertySafe(sourceBean, propertyName);
    }
}
