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
 * Maintains two synchronized {@link PropertyBinding} instances that update each other.
 *
 * @since Groovy 1.6
 */
public class MutualPropertyBinding implements FullBinding {

    /**
     * Indicates whether the mutual binding pair is currently active.
     */
    boolean bound;

    /**
     * The forward source-side property binding.
     */
    PropertyBinding sourceBinding;
    /**
     * The forward target-side property binding.
     */
    PropertyBinding targetBinding;
    /**
     * Optional validator applied to forward updates.
     */
    Closure validator;
    /**
     * Optional converter applied from source to target.
     */
    Closure converter;
    /**
     * Optional converter applied from target to source.
     */
    Closure reverseConverter;

    /**
     * Factory used to create trigger bindings for each property side.
     */
    Closure triggerFactory;

    /**
     * Trigger binding for forward updates.
     */
    TriggerBinding forwardTriggerBinding;
    /**
     * Full binding used for forward propagation.
     */
    FullBinding forwardBinding;
    /**
     * Trigger binding for reverse updates.
     */
    TriggerBinding reverseTriggerBinding;
    /**
     * Full binding used for reverse propagation.
     */
    FullBinding reverseBinding;

    /**
     * Creates a mutual binding between two property bindings.
     *
     * @param forwardTrigger the trigger binding for the initial source side
     * @param source the source property binding
     * @param target the target property binding
     * @param triggerFactory factory used to create trigger bindings for each side
     */
    MutualPropertyBinding(TriggerBinding forwardTrigger, PropertyBinding source, PropertyBinding target, Closure triggerFactory) {
        // order matters here!
        this.triggerFactory = triggerFactory;
        sourceBinding = source;
        forwardTriggerBinding = forwardTrigger;
        setTargetBinding(target);
        rebuildBindings();
    }

    /**
     * Returns the forward source-side property binding.
     *
     * @return the source binding
     */
    @Override
    public SourceBinding getSourceBinding() {
        return sourceBinding;
    }

    /**
     * Returns the forward target-side property binding.
     *
     * @return the target binding
     */
    @Override
    public TargetBinding getTargetBinding() {
        return targetBinding;
    }

    /**
     * Replaces the source-side property binding and rebuilds the paired bindings.
     *
     * @param sourceBinding the new source binding
     */
    @Override
    public void setSourceBinding(SourceBinding sourceBinding) {
        try {
            if (sourceBinding == null) {
                forwardTriggerBinding = null;
            } else {
                forwardTriggerBinding = (TriggerBinding) triggerFactory.call(sourceBinding);
            }
            this.sourceBinding = (PropertyBinding) sourceBinding;
        } catch (RuntimeException re) {
            throw new UnsupportedOperationException("Mutual Bindings may only change source bindings to other PropertyBindings");
        }
        rebuildBindings();
    }

    /**
     * Replaces the target-side property binding and rebuilds the paired bindings.
     *
     * @param targetBinding the new target binding
     */
    @Override
    public void setTargetBinding(TargetBinding targetBinding) {
        try {
            if (targetBinding == null) {
                reverseTriggerBinding = null;
            } else {
                reverseTriggerBinding = (TriggerBinding) triggerFactory.call(targetBinding);
            }
            this.targetBinding = (PropertyBinding) targetBinding;
        } catch (RuntimeException re) {
            throw new UnsupportedOperationException("Mutual Bindings may only change target bindings to other PropertyBindings");
        }
        rebuildBindings();
    }

    /**
     * Replaces the validator applied to forward updates.
     *
     * @param validator the validator closure, or {@code null}
     */
    @Override
    public void setValidator(Closure validator) {
        this.validator = validator;
        rebuildBindings();
    }

    /**
     * Returns the validator applied to forward updates.
     *
     * @return the validator closure, or {@code null}
     */
    @Override
    public Closure getValidator() {
        return validator;
    }

    /**
     * Replaces the converter applied from source to target.
     *
     * @param converter the forward converter, or {@code null}
     */
    @Override
    public void setConverter(Closure converter) {
        this.converter = converter;
        rebuildBindings();
    }

    /**
     * Returns the converter applied from source to target.
     *
     * @return the forward converter, or {@code null}
     */
    @Override
    public Closure getConverter() {
        return converter;
    }

    /**
     * Replaces the converter applied from target to source.
     *
     * @param reverseConverter the reverse converter, or {@code null}
     */
    @Override
    public void setReverseConverter(Closure reverseConverter) {
        this.reverseConverter = reverseConverter;
        rebuildBindings();
    }

    /**
     * Returns the converter applied from target to source.
     *
     * @return the reverse converter, or {@code null}
     */
    @Override
    public Closure getReverseConverter() {
        return reverseConverter;
    }

    /**
     * Rebuilds the internal forward and reverse bindings to match the current configuration.
     */
    protected void rebuildBindings() {
        // tear stuff down, even if we are half built
        if (bound) {
            if (forwardBinding != null) {
                forwardBinding.unbind();
            }
            if (reverseBinding != null) {
                reverseBinding.unbind();
            }
        }

        // check for all pieces, if we don't have the triad quit silently
        if (forwardTriggerBinding == null || sourceBinding == null || reverseTriggerBinding == null || targetBinding == null) {
            return;
        }

        // build the pieces
        forwardBinding = forwardTriggerBinding.createBinding(sourceBinding, targetBinding);
        reverseBinding = reverseTriggerBinding.createBinding(targetBinding, sourceBinding);

        // add the ancillary pieces
        if ((converter != null) && (reverseConverter != null)) {
            forwardBinding.setConverter(converter);
            reverseBinding.setConverter(reverseConverter);
        }
        if (validator != null) {
            forwardBinding.setValidator(validator);
        }

        // rebind if we were bound
        if (bound) {
            forwardBinding.bind();
            reverseBinding.bind();
        }

    }

    /**
     * Binds both forward and reverse bindings when the pair is fully configured.
     */
    @Override
    public void bind() {
        if (!bound) {
            bound = true;
            //guard checks

            // both converter and reverseConverter must be set or not
            if ((converter == null) != (reverseConverter == null)) {
                throw new RuntimeException("Both converter or reverseConverter must be set or unset to bind.  Only "
                        + ((converter != null) ? "converter": "reverseConverter") + " is set.");
            }
            // don't bind if we are half set up, quietly stop
            if (forwardBinding == null || reverseBinding == null) {
                // don't worry about the bind state, if the binding
                // is completed we will bind in rebuild
                return;
            }

            forwardBinding.bind();
            reverseBinding.bind();
        }
    }

    /**
     * Unbinds both forward and reverse bindings.
     */
    @Override
    public void unbind() {
        if (bound) {
            forwardBinding.unbind();
            reverseBinding.unbind();
            bound = false;
        }
    }

    /**
     * Rebinds both forward and reverse bindings when currently active.
     */
    @Override
    public void rebind() {
        if (bound) {
            unbind();
            bind();
        }
    }

    /**
     * Pushes an update from the source side to the target side.
     */
    @Override
    public void update() {
        forwardBinding.update();
    }

    /**
     * Pushes an update from the target side back to the source side.
     */
    @Override
    public void reverseUpdate() {
        reverseBinding.update();
    }
}
