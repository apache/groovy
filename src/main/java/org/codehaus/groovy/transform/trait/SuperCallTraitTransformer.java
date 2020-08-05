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
package org.codehaus.groovy.transform.trait;

import groovy.lang.MetaProperty;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * This transformer is used to transform calls to <code>SomeTrait.super.foo()</code> into the appropriate trait call.
 *
 * @since 2.3.0
 */
class SuperCallTraitTransformer extends ClassCodeExpressionTransformer {

    static final String UNRESOLVED_HELPER_CLASS = "UNRESOLVED_HELPER_CLASS";

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
        if (exp instanceof BinaryExpression) {
            return transformBinaryExpression((BinaryExpression) exp);
        }
        if (exp instanceof MethodCallExpression) {
            return transformMethodCallExpression((MethodCallExpression) exp);
        }
        return super.transform(exp);
    }

    private Expression transformBinaryExpression(final BinaryExpression exp) {
        Expression trn = super.transform(exp);
        if (trn instanceof BinaryExpression) {
            BinaryExpression bin = (BinaryExpression) trn;
            if (bin.getOperation().getType() == Types.ASSIGN && bin.getLeftExpression() instanceof PropertyExpression) {
                PropertyExpression leftExpression = (PropertyExpression) bin.getLeftExpression();
                ClassNode traitType = getTraitSuperTarget(leftExpression.getObjectExpression());
                if (traitType != null) {
                    // TraitType.super.foo = ... -> TraitType$Helper.setFoo(this, ...)

                    ClassNode helperType = getHelper(traitType);
                    String setterName = MetaProperty.getSetterName(leftExpression.getPropertyAsString());
                    for (MethodNode method : helperType.getMethods(setterName)) {
                        Parameter[] parameters = method.getParameters();
                        if (parameters.length == 2 && parameters[0].getType().equals(traitType)) {
                            MethodCallExpression setterCall = new MethodCallExpression(
                                    new ClassExpression(helperType),
                                    setterName,
                                    new ArgumentListExpression(
                                            new VariableExpression("this"),
                                            bin.getRightExpression()
                                    )
                            );
                            setterCall.getMethod().setSourcePosition(leftExpression.getProperty());
                            setterCall.getObjectExpression().setSourcePosition(traitType);
                            setterCall.setSpreadSafe(leftExpression.isSpreadSafe());
                            setterCall.setMethodTarget(method);
                            setterCall.setImplicitThis(false);
                            return setterCall;
                        }
                    }
                }
            }
        }
        return trn;
    }

    private Expression transformMethodCallExpression(final MethodCallExpression exp) {
        ClassNode traitType = getTraitSuperTarget(exp.getObjectExpression());
        if (traitType != null) {
            // TraitType.super.foo() -> TraitType$Helper.foo(this)

            ArgumentListExpression newArgs = new ArgumentListExpression();
            newArgs.addExpression(new VariableExpression("this"));
            Expression arguments = exp.getArguments();
            if (arguments instanceof TupleExpression) {
                for (Expression expression : (TupleExpression) arguments) {
                    newArgs.addExpression(transform(expression));
                }
            } else {
                newArgs.addExpression(transform(arguments));
            }

            MethodCallExpression newCall = new MethodCallExpression(
                    new ClassExpression(getHelper(traitType)),
                    transform(exp.getMethod()),
                    newArgs
            );
            newCall.getObjectExpression().setSourcePosition(traitType);
            newCall.setSpreadSafe(exp.isSpreadSafe());
            newCall.setImplicitThis(false);
            return newCall;
        }
        return super.transform(exp);
    }

    private ClassNode getHelper(final ClassNode traitReceiver) {
        if (helperClassNotCreatedYet(traitReceiver)) {
            // GROOVY-7909: A Helper class in same compilation unit may have not been created when referenced
            // Here create a symbol as a "placeholder" and it will be resolved later.
            ClassNode ret = new InnerClassNode(
                    traitReceiver,
                    Traits.helperClassName(traitReceiver),
                    ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_SYNTHETIC,
                    ClassHelper.OBJECT_TYPE,
                    ClassNode.EMPTY_ARRAY,
                    null
            ).getPlainNodeReference();

            ret.setRedirect(null);
            traitReceiver.redirect().setNodeMetaData(UNRESOLVED_HELPER_CLASS, ret);
            return ret;
        } else {
            TraitHelpersTuple helpers = Traits.findHelpers(traitReceiver);
            return helpers.getHelper();
        }
    }

    private boolean helperClassNotCreatedYet(final ClassNode traitReceiver) {
        return !traitReceiver.redirect().getInnerClasses().hasNext()
                && getSourceUnit().getAST().getClasses().contains(traitReceiver.redirect());
    }

    private ClassNode getTraitSuperTarget(final Expression exp) {
        if (exp instanceof PropertyExpression) {
            PropertyExpression pexp = (PropertyExpression) exp;
            Expression objExp = pexp.getObjectExpression();
            if (objExp instanceof ClassExpression) {
                ClassNode type = objExp.getType();
                if (Traits.isTrait(type) && "super".equals(pexp.getPropertyAsString())) {
                    return type;
                }
            }
        }
        return null;
    }
}
