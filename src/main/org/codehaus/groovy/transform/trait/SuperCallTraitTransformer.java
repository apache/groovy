/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform.trait;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

/**
 * This transformer is used to transform calls to <code>SomeTrait.super.foo()</code> into the appropriate trait call.
 *
 * @author Cédric Champeau
 * @since 2.3.0
 */
class SuperCallTraitTransformer extends ClassCodeExpressionTransformer {
    private final SourceUnit unit;

    SuperCallTraitTransformer(final SourceUnit unit) {
        this.unit = unit;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    @Override
    public Expression transform(final Expression exp) {
        if (exp instanceof PropertyExpression) {
            return transformPropertyExpression((PropertyExpression) exp);
        }
        if (exp instanceof MethodCallExpression) {
            return transformMethodCallExpression((MethodCallExpression)exp);
        }
        return super.transform(exp);
    }

    private Expression transformMethodCallExpression(final MethodCallExpression exp) {
        Expression objectExpression = transform(exp.getObjectExpression());
        ClassNode traitReceiver = objectExpression.getNodeMetaData(SuperCallTraitTransformer.class);
        if (traitReceiver!=null) {
            TraitHelpersTuple helpers = Traits.findHelpers(traitReceiver);
            // (SomeTrait.super).foo() --> SomeTrait$Helper.foo(this)
            ClassExpression receiver = new ClassExpression(
                    helpers.getHelper()
            );
            ArgumentListExpression newArgs = new ArgumentListExpression();
            Expression arguments = exp.getArguments();
            newArgs.addExpression(new VariableExpression("this"));
            if (arguments instanceof TupleExpression) {
                List<Expression> expressions = ((TupleExpression) arguments).getExpressions();
                for (Expression expression : expressions) {
                    newArgs.addExpression(transform(expression));
                }
            } else {
                newArgs.addExpression(arguments);
            }
            MethodCallExpression result = new MethodCallExpression(
                    receiver,
                    exp.getMethod(),
                    newArgs
            );
            result.setImplicitThis(false);
            result.setSpreadSafe(exp.isSpreadSafe());
            result.setSafe(exp.isSafe());
            result.setSourcePosition(exp);
            return result;
        }
        return super.transform(exp);
    }

    private Expression transformPropertyExpression(final PropertyExpression expression) {
        Expression objectExpression = expression.getObjectExpression();
        ClassNode type = objectExpression.getType();
        if (objectExpression instanceof ClassExpression) {
            if (Traits.isTrait(type) && "super".equals(expression.getPropertyAsString())) {
                // SomeTrait.super --> annotate to recognize later
                expression.putNodeMetaData(SuperCallTraitTransformer.class, type);
            }
        }
        return super.transform(expression);
    }
}
