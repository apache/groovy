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
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.tools.ClosureUtils;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;

import java.util.List;
import java.util.function.Function;

import static org.codehaus.groovy.ast.tools.GeneralUtils.thisPropX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
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
        if (exp instanceof ClosureExpression) {
            return transformClosureExpression((ClosureExpression) exp);
        }
        if (exp instanceof PropertyExpression) {
            return transformPropertyExpression((PropertyExpression) exp);
        }
        if (exp instanceof MethodCallExpression) {
            return transformMethodCallExpression((MethodCallExpression) exp);
        }
        return super.transform(exp);
    }

    private Expression transformBinaryExpression(final BinaryExpression exp) {
        if (Types.isAssignment(exp.getOperation().getType()))
            // prevent transform of assignment target to accessor method call
            exp.getLeftExpression().putNodeMetaData("assign.target", exp.getOperation());

        Expression trn = super.transform(exp);
        if (trn instanceof BinaryExpression) {
            BinaryExpression bin = (BinaryExpression) trn;
            if (bin.getOperation().getType() == Types.ASSIGN && bin.getLeftExpression() instanceof PropertyExpression) {
                PropertyExpression leftExpression = (PropertyExpression) bin.getLeftExpression();
                ClassNode traitType = getTraitSuperTarget(leftExpression.getObjectExpression());
                if (traitType != null) {
                    ClassNode helperType = getHelper(traitType);
                    // TraitType.super.foo = ... -> TraitType$Trait$Helper.setFoo(this, ...)

                    String setterName = MetaProperty.getSetterName(leftExpression.getPropertyAsString());
                    for (MethodNode method : helperType.getMethods(setterName)) {
                        Parameter[] parameters = method.getParameters();
                        if (parameters.length == 2 && isSelfType(parameters[0], traitType)) {
                            MethodCallExpression setterCall = new MethodCallExpression(
                                    new ClassExpression(helperType),
                                    setterName,
                                    new ArgumentListExpression(
                                            parameters[0].getType().equals(ClassHelper.CLASS_Type)
                                                ? thisPropX(false, "class") : varX("this"),
                                            bin.getRightExpression()
                                    )
                            );
                            setterCall.getObjectExpression().setSourcePosition(leftExpression.getObjectExpression());
                            setterCall.getMethod().setSourcePosition(leftExpression.getProperty());
                            setterCall.setSpreadSafe(leftExpression.isSpreadSafe());
                            setterCall.setImplicitThis(false);
                            return setterCall;
                        }
                    }
                }
            }
        }
        return trn;
    }

    private Expression transformClosureExpression(final ClosureExpression exp) {
        for (Parameter prm : ClosureUtils.getParametersSafe(exp)) {
            Expression ini = transform(prm.getInitialExpression());
            prm.setInitialExpression(ini);
        }
        visitClassCodeContainer(exp.getCode());
        return super.transform(exp);
    }

    private Expression transformPropertyExpression(final PropertyExpression exp) {
        if (exp.getNodeMetaData("assign.target") == null) {
            ClassNode traitType = getTraitSuperTarget(exp.getObjectExpression());
            if (traitType != null) {
                ClassNode helperType = getHelper(traitType);
                // TraitType.super.foo -> TraitType$Trait$Helper.getFoo(this)

                Function<MethodNode, MethodCallExpression> xform = (methodNode) -> {
                    MethodCallExpression methodCall = new MethodCallExpression(
                            new ClassExpression(helperType),
                            methodNode.getName(),
                            new ArgumentListExpression(
                                    methodNode.getParameters()[0].getType().equals(ClassHelper.CLASS_Type)
                                        ? thisPropX(false, "class") : varX("this")
                            )
                    );
                    methodCall.getObjectExpression().setSourcePosition(((PropertyExpression) exp.getObjectExpression()).getObjectExpression());
                    methodCall.getMethod().setSourcePosition(exp.getProperty());
                    methodCall.setSpreadSafe(exp.isSpreadSafe());
                    methodCall.setMethodTarget(methodNode);
                    methodCall.setImplicitThis(false);
                    return methodCall;
                };

                String getterName = MetaProperty.getGetterName(exp.getPropertyAsString(), null);
                for (MethodNode method : helperType.getMethods(getterName)) {
                    if (method.isStatic() && method.getParameters().length == 1
                            && isSelfType(method.getParameters()[0], traitType)
                            && !method.getReturnType().equals(ClassHelper.VOID_TYPE)) {
                        return xform.apply(method);
                    }
                }

                String isserName = "is" + getterName.substring(3);
                for (MethodNode method : helperType.getMethods(isserName)) {
                    if (method.isStatic() && method.getParameters().length == 1
                            && isSelfType(method.getParameters()[0], traitType)
                            && method.getReturnType().equals(ClassHelper.boolean_TYPE)) {
                        return xform.apply(method);
                    }
                }
            }
        }
        exp.removeNodeMetaData("assign.target");
        return super.transform(exp);
    }

    private Expression transformMethodCallExpression(final MethodCallExpression exp) {
        ClassNode traitType = getTraitSuperTarget(exp.getObjectExpression());
        if (traitType != null) {
            ClassNode helperType = getHelper(traitType);
            // TraitType.super.foo() -> TraitType$Trait$Helper.foo(this)

            List<MethodNode> targets = helperType.getMethods(exp.getMethodAsString());
            boolean isStatic = !targets.isEmpty() && targets.stream().map(MethodNode::getParameters)
                .allMatch(params -> params.length > 0 && params[0].getType().equals(ClassHelper.CLASS_Type));

            ArgumentListExpression newArgs = new ArgumentListExpression(
                    isStatic ? thisPropX(false, "class") : varX("this"));
            Expression arguments = exp.getArguments();
            if (arguments instanceof TupleExpression) {
                for (Expression expression : (TupleExpression) arguments) {
                    newArgs.addExpression(transform(expression));
                }
            } else {
                newArgs.addExpression(transform(arguments));
            }

            MethodCallExpression newCall = new MethodCallExpression(
                    new ClassExpression(helperType),
                    transform(exp.getMethod()),
                    newArgs
            );
            newCall.getObjectExpression().setSourcePosition(((PropertyExpression) exp.getObjectExpression()).getObjectExpression());
            newCall.setSpreadSafe(exp.isSpreadSafe());
            newCall.setImplicitThis(false);
            return newCall;
        }
        return super.transform(exp);
    }

    private ClassNode getHelper(final ClassNode traitType) {
        // GROOVY-7909: A helper class in the same compilation unit may not have
        // been created when referenced; create a placeholder to be resolved later.
        if (!traitType.redirect().getInnerClasses().hasNext()
                && getSourceUnit().getAST().getClasses().contains(traitType.redirect())) {
            ClassNode helperType = new InnerClassNode(
                    traitType,
                    Traits.helperClassName(traitType),
                    ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_SYNTHETIC,
                    ClassHelper.OBJECT_TYPE,
                    ClassNode.EMPTY_ARRAY,
                    null
            ).getPlainNodeReference();
            helperType.setRedirect(null);
            traitType.redirect().setNodeMetaData(UNRESOLVED_HELPER_CLASS, helperType);
            return helperType;
        }
        return Traits.findHelper(traitType);
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

    private static boolean isSelfType(final Parameter parameter, final ClassNode traitType) {
        ClassNode paramType = parameter.getType();
        if (paramType.equals(traitType)) return true;
        return paramType.equals(ClassHelper.CLASS_Type)
                && paramType.getGenericsTypes() != null
                && paramType.getGenericsTypes()[0].getType().equals(traitType);
    }
}
