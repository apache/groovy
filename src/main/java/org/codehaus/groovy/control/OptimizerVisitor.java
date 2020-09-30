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
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;

/**
 * Visitor to produce several optimizations:
 * <ul>
 * <li>to replace numbered constants with references to static fields</li>
 * <li>remove superfluous references to GroovyObject interface</li>
 * </ul>
 */
public class OptimizerVisitor extends ClassCodeExpressionTransformer {
    private ClassNode currentClass;
    private SourceUnit source;

    // TODO make @CS lookup smarter so that we don't need both these maps
    private final Map<Object, FieldNode> const2Objects = new HashMap<Object, FieldNode>();
    private final Map<Object, FieldNode> const2Prims = new HashMap<Object, FieldNode>();
    private int index;
    private final List<FieldNode> missingFields = new LinkedList<FieldNode>();

    public OptimizerVisitor(CompilationUnit cu) {
    }

    public void visitClass(ClassNode node, SourceUnit source) {
        this.currentClass = node;
        this.source = source;
        const2Objects.clear();
        const2Prims.clear();
        missingFields.clear();
        index = 0;
        super.visitClass(node);
        addMissingFields();
        pruneUnneededGroovyObjectInterface(node);
    }

    private void pruneUnneededGroovyObjectInterface(ClassNode node) {
        ClassNode superClass = node.getSuperClass();
        boolean isSuperGroovy = superClass.isDerivedFromGroovyObject();
        if (isSuperGroovy) {
            ClassNode[] interfaces = node.getInterfaces();
            boolean needsFix = false;
            for (ClassNode classNode : interfaces) {
                if (classNode.equals(ClassHelper.GROOVY_OBJECT_TYPE)) {
                    needsFix = true;
                    break;
                }
            }
            if (needsFix) {
                List<ClassNode> newInterfaces = new ArrayList<ClassNode>(interfaces.length);
                for (ClassNode classNode : interfaces) {
                    if (!classNode.equals(ClassHelper.GROOVY_OBJECT_TYPE)) {
                        newInterfaces.add(classNode);
                    }
                }
                node.setInterfaces(newInterfaces.toArray(ClassNode.EMPTY_ARRAY));
            }
        }
    }

    private void addMissingFields() {
        for (FieldNode f : missingFields) {
            currentClass.addField(f);
        }
    }

    private void setConstField(ConstantExpression constantExpression) {
        final Object n = constantExpression.getValue();
        if (!(n instanceof Number)) return;
        if (n instanceof Integer || n instanceof Double) return;
        if (n instanceof Long && (0L == (Long) n || 1L == (Long) n)) return; // LCONST_0, LCONST_1

        boolean isPrimitive = isPrimitiveType(constantExpression.getType());
        FieldNode field = isPrimitive ? const2Prims.get(n) : const2Objects.get(n);
        if (field != null) {
            constantExpression.setConstantName(field.getName());
            return;
        }
        String name;
        do {
            name = "$const$" + index++;
        } while (currentClass.getDeclaredField(name) != null);
        // TODO consider moving initcode to <clinit> and remaking field final
        field = new FieldNode(name,
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                constantExpression.getType(),
                currentClass,
                constantExpression);
        field.setSynthetic(true);
        missingFields.add(field);
        constantExpression.setConstantName(field.getName());
        if (isPrimitive) {
            const2Prims.put(n, field);
        } else {
            const2Objects.put(n, field);
        }
    }

    @Override
    public Expression transform(Expression exp) {
        if (exp == null) return null;
        if (!currentClass.isInterface() && exp.getClass() == ConstantExpression.class) {
            setConstField((ConstantExpression) exp);
        }
        return exp.transformExpression(this);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        /*
         * GROOVY-3339 - do nothing - so that numbers don't get replaced by cached constants in closure classes
         */
    }
}
