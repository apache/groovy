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
package org.codehaus.groovy.macro.transform;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.macro.runtime.Macro;
import org.codehaus.groovy.transform.stc.AbstractExtensionMethodCache;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @since 2.5.0
 */
final class MacroMethodsCache extends AbstractExtensionMethodCache {
    private static final ClassNode MACRO_ANNOTATION_CLASS_NODE = ClassHelper.make(Macro.class);

    /**
     * Shared macro methods cache.
     */
    public static final MacroMethodsCache INSTANCE = new MacroMethodsCache();

    private MacroMethodsCache() {}

    /**
     * Adds extra extension method containers to scan.
     *
     * @param instanceExtClasses instance extension classes
     * @param staticExtClasses static extension classes
     */
    @Override
    protected void addAdditionalClassesToScan(Set<Class> instanceExtClasses, Set<Class> staticExtClasses) {}

    /**
     * Returns the system property that disables macro extension lookup.
     *
     * @return the disable property name
     */
    @Override
    protected String getDisablePropertyName() {
        return "groovy.macro.disable";
    }

    /**
     * Returns the filter used to discard macro-annotated methods from normal extension lookup.
     *
     * @return the method filter
     */
    @Override
    protected Predicate<MethodNode> getMethodFilter() {
        return m -> m.getAnnotations(MACRO_ANNOTATION_CLASS_NODE).isEmpty();
    }

    /**
     * Returns the mapper used to index macro methods by name.
     *
     * @return the method-name mapper
     */
    @Override
    protected Function<MethodNode, String> getMethodMapper() {
        return m -> m.getName();
    }
}
