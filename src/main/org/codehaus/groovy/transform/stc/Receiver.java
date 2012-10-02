/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;

public class Receiver<T> {
    private final ClassNode type;
    private final T data;

    public static <T> Receiver<T> make(final ClassNode type) {
        return new Receiver<T>(type);
    }

    public Receiver(final ClassNode type) {
        this.type = type;
        this.data = null;
    }

    public Receiver(final ClassNode type, final T data) {
        this.data = data;
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public ClassNode getType() {
        return type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Receiver");
        sb.append("{type=").append(type);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
