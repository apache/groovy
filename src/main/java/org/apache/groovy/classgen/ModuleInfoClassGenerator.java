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
package org.apache.groovy.classgen;

import org.apache.groovy.ast.ModuleInfoNode;
import org.apache.groovy.ast.ModuleInfoNode.Exports;
import org.apache.groovy.ast.ModuleInfoNode.Opens;
import org.apache.groovy.ast.ModuleInfoNode.Provides;
import org.apache.groovy.ast.ModuleInfoNode.Requires;
import org.apache.groovy.ast.ModuleInfoNode.Uses;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.control.CompiledClassIndex;
import org.codehaus.groovy.control.ModuleImportHelper;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ModuleVisitor;

import java.lang.module.ModuleFinder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ACC_MANDATED;
import static org.objectweb.asm.Opcodes.ACC_MODULE;
import static org.objectweb.asm.Opcodes.ACC_OPEN;
import static org.objectweb.asm.Opcodes.ACC_STATIC_PHASE;
import static org.objectweb.asm.Opcodes.ACC_TRANSITIVE;
import static org.objectweb.asm.Opcodes.V9;
import static org.objectweb.asm.Opcodes.V_PREVIEW;

/**
 * Writes {@code module-info.class} (JVMS §4.7.25) from a {@link ModuleInfoNode}.
 * Generation does not visit a class: a module declaration has no methods, fields,
 * or dynamic call sites, and it must not pass through {@link AsmClassGenerator#visitClass}.
 * Annotation member values are written with {@link AsmClassGenerator#visitAnnotationAttributes}
 * because that walk is not class generation.
 */
public final class ModuleInfoClassGenerator {

    private ModuleInfoClassGenerator() {
    }

    /**
     * Generates {@code module-info.class} for {@code declaration}.
     * The class-file version comes from the compile unit and is raised to 9
     * when the configured target is older, keeping the preview bit.
     *
     * @param source the modular compilation unit, or {@code null}
     * @param context the generator context of the compilation
     * @param declaration the module declaration
     * @return the class file bytes
     */
    public static byte[] generate(final SourceUnit source, final GeneratorContext context, final ModuleInfoNode declaration) {
        ClassWriter writer = new ClassWriter(0);
        int version = V9;
        CompileUnit unit = context != null ? context.getCompileUnit() : null;
        if (unit != null && unit.getConfig() != null) {
            version = unit.getConfig().getBytecodeVersion();
        }
        write(writer, source, context, declaration, version);
        return writer.toByteArray();
    }

    /**
     * Writes one module descriptor to {@code classVisitor}.
     * A visitor that declines the module ({@code visitModule} returns {@code null})
     * receives {@code visitEnd} and nothing else. An unresolved service or provider
     * name is written with {@code '.'} folded to {@code '/'}.
     *
     * @param classVisitor the visitor that receives the class
     * @param source the modular compilation unit, or {@code null}
     * @param context the generator context, or {@code null} when packages come only from the declaration
     * @param declaration the module declaration
     * @param bytecodeVersion the requested class-file version; values below 9 are raised to 9
     */
    public static void write(final ClassVisitor classVisitor, final SourceUnit source, final GeneratorContext context,
            final ModuleInfoNode declaration, int bytecodeVersion) {
        if ((bytecodeVersion & ~V_PREVIEW) < V9) {
            bytecodeVersion = V9 | (bytecodeVersion & V_PREVIEW);
        }
        classVisitor.visit(bytecodeVersion, ACC_MODULE, ModuleInfoNode.CLASS_FILE_NAME, null, null, null);
        classVisitor.visitSource(sourceFileName(source), null);
        ModuleVisitor moduleVisitor = classVisitor.visitModule(declaration.getName(), declaration.isOpen() ? ACC_OPEN : 0, null);
        if (moduleVisitor == null) {
            classVisitor.visitEnd();
            return;
        }
        // When ModulePackages is present it must list every package the module
        // exports, opens, or contains. An open module's opens table must be empty.
        Set<String> packages = new LinkedHashSet<>();
        for (Exports exports : declaration.getExports()) {
            addPackage(packages, exports.getPackageName());
        }
        if (!declaration.isOpen()) {
            for (Opens opens : declaration.getOpens()) {
                addPackage(packages, opens.getPackageName());
            }
        }
        CompileUnit compileUnit = context != null ? context.getCompileUnit() : null;
        if (compileUnit == null && declaration.getSourceModule() != null) {
            compileUnit = declaration.getSourceModule().getUnit();
        }
        if (compileUnit != null) {
            for (ClassNode cn : compileUnit.getClasses()) {
                addPackage(packages, cn.getPackageName());
            }
        }
        CompiledClassIndex javaClasses = context != null ? context.getCompiledClasses() : null;
        if (javaClasses != null) {
            packages.addAll(javaClasses.getPackages());
        }
        for (Provides provides : declaration.getProvides()) {
            for (ClassNode provider : provides.getResolvedProviders()) {
                if (provider != null) {
                    addPackage(packages, provider.getPackageName());
                }
            }
        }
        for (String pkg : packages) {
            moduleVisitor.visitPackage(pkg.replace('.', '/'));
        }
        ModuleFinder versionFinder = source != null ? ModuleImportHelper.moduleFinder(source) : ModuleFinder.ofSystem();
        boolean implicitJavaBase = !"java.base".equals(declaration.getName());
        if (implicitJavaBase) {
            for (Requires requires : declaration.getRequires()) {
                if ("java.base".equals(requires.getModuleName())) {
                    implicitJavaBase = false;
                    break;
                }
            }
        }
        if (implicitJavaBase) {
            moduleVisitor.visitRequire("java.base", ACC_MANDATED, ModuleInfoVerifier.readModuleVersion(versionFinder, "java.base"));
        }
        for (Requires requires : declaration.getRequires()) {
            int access = 0;
            if (requires.isTransitive()) {
                access |= ACC_TRANSITIVE;
            }
            if (requires.isStaticPhase()) {
                access |= ACC_STATIC_PHASE;
            }
            moduleVisitor.visitRequire(requires.getModuleName(), access, ModuleInfoVerifier.readModuleVersion(versionFinder, requires.getModuleName()));
        }
        for (Exports exports : declaration.getExports()) {
            moduleVisitor.visitExport(exports.getPackageName().replace('.', '/'), 0, friendModules(exports.getTargets()));
        }
        if (!declaration.isOpen()) {
            for (Opens opens : declaration.getOpens()) {
                moduleVisitor.visitOpen(opens.getPackageName().replace('.', '/'), 0, friendModules(opens.getTargets()));
            }
        }
        for (Uses uses : declaration.getUses()) {
            moduleVisitor.visitUse(internalTypeName(uses.getResolvedType(), uses.getServiceName()));
        }
        for (Provides provides : declaration.getProvides()) {
            String service = internalTypeName(provides.getResolvedService(), provides.getServiceName());
            List<ClassNode> resolved = provides.getResolvedProviders();
            String[] providers = new String[provides.getProviderNames().size()];
            for (int i = 0; i < providers.length; i++) {
                ClassNode type = i < resolved.size() ? resolved.get(i) : null;
                providers[i] = internalTypeName(type, provides.getProviderNames().get(i));
            }
            moduleVisitor.visitProvide(service, providers);
        }
        moduleVisitor.visitEnd();
        writeAnnotations(classVisitor, source, context, declaration);
        classVisitor.visitEnd();
    }

    private static void addPackage(final Set<String> packages, final String pkg) {
        if (pkg != null && !pkg.isEmpty()) {
            packages.add(pkg);
        }
    }

    private static String[] friendModules(final List<String> targets) {
        return targets.isEmpty() ? null : targets.toArray(String[]::new);
    }

    private static String internalTypeName(final ClassNode type, final String sourceName) {
        if (type != null) {
            return BytecodeHelper.getClassInternalName(type);
        }
        return sourceName.replace('.', '/');
    }

    private static String sourceFileName(final SourceUnit source) {
        if (source == null || source.getName() == null) {
            return "module-info.groovy";
        }
        String name = source.getName();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        return slash >= 0 ? name.substring(slash + 1) : name;
    }

    private static void writeAnnotations(final ClassVisitor classVisitor, final SourceUnit source,
            final GeneratorContext context, final ModuleInfoNode declaration) {
        if (declaration.getAnnotations().isEmpty()) {
            return;
        }
        AsmClassGenerator attributes = new AsmClassGenerator(source, context, classVisitor, sourceFileName(source));
        for (AnnotationNode annotation : declaration.getAnnotations()) {
            if (annotation.hasSourceRetention()) {
                continue;
            }
            AnnotationVisitor visitor = classVisitor.visitAnnotation(
                    BytecodeHelper.getTypeDescription(annotation.getClassNode()), annotation.hasRuntimeRetention());
            if (visitor == null) {
                continue;
            }
            attributes.visitAnnotationAttributes(annotation, visitor);
            visitor.visitEnd();
        }
    }
}
