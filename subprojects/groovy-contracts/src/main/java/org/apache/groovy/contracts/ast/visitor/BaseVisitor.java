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

    /**
     * Local variable name used by generated code to guard contract execution.
     */
    public static final String GCONTRACTS_ENABLED_VAR = "$GCONTRACTS_ENABLED";

    /**
     * Source unit currently being visited.
     */
    protected final SourceUnit sourceUnit;

    /**
     * Returns the source unit associated with this visitor.
     *
     * @return the current source unit
     */
    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    /**
     * Creates a visitor bound to the supplied source unit.
     *
     * @param sourceUnit the source unit currently being transformed
     * @param source the reader source backing the source unit
     */
    public BaseVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        this.sourceUnit = sourceUnit;
    }

    /**
     * Creates a boolean expression that invokes the generated closure backing the supplied annotation.
     *
     * @param annotation the contract annotation whose closure should be executed
     * @return a boolean expression that evaluates the generated closure
     */
    public static BooleanExpression asConditionExecution(final AnnotationNode annotation) {
        var conditionClass = annotation.getMember("value").getType();
        var createInstance = ctorX(conditionClass, args(VariableExpression.THIS_EXPRESSION, VariableExpression.THIS_EXPRESSION));
        final MethodCallExpression doCall = callX(createInstance, "doCall");
        doCall.setMethodTarget(conditionClass.getMethods("doCall").get(0));
        BooleanExpression asBoolean = boolX(doCall);
        asBoolean.setSourcePosition(annotation);
        return asBoolean;
    }

    /**
     * Returns the original closure expression stored on the annotation, if it has not yet been replaced.
     *
     * @param annotation the annotation to inspect
     * @return the original closure expression, or {@code null} if it has already been rewritten
     */
    protected static ClosureExpression getOriginalCondition(final AnnotationNode annotation) {
        Expression value = annotation.getMember("value");
        if (value instanceof ClosureExpression) {
            return (ClosureExpression) value;
        }
        return null;
    }

    /**
     * Returns the rewritten annotation value once the original closure has been replaced.
     *
     * @param annotation the annotation to inspect
     * @return the replacement expression, or {@code null} while the original closure is still present
     */
    protected static /*???*/Expression getReplacedCondition(final AnnotationNode annotation) {
        Expression value = annotation.getMember("value");
        if (!(value instanceof ClosureExpression)) {
            return value;
        }
        return null;
    }

    /**
     * Replaces the annotation value with the generated expression used by later transformation phases.
     *
     * @param node the annotation to update
     * @param expr the replacement expression
     */
    protected static void replaceCondition(final AnnotationNode node, final Expression expr) {
        node.setMember("value", Objects.requireNonNull(expr));
    }
}
