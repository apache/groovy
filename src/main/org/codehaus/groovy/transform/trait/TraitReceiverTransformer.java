/*
 * Copyright 2003-2013 the original author or authors.
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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.ExceptionUtils;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;

/**
 * This expression transformer is used internally by the {@link org.codehaus.groovy.transform.trait.TraitASTTransformation trait}
 * AST transformation to change the receiver of a message on "this" into a static method call on the trait helper class.
 * <p></p>
 * In a nutshell, code like this one in a trait:<p></p>
 * <code>void foo() { this.bar() }</code>
 * is transformed into:
 * <code>void foo() { TraitHelper$bar(this) }</code>
 *
 * @author Cedric Champeau
 * @since 2.3.0
 */
class TraitReceiverTransformer extends ClassCodeExpressionTransformer {

    private final VariableExpression weaved;
    private final SourceUnit unit;
    private final ClassNode fieldHelper;

    public TraitReceiverTransformer(VariableExpression thisObject, SourceUnit unit, ClassNode fieldHelper) {
        this.weaved = thisObject;
        this.unit = unit;
        this.fieldHelper = fieldHelper;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    @Override
    public Expression transform(final Expression exp) {
        if (exp instanceof BinaryExpression) {
            Expression leftExpression = ((BinaryExpression) exp).getLeftExpression();
            Expression rightExpression = ((BinaryExpression) exp).getRightExpression();
            Token operation = ((BinaryExpression) exp).getOperation();
            if (operation.getText().equals("=")) {
                String leftFieldName = null;
                // it's an assignment
                if (leftExpression instanceof VariableExpression && ((VariableExpression) leftExpression).getAccessedVariable() instanceof FieldNode) {
                    leftFieldName = ((VariableExpression) leftExpression).getAccessedVariable().getName();
                } else if (leftExpression instanceof FieldExpression) {
                    leftFieldName = ((FieldExpression) leftExpression).getFieldName();
                } else if (leftExpression instanceof PropertyExpression
                        && (((PropertyExpression) leftExpression).isImplicitThis() || "this".equals(((PropertyExpression) leftExpression).getObjectExpression().getText()))) {
                    leftFieldName = ((PropertyExpression) leftExpression).getPropertyAsString();
                }
                if (leftFieldName!=null) {
                    MethodCallExpression mce = new MethodCallExpression(
                            new CastExpression(fieldHelper,weaved),
                            leftFieldName + TraitASTTransformation.DIRECT_SETTER_SUFFIX,
                            new ArgumentListExpression(super.transform(rightExpression))
                    );
                    mce.setSourcePosition(exp);
                    mce.setImplicitThis(false);
                    return mce;
                }
            }
            Expression leftTransform = transform(leftExpression);
            Expression rightTransform = transform(rightExpression);
            Expression ret =
                    exp instanceof DeclarationExpression ?new DeclarationExpression(
                            leftTransform, operation, rightTransform
                    ):
                    new BinaryExpression(leftTransform, operation, rightTransform);
            ret.setSourcePosition(exp);
            ret.copyNodeMetaData(exp);
            return ret;
        } else if (exp instanceof MethodCallExpression) {
            MethodCallExpression call = (MethodCallExpression) exp;
            Expression obj = call.getObjectExpression();
            if (call.isImplicitThis() || obj.getText().equals("this")) {
                MethodCallExpression transformed = new MethodCallExpression(
                        weaved,
                        call.getMethod(),
                        transform(call.getArguments())
                );
                transformed.setSourcePosition(call);
                transformed.setSafe(call.isSafe());
                transformed.setSpreadSafe(call.isSpreadSafe());
                transformed.setImplicitThis(false);
                return transformed;
            }
        } else if (exp instanceof FieldExpression) {
            MethodCallExpression mce = new MethodCallExpression(
                    new CastExpression(fieldHelper,weaved),
                    TraitASTTransformation.helperGetterName(((FieldExpression) exp).getField()),
                    ArgumentListExpression.EMPTY_ARGUMENTS
            );
            mce.setSourcePosition(exp);
            mce.setImplicitThis(false);
            return mce;
        } else if (exp instanceof VariableExpression) {
            VariableExpression vexp = (VariableExpression) exp;
            if (vexp.getAccessedVariable() instanceof FieldNode) {
                MethodCallExpression mce = new MethodCallExpression(
                        new CastExpression(fieldHelper,weaved),
                        TraitASTTransformation.helperGetterName((FieldNode) vexp.getAccessedVariable()),
                        ArgumentListExpression.EMPTY_ARGUMENTS
                );
                mce.setSourcePosition(exp);
                mce.setImplicitThis(false);
                return mce;
            }
            if (vexp.isThisExpression()) {
                VariableExpression res = new VariableExpression(weaved);
                res.setSourcePosition(exp);
                return res;
            }
            if (vexp.isSuperExpression()) {
                ExceptionUtils.sneakyThrow(
                        new SyntaxException("Call to super is not allowed in a trait", vexp.getLineNumber(), vexp.getColumnNumber()));
            }
        } else if (exp instanceof PropertyExpression) {
            if (((PropertyExpression) exp).isImplicitThis() || "this".equals(((PropertyExpression) exp).getObjectExpression().getText())) {
                MethodCallExpression mce = new MethodCallExpression(
                        new CastExpression(fieldHelper,weaved),
                        ((PropertyExpression) exp).getPropertyAsString() + TraitASTTransformation.DIRECT_GETTER_SUFFIX,
                        ArgumentListExpression.EMPTY_ARGUMENTS
                );
                mce.setSourcePosition(exp);
                mce.setImplicitThis(false);
                return mce;
            }
        } else if (exp instanceof ClosureExpression) {
            MethodCallExpression mce = new MethodCallExpression(
                    exp,
                    "rehydrate",
                    new ArgumentListExpression(
                            new VariableExpression(weaved),
                            new VariableExpression(weaved),
                            new VariableExpression(weaved)
                    )
            );
            mce.setImplicitThis(false);
            mce.setSourcePosition(exp);
            return mce;
        }

        // todo: unary expressions (field++, field+=, ...)
        return super.transform(exp);
    }
}
