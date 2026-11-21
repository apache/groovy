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
package org.apache.groovy.control;

import org.apache.groovy.ast.ModuleInfoNode;
import org.apache.groovy.ast.ModuleInfoNode.Provides;
import org.apache.groovy.ast.ModuleInfoNode.Uses;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves imports and type names in a module declaration.
 * <p>
 * A module declaration has no executable code, so the ordinary class visitor is
 * not used. Import resolution and classpath lookup are the same operations
 * {@link ResolveVisitor} uses for classes. Simple names follow JLS §6.4.1:
 * single-type and single-static imports, then on-demand imports, then
 * {@code java.lang}, then {@code import module}. The Groovy default imports
 * ({@code groovy.lang}, {@code java.util}, and the rest) are not implied.
 * Accessibility of a resolved directive type is checked later, once the module
 * graph is known; this visitor does not apply that test.
 */
public final class ModuleInfoResolveVisitor extends ResolveVisitor {

    private final CompilationUnit unit;
    private final ModuleInfoNode declaration;

    private ModuleInfoResolveVisitor(final CompilationUnit compilationUnit, final ModuleInfoNode declaration) {
        super(compilationUnit);
        this.unit = compilationUnit;
        this.declaration = declaration;
    }

    /**
     * Resolves the declaration's imports, annotations, and directive types.
     * Does nothing when the declaration is missing or its types were already resolved.
     * A missing source module still resolves qualified names against the loader.
     *
     * @param source the modular compilation unit, or {@code null}
     * @param declaration the module declaration, or {@code null}
     * @param compilationUnit the compilation, or {@code null} when the declaration is checked on its own
     */
    public static void resolve(final SourceUnit source, final ModuleInfoNode declaration, final CompilationUnit compilationUnit) {
        if (source == null || declaration == null || declaration.hasResolvedTypes()) {
            return;
        }
        ModuleInfoResolveVisitor visitor = new ModuleInfoResolveVisitor(compilationUnit, declaration);
        ModuleNode module = declaration.getSourceModule();
        visitor.withResolveContext(source, contextClass(module), () -> {
            if (module != null) {
                visitor.resolveImports(module);
                for (AnnotationNode annotation : declaration.getAnnotations()) {
                    visitor.visitAnnotation(annotation);
                }
            }
            for (Uses uses : declaration.getUses()) {
                uses.setResolvedType(visitor.resolveType(uses.getServiceName(), uses));
            }
            for (Provides provides : declaration.getProvides()) {
                provides.setResolvedService(visitor.resolveType(provides.getServiceName(), provides));
                List<ClassNode> providers = new ArrayList<>();
                for (String providerName : provides.getProviderNames()) {
                    providers.add(visitor.resolveType(providerName, provides));
                }
                provides.setResolvedProviders(providers);
            }
            declaration.markTypesResolved();
        });
    }

    /**
     * The context class exists only so shared lookup can see the source module.
     * It is not added to the module. Its name is not a legal type name, so the
     * resolver's "current class" shortcut cannot bind a source type to it.
     */
    private static ClassNode contextClass(final ModuleNode module) {
        ClassNode context = new ClassNode("<module-info>", Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        if (module != null) {
            context.setModule(module);
        }
        return context;
    }

    @Override
    protected void checkModuleAccessibility(final ClassNode type, final ASTNode node) {
        // Directive types are checked by ModuleInfoVerifier once the graph is final.
    }

    private ClassNode resolveType(final String sourceName, final ASTNode where) {
        String qualified = qualify(sourceName, where);
        if (qualified == null) {
            return null;
        }
        ClassNode resolved = resolveBinary(qualified);
        if (resolved == null) {
            addError("unable to resolve class " + sourceName, where);
        }
        return resolved;
    }

    /**
     * A simple name follows single-type imports, static single-type imports,
     * on-demand imports, {@code java.lang}, then {@code import module}.
     * A qualified name tries its leftmost identifier as an in-scope type so
     * {@code System.LoggerFinder} and {@code Outer.Inner} resolve to nested types.
     *
     * @return a binary name, or {@code null} when the simple name is ambiguous or unknown
     */
    private String qualify(final String sourceName, final ASTNode where) {
        ModuleNode module = declaration.getSourceModule();
        if (module == null) {
            return sourceName;
        }
        int dot = sourceName.indexOf('.');
        if (dot >= 0) {
            String head = sourceName.substring(0, dot);
            String tail = sourceName.substring(dot + 1);
            String resolvedHead = resolveSimpleType(head, where, false);
            if (resolvedHead != null) {
                return resolvedHead + "$" + tail.replace('.', '$');
            }
            return sourceName;
        }
        return resolveSimpleType(sourceName, where, true);
    }

    private String resolveSimpleType(final String simple, final ASTNode where, final boolean reportAmbiguous) {
        ModuleNode module = declaration.getSourceModule();
        ImportNode single = module.getImport(simple);
        if (single != null && single.getType() != null) {
            return single.getType().getName();
        }
        String fromStatic = staticSingleType(simple);
        if (fromStatic != null) {
            return fromStatic;
        }
        List<String> onDemand = new ArrayList<>();
        collectStarCandidates(module.getStarImports(), simple, onDemand);
        collectStaticStarCandidates(simple, onDemand);
        String fromJavaLang = "java.lang." + simple;
        if (lookup(fromJavaLang) != null && !onDemand.contains(fromJavaLang)) {
            onDemand.add(fromJavaLang);
        }
        if (onDemand.size() == 1) {
            return onDemand.get(0);
        }
        if (onDemand.size() > 1) {
            if (reportAmbiguous) {
                addError("reference to " + simple + " is ambiguous", where);
            }
            return null;
        }
        List<String> fromModules = new ArrayList<>();
        collectStarCandidates(module.getModuleStarImports(), simple, fromModules);
        if (fromModules.size() == 1) {
            return fromModules.get(0);
        }
        if (fromModules.size() > 1) {
            if (reportAmbiguous) {
                addError("reference to " + simple + " is ambiguous", where);
            }
            return null;
        }
        if (reportAmbiguous) {
            addError("unable to resolve class " + simple, where);
        }
        return null;
    }

    private String staticSingleType(final String simple) {
        ImportNode imported = declaration.getSourceModule().getStaticImports().get(simple);
        if (imported == null || imported.getType() == null || imported.getFieldName() == null) {
            return null;
        }
        String binary = imported.getType().getName() + "$" + imported.getFieldName();
        ClassNode resolved = lookup(binary);
        return resolved == null ? null : resolved.getName();
    }

    private void collectStarCandidates(final List<ImportNode> stars, final String simple, final List<String> candidates) {
        if (stars == null) {
            return;
        }
        for (ImportNode star : stars) {
            String packageName = packagePrefix(star.getPackageName());
            if (packageName == null) {
                continue;
            }
            String qualified = packageName + simple;
            if (lookup(qualified) != null && !candidates.contains(qualified)) {
                candidates.add(qualified);
            }
        }
    }

    private void collectStaticStarCandidates(final String simple, final List<String> candidates) {
        for (ImportNode star : declaration.getSourceModule().getStaticStarImports().values()) {
            if (star.getType() == null) {
                continue;
            }
            String qualified = star.getType().getName() + "$" + simple;
            if (lookup(qualified) != null && !candidates.contains(qualified)) {
                candidates.add(qualified);
            }
        }
    }

    private static String packagePrefix(final String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return null;
        }
        return packageName.endsWith(".") ? packageName : packageName + ".";
    }

    private ClassNode resolveBinary(final String sourceName) {
        String[] parts = sourceName.split("\\.");
        for (int classStart = parts.length - 1; classStart >= 0; classStart--) {
            ClassNode found = lookup(binaryName(parts, classStart));
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static String binaryName(final String[] parts, final int classStart) {
        StringBuilder binary = new StringBuilder();
        for (int i = 0; i < classStart; i++) {
            if (i > 0) {
                binary.append('.');
            }
            binary.append(parts[i]);
        }
        if (classStart > 0) {
            binary.append('.');
        }
        for (int i = classStart; i < parts.length; i++) {
            if (i > classStart) {
                binary.append('$');
            }
            binary.append(parts[i]);
        }
        return binary.toString();
    }

    private ClassNode lookup(final String binary) {
        ModuleNode module = declaration.getSourceModule();
        if (unit != null && module != null && module.getUnit() != null) {
            ClassNode candidate = ClassHelper.makeWithoutCaching(binary);
            boolean[] found = new boolean[1];
            withResolveContext(getSourceUnit(), contextClass(module), () -> {
                if (resolveFromCompileUnit(candidate) || resolveToOuter(candidate)) {
                    found[0] = true;
                }
            });
            if (found[0]) {
                return candidate.redirect();
            }
            return null;
        }
        ClassLoader loader = loader();
        try {
            return ClassHelper.make(Class.forName(binary, false, loader));
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private ClassLoader loader() {
        SourceUnit source = getSourceUnit();
        if (source != null && source.getClassLoader() != null) {
            return source.getClassLoader();
        }
        return ClassLoader.getSystemClassLoader();
    }
}
