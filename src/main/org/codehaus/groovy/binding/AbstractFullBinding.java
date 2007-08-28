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

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public abstract class AbstractFullBinding  implements FullBinding {

    protected SourceBinding sourceBinding;
    protected TargetBinding targetBinding;
    protected Closure validator;
    protected Closure converter;

    private void fireBinding() {
        if ((sourceBinding == null) || (targetBinding == null)) {
            // should we throw invalid binding exception?  or fail quietly?
            return;
        }
        Object result = sourceBinding.getSourceValue();
        if (getValidator() != null) {
            Object validation = getValidator().call();
            if ((validation == null)
                || ((validation instanceof Boolean) && !((Boolean)validation).booleanValue()))
            {
                // should we throw a validation failed exception?  or fail quietly?
                return;
            }
        }
        if (getConverter() != null) {
            result = getConverter().call(result);
        }
        targetBinding.updateTargetValue(result);
    }

    public void forceUpdate() {
        fireBinding();
    }


    public SourceBinding getSourceBinding() {
        return sourceBinding;
    }

    public void setSourceBinding(SourceBinding sourceBinding) {
        this.sourceBinding = sourceBinding;
    }

    public TargetBinding getTargetBinding() {
        return targetBinding;
    }

    public void setTargetBinding(TargetBinding targetBinding) {
        this.targetBinding = targetBinding;
    }

    public Closure getValidator() {
        return validator;
    }

    public void setValidator(Closure validator) {
        this.validator = validator;
    }

    public Closure getConverter() {
        return converter;
    }

    public void setConverter(Closure converter) {
        this.converter = converter;
    }
}
