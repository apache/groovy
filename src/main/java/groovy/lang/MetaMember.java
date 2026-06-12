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

    /**
     * Returns the simple name of this meta member.
     *
     * @return the member name
     */
    String getName();

    /**
     * Returns the Java modifier bit set for this meta member.
     *
     * @return the modifier flags
     */
    int getModifiers();

    /**
     * Indicates whether this member is declared {@code final}.
     *
     * @return {@code true} if the final modifier is present
     */
    default boolean isFinal() {
        return (getModifiers() & Modifier.FINAL) != 0;
    }

    /**
     * Indicates whether this member has package visibility.
     *
     * @return {@code true} if no explicit access modifier is present
     */
    default boolean isPackagePrivate() {
      //return !isPublic() && !isPrivate() && !isProtected()
        return (getModifiers() & Modifier.PUBLIC + Modifier.PRIVATE + Modifier.PROTECTED) == 0;
    }

    /**
     * Indicates whether this member is declared {@code private}.
     *
     * @return {@code true} if the private modifier is present
     */
    default boolean isPrivate() {
        return (getModifiers() & Modifier.PRIVATE) != 0;
    }

    /**
     * Indicates whether this member is declared {@code protected}.
     *
     * @return {@code true} if the protected modifier is present
     */
    default boolean isProtected() {
        return (getModifiers() & Modifier.PROTECTED) != 0;
    }

    /**
     * Indicates whether this member is declared {@code public}.
     *
     * @return {@code true} if the public modifier is present
     */
    default boolean isPublic() {
        return (getModifiers() & Modifier.PUBLIC) != 0;
    }

    /**
     * Indicates whether this member is declared {@code static}.
     *
     * @return {@code true} if the static modifier is present
     */
    default boolean isStatic() {
        return (getModifiers() & Modifier.STATIC) != 0;
    }

    /**
     * Indicates whether this member is synthetic from the JVM's perspective.
     *
     * @return {@code true} if the synthetic modifier bit is present
     */
    default boolean isSynthetic() {
        return (getModifiers() & /*Modifier.SYNTHETIC*/0x1000) != 0;
    }

    // getDeclaringClass()->Class cannot be included because MetaMethod declares getDeclaringClass()->CachedClass
}
