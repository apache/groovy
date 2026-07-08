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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.Collection;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.hasAnnotation;
import static org.apache.groovy.ast.tools.ExpressionUtils.isSuperExpression;
import static org.apache.groovy.ast.tools.ExpressionUtils.isThisExpression;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * This expression transformer is used internally by the {@link org.codehaus.groovy.transform.trait.TraitASTTransformation
 * trait} AST transformation to change the receiver of a message on "this" into a static method call on the trait helper
 * class.
 * <p>
 * In a nutshell, code like the following method definition in a trait: <code>void foo() { this.bar() }</code> is
 * transformed into: <code>void foo() { TraitHelper$bar(this) }</code>
 *
 * @since 2.3.0
 */
class TraitReceiverTransformer extends ClassCodeExpressionTransformer {

    private static final ClassNode VIRTUAL_TYPE = ClassHelper.make(groovy.transform.Virtual.class);

    private final VariableExpression weaved;
    private final SourceUnit unit;
    private final ClassNode traitClass;
    private final ClassNode traitHelper;
    private final ClassNode fieldHelper;
    private final Collection<String> knownFields;

    private boolean inClosure;

    /**
     * Creates a transformer that rewrites trait receiver access against the woven receiver.
     *
     * @param thisObject the synthetic receiver expression used for rewritten calls
     * @param unit the source unit that receives transformation errors
     * @param traitClass the trait currently being transformed
     * @param traitHelper the helper class generated for the trait
     * @param fieldHelper the helper class generated for trait field access
     * @param knownFields the trait field names that require remapping
     */
    public TraitReceiverTransformer(final VariableExpression thisObject, final SourceUnit unit, final ClassNode traitClass,
                                    final ClassNode traitHelper, final ClassNode fieldHelper, final Collection<String> knownFields) {
        this.weaved = thisObject;
        this.unit = unit;
        this.traitClass = traitClass;
        this.traitHelper = traitHelper;
        this.fieldHelper = fieldHelper;
        this.knownFields = knownFields;
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
            if (accessedVariable instanceof FieldNode || accessedVariable instanceof PropertyNode) {
                if (knownFields.contains(vexp.getName())) {
                    return transformFieldReference(exp, accessedVariable instanceof FieldNode
                            ? (FieldNode) accessedVariable : ((PropertyNode) accessedVariable).getField(), null);
                } else {
                    PropertyExpression propertyExpression = propX(varX(weaved), vexp.getName());
                    propertyExpression.getProperty().setSourcePosition(exp);
                    return propertyExpression;
                }
            } else if (accessedVariable instanceof DynamicVariable && !inClosure) { // GROOVY-8049, GROOVY-9386
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
                    FieldNode fn = new FieldNode(propName, 0, ClassHelper.OBJECT_TYPE, weaved.getOriginType(), null);
                    return transformFieldReference(exp, fn, null);
                }
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
            boolean oldInClosure = inClosure;
            inClosure = true;
            ((ClosureExpression) exp).getCode().visit(this);
            inClosure = oldInClosure;
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
        if (operation.getType() == Types.ASSIGN) {
            String leftFieldName = null;
            if (leftExpression instanceof VariableExpression && ((VariableExpression) leftExpression).getAccessedVariable() instanceof FieldNode) {
                leftFieldName = ((VariableExpression) leftExpression).getAccessedVariable().getName();
            } else if (leftExpression instanceof FieldExpression) {
                leftFieldName = ((FieldExpression) leftExpression).getFieldName();
            } else if (leftExpression instanceof PropertyExpression
                    && (((PropertyExpression) leftExpression).isImplicitThis() || "this".equals(((PropertyExpression) leftExpression).getObjectExpression().getText()))) {
                leftFieldName = ((PropertyExpression) leftExpression).getPropertyAsString();
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

        Expression leftTransform = transform(leftExpression);
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
            receiver = varX(weaved); // $static$self
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
            mce = callX(receiver, Traits.helperSetterName(fn), transform(value));
        }
        mce.setImplicitThis(false);
        mce.setSourcePosition(exp);

        // GROOVY-7255: static fields are available via the implementing class, which is not checkable
        if (fn.isStatic()) mce.putNodeMetaData(TraitASTTransformation.DO_DYNAMIC, fn.getOriginType());

        return mce;
    }

    private static FieldNode tryGetFieldNode(final ClassNode weavedType, final String fieldName) {
        FieldNode fn = weavedType.getDeclaredField(fieldName);
        if (fn == null && ClassHelper.isClassType(weavedType)) {
            GenericsType[] genericsTypes = weavedType.getGenericsTypes();
            if (genericsTypes != null && genericsTypes.length == 1) {
                // for static properties
                fn = genericsTypes[0].getType().getDeclaredField(fieldName);
            }
        }
        return fn;
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
                if (methodNode.isStatic() && !methodNode.isPrivate() && !inClosure && hasAnnotation(methodNode, VIRTUAL_TYPE)) {
                    // Default dispatch for trait static methods is
                    // declarer-bound; per-implementer override visibility
                    // is opt-in via `@Virtual`. Annotating a public trait
                    // static with @Virtual emits the dynamic-dispatch
                    // path so the implementer's override (if any) is
                    // visible from trait code.

                    // GROOVY-11985: this.m(x) --> ($static$self or (Class)$self.getClass()).m(x)
                    Expression selfClass = ClassHelper.isClassType(weaved.getOriginType()) ? varX(weaved) : castX(ClassHelper.CLASS_Type.getPlainNodeReference(), callX(varX(weaved), "getClass"));
                    newCall = callX(selfClass, method, transform(arguments));
                    newCall.setImplicitThis(false);
                    newCall.putNodeMetaData(TraitASTTransformation.DO_DYNAMIC, methodNode.getReturnType());
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
            // GROOVY-12117: when a co-compiled super trait has not been transformed
            // yet, its helper is still an empty GROOVY-7909 stub, so the lowered
            // static above is not found. The original static is still declared on
            // the trait node at this point, so resolve it there. This keeps the
            // rewrite independent of the order in which sibling traits are
            // transformed (GEP-22 P1' dispatch consistency); the helper resolves
            // identically once every trait is lowered, so this only matters for
            // the not-yet-lowered super trait.
            for (MethodNode methodNode : superTrait.getDeclaredMethods(methodName)) {
                if (methodNode.isPublic() && methodNode.isStatic()) {
                    return methodNode;
                }
            }
        }

        return null;
    }

    private ArgumentListExpression createArgumentList(final Expression self, final Expression arguments) {
        ArgumentListExpression newArgs = new ArgumentListExpression();
        newArgs.addExpression(self);
        if (arguments instanceof TupleExpression) {
            for (Expression argument : (TupleExpression) arguments) {
                newArgs.addExpression(transform(argument));
            }
        } else {
            newArgs.addExpression(transform(arguments));
        }
        return newArgs;
    }
}
