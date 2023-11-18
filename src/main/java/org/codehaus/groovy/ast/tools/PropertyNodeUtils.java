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
package org.codehaus.groovy.ast.tools;

import org.codehaus.groovy.ast.PropertyNode;

import java.lang.reflect.Modifier;

public class PropertyNodeUtils {
    /**
     * Fields within the AST that have no explicit visibility are deemed to be properties
     * and represented by a PropertyNode. The Groovy compiler creates accessor methods and
     * a backing field for such property nodes. During this process, all modifiers
     * from the property are carried over to the backing field (so a property marked as
     * {@code transient} will have a {@code transient} backing field) but when creating
     * the accessor methods we don't carry over modifier values which don't make sense for
     * methods (such as {@code volatile} and {@code transient}) but other modifiers are carried over,
     * for example {@code static}.
     *
     * @param propNode the original property node
     * @return the modifiers which make sense for an accessor method
     */
    public static int adjustPropertyModifiersForMethod(PropertyNode propNode) {
        // GROOVY-3726: clear some modifiers so that they do not get applied to methods
        return propNode.getModifiers() & ~(Modifier.FINAL | Modifier.TRANSIENT | Modifier.VOLATILE);
    }
}
