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
package org.codehaus.groovy.control.customizers;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.LinkedList;
import java.util.List;

/**
 * This compilation customizer allows addiing various types of imports to the compilation unit. Supports adding:
 * <ul>
 *     <li>standard imports via {@link #addImports(String...)} or {@link #addImport(String, String)}</li>
 *     <li>star imports via {@link #addStarImports(String...)}</li>
 *     <li>static imports via {@link #addStaticImport(String, String)} or {@link #addStaticImport(String, String, String)}</li>
 *     <li>static star imports via {@link #addStaticStars(String...)}</li>
 * </ul>
 *
 * @since 1.8.0
 */
public class ImportCustomizer extends CompilationCustomizer {

    private final List<Import> imports = new LinkedList<>();

    public ImportCustomizer() {
        super(CompilePhase.CONVERSION);
    }

    @Override
    public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
        ModuleNode ast = source.getAST();

        // GROOVY-8399: apply import customizations only once per module
        if (!classNode.getName().equals(ast.getMainClassName())) return;

        for (Import anImport : imports) {
            switch (anImport.type) {
                case regular:
                    ast.addImport(anImport.alias, anImport.classNode);
                    break;
                case staticImport:
                    ast.addStaticImport(anImport.classNode, anImport.field, anImport.alias);
                    break;
                case staticStar:
                    ast.addStaticStarImport(anImport.alias, anImport.classNode);
                    break;
                case star:
                    ast.addStarImport(anImport.star);
                    break;
            }
        }
    }

    public ImportCustomizer addImport(final String alias, final String className) {
        imports.add(new Import(ImportType.regular, alias, ClassHelper.make(className)));
        return this;
    }

    public ImportCustomizer addStaticImport(final String className, final String fieldName) {
        imports.add(new Import(ImportType.staticImport, fieldName, ClassHelper.make(className), fieldName));
        return this;
    }

    public ImportCustomizer addStaticImport(final String alias, final String className, final String fieldName) {
        imports.add(new Import(ImportCustomizer.ImportType.staticImport, alias, ClassHelper.make(className), fieldName));
        return this;
    }

    public ImportCustomizer addImports(final String... classNames) {
        for (String className : classNames) {
            addImport(className);
        }
        return this;
    }

    public ImportCustomizer addStarImports(final String... packageNames) {
        for (String packageName : packageNames) {
            addStarImport(packageName);
        }
        return this;
    }

    public ImportCustomizer addStaticStars(final String... classNames) {
        for (String className : classNames) {
            addStaticStar(className);
        }
        return this;
    }

    //

    private void addImport(final String className) {
        ClassNode node = ClassHelper.make(className);
        imports.add(new Import(ImportType.regular, node.getNameWithoutPackage(), node));
    }

    private void addStarImport(final String packageName) {
        imports.add(new Import(ImportType.star, packageName.endsWith(".") ? packageName : packageName + "."));
    }

    private void addStaticStar(final String className) {
        imports.add(new Import(ImportCustomizer.ImportType.staticStar, className, ClassHelper.make(className)));
    }

    // -------------------- Helper classes -------------------------

    /**
     * Represents imports which are possibly aliased.
     */
    private static final class Import {
        final ImportType type;
        final ClassNode classNode;
        final String alias;
        final String field;
        final String star; // only used for star imports

        private Import(final ImportType type, final String alias, final ClassNode classNode, final String field) {
            this.alias = alias;
            this.classNode = classNode;
            this.field = field;
            this.type = type;
            this.star = null;
        }

        private Import(final ImportType type, final String alias, final ClassNode classNode) {
            this.alias = alias;
            this.classNode = classNode;
            this.type = type;
            this.field = null;
            this.star = null;
        }

        private Import(final ImportType type, final String star) {
            this.type = type;
            this.star = star;
            this.alias = null;
            this.classNode = null;
            this.field = null;
        }
    }

    private enum ImportType {
        regular,
        staticImport,
        staticStar,
        star
    }
}
