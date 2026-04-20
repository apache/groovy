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
package org.codehaus.groovy.tools.groovydoc;

import java.lang.annotation.Documented;

/**
 * Utilities shared between {@code GroovydocVisitor} (Groovy AST) and
 * {@code GroovydocJavaVisitor} (JavaParser AST). Kept narrow on purpose:
 * only logic that is genuinely pure (no AST-type dependency) belongs here.
 * Everything else is parallel in both visitors because their input AST
 * types are fundamentally different hierarchies and the adapter layer
 * needed to unify them would cost more than the duplication it saves.
 */
public final class GroovydocAnnotationUtils {

    private GroovydocAnnotationUtils() {}

    /**
     * GROOVY-4634: emit an annotation reference only when the annotation
     * type is itself marked {@link Documented}, matching Javadoc's
     * behavior. When the annotation type cannot be resolved on the
     * current classpath (common for user-defined annotations in the
     * source tree being documented), default to {@code true} so that
     * groovydoc does not silently drop user annotations.
     *
     * <p>Takes a fully-qualified annotation type name and returns
     * {@code true} when the annotation should be shown in the rendered
     * documentation.
     */
    public static boolean shouldDocument(String fqn) {
        try {
            Class<?> c = Class.forName(fqn);
            if (!c.isAnnotation()) return true;
            return c.isAnnotationPresent(Documented.class);
        } catch (Throwable t) {
            return true;
        }
    }
}
