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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.isGroovyObjectType;

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

    /**
     * Creates an optimizer visitor.
     *
     * @param cu the owning compilation unit
     */
    public OptimizerVisitor(CompilationUnit cu) {
    }

    /**
     * Applies optimizer rewrites to the supplied class.
     *
     * @param node the class to optimize
     * @param source the source unit containing the class
     */
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
                if (isGroovyObjectType(classNode)) {
                    needsFix = true;
                    break;
                }
            }
            if (needsFix) {
                List<ClassNode> newInterfaces = new ArrayList<ClassNode>(interfaces.length);
                for (ClassNode classNode : interfaces) {
                    if (!isGroovyObjectType(classNode)) {
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
            stampConstantName(constantExpression, field.getName());
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
        stampConstantName(constantExpression, field.getName());
        if (isPrimitive) {
            const2Prims.put(n, field);
        } else {
            const2Objects.put(n, field);
        }
    }

    /**
     * Records the cached-constant field name on the expression node, warning when the node was
     * already stamped with a different name (GROOVY-12131). The field name lives on the node
     * itself, which assumes each node is reachable from exactly one class — the parser only ever
     * produces such trees, and closure bodies are exempt from caching (GROOVY-3339). An AST
     * transform that aliases a constant-bearing subtree into a second class breaks the
     * assumption: the classes overwrite each other's stamp and whichever class generates
     * bytecode against the other's numbering silently reads the wrong {@code $const$} field at
     * runtime. A same-name re-stamp is harmless (each class reads its own field, holding the
     * same value), so only a diverging name is reported.
     *
     * @param constantExpression the constant being cached
     * @param name the {@code $const$} field name assigned for the current class
     */
    private void stampConstantName(ConstantExpression constantExpression, String name) {
        String previousName = constantExpression.getConstantName();
        if (previousName != null && !previousName.equals(name)) {
            source.getErrorCollector().addWarning(WarningMessage.LIKELY_ERRORS,
                    "Constant expression " + constantExpression.getText() + " is shared between classes: cached as "
                            + previousName + " in a previously optimized class and now as " + name + " in "
                            + currentClass.getName() + ". One of the classes will read the wrong cached constant"
                            + " at runtime; the responsible AST transform should copy expression nodes rather than"
                            + " alias them across classes.",
                    Token.newString(constantExpression.getText(),
                            constantExpression.getLineNumber(), constantExpression.getColumnNumber()),
                    source);
        }
        constantExpression.setConstantName(name);
    }

    /**
     * Rewrites constant expressions that can be cached in synthetic fields.
     *
     * @param exp the expression to transform
     * @return the transformed expression
     */
    @Override
    public Expression transform(Expression exp) {
        if (exp == null) return null;
        if (!currentClass.isInterface() && exp.getClass() == ConstantExpression.class) {
            setConstField((ConstantExpression) exp);
        }
        return exp.transformExpression(this);
    }

    /**
     * Returns the source unit currently being optimized.
     *
     * @return the active source unit
     */
    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    /**
     * Leaves closure bodies untouched so constant caching does not leak into closure classes.
     *
     * @param expression the closure expression being skipped
     */
    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        /*
         * GROOVY-3339 - do nothing - so that numbers don't get replaced by cached constants in closure classes
         */
    }
}
