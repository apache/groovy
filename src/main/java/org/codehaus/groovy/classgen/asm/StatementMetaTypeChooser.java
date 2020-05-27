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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;

/**
 * A {@link TypeChooser} which is aware of statement metadata.
 */
public class StatementMetaTypeChooser implements TypeChooser {
    @Override
    public ClassNode resolveType(final Expression exp, final ClassNode current) {
        ClassNode type = null;
        if (exp instanceof ClassExpression) { type = exp.getType();
            ClassNode classType = ClassHelper.makeWithoutCaching("java.lang.Class");
            classType.setGenericsTypes(new GenericsType[] {new GenericsType(type)});
            classType.setRedirect(ClassHelper.CLASS_Type);
            return classType;
        }

        OptimizingStatementWriter.StatementMeta meta = exp.getNodeMetaData(OptimizingStatementWriter.StatementMeta.class);
        if (meta != null) type = meta.type;
        if (type != null) return type;

        if (exp instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) exp;
            if (ve.isClosureSharedVariable()) return ve.getType();
            if (ve.getAccessedVariable() instanceof FieldNode) {
                FieldNode fn = (FieldNode) ve.getAccessedVariable();
                if (!fn.getDeclaringClass().equals(current)) return fn.getOriginType();
            }
            type = ve.getOriginType();
        } else if (exp instanceof Variable) {
            Variable v = (Variable) exp;
            type = v.getOriginType();
        } else {
            type = exp.getType();
        }
        return type.redirect();
    }
}
