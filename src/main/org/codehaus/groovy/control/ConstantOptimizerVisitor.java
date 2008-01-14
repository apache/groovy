/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Visitor to replace numbered constants with references to static fields
 *
 * @author Alex Tkachman
 */
public class ConstantOptimizerVisitor extends ClassCodeExpressionTransformer {
    private ClassNode currentClass;
    private SourceUnit source;

    Map const2Var = new HashMap();

    public ConstantOptimizerVisitor(CompilationUnit cu) {
    }

    public void visitClass(ClassNode node, SourceUnit source) {
        this.currentClass = node;
        this.source = source;
        const2Var.clear();
        super.visitClass(node);
        addFields();
    }

    public Expression transform(Expression exp) {
        if (exp == null) return null;
        if (!currentClass.isInterface() && exp.getClass() == ConstantExpression.class) {
            ConstantExpression constantExpression = (ConstantExpression) exp;
            final Object n = constantExpression.getValue();
            if (n instanceof Number || n instanceof Character) {
                FieldNode field = (FieldNode) const2Var.get(n);
                if (field == null) {
                    field = new FieldNode("$const$" + const2Var.size(),
                            Opcodes.ACC_PRIVATE|Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC| Opcodes.ACC_FINAL,
                            constantExpression.getType(),
                            currentClass,
                            constantExpression
                            );
                    field.setSynthetic(true);
                    const2Var.put(n, field);
                }
                constantExpression.setConstantName(field.getName());
            }
        }
        return exp.transformExpression(this);
    }

    private void addFields() {
        for (Iterator it = const2Var.values().iterator(); it.hasNext(); ) {
            currentClass.addConstField((FieldNode) it.next());
        }
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }
}
