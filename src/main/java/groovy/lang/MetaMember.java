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
package groovy.lang;

import java.lang.reflect.Modifier;

/**
 * Common values of a class member -- a field, property, method, or constructor.
 *
 * @since 5.0.0
 */
public interface MetaMember {

    String getName();

    int getModifiers();

    default boolean isFinal() {
        return (getModifiers() & Modifier.FINAL) != 0;
    }

    default boolean isPackagePrivate() {
      //return !isPublic() && !isPrivate() && !isProtected()
        return (getModifiers() & Modifier.PUBLIC + Modifier.PRIVATE + Modifier.PROTECTED) == 0;
    }

    default boolean isPrivate() {
        return (getModifiers() & Modifier.PRIVATE) != 0;
    }

    default boolean isProtected() {
        return (getModifiers() & Modifier.PROTECTED) != 0;
    }

    default boolean isPublic() {
        return (getModifiers() & Modifier.PUBLIC) != 0;
    }

    default boolean isStatic() {
        return (getModifiers() & Modifier.STATIC) != 0;
    }

    default boolean isSynthetic() {
        return (getModifiers() & /*Modifier.SYNTHETIC*/0x1000) != 0;
    }

    // getDeclaringClass()->Class cannot be included because MetaMethod declares getDeclaringClass()->CachedClass
}
