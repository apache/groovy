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
package org.apache.groovy.swing.binding;

import groovy.lang.Closure;

/**
 * Base implementation for {@link FullBinding} that handles validation and value conversion.
 *
 * @since Groovy 1.1
 */
public abstract class AbstractFullBinding  implements FullBinding {

    /**
     * The source side of the binding.
     */
    protected SourceBinding sourceBinding;
    /**
     * The target side of the binding.
     */
    protected TargetBinding targetBinding;
    /**
     * Optional validator invoked before forward propagation.
     */
    protected Closure validator;
    /**
     * Optional converter applied before writing to the target.
     */
    protected Closure converter;
    /**
     * Optional converter applied before writing back to the source.
     */
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

    /**
     * Propagates the current source value to the target.
     */
    @Override
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

    /**
     * Propagates the current target value back to the source.
     */
    @Override
    public void reverseUpdate() {
        fireReverseBinding();
    }

    /**
     * Returns the current source binding.
     *
     * @return the source binding
     */
    @Override
    public SourceBinding getSourceBinding() {
        return sourceBinding;
    }

    /**
     * Replaces the current source binding.
     *
     * @param sourceBinding the new source binding
     */
    @Override
    public void setSourceBinding(SourceBinding sourceBinding) {
        this.sourceBinding = sourceBinding;
    }

    /**
     * Returns the current target binding.
     *
     * @return the target binding
     */
    @Override
    public TargetBinding getTargetBinding() {
        return targetBinding;
    }

    /**
     * Replaces the current target binding.
     *
     * @param targetBinding the new target binding
     */
    @Override
    public void setTargetBinding(TargetBinding targetBinding) {
        this.targetBinding = targetBinding;
    }

    /**
     * Returns the validator invoked before forward propagation.
     *
     * @return the validator closure, or {@code null}
     */
    @Override
    public Closure getValidator() {
        return validator;
    }

    /**
     * Sets the validator invoked before forward propagation.
     *
     * @param validator the validator closure, or {@code null}
     */
    @Override
    public void setValidator(Closure validator) {
        this.validator = validator;
    }

    /**
     * Returns the forward converter.
     *
     * @return the converter closure, or {@code null}
     */
    @Override
    public Closure getConverter() {
        return converter;
    }

    /**
     * Sets the forward converter.
     *
     * @param converter the converter closure, or {@code null}
     */
    @Override
    public void setConverter(Closure converter) {
        this.converter = converter;
    }

    /**
     * Returns the reverse converter.
     *
     * @return the reverse converter, or {@code null}
     */
    @Override
    public Closure getReverseConverter() {
        return reverseConverter;
    }

    /**
     * Sets the reverse converter.
     *
     * @param reverseConverter the reverse converter, or {@code null}
     */
    @Override
    public void setReverseConverter(Closure reverseConverter) {
        this.reverseConverter = reverseConverter;
    }
}
