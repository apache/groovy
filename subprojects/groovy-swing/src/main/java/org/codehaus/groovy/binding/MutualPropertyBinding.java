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
 * @since Groovy 1.6
 */

@Deprecated
public class MutualPropertyBinding implements FullBinding {

    boolean bound;

    PropertyBinding sourceBinding;
    PropertyBinding targetBinding;
    Closure validator;
    Closure converter;
    Closure reverseConverter;

    Closure triggerFactory;

    TriggerBinding forwardTriggerBinding;
    FullBinding forwardBinding;
    TriggerBinding reverseTriggerBinding;
    FullBinding reverseBinding;

    MutualPropertyBinding(TriggerBinding forwardTrigger, PropertyBinding source, PropertyBinding target, Closure triggerFactory) {
        // order matters here!
        this.triggerFactory = triggerFactory;
        sourceBinding = source;
        forwardTriggerBinding = forwardTrigger;
        setTargetBinding(target);
        rebuildBindings();
    }

    public SourceBinding getSourceBinding() {
        return sourceBinding;
    }

    public TargetBinding getTargetBinding() {
        return targetBinding;
    }

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

    public void setValidator(Closure validator) {
        this.validator = validator;
        rebuildBindings();
    }

    public Closure getValidator() {
        return validator;
    }

    public void setConverter(Closure converter) {
        this.converter = converter;
        rebuildBindings();
    }

    public Closure getConverter() {
        return converter;
    }

    public void setReverseConverter(Closure reverseConverter) {
       this.reverseConverter = reverseConverter;
        rebuildBindings();
    }

    public Closure getReverseConverter() {
        return reverseConverter;
    }

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

    public void unbind() {
        if (bound) {
            forwardBinding.unbind();
            reverseBinding.unbind();
            bound = false;
        }
    }

    public void rebind() {
        if (bound) {
            unbind();
            bind();
        }
    }

    public void update() {
        forwardBinding.update();
    }

    public void reverseUpdate() {
        reverseBinding.update();
    }
}

