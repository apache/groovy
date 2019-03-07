/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.binding;

import groovy.lang.Closure;

/**
 * @since Groovy 1.1
 */
public abstract class AbstractFullBinding  implements FullBinding {

    protected SourceBinding sourceBinding;
    protected TargetBinding targetBinding;
    protected Closure validator;
    protected Closure converter;
    protected Closure reverseConverter;

    private void fireBinding() {
        if ((sourceBinding == null) || (targetBinding == null)) {
            // should we throw invalid binding exception?  or fail quietly?
            return;
        }
        Object result = sourceBinding.getSourceValue();
        if (getValidator() != null) {
            Object validation = getValidator().call(result);
            if ((validation == null)
                || ((validation instanceof Boolean) && !(Boolean) validation))
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

    public void update() {
        fireBinding();
    }

    private void fireReverseBinding() {
        if (!(sourceBinding instanceof TargetBinding) || !(targetBinding instanceof SourceBinding)) {
            throw new RuntimeException("Binding Instance is not reversable");
        }
        Object result = ((SourceBinding)targetBinding).getSourceValue();
        if (getReverseConverter() != null) {
            result = getReverseConverter().call(result);
        }
        ((TargetBinding)sourceBinding).updateTargetValue(result);
    }

    public void reverseUpdate() {
        fireReverseBinding();
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

    public Closure getReverseConverter() {
        return reverseConverter;
    }

    public void setReverseConverter(Closure reverseConverter) {
        this.reverseConverter = reverseConverter;
    }
}
