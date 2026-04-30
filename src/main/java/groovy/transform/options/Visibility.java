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
package groovy.transform.options;

import java.lang.reflect.Modifier;

/**
 * Indicates the visibility of a node.
 *
 * @since 2.5.0
 */
public enum Visibility {
    /**
     * Public visibility.
     */
    PUBLIC(Modifier.PUBLIC),

    /**
     * Protected visibility.
     */
    PROTECTED(Modifier.PROTECTED),

    /**
     * Package-private visibility.
     */
    PACKAGE_PRIVATE(0),

    /**
     * Private visibility.
     */
    PRIVATE(Modifier.PRIVATE),

    /**
     * Marker indicating that no explicit visibility was requested.
     */
    UNDEFINED(-1);

    private final int modifier;

    /**
     * Creates a visibility value backed by the supplied JVM modifier flag.
     *
     * @param modifier the modifier flag, or {@code -1} for {@link #UNDEFINED}
     */
    Visibility(final int modifier) {
        this.modifier = modifier;
    }

    /**
     * Returns the JVM modifier flag for this visibility.
     *
     * @return the modifier flag
     * @throws UnsupportedOperationException if this value is {@link #UNDEFINED}
     */
    public int getModifier() {
        if (modifier == -1) {
            throw new UnsupportedOperationException("getModifier() not supported for UNDEFINED");
        }
        return modifier;
    }
}
