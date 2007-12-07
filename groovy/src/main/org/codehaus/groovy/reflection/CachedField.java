/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CachedField {
    public final Field field;

    CachedClass cachedClass;

    public CachedField(CachedClass clazz, Field field) {
        this.field = field;
        cachedClass = clazz;
    }

    public String getName() {
        return field.getName();
    }

    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public Class getType() {
        return field.getType();
    }

    public int getModifiers() {
        return field.getModifiers();
    }
}
