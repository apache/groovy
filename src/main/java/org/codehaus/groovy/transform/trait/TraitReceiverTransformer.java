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

import groovy.transform.Virtual;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenUtil;
import org.codehaus.groovy.syntax.Types;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.hasAnnotation;
import static org.apache.groovy.ast.tools.ExpressionUtils.isSuperExpression;
import static org.apache.groovy.ast.tools.ExpressionUtils.isThisExpression;
import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * This expression transformer is used internally by the {@link TraitASTTransformation
 * trait} AST transformation to change the receiver of a message on "this" into a static method call on the trait helper
 * class.
 * <p>
 * In a nutshell, code like the following method definition in a trait: <code>void foo() { this.bar() }</code> is
 * transformed into: <code>void foo() { TraitHelper$bar(this) }</code>
 *
 * @since 2.3.0
 */
class TraitReceiverTransformer extends ClassCodeExpressionTransformer {

    private static final ClassNode VIRTUAL_TYPE = ClassHelper.make(Virtual.class);
    private static final ClassNode TRAITS_TYPE = ClassHelper.make(Traits.class);

    private final VariableExpression weaved;
    private final SourceUnit unit;
    private final ClassNode traitClass;
    private final ClassNode traitHelper;
    private final ClassNode fieldHelper;
    private final Collection<String> knownFields;
    private final boolean staticallyCompiled;

    private boolean inClosure;
    private boolean inLambdaOnly; // in a lambda that is not in a closure
    private boolean inLambda;
    private Expression assignmentTarget; // the expression being assigned, incremented or decremented, which is not a read

    /**
     * Creates a transformer that rewrites trait receiver access against the woven receiver.
     *
     * @param thisObject the synthetic receiver expression used for rewritten calls
     * @param unit the source unit that receives transformation errors
     * @param traitClass the trait currently being transformed
     * @param traitHelper the helper class generated for the trait
     * @param fieldHelper the helper class generated for trait field access
     * @param knownFields the trait field names that require remapping
     * @param staticallyCompiled whether the code is statically compiled
     */
    public TraitReceiverTransformer(final VariableExpression thisObject, final SourceUnit unit, final ClassNode traitClass,
                                    final ClassNode traitHelper, final ClassNode fieldHelper, final Collection<String> knownFields,
                                    final boolean staticallyCompiled) {
        this.weaved = thisObject;
        this.unit = unit;
        this.traitClass = traitClass;
        this.traitHelper = traitHelper;
        this.fieldHelper = fieldHelper;
        this.knownFields = knownFields;
        this.staticallyCompiled = staticallyCompiled;
    }

    /**
     * Returns the source unit used for diagnostics.
     *
     * @return the current source unit
     */
    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    /**
     * Rewrites trait expressions that reference {@code this}, {@code super}, or trait fields.
     *
     * @param exp the expression to transform
     * @return the transformed expression
     */
    @Override
    public Expression transform(final Expression exp) {
        if (exp instanceof BinaryExpression) {
            return transformBinaryExpression((BinaryExpression) exp);
        } else if (exp instanceof MethodCallExpression mce) {
            // GROOVY-12104: T.this.m() inside trait code (where T is the
            // enclosing trait) compiles successfully today but generates
            // invalid or mis-typed bytecode that fails at runtime — Verify-
            // Error on 4.x ("Class not assignable to Closure"), ClassCast-
            // Exception on 5.x/6.x. T.this has no coherent meaning for
            // traits anyway (per GEP-22 § this, super, and stackable traits
            // item 1, this is the implementing instance, and the trait is
            // not an enclosing scope of its implementer). Reject the
            // qualifier at compile time pointing at the existing supported
            // alternatives.
            if (isTraitThisQualifier(mce.getObjectExpression())) {
                unit.addError(new SyntaxException(
                        "'" + traitClass.getNameWithoutPackage() + ".this' is not allowed inside trait code; use 'this." + mce.getMethodAsString() + "(...)' for normal dispatch, or '" + traitClass.getNameWithoutPackage() + ".super." + mce.getMethodAsString() + "(...)' for explicit trait-anchored dispatch",
                        mce.getLineNumber(), mce.getColumnNumber()));
                return mce;
            }
            // GROOVY-12105: in a static trait method, the parser/resolver
            // rewrites `super.m(...)` to a static call on the trait's
            // declared superclass (typically Object) before we see it. Reject
            // that pattern at compile time, pointing at `T.super.m(...)` as
            // the supported explicit form. The discriminator is
            // `mce.isImplicitThis()`: the rewritten super call carries
            // `isImplicitThis=true` (because the user wrote no explicit
            // receiver — the ClassExpression was synthesised by the
            // resolver), while an explicit `ClassName.m()` call from
            // user source has `isImplicitThis=false`. The synthesised
            // ClassExpression also has no source position (line=-1), an
            // independent signal of the same fact.
            if (ClassHelper.isClassType(weaved.getOriginType())
                && mce.isImplicitThis()
                && mce.getObjectExpression() instanceof ClassExpression ce
                && traitClass.getSuperClass() != null
                && ce.getType().equals(traitClass.getSuperClass())) {
                unit.addError(new SyntaxException(
                    "'super' is not allowed in a static trait method; use '" + traitClass.getNameWithoutPackage() + ".super." + mce.getMethodAsString() + "(...)' for explicit trait-anchored dispatch",
                    mce.getLineNumber(), mce.getColumnNumber()));
                return mce;
            }
            if (isSuperExpression(mce.getObjectExpression())) {
                return transformSuperMethodCall(mce); // super.m(x) --> $self.Ttrait$super$m(x)
            } else if (isThisExpression(mce.getObjectExpression())) {
                return transformMethodCallOnThis(mce); // this.m(x) --> $self.m(x) or this.m($self, x)
            }
        } else if (exp instanceof FieldExpression) {
            return transformFieldReference(exp, ((FieldExpression) exp).getField(), null);
        } else if (exp instanceof VariableExpression vexp) {
            Variable accessedVariable = vexp.getAccessedVariable();
            if (!inClosure && exp != assignmentTarget && isStaticAccessorPropertyName(vexp)) {
                String getter = findStaticAccessor(vexp.getName(), false);
                if (getter != null) { // GROOVY-12614: a static accessor read as a property is a call of the accessor
                    return transformStaticAccessorCall(exp, getter, null);
                }
            }
            if (accessedVariable instanceof FieldNode || accessedVariable instanceof PropertyNode) {
                if (isClosurePropertyReference(vexp)) {
                    return exp; // GROOVY-12598: resolve through the closure's delegate and owner
                }
                if (knownFields.contains(vexp.getName())) {
                    return transformFieldReference(exp, accessedVariable instanceof FieldNode
                            ? (FieldNode) accessedVariable : ((PropertyNode) accessedVariable).getField(), null);
                } else {
                    PropertyExpression propertyExpression = propX(varX(weaved), vexp.getName());
                    propertyExpression.getProperty().setSourcePosition(exp);
                    return propertyExpression;
                }
            } else if (accessedVariable instanceof DynamicVariable && (!inClosure // GROOVY-8049, GROOVY-9386
                    // GROOVY-12608: a statically compiled lambda has no delegate or thisObject to resolve
                    // a static accessor of a super trait through, so it captures $self or $static$self;
                    // otherwise a lambda is a closure, whose delegate may provide the name
                    || (inLambdaOnly && staticallyCompiled && isSuperTraitStaticProperty(vexp.getName()))
                    || isSuperTraitFieldName(vexp.getName()))) { // GROOVY-12609
                PropertyExpression propertyExpression = propX(varX(weaved), vexp.getName());
                propertyExpression.getProperty().setSourcePosition(exp);
                return propertyExpression;
            }
            if (vexp.isThisExpression()) {
                VariableExpression variableExpression = varX(weaved);
                variableExpression.setSourcePosition(exp);
                return variableExpression;
            }
            if (vexp.isSuperExpression()) {
                throwSuperError(vexp);
            }
        } else if (exp instanceof PropertyExpression pexp) {
            String obj = pexp.getObjectExpression().getText();
            // GROOVY-12104: T.this.field qualifier — same rejection rationale
            // as the method-call form above. Reject at compile time.
            if (isTraitThisQualifier(pexp.getObjectExpression())) {
                unit.addError(new SyntaxException(
                        "'" + traitClass.getNameWithoutPackage() + ".this' is not allowed inside trait code; use 'this." + pexp.getPropertyAsString() + "' for the field reference",
                        pexp.getLineNumber(), pexp.getColumnNumber()));
                return pexp;
            }
            if (pexp.isImplicitThis() || "this".equals(obj)) {
                String propName = pexp.getPropertyAsString();
                if (knownFields.contains(propName)) {
                    if (isDeclaredInstancePropertyInClosure(propName)) {
                        // this.name in a closure is read through the accessor, as in a class
                        PropertyExpression propertyExpression = propX(varX(weaved), propName);
                        propertyExpression.setSourcePosition(exp);
                        return propertyExpression;
                    }
                    // GROOVY-12587: use the declared field, so a static one is read through the implementing class
                    return transformFieldReference(exp, traitClass.getDeclaredField(propName), null);
                }
                if (propName != null && !inClosure && exp != assignmentTarget) {
                    String getter = findStaticAccessor(propName, false);
                    if (getter != null) { // GROOVY-12614
                        return transformStaticAccessorCall(exp, getter, null);
                    }
                }
            }
        } else if (exp instanceof PrefixExpression || exp instanceof PostfixExpression) {
            Expression oldTarget = assignmentTarget;
            assignmentTarget = exp instanceof PrefixExpression ? ((PrefixExpression) exp).getExpression() : ((PostfixExpression) exp).getExpression();
            try {
                return super.transform(exp);
            } finally {
                assignmentTarget = oldTarget;
            }
        } else if (exp instanceof ClosureExpression) {
            MethodCallExpression mce = callX(exp, "rehydrate", args(
                    varX(weaved),
                    varX(weaved),
                    varX(weaved)
            ));
            mce.setImplicitThis(false);
            mce.setMethodTarget(ClassHelper.CLOSURE_TYPE.getMethods("rehydrate").get(0));
            mce.setSourcePosition(exp);
            boolean oldInClosure = inClosure, oldInLambdaOnly = inLambdaOnly, oldInLambda = inLambda;
            inLambdaOnly = exp instanceof LambdaExpression && (!inClosure || inLambdaOnly);
            inClosure = true;
            inLambda = oldInLambda || exp instanceof LambdaExpression;
            ((ClosureExpression) exp).getCode().visit(this);
            inClosure = oldInClosure;
            inLambdaOnly = oldInLambdaOnly;
            inLambda = oldInLambda;
            // The rewrite we do is causing some troubles with type checking, which will
            // not be able to perform closure parameter type inference
            // so we store the replacement, which will be done *after* type checking.
            exp.putNodeMetaData(TraitASTTransformation.POST_TYPECHECKING_REPLACEMENT, mce);
            return exp;
        }

        // TODO: unary expressions (field++, field+=, ...)
        return super.transform(exp);
    }

    private Expression transformBinaryExpression(final BinaryExpression exp) {
        Expression leftExpression = exp.getLeftExpression();
        Expression rightExpression = exp.getRightExpression();
        Token operation = exp.getOperation();
        // in a closure or lambda the name is left for the closure to resolve, since its delegate may provide it
        String accessorProperty = inClosure ? null : staticAccessorPropertyName(leftExpression);
        if (accessorProperty != null && Types.isAssignment(operation.getType())) {
            // GROOVY-12614: a static accessor written as a property is a call of the accessor
            String setter = findStaticAccessor(accessorProperty, true);
            if (setter != null) {
                int op = operation.getType(), token = TokenUtil.removeAssignment(op);
                boolean elvis = (op == Types.ELVIS_EQUAL || op == Types.NULL_COALESCE_EQUAL);
                if (op == Types.ASSIGN) {
                    return transformStaticAccessorCall(exp, setter, rightExpression);
                }
                String getter = findStaticAccessor(accessorProperty, false);
                if (getter != null && (token != op || elvis)) { // p op= v is setP(getP() op v)
                    MethodCallExpression read = callThisX(getter);
                    read.setSourcePosition(leftExpression);
                    Expression value = elvis ? new ElvisOperatorExpression(read, rightExpression, op == Types.NULL_COALESCE_EQUAL)
                            : new BinaryExpression(read, Token.newSymbol(token, -1, -1), rightExpression);
                    value.setSourcePosition(exp);
                    return transformStaticAccessorCall(exp, setter, value);
                }
            }
        }
        if (operation.getType() == Types.ASSIGN) {
            String leftFieldName = null;
            if (leftExpression instanceof VariableExpression && isClosurePropertyReference((VariableExpression) leftExpression)) {
                return binX(leftExpression, operation, transform(rightExpression)); // GROOVY-12598
            }
            if (leftExpression instanceof VariableExpression && ((VariableExpression) leftExpression).getAccessedVariable() instanceof FieldNode) {
                leftFieldName = ((VariableExpression) leftExpression).getAccessedVariable().getName();
            } else if (leftExpression instanceof FieldExpression) {
                leftFieldName = ((FieldExpression) leftExpression).getFieldName();
            } else if (leftExpression instanceof PropertyExpression
                    && (((PropertyExpression) leftExpression).isImplicitThis() || "this".equals(((PropertyExpression) leftExpression).getObjectExpression().getText()))) {
                leftFieldName = ((PropertyExpression) leftExpression).getPropertyAsString();
            }
            if (leftFieldName != null && leftExpression instanceof PropertyExpression && isDeclaredInstancePropertyInClosure(leftFieldName)) {
                return binX(propX(varX(weaved), leftFieldName), operation, transform(rightExpression)); // this.name = value in a closure
            }
            if (leftFieldName != null) {
                ClassNode weavedType = weaved.getOriginType();
                FieldNode traitField = tryGetFieldNode(weavedType, leftFieldName);
                FieldNode dummyField = new FieldNode(leftFieldName, 0, ClassHelper.OBJECT_TYPE, weavedType, null);
                if (fieldHelper == null || traitField == null && !fieldHelper.hasPossibleMethod(Traits.helperSetterName(dummyField), rightExpression)) {
                    return binX(propX(varX(weaved), leftFieldName), operation, transform(rightExpression)); // GROOVY-7342, GROOVY-7456, GROOVY-9739, GROOVY-10143, et al.
                }
                return transformFieldReference(exp, traitField != null ? traitField : dummyField, rightExpression);
            }
        }

        Expression oldTarget = assignmentTarget;
        if (Types.isAssignment(operation.getType())) assignmentTarget = leftExpression;
        Expression leftTransform = transform(leftExpression);
        assignmentTarget = oldTarget;
        Expression rightTransform = transform(rightExpression);
        Expression ret = exp instanceof DeclarationExpression ?
                new DeclarationExpression(leftTransform, operation, rightTransform) : binX(leftTransform, operation, rightTransform);
        ret.setSourcePosition(exp);
        ret.copyNodeMetaData(exp);
        return ret;
    }

    private Expression transformFieldReference(final Expression exp, final FieldNode fn, final Expression value) {
        Expression receiver;
        if (ClassHelper.isClassType(weaved.getOriginType())) {
            // GROOVY-12597: (Class) $static$self, so static compilation does not bind to the trait interface's accessor
            receiver = castX(ClassHelper.CLASS_Type.getPlainNodeReference(), varX(weaved));
        } else if (fn.isStatic()) {
            var call = callX(varX(weaved), "getClass"); // $self.getClass()
            call.setImplicitThis(false);
            call.setMethodTarget(ClassHelper.OBJECT_TYPE.getGetterMethod("getClass", false));
            receiver = call;
        } else {
            receiver = castX(fieldHelper, varX(weaved)); // (<trait class>$Trait$FieldHelper) $self
        }

        MethodCallExpression mce;
        if (value == null) {
            mce = callX(receiver, Traits.helperGetterName(fn));
        } else {
            Expression newValue = transform(value);
            ClassNode type = fn.getOriginType();
            if (!ClassHelper.isDynamicTyped(type) && !type.isGenericsPlaceHolder() && !GenericsUtils.hasUnresolvedGenerics(type)) {
                // GROOVY-12639: convert the value as an assignment to the field would
                CastExpression cast = castX(type, newValue);
                cast.setSourcePosition(value);
                cast.putNodeMetaData(TraitASTTransformation.FIELD_ASSIGNMENT, Boolean.TRUE);
                newValue = cast;
            }
            mce = callX(receiver, Traits.helperSetterName(fn), newValue);
        }
        mce.setImplicitThis(false);
        mce.setSourcePosition(exp);

        // GROOVY-7255: static fields are available via the implementing class, which is not checkable
        if (fn.isStatic()) mce.putNodeMetaData(TraitASTTransformation.DO_DYNAMIC, fn.getOriginType());

        return mce;
    }

    /**
     * Indicates whether the variable is an instance property of the trait (declared
     * or backed by an accessor method) referenced from a closure. Such a reference
     * is left for the closure to resolve, so that its delegate is checked according
     * to the resolve strategy, as it is for a property of a class. The closure owner
     * is the implementing instance, which has the property's accessor methods.
     * A lambda keeps the direct reference: when statically compiled it has no
     * delegate or owner to resolve the name through.
     */
    private boolean isClosurePropertyReference(final VariableExpression vexp) {
        if (!inClosure || inLambda || ClassHelper.isClassType(weaved.getOriginType())) return false;
        Variable accessedVariable = vexp.getAccessedVariable();
        return !accessedVariable.isInStaticContext() && (accessedVariable instanceof PropertyNode
            || (accessedVariable instanceof FieldNode && traitClass.getProperty(vexp.getName()) != null));
    }

    /**
     * Indicates whether {@code this.name} in a closure names a declared instance
     * property of the trait, which is then accessed through its accessor.
     */
    private boolean isDeclaredInstancePropertyInClosure(final String name) {
        if (!inClosure || inLambda || ClassHelper.isClassType(weaved.getOriginType())) return false;
        PropertyNode pn = traitClass.getProperty(name);
        return pn != null && !pn.isStatic();
    }

    /**
     * Indicates whether the variable may name a static accessor of the trait: it is
     * not a local variable, a parameter or a field of the trait.
     */
    private boolean isStaticAccessorPropertyName(final VariableExpression vexp) {
        if (vexp.isThisExpression() || vexp.isSuperExpression() || knownFields.contains(vexp.getName())) return false;
        Variable accessedVariable = vexp.getAccessedVariable();
        return accessedVariable instanceof DynamicVariable || accessedVariable instanceof PropertyNode;
    }

    /**
     * Returns the property name if the expression is {@code name} or {@code this.name}
     * and may name a static accessor of the trait, or {@code null}.
     */
    private String staticAccessorPropertyName(final Expression exp) {
        if (exp instanceof VariableExpression vexp) {
            return isStaticAccessorPropertyName(vexp) ? vexp.getName() : null;
        }
        if (exp instanceof PropertyExpression pexp
                && (pexp.isImplicitThis() || "this".equals(pexp.getObjectExpression().getText()))) {
            String name = pexp.getPropertyAsString();
            return name != null && !knownFields.contains(name) ? name : null;
        }
        return null;
    }

    /**
     * Returns the name of the static getter (or setter) that the trait or one of its
     * super traits declares for the property, or {@code null} if there is none.
     */
    private String findStaticAccessor(final String propertyName, final boolean setter) {
        String suffix = capitalize(propertyName);
        String[] names = setter ? new String[]{"set" + suffix} : new String[]{"get" + suffix, "is" + suffix};
        for (String name : names) {
            for (MethodNode methodNode : traitClass.getDeclaredMethods(name)) {
                if (methodNode.isStatic() && methodNode.getParameters().length == (setter ? 1 : 0)
                        && (setter || !name.startsWith("is") || ClassHelper.isPrimitiveBoolean(methodNode.getReturnType()))) {
                    return name;
                }
            }
        }
        var traits = Traits.findTraits(traitClass); traits.remove(traitClass);
        for (ClassNode superTrait : traits) {
            ClassNode helper = Traits.findHelper(superTrait);
            if (helper == null) continue;
            for (String name : names) {
                for (MethodNode methodNode : helper.getDeclaredMethods(name)) {
                    Parameter[] parameters = methodNode.getParameters();
                    if (methodNode.isPublic() && methodNode.isStatic() && parameters.length == (setter ? 2 : 1)
                            && ClassHelper.isClassType(parameters[0].getType())
                            && (setter || !name.startsWith("is") || ClassHelper.isPrimitiveBoolean(methodNode.getReturnType()))) {
                        return name;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Makes the call of a static accessor that a property reference stands for,
     * which is then dispatched as the method form is.
     */
    private Expression transformStaticAccessorCall(final Expression exp, final String accessor, final Expression value) {
        MethodCallExpression call = value == null ? callThisX(accessor) : callThisX(accessor, args(value));
        call.setSourcePosition(exp);
        return transformMethodCallOnThis(call);
    }

    /**
     * Checks if a name is the remapped name ("pack_Type__name") of
     * a field of a super trait, which refers to the implementing class also in a
     * closure.
     */
    private boolean isSuperTraitFieldName(final String name) {
        if (!name.contains("__")) return false;
        Set<ClassNode> traits = new LinkedHashSet<>();
        collectSuperTraits(traitClass, traits);
        for (ClassNode trait : traits) {
            String prefix = trait.getName().replace('.', '_') + "__";
            if (name.startsWith(prefix)) {
                if (trait.getDeclaredField(name.substring(prefix.length())) != null) {
                    return true; // trait not transformed yet
                }
                TraitHelpersTuple helpers = Traits.findHelpers(trait);
                for (ClassNode fieldHelper : new ClassNode[]{helpers.getFieldHelper(), helpers.getStaticFieldHelper()}) {
                    if (fieldHelper != null && fieldHelper.getDeclaredMethod(name + Traits.DIRECT_GETTER_SUFFIX, Parameter.EMPTY_ARRAY) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void collectSuperTraits(final ClassNode cNode, final Set<ClassNode> traits) {
        for (ClassNode face : cNode.getInterfaces()) {
            if (Traits.isTrait(face) && traits.add(face)) collectSuperTraits(face, traits);
        }
        ClassNode superClass = cNode.getUnresolvedSuperClass();
        if (Traits.isTrait(superClass) && traits.add(superClass)) { // trait not transformed yet
            collectSuperTraits(superClass, traits);
        }
    }

    private static FieldNode tryGetFieldNode(final ClassNode weavedType, final String fieldName) {
        if (ClassHelper.isClassType(weavedType)) {
            // GROOVY-12593: for static properties look in the trait, not in java.lang.Class (name, module, ...)
            GenericsType[] genericsTypes = weavedType.getGenericsTypes();
            if (genericsTypes != null && genericsTypes.length == 1) {
                return genericsTypes[0].getType().getDeclaredField(fieldName);
            }
            return null;
        }
        return weavedType.getDeclaredField(fieldName);
    }

    private void throwSuperError(final ASTNode node) {
        unit.addError(new SyntaxException("Call to super is not allowed in a trait", node.getLineNumber(), node.getColumnNumber()));
    }

    /**
     * Returns {@code true} if the given expression has the shape
     * {@code <traitClass>.this} — i.e. a {@link PropertyExpression} whose
     * object is a {@link ClassExpression} of the enclosing trait class
     * and whose property is the literal "this". Used by GROOVY-12104's
     * compile-time rejection of {@code T.this.*} qualifier syntax in
     * trait code.
     */
    private boolean isTraitThisQualifier(final Expression exp) {
        if (!(exp instanceof PropertyExpression pe)) return false;
        if (!"this".equals(pe.getPropertyAsString())) return false;
        if (!(pe.getObjectExpression() instanceof ClassExpression ce)) return false;
        return ce.getType().equals(traitClass);
    }

    private Expression transformSuperMethodCall(final MethodCallExpression call) {
        String method = call.getMethodAsString();
        if (method == null) {
            throwSuperError(call);
        }

        Expression arguments = transform(call.getArguments());
        ArgumentListExpression superCallArgs = new ArgumentListExpression();
        if (arguments instanceof ArgumentListExpression list) {
            for (Expression expression : list) {
                superCallArgs.addExpression(expression);
            }
        } else {
            superCallArgs.addExpression(arguments);
        }
        MethodCallExpression newCall = callX(
                weaved,
                Traits.getSuperTraitMethodName(traitClass, method),
                superCallArgs
        );
        newCall.setSourcePosition(call);
        newCall.setSafe(call.isSafe());
        newCall.setSpreadSafe(call.isSpreadSafe());
        newCall.setImplicitThis(false);
        return newCall;
    }

    private Expression transformMethodCallOnThis(final MethodCallExpression call) {
        Expression method    = call.getMethod();
        Expression arguments = call.getArguments();
        Expression thisExpr  = call.getObjectExpression();

        if (method instanceof ConstantExpression) {
            // GROOVY-7213, GROOVY-7214, GROOVY-8282, GROOVY-8859, GROOVY-10106, GROOVY-10312
            MethodNode methodNode = findConcreteMethod(traitClass, call.getMethodAsString());
            if (methodNode != null) {
                MethodCallExpression newCall;
                MethodCallExpression helperCall = null;
                if (methodNode.isStatic() && !methodNode.isPrivate() && !inClosure && hasAnnotation(methodNode, VIRTUAL_TYPE)) {
                    // Default dispatch for trait static methods is
                    // declarer-bound; per-implementer override visibility
                    // is opt-in via `@Virtual`. Annotating a public trait
                    // static with @Virtual emits the dynamic-dispatch
                    // path so the implementer's override (if any) is
                    // visible from trait code.

                    // GROOVY-11985: this.m(x) --> ($static$self or (Class)$self.getClass()).m(x)
                    boolean staticSelf = ClassHelper.isClassType(weaved.getOriginType());
                    Expression selfClass = staticSelf ? varX(weaved) : castX(ClassHelper.CLASS_Type.getPlainNodeReference(), callX(varX(weaved), "getClass"));
                    Expression newArgs = transform(arguments);
                    newCall = callX(selfClass, method, newArgs);
                    newCall.setImplicitThis(false);
                    newCall.putNodeMetaData(TraitASTTransformation.DO_DYNAMIC, methodNode.getReturnType());
                    if (staticSelf) {
                        // GROOVY-12604: a promoted static called through the trait (T.m())
                        // has the trait interface as $static$self, which does not carry
                        // @Virtual statics; there is no implementing class to dispatch to
                        ClassNode helper = traitClass.equals(methodNode.getDeclaringClass()) ? traitHelper : methodNode.getDeclaringClass();
                        Expression self = traitClass.equals(methodNode.getDeclaringClass()) ? varX(weaved) : castX(ClassHelper.CLASS_Type.getPlainNodeReference(), varX(weaved)); // Class<U> to Class<T>
                        helperCall = callX(classX(helper), method, createArgumentList(self, newArgs, false));
                        helperCall.setImplicitThis(false);
                        helperCall.setGenericsTypes(call.getGenericsTypes());
                        helperCall.setSpreadSafe(call.isSpreadSafe());
                        helperCall.setSourcePosition(call);
                    }
                } else {
                    // Reached for: plain (non-@Virtual) static, private static,
                    // instance method, or any call inside a closure.

                    // this.m(x) --> (this or T$Trait$Helper).m($self or $static$self or (Class)$self.getClass(), x)
                    Expression selfClassOrObject = methodNode.isStatic() && !ClassHelper.isClassType(weaved.getOriginType()) ? castX(ClassHelper.CLASS_Type.getPlainNodeReference(), callX(weaved, "getClass")) : weaved;
                    newCall = callX(!inClosure ? thisExpr : classX(traitHelper), method, createArgumentList(selfClassOrObject, arguments));
                }
                newCall.setGenericsTypes(call.getGenericsTypes());
                newCall.setSpreadSafe(call.isSpreadSafe());
                newCall.setSourcePosition(call);
                if (helperCall != null) {
                    // (Traits.isTrait($static$self) ? T$Trait$Helper.m($static$self, x) : $static$self.m(x));
                    // a dynamic $static$self.isInterface() would call a static isInterface() of the class
                    Expression dispatch = ternaryX(callX(TRAITS_TYPE, "isTrait", args(varX(weaved))), helperCall, newCall);
                    dispatch.setSourcePosition(call);
                    return dispatch;
                }
                return newCall;
            }
        }

        // this.m(x) --> (this or $self or $static$self).m(x)
        MethodCallExpression newCall = callX(inClosure ? thisExpr : weaved, method, transform(arguments));
        newCall.setGenericsTypes(call.getGenericsTypes()); // GROOVY-11302: this.<T>m(x)
        newCall.setImplicitThis(inClosure ? call.isImplicitThis() : false);
        newCall.setSafe(inClosure ? call.isSafe() : false);
        newCall.setSpreadSafe(call.isSpreadSafe());
        newCall.setSourcePosition(call);
        return newCall;
    }

    private static MethodNode findConcreteMethod(final ClassNode traitClass, final String methodName) {
        for (MethodNode methodNode : traitClass.getDeclaredMethods(methodName)) {
            if (methodNode.isPrivate() || methodNode.isStatic()) {
                return methodNode;
            }
        }

        // GROOVY-8272, GROOVY-10312: public static method from super trait
        var traits = Traits.findTraits(traitClass); traits.remove(traitClass);

        for (ClassNode superTrait : traits) {
            ClassNode traitHelper = Traits.findHelper(superTrait);
            for (MethodNode methodNode : traitHelper.getDeclaredMethods(methodName)) {
                if (methodNode.isPublic() && methodNode.isStatic()
                        // exclude public method with body as it's included in trait interface
                        && ClassHelper.isClassType(methodNode.getParameters()[0].getType())) {
                    return methodNode;
                }
            }
        }

        return null;
    }

    /**
     * Indicates whether a super trait has a public static getter or setter for the
     * named property.
     */
    private boolean isSuperTraitStaticProperty(final String name) {
        String suffix = capitalize(name);
        var traits = Traits.findTraits(traitClass); traits.remove(traitClass);

        for (ClassNode superTrait : traits) {
            for (MethodNode methodNode : Traits.findHelper(superTrait).getMethods()) {
                Parameter[] parameters = methodNode.getParameters();
                if (methodNode.isPublic() && methodNode.isStatic() && parameters.length > 0
                        && ClassHelper.isClassType(parameters[0].getType())) {
                    String methodName = methodNode.getName();
                    if (parameters.length == 1 ? methodName.equals("get" + suffix)
                            || (methodName.equals("is" + suffix) && ClassHelper.isPrimitiveBoolean(methodNode.getReturnType()))
                            : parameters.length == 2 && methodName.equals("set" + suffix)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private ArgumentListExpression createArgumentList(final Expression self, final Expression arguments) {
        return createArgumentList(self, arguments, true);
    }

    private ArgumentListExpression createArgumentList(final Expression self, final Expression arguments, final boolean transform) {
        ArgumentListExpression newArgs = new ArgumentListExpression();
        newArgs.addExpression(self);
        if (arguments instanceof TupleExpression) {
            for (Expression argument : (TupleExpression) arguments) {
                newArgs.addExpression(transform ? transform(argument) : argument);
            }
        } else {
            newArgs.addExpression(transform ? transform(arguments) : arguments);
        }
        return newArgs;
    }
}
