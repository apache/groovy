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

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.transform.trait.Traits;

import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isUnboundedWildcard;

/**
 * Verify correct usage of generics.
 * This includes:
 * <ul>
 * <li>class header (class and superclass declaration)</li>
 * <li>arity of type parameters for fields, parameters, local variables</li>
 * <li>invalid diamond {@code <>} usage</li>
 * </ul>
 */
public class GenericsVisitor extends ClassCodeVisitorSupport {

    private final SourceUnit source;

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    public GenericsVisitor(final SourceUnit source) {
        this.source = source;
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitClass(final ClassNode node) {
        ClassNode sc = node.getUnresolvedSuperClass(false);
        if (checkWildcard(sc)) return;

        boolean isAIC = node instanceof InnerClassNode && ((InnerClassNode) node).isAnonymous();
        checkGenericsUsage(sc, sc.redirect(), isAIC ? Boolean.TRUE : null);
        for (ClassNode face : node.getInterfaces()) {
            checkGenericsUsage(face);
        }

        visitObjectInitializerStatements(node);
        node.visitContents(this);
    }

    @Override
    public void visitField(final FieldNode node) {
        checkGenericsUsage(node.getType());

        super.visitField(node);
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        for (Parameter p : node.getParameters()) {
            checkGenericsUsage(p.getType());
        }
        if (!isConstructor) {
            checkGenericsUsage(node.getReturnType());
        }

        super.visitConstructorOrMethod(node, isConstructor);
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression expression) {
        ClassNode type = expression.getType();
        boolean isAIC = type instanceof InnerClassNode
                && ((InnerClassNode) type).isAnonymous();
        checkGenericsUsage(type, type.redirect(), isAIC);

        super.visitConstructorCallExpression(expression);
    }

    @Override
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        if (expression.isMultipleAssignmentDeclaration()) {
            for (Expression e : expression.getTupleExpression().getExpressions()) {
                checkGenericsUsage(((VariableExpression) e).getOriginType());
            }
        } else {
            checkGenericsUsage(expression.getVariableExpression().getOriginType());
        }

        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        checkGenericsUsage(expression.getType());

        super.visitArrayExpression(expression);
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        checkGenericsUsage(expression.getType());

        super.visitCastExpression(expression);
    }

    //--------------------------------------------------------------------------

    private boolean checkWildcard(final ClassNode sn) {
        boolean wildcard = false;
        if (sn.getGenericsTypes() != null) {
            for (GenericsType gt : sn.getGenericsTypes()) {
                if (gt.isWildcard()) {
                    addError("A supertype may not specify a wildcard type", sn);
                    wildcard = true;
                }
            }
        }
        return wildcard;
    }

    private void checkGenericsUsage(ClassNode cn) {
        while (cn.isArray())
            cn = cn.getComponentType();
        checkGenericsUsage(cn, cn.redirect(), null);
    }

    private void checkGenericsUsage(final ClassNode cn, final ClassNode rn, final Boolean isAIC) {
        if (cn.isGenericsPlaceHolder()) return;
        GenericsType[] cnTypes = cn.getGenericsTypes();
        // raw type usage is always allowed
        if (cnTypes == null) return;
        GenericsType[] rnTypes = rn.getGenericsTypes();
        // you can't parameterize a non-generified type
        if (rnTypes == null) {
            String message = "The class " + cn.toString(false) + " (supplied with " + plural("type parameter", cnTypes.length) +
                    ") refers to the class " + rn.toString(false) + " which takes no parameters";
            if (cnTypes.length == 0) {
                message += " (invalid Diamond <> usage?)";
            }
            addError(message, cn);
            return;
        }
        // parameterize a type by using all the parameters only
        if (cnTypes.length != rnTypes.length) {
            if (Boolean.FALSE.equals(isAIC) && cnTypes.length == 0) {
                return; // allow Diamond for non-AIC cases from CCE
            }
            String message;
            if (Boolean.TRUE.equals(isAIC) && cnTypes.length == 0) {
                message = "Cannot use diamond <> with anonymous inner classes";
            } else {
                message = "The class " + cn.toString(false) + " (supplied with " + plural("type parameter", cnTypes.length) +
                        ") refers to the class " + rn.toString(false) + " which takes " + plural("parameter", rnTypes.length);
                if (cnTypes.length == 0) {
                    message += " (invalid Diamond <> usage?)";
                }
            }
            addError(message, cn);
            return;
        }
        for (int i = 0; i < cnTypes.length; i++) {
            ClassNode cnType = cnTypes[i].getType();
            ClassNode rnType = rnTypes[i].getType();
            // check nested type parameters
            checkGenericsUsage(cnType);
            // check bounds: unbounded wildcard (aka "?") is universal substitute
            if (!isUnboundedWildcard(cnTypes[i])) {
                // check upper bound(s)
                ClassNode[] bounds = rnTypes[i].getUpperBounds();

                // first can be class or interface
                boolean valid = cnType.isDerivedFrom(rnType) || ((rnType.isInterface() || Traits.isTrait(rnType)) && cnType.implementsInterface(rnType));

                // subsequent bounds if present can be interfaces
                if (valid && bounds != null && bounds.length > 1) {
                    for (int j = 1; j < bounds.length; j++) {
                        ClassNode bound = bounds[j];
                        if (!cnType.implementsInterface(bound)) {
                            valid = false;
                            break;
                        }
                    }
                }

                if (!valid) {
                    addError("The type " + cnTypes[i].getName() + " is not a valid substitute for the bounded parameter <" + rnTypes[i] + ">", cnTypes[i]);
                }
            }
        }
    }

    private static String plural(final String string, final int count) {
        return "" + count + " " + (count == 1 ? string : string + "s");
    }
}
