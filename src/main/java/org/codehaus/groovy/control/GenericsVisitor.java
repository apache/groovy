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
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;

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

    public GenericsVisitor(SourceUnit source) {
        this.source = source;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    @Override
    public void visitClass(ClassNode node) {
        boolean error = checkWildcard(node);
        if (error) return;
        boolean isAnon = node instanceof InnerClassNode && ((InnerClassNode) node).isAnonymous();
        checkGenericsUsage(node.getUnresolvedSuperClass(false), node.getSuperClass(), isAnon ? true : null);
        ClassNode[] interfaces = node.getInterfaces();
        for (ClassNode anInterface : interfaces) {
            checkGenericsUsage(anInterface, anInterface.redirect());
        }
        node.visitContents(this);
    }

    @Override
    public void visitField(FieldNode node) {
        ClassNode type = node.getType();
        checkGenericsUsage(type, type.redirect());
        super.visitField(node);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        ClassNode type = call.getType();
        boolean isAnon = type instanceof InnerClassNode && ((InnerClassNode) type).isAnonymous();
        checkGenericsUsage(type, type.redirect(), isAnon);
    }

    @Override
    public void visitMethod(MethodNode node) {
        Parameter[] parameters = node.getParameters();
        for (Parameter param : parameters) {
            ClassNode paramType = param.getType();
            checkGenericsUsage(paramType, paramType.redirect());
        }
        ClassNode returnType = node.getReturnType();
        checkGenericsUsage(returnType, returnType.redirect());
        super.visitMethod(node);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        if (expression.isMultipleAssignmentDeclaration()) {
            TupleExpression tExpr = expression.getTupleExpression();
            for (Expression nextExpr : tExpr.getExpressions()) {
                ClassNode declType = nextExpr.getType();
                checkGenericsUsage(declType, declType.redirect());
            }
        } else {
            ClassNode declType = expression.getVariableExpression().getType();
            checkGenericsUsage(declType, declType.redirect());
        }
        super.visitDeclarationExpression(expression);
    }

    private boolean checkWildcard(ClassNode cn) {
        ClassNode sn = cn.getUnresolvedSuperClass(false);
        if (sn == null) return false;
        GenericsType[] generics = sn.getGenericsTypes();
        if (generics == null) return false;
        boolean error = false;
        for (GenericsType generic : generics) {
            if (generic.isWildcard()) {
                addError("A supertype may not specify a wildcard type", sn);
                error = true;
            }
        }
        return error;
    }

    private void checkGenericsUsage(ClassNode n, ClassNode cn) {
        checkGenericsUsage(n, cn, null);
    }

    private void checkGenericsUsage(ClassNode n, ClassNode cn, Boolean isAnonInnerClass) {
        if (n.isGenericsPlaceHolder()) return;
        GenericsType[] nTypes = n.getGenericsTypes();
        GenericsType[] cnTypes = cn.getGenericsTypes();
        // raw type usage is always allowed
        if (nTypes == null) return;
        // you can't parameterize a non-generified type
        if (cnTypes == null) {
            String message = "The class " + getPrintName(n) + " (supplied with " + plural("type parameter", nTypes.length) +
                    ") refers to the class " + getPrintName(cn) + " which takes no parameters";
            if (nTypes.length == 0) {
                message += " (invalid Diamond <> usage?)";
            }
            addError(message, n);
            return;
        }
        // parameterize a type by using all of the parameters only
        if (nTypes.length != cnTypes.length) {
            if (Boolean.FALSE.equals(isAnonInnerClass) && nTypes.length == 0) {
                return; // allow Diamond for non-AIC cases from CCE
            }
            String message;
            if (Boolean.TRUE.equals(isAnonInnerClass) && nTypes.length == 0) {
                message = "Cannot use diamond <> with anonymous inner classes";
            } else {
                message = "The class " + getPrintName(n) + " (supplied with " + plural("type parameter", nTypes.length) +
                        ") refers to the class " + getPrintName(cn) +
                        " which takes " + plural("parameter", cnTypes.length);
                if (nTypes.length == 0) {
                    message += " (invalid Diamond <> usage?)";
                }
            }
            addError(message, n);
            return;
        }
        for (int i = 0; i < nTypes.length; i++) {
            ClassNode nType = nTypes[i].getType();
            ClassNode cnType = cnTypes[i].getType();
            // check nested type parameters
            checkGenericsUsage(nType, nType.redirect());
            // check bounds
            if (!nType.isDerivedFrom(cnType)) {
                if (cnType.isInterface() && nType.implementsInterface(cnType)) continue;
                addError("The type " + nTypes[i].getName() +
                        " is not a valid substitute for the bounded parameter <" +
                        getPrintName(cnTypes[i]) + ">", n);
            }
        }
    }

    private String plural(String orig, int count) {
        return "" + count + " " + (count == 1 ? orig : orig + "s");
    }

    private static String getPrintName(GenericsType gt) {
        StringBuilder ret = new StringBuilder(gt.getName());
        ClassNode[] upperBounds = gt.getUpperBounds();
        ClassNode lowerBound = gt.getLowerBound();
        if (upperBounds != null) {
            if (upperBounds.length != 1 || !"java.lang.Object".equals(getPrintName(upperBounds[0]))) {
                ret.append(" extends ");
                for (int i = 0; i < upperBounds.length; i++) {
                    ret.append(getPrintName(upperBounds[i]));
                    if (i + 1 < upperBounds.length) ret.append(" & ");
                }
            }
        } else if (lowerBound != null) {
            ret.append(" super ").append(getPrintName(lowerBound));
        }
        return ret.toString();
    }

    private static String getPrintName(ClassNode cn) {
        StringBuilder ret = new StringBuilder(cn.getName());
        GenericsType[] gts = cn.getGenericsTypes();
        if (gts != null) {
            ret.append("<");
            for (int i = 0; i < gts.length; i++) {
                if (i != 0) ret.append(",");
                ret.append(getPrintName(gts[i]));
            }
            ret.append(">");
        }
        return ret.toString();
    }
}
