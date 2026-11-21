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
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Expands {@code @AnnotationCollector} aliases on a module declaration.
 * <p>
 * The collector visitor only accepts a class. A temporary class is built for
 * that call and then discarded: it is not added to the module, and transforms
 * recorded on it are not run. A module declaration has no dynamic code, so
 * static compilation and other class transforms do not apply to it.
 */
public final class ModuleInfoAnnotations {

    private ModuleInfoAnnotations() {
    }

    /**
     * Expands collector annotations on {@code declaration} into the annotations
     * that will be checked and written. No-op when the declaration has none.
     *
     * @param source the modular compilation unit
     * @param declaration the module declaration
     * @param compilationUnit the compilation whose transform loader is used
     */
    public static void expand(final SourceUnit source, final ModuleInfoNode declaration, final CompilationUnit compilationUnit) {
        if (source == null || declaration == null || declaration.getAnnotations().isEmpty()) {
            return;
        }
        ClassNode carrier = new ClassNode(ModuleInfoNode.CLASS_FILE_NAME, Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        if (declaration.getSourceModule() != null) {
            carrier.setModule(declaration.getSourceModule());
        }
        carrier.addAnnotations(new ArrayList<>(declaration.getAnnotations()));
        ASTTransformationCollectorCodeVisitor collector =
                new ASTTransformationCollectorCodeVisitor(source, compilationUnit.getTransformLoader());
        collector.visitClass(carrier);
        List<AnnotationNode> expanded = new ArrayList<>(carrier.getAnnotations());
        declaration.getAnnotations().clear();
        declaration.addAnnotations(expanded);
    }
}
