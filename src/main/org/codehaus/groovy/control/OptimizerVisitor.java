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
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.objectweb.asm.Opcodes;

import java.util.*;

/**
 * Visitor to produce several optimizations
 *  - to replace numbered constants with references to static fields
 *
 * @author Alex Tkachman
 */
public class OptimizerVisitor extends ClassCodeExpressionTransformer {
    private ClassNode currentClass;
    private SourceUnit source;

    private Map const2Var = new HashMap();
    private List<FieldNode> missingFields = new LinkedList<FieldNode>();

    public OptimizerVisitor(CompilationUnit cu) {
    }

    public void visitClass(ClassNode node, SourceUnit source) {
        this.currentClass = node;
        this.source = source;
        const2Var.clear();
        missingFields.clear();
        super.visitClass(node);
        addMissingFields();
    }

    private void addMissingFields() {
        for (Object missingField : missingFields) {
            FieldNode f = (FieldNode) missingField;
            currentClass.addField(f);
        }
    }

    private void setConstField(ConstantExpression constantExpression) {
        final Object n = constantExpression.getValue();
        if (!(n instanceof Number || n instanceof Character)) return;
        if (n instanceof Integer || n instanceof Double) return;
        FieldNode field = (FieldNode) const2Var.get(n);
        if (field!=null) {
            constantExpression.setConstantName(field.getName());
            return;
        }
        final String name = "$const$" + const2Var.size();
        //TODO: this part here needs a bit of rethinking. If it can happen that the field is defined already,
        //      then is this code still valid?
        field = currentClass.getDeclaredField(name);
        if (field==null) {
            field = new FieldNode(name,
                    Opcodes.ACC_PRIVATE|Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC| Opcodes.ACC_FINAL,
                    constantExpression.getType(),
                    currentClass,
                    constantExpression
                    );
            field.setSynthetic(true);
            missingFields.add(field);
        }
        constantExpression.setConstantName(field.getName());
        const2Var.put(n, field);
    }

    public Expression transform(Expression exp) {
        if (exp == null) return null;
        if (!currentClass.isInterface() && exp.getClass() == ConstantExpression.class) {
            setConstField((ConstantExpression)exp);
        }
        return exp.transformExpression(this);
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    public void visitClosureExpression(ClosureExpression expression) {
        /*
         * GROOVY-3339 - do nothing - so that numbers don't get replaced by cached constants in closure classes
         */
    }
}
