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
 * Coordinates value propagation between a {@link SourceBinding} and a {@link TargetBinding}.
 *
 * @since Groovy 1.1
 */
public interface FullBinding extends BindingUpdatable {
    /**
     * Returns the source side of the binding.
     *
     * @return the source binding
     */
    SourceBinding getSourceBinding();

    /**
     * Returns the target side of the binding.
     *
     * @return the target binding
     */
    TargetBinding getTargetBinding();

    /**
     * Replaces the source side of the binding.
     *
     * @param source the new source binding
     */
    void setSourceBinding(SourceBinding source);

    /**
     * Replaces the target side of the binding.
     *
     * @param target the new target binding
     */
    void setTargetBinding(TargetBinding target);

    /**
     * Sets a validator invoked before propagating source values to the target.
     *
     * @param validator the validation closure, or {@code null}
     */
    void setValidator(Closure validator);

    /**
     * Returns the validator used before forward propagation.
     *
     * @return the validator closure, or {@code null}
     */
    Closure getValidator();

    /**
     * Sets the forward converter used before writing to the target.
     *
     * @param converter the forward converter, or {@code null}
     */
    void setConverter(Closure converter);

    /**
     * Returns the forward converter used before writing to the target.
     *
     * @return the forward converter, or {@code null}
     */
    Closure getConverter();

    /**
     * Sets the reverse converter used before writing back to the source.
     *
     * @param reverseConverter the reverse converter, or {@code null}
     */
    void setReverseConverter(Closure reverseConverter);

    /**
     * Returns the reverse converter used during reverse updates.
     *
     * @return the reverse converter, or {@code null}
     */
    Closure getReverseConverter();
}
