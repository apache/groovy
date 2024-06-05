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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;

import static java.util.Objects.requireNonNull;

public class Receiver<T> {

    public static <T> Receiver<T> make(final ClassNode type) {
        return new Receiver<>(type == null ? ClassHelper.OBJECT_TYPE.getPlainNodeReference() : type);
    }

    private final ClassNode type;
    private final boolean object;
    private final T data;

    public Receiver(final ClassNode type) {
        this(type, true, null);
    }

    public Receiver(final ClassNode type, final T data) {
        this(type, true, data);
    }

    public Receiver(final ClassNode type, final boolean object, final T data) {
        this.type = requireNonNull(type);
        this.object = object;
        this.data = data;
    }

    //--------------------------------------------------------------------------

    public T getData() {
        return data;
    }

    /**
     * Indicates if receiver is an object instance or a class (static) reference.
     *
     * @since 5.0.0
     */
    public boolean isObject() {
        return object;
    }

    public ClassNode getType() {
        return type;
    }

    @Override
    public final String toString() {
        return "Receiver{data=" + data + ", type=" + (object ? "" : "*") + type.toString(false) + "}";
    }
}
