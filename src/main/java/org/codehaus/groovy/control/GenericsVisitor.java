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
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.trait.Traits;

import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.tools.GenericsUtils.diamondTargetOfAnonymousClass;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isUnboundedWildcard;

/**
 * Verify correct usage of generics.
 * This includes:
 * <ul>
 * <li>class header (class and superclass declaration)</li>
 * <li>arity of type parameters for fields, parameters, local variables</li>
 * <li>invalid diamond {@code <>} usage</li>
 * <li>JLS well-formedness of type arguments, bounds, array creation, and type parameters</li>
 * </ul>
 */
public class GenericsVisitor extends ClassCodeVisitorSupport {

    private final SourceUnit source;

    /**
     * Returns the source unit currently being verified.
     *
     * @return the active source unit
     */
    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    /**
     * Creates a visitor that validates generic type usage for one source unit.
     *
     * @param source the source unit being verified
     */
    public GenericsVisitor(final SourceUnit source) {
        this.source = source;
    }

    //--------------------------------------------------------------------------

    /**
     * Validates generic usage in a class header and its contents.
     *
     * @param node the class to inspect
     */
    @Override
    public void visitClass(final ClassNode node) {
        checkTypeParameterBounds(node.getGenericsTypes());
        if (node.getGenericsTypes() != null && node.getGenericsTypes().length > 0
                && node.isDerivedFrom(ClassHelper.THROWABLE_TYPE)) {
            addError("A generic class may not extend java.lang.Throwable", node);
        }

        ClassNode sc = node.getUnresolvedSuperClass(false);
        if (checkWildcard(sc)) return;

        boolean isAIC = node instanceof InnerClassNode && ((InnerClassNode) node).isAnonymous();
        checkGenericsUsage(sc, sc.redirect(), isAIC ? Boolean.TRUE : null);
        for (ClassNode face : node.getInterfaces()) {
            checkWildcard(face); // JLS 8.1.5: superinterfaces may not specify a wildcard
            checkGenericsUsage(face, face.redirect(), isAIC ? Boolean.TRUE : null);
        }

        visitObjectInitializerStatements(node);
        node.visitContents(this);
    }

    /**
     * Validates generic usage for a field declaration.
     *
     * @param node the field to inspect
     */
    @Override
    public void visitField(final FieldNode node) {
        checkGenericsUsage(node.getType());

        super.visitField(node);
    }

    /**
     * Validates generic usage for a constructor or method signature.
     *
     * @param node the executable member to inspect
     * @param isConstructor whether {@code node} is a constructor
     */
    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        checkTypeParameterBounds(node.getGenericsTypes());
        for (Parameter p : node.getParameters()) {
            checkGenericsUsage(p.getType());
        }
        if (!isConstructor) {
            checkGenericsUsage(node.getReturnType());
        }

        super.visitConstructorOrMethod(node, isConstructor);
    }

    /**
     * Validates generic usage on constructor call types.
     *
     * @param expression the constructor call to inspect
     */
    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression expression) {
        ClassNode type = expression.getType();
        if (type.isGenericsPlaceHolder()) {
            addError("Cannot instantiate the type parameter " + type.getUnresolvedName(), expression);
        }
        if (hasWildcardTypeArgument(type)) {
            addError("A constructor call may not specify a wildcard type", expression);
        }
        boolean isAIC = type instanceof InnerClassNode
                && ((InnerClassNode) type).isAnonymous();
        checkGenericsUsage(type, type.redirect(), isAIC);
        if (expression.isUsingGenerics()) {
            ClassNode created = isAIC ? diamondTargetOfAnonymousClass(type) : type;
            if (created != null && created.getGenericsTypes() != null && created.getGenericsTypes().length == 0) {
                addError("Cannot use diamond <> together with constructor type arguments", expression);
            }
        }

        super.visitConstructorCallExpression(expression);
    }

    /**
     * Validates generic usage in declared variable types.
     *
     * @param expression the declaration expression to inspect
     */
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

    /**
     * Validates generic usage for array element types.
     *
     * @param expression the array expression to inspect
     */
    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        ClassNode elementType = expression.getElementType();
        if (!isReifiable(elementType)) {
            addError("generic array creation", expression);
        }
        checkGenericsUsage(expression.getType());

        super.visitArrayExpression(expression);
    }

    /**
     * JLS 15.8.2: a class literal may not name a type variable.
     */
    @Override
    public void visitClassExpression(final ClassExpression expression) {
        ClassNode type = expression.getType();
        if (type.isGenericsPlaceHolder()) {
            addError("Cannot select from a type parameter " + type.getUnresolvedName(), expression);
        }
        super.visitClassExpression(expression);
    }

    /**
     * Groovy represents the type operand of {@code instanceof} as a
     * {@link ClassExpression}, but it is not a class literal. Skip it so
     * {@link InstanceOfVerifier} can diagnose JLS 15.20.2.
     */
    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        if (expression.getOperation().isA(Types.INSTANCEOF_OPERATOR)
                && expression.getRightExpression() instanceof ClassExpression) {
            expression.getLeftExpression().visit(this);
            return;
        }
        super.visitBinaryExpression(expression);
    }

    /**
     * JLS 4.5.2: a static member of a generic type must be referred to by the
     * generic type name, not a parameterization such as {@code Cell<String>.id()}.
     */
    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        checkStaticMemberViaParameterizedType(call.getObjectExpression(), call);
        super.visitMethodCallExpression(call);
    }

    /**
     * JLS 4.5.2 also applies to static fields accessed as {@code Cell<String>.ID}.
     */
    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        checkStaticMemberViaParameterizedType(expression.getObjectExpression(), expression);
        super.visitPropertyExpression(expression);
    }

    /**
     * Validates generic usage for cast target types.
     *
     * @param expression the cast expression to inspect
     */
    @Override
    public void visitCastExpression(final CastExpression expression) {
        checkGenericsUsage(expression.getType());

        super.visitCastExpression(expression);
    }

    //--------------------------------------------------------------------------

    private void checkStaticMemberViaParameterizedType(final Expression object, final Expression location) {
        if (object instanceof ClassExpression && isParameterizedTypeUsage(object.getType())) {
            addError("Cannot refer to a static member of a generic type through a parameterization", location);
        }
    }

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
        ClassNode oc = cn.getOuterClassType(); // GROOVY-12319: Outer<T>.Inner
        if (oc != null) {
            checkGenericsUsage(oc);
            // JLS 4.5 / 6.5.5: a parameterized enclosing type may only select a
            // non-static member type, and a generic member must be parameterized.
            if (isParameterizedEnclosingType(oc)) {
                if (isStaticMemberType(cn)) {
                    addError("Cannot refer to a static member of a generic type through a parameterization", cn);
                } else if (cn.getGenericsTypes() == null && rn.getGenericsTypes() != null) {
                    addError("A raw member type may not be used with a parameterized enclosing type", cn);
                }
            }
        }
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
            if (isAIC != null && cnTypes.length == 0) {
                return; // diamond on constructor calls, including anonymous classes (GROOVY-12319 / JEP 213)
            }
            String message = "The class " + cn.toString(false) + " (supplied with " + plural("type parameter", cnTypes.length) +
                    ") refers to the class " + rn.toString(false) + " which takes " + plural("parameter", rnTypes.length);
            if (cnTypes.length == 0) {
                message += " (invalid Diamond <> usage?)";
            }
            addError(message, cn);
            return;
        }
        for (int i = 0; i < cnTypes.length; i++) {
            ClassNode cnType = cnTypes[i].getType();
            if (isPrimitiveType(cnType)) {
                addError("The type argument " + cnType.getName() + " is of primitive type; a reference type is required", cnTypes[i]);
                continue;
            }
            // check nested type parameters
            checkGenericsUsage(cnType);
            if (!isTypeArgumentWithinBounds(cnTypes[i], rnTypes[i])) {
                addError("The type " + cnTypes[i].getName() + " is not a valid substitute for the bounded parameter <" + rnTypes[i] + ">", cnTypes[i]);
            }
        }
    }

    /**
     * JLS 4.5: a type argument is within bounds of the corresponding type
     * parameter. Unbounded {@code ?} is always within bounds. For
     * {@code ? extends U} against {@code T extends B}, javac accepts the
     * argument when {@code glb(U, B)} is well-formed (not an empty
     * intersection of two classes). For {@code ? super L}, {@code L} must
     * be a subtype of {@code B}.
     */
    private boolean isTypeArgumentWithinBounds(final GenericsType arg, final GenericsType param) {
        if (isUnboundedWildcard(arg)) return true;
        ClassNode[] paramBounds = param.getUpperBounds();
        ClassNode firstBound = (paramBounds != null && paramBounds.length > 0)
                ? paramBounds[0] : param.getType().redirect();

        if (arg.isWildcard()) {
            ClassNode lower = arg.getLowerBound();
            if (lower != null) {
                return isSubtypeOfBound(lower, firstBound);
            }
            ClassNode[] uppers = arg.getUpperBounds();
            if (uppers != null && uppers.length > 0) {
                return !areDisjointClassTypes(uppers[0], firstBound);
            }
            return true;
        }

        ClassNode cnType = arg.getType();
        boolean valid = isSubtypeOfBound(cnType, firstBound);
        if (valid && paramBounds != null && paramBounds.length > 1) {
            for (int j = 1; j < paramBounds.length; j++) {
                ClassNode bound = paramBounds[j];
                if (!cnType.implementsInterface(bound)) {
                    return false;
                }
            }
        }
        return valid;
    }

    private static boolean isSubtypeOfBound(final ClassNode type, final ClassNode bound) {
        if (bound == null || isObjectType(bound)) return true;
        return type.isDerivedFrom(bound)
                || ((bound.isInterface() || Traits.isTrait(bound)) && type.implementsInterface(bound));
    }

    /**
     * Two classes (not interfaces) whose glb would be empty: neither is a
     * subtype of the other. {@code Object} is a universal upper bound.
     */
    private static boolean areDisjointClassTypes(final ClassNode a, final ClassNode b) {
        if (a == null || b == null || isObjectType(a) || isObjectType(b)) return false;
        if (a.isInterface() || b.isInterface() || Traits.isTrait(a) || Traits.isTrait(b)) {
            return false;
        }
        return !a.isDerivedFrom(b) && !b.isDerivedFrom(a);
    }

    /**
     * JLS 4.4: a class type or type variable may appear only as the first type
     * of a bound; additional bounds must be interfaces.
     */
    private void checkTypeParameterBounds(final GenericsType[] typeParameters) {
        if (typeParameters == null) return;
        for (GenericsType tp : typeParameters) {
            ClassNode[] bounds = tp.getUpperBounds();
            if (bounds == null) continue;
            for (int i = 1; i < bounds.length; i++) {
                ClassNode bound = bounds[i];
                if (bound != null && !bound.isInterface() && !Traits.isTrait(bound)) {
                    addError("Additional bounds of a type parameter must be interfaces", tp);
                }
            }
        }
    }

    /**
     * True when {@code type} is a parameterized usage ({@code Cell<String>}), as
     * opposed to the generic type name ({@code Cell}) or the class declaration
     * node (whose {@link ClassNode#getGenericsTypes()} are the formals).
     */
    private static boolean isParameterizedTypeUsage(final ClassNode type) {
        GenericsType[] gt = type.getGenericsTypes();
        return gt != null && gt.length > 0 && type.isRedirectNode() && !type.isGenericsPlaceHolder();
    }

    /**
     * True when {@code type} carries type arguments, including wildcards
     * ({@code Outer<?>}). Raw names ({@code Outer}) are not parameterized.
     */
    private static boolean isParameterizedEnclosingType(final ClassNode type) {
        GenericsType[] gt = type.getGenericsTypes();
        return gt != null && gt.length > 0 && !type.isGenericsPlaceHolder();
    }

    /**
     * Nested interfaces and enums are implicitly static; otherwise the
     * {@code ACC_STATIC} bit on the declaration is authoritative.
     */
    private static boolean isStaticMemberType(final ClassNode type) {
        ClassNode declared = type.redirect();
        return declared.isStatic()
                || declared.isInterface()
                || declared.isEnum();
    }

    private static boolean hasWildcardTypeArgument(final ClassNode type) {
        GenericsType[] gt = type.getGenericsTypes();
        if (gt == null) return false;
        for (GenericsType t : gt) {
            if (t.isWildcard()) return true;
        }
        ClassNode oc = type.getOuterClassType();
        return oc != null && hasWildcardTypeArgument(oc);
    }

    /**
     * JLS 4.7: types available in full at run time. Used for array creation
     * (JLS 15.10.1), which requires a reifiable component type.
     */
    private static boolean isReifiable(ClassNode type) {
        while (type.isArray()) {
            type = type.getComponentType();
        }
        if (type.isGenericsPlaceHolder()) return false;
        if (isPrimitiveType(type)) return true;
        GenericsType[] gt = type.getGenericsTypes();
        if (gt != null) {
            if (gt.length == 0) return false;
            for (GenericsType t : gt) {
                if (!isUnboundedWildcard(t)) return false;
            }
        }
        ClassNode oc = type.getOuterClassType();
        return oc == null || isReifiable(oc);
    }

    private static String plural(final String string, final int count) {
        return count + " " + (count == 1 ? string : string + "s");
    }
}
