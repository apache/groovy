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

/**
 * @since Groovy 1.1
 */
@Deprecated
public interface BindingUpdatable {

    /**
     * Causes automatic updating of bound values to be turned on.
     * This is idempotent between calls to unbind and rebind; i.e. multiple calls
     * to bind will have only the effect of the first call.
     */
    void bind();

    /**
     * Causes automatic updating of bound values to be turned off.
     * This is idempotent between calls to bind and rebind; i.e. multiple calls
     * to unbind will have only the effect of the first call. 
     */
    void unbind();

    /**
     * Causes the current bindings to be reset.
     * If the binding is not bound, it is a no-op.
     * If the binding is bound, it will be turned off, then turned on against current values.
     */
    void rebind();

    /**
     * Causes the values to be propagated from the source to the target
     */
    void update();

    /**
     * If supported, Causes the values to be propagated from the target to the source,
     * If not supported, an exception may be thrown 
     */
    void reverseUpdate();
}
