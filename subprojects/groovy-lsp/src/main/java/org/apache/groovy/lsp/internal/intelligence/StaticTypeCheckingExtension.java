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
package org.apache.groovy.lsp.internal.intelligence;

import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;
import org.apache.groovy.lsp.internal.compile.AstQuery;
import org.apache.groovy.lsp.spi.GroovyLspExtension;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Default intelligence pass for {@code @TypeChecked} / {@code @CompileStatic}
 * members. Those transforms run at {@code INSTRUCTION_SELECTION}, which
 * groovy-lsp never reaches; this copies {@code DIRECT_METHOD_CALL_TARGET}
 * onto {@code methodTarget} so hover, rename and parameter-name inlays
 * see a compiler bind. Dynamic Groovy is left alone.
 */
public final class StaticTypeCheckingExtension implements GroovyLspExtension {

    private static final ClassNode TYPE_CHECKED = ClassHelper.make(TypeChecked.class);
    private static final ClassNode COMPILE_STATIC = ClassHelper.make(CompileStatic.class);

    @Override
    public String id() {
        return "groovy.lsp.stc";
    }

    @Override
    public void afterCompile(final CompilationUnit unit) {
        if (unit == null) {
            return;
        }
        Iterator<SourceUnit> sources = unit.iterator();
        while (sources.hasNext()) {
            SourceUnit source = sources.next();
            ModuleNode module = source == null ? null : source.getAST();
            if (module == null) {
                continue;
            }
            for (ClassNode classNode : module.getClasses()) {
                checkClass(source, unit, classNode);
            }
        }
    }

    private static void checkClass(final SourceUnit source, final CompilationUnit unit, final ClassNode classNode) {
        if (classNode == null) {
            return;
        }
        LspTypeCheckingVisitor probe = new LspTypeCheckingVisitor(source, classNode);
        boolean classWants = annotated(classNode) && !probe.isSkipMode(classNode);
        Set<MethodNode> methods = new LinkedHashSet<>();
        for (MethodNode method : classNode.getMethods()) {
            if (method != null && annotated(method) && !probe.isSkipMode(method)) {
                methods.add(method);
            }
        }
        if (!classWants && methods.isEmpty()) {
            return;
        }
        LspTypeCheckingVisitor visitor = new LspTypeCheckingVisitor(source, classNode);
        visitor.setCompilationUnit(unit);
        if (!classWants) {
            visitor.setMethodsToBeVisited(methods);
        }
        visitor.initialize();
        visitor.visitClass(classNode);
        visitor.performSecondPass();
        bindTargets(classNode);
    }

    private static void bindTargets(final ClassNode classNode) {
        AstQuery.walk(classNode, (node, ctx) -> {
            if (node instanceof MethodCallExpression call && call.getMethodTarget() == null) {
                Object target = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                if (target instanceof MethodNode method) {
                    call.setMethodTarget(method);
                }
            }
        });
    }

    private static boolean annotated(final AnnotatedNode node) {
        return node != null && (!node.getAnnotations(TYPE_CHECKED).isEmpty()
                || !node.getAnnotations(COMPILE_STATIC).isEmpty());
    }

    private static final class LspTypeCheckingVisitor extends StaticTypeCheckingVisitor {

        LspTypeCheckingVisitor(final SourceUnit source, final ClassNode classNode) {
            super(source, classNode);
        }

        @Override
        protected ClassNode[] getTypeCheckingAnnotations() {
            return new ClassNode[]{TYPE_CHECKED, COMPILE_STATIC};
        }
    }
}
