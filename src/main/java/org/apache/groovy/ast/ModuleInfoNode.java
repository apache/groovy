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
package org.apache.groovy.ast;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AST for a JPMS module declaration (JLS §7.7), produced from
 * {@code module-info.groovy} (or {@code module-info.java} fed to the Groovy parser).
 * Attached to the synthetic {@code module-info} class via node metadata.
 */
public final class ModuleInfoNode extends AnnotatedNode {

    private final String name;
    private final boolean open;
    private final List<Directive> directives;

    public ModuleInfoNode(final String name, final boolean open, final List<Directive> directives) {
        this.name = name;
        this.open = open;
        this.directives = Collections.unmodifiableList(new ArrayList<>(directives));
    }

    /**
     * Whether {@code name} is the file name of a modular compilation unit.
     * A path is reduced to its last segment so {@code src/module-info.groovy} matches.
     */
    public static boolean isModuleInfoSource(final String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        String file = slash >= 0 ? name.substring(slash + 1) : name;
        return "module-info.groovy".equals(file) || "module-info.java".equals(file);
    }

    /**
     * Returns the module declaration stored on {@code classNode}, or {@code null}.
     */
    public static ModuleInfoNode of(final ClassNode classNode) {
        if (classNode == null) {
            return null;
        }
        Object metadata = classNode.getNodeMetaData(ModuleInfoNode.class);
        return metadata instanceof ModuleInfoNode ? (ModuleInfoNode) metadata : null;
    }

    public String getName() {
        return name;
    }

    public boolean isOpen() {
        return open;
    }

    public List<Directive> getDirectives() {
        return directives;
    }

    public List<Requires> getRequires() {
        return ofKind(Requires.class);
    }

    public List<Exports> getExports() {
        return ofKind(Exports.class);
    }

    public List<Opens> getOpens() {
        return ofKind(Opens.class);
    }

    public List<Uses> getUses() {
        return ofKind(Uses.class);
    }

    public List<Provides> getProvides() {
        return ofKind(Provides.class);
    }

    private <T extends Directive> List<T> ofKind(final Class<T> kind) {
        List<T> answer = new ArrayList<>();
        for (Directive directive : directives) {
            if (kind.isInstance(directive)) {
                answer.add(kind.cast(directive));
            }
        }
        return answer;
    }

    /**
     * One module directive. Subclasses carry the directive's operands.
     */
    public abstract static class Directive extends ASTNode {
    }

    /**
     * {@code requires [transitive] [static] ModuleName}.
     * Modifier order in source is not significant.
     */
    public static final class Requires extends Directive {
        private final String moduleName;
        private final boolean transitive;
        private final boolean staticPhase;

        public Requires(final String moduleName, final boolean transitive, final boolean staticPhase) {
            this.moduleName = moduleName;
            this.transitive = transitive;
            this.staticPhase = staticPhase;
        }

        public String getModuleName() {
            return moduleName;
        }

        public boolean isTransitive() {
            return transitive;
        }

        public boolean isStaticPhase() {
            return staticPhase;
        }
    }

    /**
     * {@code exports PackageName [to ModuleName {, ModuleName}]}.
     * An empty target list is an unqualified export.
     */
    public static final class Exports extends PackageDirective {
        public Exports(final String packageName, final List<String> targets) {
            super(packageName, targets);
        }
    }

    /**
     * {@code opens PackageName [to ModuleName {, ModuleName}]}.
     * An empty target list is an unqualified open.
     */
    public static final class Opens extends PackageDirective {
        public Opens(final String packageName, final List<String> targets) {
            super(packageName, targets);
        }
    }

    /**
     * Shared shape of {@code exports} and {@code opens}.
     */
    public abstract static class PackageDirective extends Directive {
        private final String packageName;
        private final List<String> targets;

        protected PackageDirective(final String packageName, final List<String> targets) {
            this.packageName = packageName;
            this.targets = Collections.unmodifiableList(new ArrayList<>(targets));
        }

        public String getPackageName() {
            return packageName;
        }

        /**
         * Friend modules of a qualified directive. Empty when the directive is unqualified.
         */
        public List<String> getTargets() {
            return targets;
        }

        public boolean isQualified() {
            return !targets.isEmpty();
        }
    }

    /**
     * {@code uses TypeName}. {@link #getResolvedType()} is filled in during semantic analysis.
     */
    public static final class Uses extends Directive {
        private final String serviceName;
        private ClassNode resolvedType;

        public Uses(final String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public ClassNode getResolvedType() {
            return resolvedType;
        }

        public void setResolvedType(final ClassNode resolvedType) {
            this.resolvedType = resolvedType;
        }
    }

    /**
     * {@code provides TypeName with TypeName {, TypeName}}.
     * Resolved types are filled in during semantic analysis.
     */
    public static final class Provides extends Directive {
        private final String serviceName;
        private final List<String> providerNames;
        private ClassNode resolvedService;
        private List<ClassNode> resolvedProviders = List.of();

        public Provides(final String serviceName, final List<String> providerNames) {
            this.serviceName = serviceName;
            this.providerNames = Collections.unmodifiableList(new ArrayList<>(providerNames));
        }

        public String getServiceName() {
            return serviceName;
        }

        public List<String> getProviderNames() {
            return providerNames;
        }

        public ClassNode getResolvedService() {
            return resolvedService;
        }

        public void setResolvedService(final ClassNode resolvedService) {
            this.resolvedService = resolvedService;
        }

        public List<ClassNode> getResolvedProviders() {
            return resolvedProviders;
        }

        public void setResolvedProviders(final List<ClassNode> resolvedProviders) {
            this.resolvedProviders = Collections.unmodifiableList(new ArrayList<>(resolvedProviders));
        }
    }
}
