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
package org.apache.groovy.contracts.ast.visitor;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;

import java.util.Objects;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;

/**
 * <p>
 * Base class for {@link org.codehaus.groovy.ast.ClassCodeVisitorSupport} descendants. This class is used in groovy-contracts
 * as root class for all code visitors directly used by global AST transformations.
 * </p>
 *
 * @see org.codehaus.groovy.ast.ClassCodeVisitorSupport
 */
public abstract class BaseVisitor extends ClassCodeVisitorSupport {

    public static final String GCONTRACTS_ENABLED_VAR = "$GCONTRACTS_ENABLED";

    protected final SourceUnit sourceUnit;

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public BaseVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        this.sourceUnit = sourceUnit;
    }

    public    static BooleanExpression asConditionExecution(final AnnotationNode annotation) {
        var conditionClass = annotation.getMember("value").getType();
        var createInstance = ctorX(conditionClass, args(VariableExpression.THIS_EXPRESSION, VariableExpression.THIS_EXPRESSION));
        final MethodCallExpression doCall = callX(createInstance, "doCall");
        doCall.setMethodTarget(conditionClass.getMethods("doCall").get(0));
        BooleanExpression asBoolean = boolX(doCall);
        asBoolean.setSourcePosition(annotation);
        return asBoolean;
    }

    protected static ClosureExpression getOriginalCondition(final AnnotationNode annotation) {
        Expression value = annotation.getMember("value");
        if (value instanceof ClosureExpression) {
            return (ClosureExpression) value;
        }
        return null;
    }

    protected static /*???*/Expression getReplacedCondition(final AnnotationNode annotation) {
        Expression value = annotation.getMember("value");
        if (!(value instanceof ClosureExpression)) {
            return value;
        }
        return null;
    }

    protected static void replaceCondition(final AnnotationNode node, final Expression expr) {
        node.setMember("value", Objects.requireNonNull(expr));
    }
}
