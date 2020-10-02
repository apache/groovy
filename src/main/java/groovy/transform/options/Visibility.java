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
    PUBLIC(Modifier.PUBLIC),
    PROTECTED(Modifier.PROTECTED),
    PACKAGE_PRIVATE(0),
    PRIVATE(Modifier.PRIVATE),
    UNDEFINED(-1);

    private final int modifier;

    Visibility(final int modifier) {
        this.modifier = modifier;
    }

    public int getModifier() {
        if (modifier == -1) {
            throw new UnsupportedOperationException("getModifier() not supported for UNDEFINED");
        }
        return modifier;
    }
}
