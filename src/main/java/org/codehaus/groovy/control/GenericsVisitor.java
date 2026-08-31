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

import org.codehaus.groovy.ast.ASTNode;
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
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.trait.Traits;

import java.util.HashSet;
import java.util.Set;

import static org.apache.groovy.ast.tools.TypeUseUtils.describeTypeUse;
import static org.apache.groovy.ast.tools.TypeUseUtils.hasWildcardTypeArgument;
import static org.apache.groovy.ast.tools.TypeUseUtils.isParameterizedEnclosingType;
import static org.apache.groovy.ast.tools.TypeUseUtils.isParameterizedTypeUsage;
import static org.apache.groovy.ast.tools.TypeUseUtils.isParameterizedTypeUsageIncludingEnclosing;
import static org.apache.groovy.ast.tools.TypeUseUtils.isRawGenericType;
import static org.apache.groovy.ast.tools.TypeUseUtils.isReifiable;
import static org.apache.groovy.ast.tools.TypeUseUtils.isStaticMemberType;
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
        checkGenericsUsage(sc, sc.redirect(), isAIC);
        for (ClassNode face : node.getInterfaces()) {
            checkWildcard(face); // JLS 8.1.5: superinterfaces may not specify a wildcard
            checkGenericsUsage(face, face.redirect(), isAIC);
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
     * JLS 14.20: a catch type must be a reifiable class or union; a type
     * parameter is not reifiable and may not be caught.
     */
    @Override
    public void visitCatchStatement(final CatchStatement statement) {
        ClassNode type = statement.getExceptionType();
        if (type != null && type.isGenericsPlaceHolder()) {
            addError("Cannot catch type parameter " + type.getUnresolvedName(), statement);
        }
        super.visitCatchStatement(statement);
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
        ClassNode[] exceptions = node.getExceptions();
        if (exceptions != null) {
            for (ClassNode exception : exceptions) {
                checkThrowsType(exception, node);
            }
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
        checkGenericsUsage(type, type.redirect(), true);
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
            addError("generic array creation of " + describeTypeUse(elementType), expression);
        }
        checkGenericsUsage(expression.getType());

        super.visitArrayExpression(expression);
    }

    /**
     * JLS 15.8.2: a class literal may not name a type variable or a
     * parameterized type ({@code T.class}, {@code Cell<String>.class}).
     * After resolve, {@code Foo.class} is a {@link ClassExpression};
     * {@code Foo<String>.class} may still be a {@link PropertyExpression}
     * whose object is that class expression — see
     * {@link #visitPropertyExpression}. {@link ClassExpression} is also
     * the TypeName of a static qualifier, {@code instanceof} operand, or
     * method reference; those parents visit the type without entering
     * this method, so a class-literal check here is only for an actual
     * class literal.
     */
    @Override
    public void visitClassExpression(final ClassExpression expression) {
        checkClassLiteral(expression.getType(), expression);
        super.visitClassExpression(expression);
    }

    /**
     * Groovy represents the type operand of {@code instanceof} as a
     * {@link ClassExpression}, but it is not a class literal ({@code T.class}).
     * Do not visit that operand as a class expression (that would reject
     * {@code instanceof T} as {@code T.class}). Still run
     * {@link #checkGenericsUsage(ClassNode)} so nested-type well-formedness
     * applies ({@code Outer<?>.Inner} with a generic {@code Inner},
     * {@code Outer.Inner<?>}). Parameterized {@code instanceof} is handled at
     * parse ({@code AstBuilder#rejectNonReifiableInstanceof}): the grammar
     * allows type arguments so {@code instanceof List<?>} can parse, while
     * {@code instanceof Map<String,Integer>} is rejected (JLS 15.20.2).
     * {@link InstanceOfVerifier} then checks primitives and type-parameter targets.
     */
    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        if (expression.getOperation().isA(Types.INSTANCEOF_OPERATOR)
                && expression.getRightExpression() instanceof ClassExpression) {
            expression.getLeftExpression().visit(this);
            checkGenericsUsage(expression.getRightExpression().getType());
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
        Expression object = call.getObjectExpression();
        checkStaticMemberViaParameterizedType(object, call);
        if (object instanceof ClassExpression) {
            // TypeName of a static qualifier, not a class literal.
            call.getMethod().visit(this);
            call.getArguments().visit(this);
        } else {
            super.visitMethodCallExpression(call);
        }
    }

    /**
     * JLS 4.5.2 also applies to static fields accessed as {@code Cell<String>.ID}.
     * JLS 15.8.2: {@code Cell<String>.class} is a class literal, not a static
     * member selection — do not use the 4.5.2 diagnostic for {@code .class}.
     */
    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        Expression object = expression.getObjectExpression();
        // Only TypeName.class is a class literal (JLS 15.8.2).
        // value.class / value*.class is getClass() and must stay legal.
        if (isClassLiteralProperty(expression) && object instanceof ClassExpression) {
            checkClassLiteral(object.getType(), expression);
            expression.getProperty().visit(this);
            return;
        }
        if (object instanceof ClassExpression) {
            // TypeName of a static qualifier, not a class literal.
            checkStaticMemberViaParameterizedType(object, expression);
            expression.getProperty().visit(this);
            return;
        }
        super.visitPropertyExpression(expression);
    }

    /**
     * JLS 15.13: {@code TypeName::m} and {@code TypeName::new} are method or
     * constructor references, not class literals. A parameterization of
     * {@code TypeName} is legal for an instance method or constructor
     * reference ({@code Iterable<String>::asCollection},
     * {@code HashMap<String,Integer>::new}).
     */
    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        Expression object = expression.getExpression();
        if (object instanceof ClassExpression) {
            // TypeName of a method or constructor reference, not a class literal.
            checkGenericsUsage(object.getType());
            expression.getMethodName().visit(this);
        } else {
            super.visitMethodPointerExpression(expression);
        }
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

    /**
     * JLS 15.8.2: {@code TypeName} in a class literal may not denote a type
     * variable or a parameterized type (including a rare type whose enclosing
     * type is parameterized). Arrays of those types are likewise illegal.
     */
    private void checkClassLiteral(final ClassNode type, final Expression location) {
        ClassNode element = type;
        while (element.isArray()) {
            element = element.getComponentType();
        }
        if (element.isGenericsPlaceHolder()) {
            addError("Cannot select from a type parameter " + element.getUnresolvedName(), location);
            return;
        }
        if (isParameterizedTypeUsageIncludingEnclosing(element)) {
            addError("Cannot select from a parameterized type", location);
        }
    }

    private static boolean isClassLiteralProperty(final PropertyExpression expression) {
        return "class".equals(expression.getPropertyAsString());
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
        checkGenericsUsage(cn, cn.redirect(), false);
    }

    private void checkGenericsUsage(final ClassNode cn, final ClassNode rn, final boolean allowDiamond) {
        if (cn.isGenericsPlaceHolder()) return;
        ClassNode oc = cn.getOuterClassType(); // GROOVY-12319: Outer<T>.Inner
        if (oc != null) {
            checkGenericsUsage(oc);
            // JLS 4.5 / 4.8 / 6.5.5: a parameterized enclosing type may only
            // select a non-static member type, and a generic member must be
            // parameterized. Type arguments on a non-static member of a raw
            // enclosing type ({@code Outer.Inner<?>}) are also illegal.
            if (isParameterizedEnclosingType(oc)) {
                if (isStaticMemberType(cn)) {
                    addError("Cannot select a static nested type from a parameterized type", cn);
                } else if (cn.getGenericsTypes() == null && rn.getGenericsTypes() != null) {
                    addError("A raw member type may not be used with a parameterized enclosing type: " + describeTypeUse(cn), cn);
                }
            } else if (cn.getGenericsTypes() != null && isRawGenericType(oc) && !isStaticMemberType(cn)) {
                addError("Type arguments cannot be given on a raw nested type: " + describeTypeUse(cn), cn);
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
            if (allowDiamond && cnTypes.length == 0) {
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
                String argument = cnTypes[i].isWildcard() ? cnTypes[i].toString() : cnTypes[i].getName();
                addError("The type " + argument + " is not a valid substitute for the bounded parameter <" + rnTypes[i] + ">", cnTypes[i]);
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
     * of a bound; additional bounds must be interfaces. Type-parameter names
     * in one section must be distinct. A primitive type is not a legal bound.
     */
    private void checkTypeParameterBounds(final GenericsType[] typeParameters) {
        if (typeParameters == null) return;
        Set<String> names = new HashSet<>();
        for (GenericsType tp : typeParameters) {
            if (!names.add(tp.getName())) {
                addError("Duplicate type parameter " + tp.getName(), tp);
            }
            ClassNode[] bounds = tp.getUpperBounds();
            if (bounds == null) continue;
            for (int i = 0; i < bounds.length; i++) {
                ClassNode bound = bounds[i];
                if (bound == null) continue;
                if (isPrimitiveType(bound)) {
                    addError("The bound of type parameter " + tp.getName() + " must be a class type, not the primitive type " + bound.getName(), tp);
                    continue;
                }
                if (i > 0 && !bound.isInterface() && !Traits.isTrait(bound)) {
                    addError("Additional bounds of a type parameter must be interfaces", tp);
                }
            }
        }
    }

    /**
     * JLS 8.4.6: a type variable may appear in {@code throws} only when its
     * erasure is a subtype of {@code Throwable}. An unbounded {@code T}
     * erases to {@code Object}.
     */
    private void checkThrowsType(final ClassNode type, final ASTNode location) {
        if (type == null || !type.isGenericsPlaceHolder()) return;
        ClassNode erasure = type.redirect();
        if (erasure != null && !isObjectType(erasure)
                && (erasure.isDerivedFrom(ClassHelper.THROWABLE_TYPE)
                    || erasure.implementsInterface(ClassHelper.THROWABLE_TYPE))) {
            return;
        }
        addError("The type parameter " + type.getUnresolvedName() + " is not a valid type for a throws clause", location);
    }

    private static String plural(final String string, final int count) {
        return count + " " + (count == 1 ? string : string + "s");
    }
}
