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
package org.codehaus.groovy.transform.sc.transformers;

import org.apache.groovy.ast.tools.ExpressionUtils;
import org.apache.groovy.util.Maps;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.SwitchExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.ReturnAdder;
import org.codehaus.groovy.classgen.asm.ClosureWriter;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesTypeChooser;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Some expressions use symbols as aliases to method calls (&lt;&lt;, +=, ...). In static compilation,
 * if such a method call is found, we transform the original binary expression into a method
 * call expression so that the call gets statically compiled.
 */
public class StaticCompilationTransformer extends ClassCodeExpressionTransformer {

    /**
     * Class node for {@link ScriptBytecodeAdapter}.
     */
    protected static final ClassNode BYTECODE_ADAPTER_CLASS = ClassHelper.make(ScriptBytecodeAdapter.class);
    /**
     * Comparison helpers on {@link ScriptBytecodeAdapter} keyed by Groovy token type.
     */
    protected static final Map<Integer, MethodNode> BYTECODE_BINARY_ADAPTERS = Maps.of(
        Types.COMPARE_EQUAL, BYTECODE_ADAPTER_CLASS.getMethods("compareEqual").get(0),
        Types.COMPARE_GREATER_THAN, BYTECODE_ADAPTER_CLASS.getMethods("compareGreaterThan").get(0),
        Types.COMPARE_GREATER_THAN_EQUAL, BYTECODE_ADAPTER_CLASS.getMethods("compareGreaterThanEqual").get(0),
        Types.COMPARE_LESS_THAN, BYTECODE_ADAPTER_CLASS.getMethods("compareLessThan").get(0),
        Types.COMPARE_LESS_THAN_EQUAL, BYTECODE_ADAPTER_CLASS.getMethods("compareLessThanEqual").get(0),
        Types.COMPARE_NOT_EQUAL, BYTECODE_ADAPTER_CLASS.getMethods("compareNotEqual").get(0),
        Types.COMPARE_TO, BYTECODE_ADAPTER_CLASS.getMethods("compareTo").get(0)
    );

    private final SourceUnit unit;

    private ClassNode transformClass;

    private final StaticTypesTypeChooser typeChooser = new StaticTypesTypeChooser();
    private final StaticTypeCheckingVisitor staticCompilationVisitor;

    // various helpers in order to avoid a potential very big class
    private final StaticMethodCallExpressionTransformer staticMethodCallExpressionTransformer = new StaticMethodCallExpressionTransformer(this);
    private final MethodCallExpressionTransformer methodCallExpressionTransformer = new MethodCallExpressionTransformer(this);
    private final ConstructorCallTransformer constructorCallTransformer = new ConstructorCallTransformer(this);
    private final PropertyExpressionTransformer propertyExpressionTransformer = new PropertyExpressionTransformer(this);
    private final VariableExpressionTransformer variableExpressionTransformer = new VariableExpressionTransformer(this);
    private final ClosureExpressionTransformer closureExpressionTransformer = new ClosureExpressionTransformer(this);
    private final BooleanExpressionTransformer booleanExpressionTransformer = new BooleanExpressionTransformer(this);
    private final BinaryExpressionTransformer binaryExpressionTransformer = new BinaryExpressionTransformer(this);
    private final RangeExpressionTransformer rangeExpressionTransformer = new RangeExpressionTransformer(this);
    private final ListExpressionTransformer listExpressionTransformer = new ListExpressionTransformer(this);
    private final MapExpressionTransformer mapExpressionTransformer = new MapExpressionTransformer(this);
    private final CastExpressionOptimizer castExpressionTransformer = new CastExpressionOptimizer(this);

    // expressions that become the implicit return value of a method or closure
    private final Set<Expression> implicitReturns = Collections.newSetFromMap(new IdentityHashMap<>());
    // multiple assignments whose value is not used
    private final Set<Expression> discardedMultipleAssignments = Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * Creates a static compilation transformer for the supplied source unit and visitor.
     *
     * @param unit the source unit being transformed
     * @param visitor the visitor providing type-checking metadata
     */
    public StaticCompilationTransformer(final SourceUnit unit, final StaticTypeCheckingVisitor visitor) {
        this.unit = unit;
        this.staticCompilationVisitor = visitor;
    }

    /**
     * Returns the source unit currently being transformed.
     *
     * @return the active source unit
     */
    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    /**
     * Returns the type chooser used by this transformer.
     *
     * @return the static types type chooser
     */
    public StaticTypesTypeChooser getTypeChooser() {
        return typeChooser;
    }

    /**
     * Returns the class currently enclosing the transformation context.
     *
     * @return the current enclosing class node
     */
    public ClassNode getClassNode() {
        return staticCompilationVisitor.getTypeCheckingContext().getEnclosingClassNode();
    }

    /**
     * Tells whether the class whose body is being rewritten extends the given
     * class. Closures are not classes yet at this phase, so a closure body
     * reports the enclosing class, the receiver of {@code this} in the closure.
     */
    boolean extendsClass(final ClassNode type) {
        return transformClass != null && !transformClass.equals(type) && transformClass.isDerivedFrom(type);
    }

    /**
     * Tells whether the class whose body is being rewritten is nested in the
     * given class with an enclosing instance of it, so that {@code Type.this}
     * is available: no class on the way out is static.
     */
    boolean hasEnclosingInstanceOf(final ClassNode type) {
        ClassNode cn = transformClass;
        while (cn instanceof InnerClassNode && !cn.isStaticClass() && (cn.getModifiers() & ACC_STATIC) == 0) {
            cn = cn.getOuterClass();
            if (cn == null) break;
            if (cn.equals(type)) return true;
        }
        return false;
    }

    /**
     * Visits a statement container within the current class or method.
     *
     * @param code the code container to visit
     */
    @Override
    public void visitClassCodeContainer(final Statement code) {
        super.visitClassCodeContainer(code);
    }

    /**
     * Dispatches expression transformation to the specialized helper for the expression type.
     *
     * @param expr the expression to transform
     * @return the transformed expression
     */
    @Override
    public Expression transform(final Expression expr) {
        if (expr instanceof StaticMethodCallExpression) {
            return staticMethodCallExpressionTransformer.transformStaticMethodCallExpression((StaticMethodCallExpression) expr);
        }
        if (expr instanceof MethodCallExpression) {
            return methodCallExpressionTransformer.transformMethodCallExpression((MethodCallExpression) expr);
        }
        if (expr instanceof ConstructorCallExpression) {
            return constructorCallTransformer.transformConstructorCall((ConstructorCallExpression) expr);
        }
        if (expr instanceof PropertyExpression) {
            return propertyExpressionTransformer.transformPropertyExpression((PropertyExpression) expr);
        }
        if (expr instanceof VariableExpression) {
            return variableExpressionTransformer.transformVariableExpression((VariableExpression) expr);
        }
        if (expr instanceof ClosureExpression) {
            return closureExpressionTransformer.transformClosureExpression((ClosureExpression) expr);
        }
        if (expr instanceof SwitchExpression se) {
            se.visit(this);
            return se;
        }
        if (expr instanceof BooleanExpression) {
            return booleanExpressionTransformer.transformBooleanExpression((BooleanExpression) expr);
        }
        if (expr instanceof BinaryExpression) {
            return binaryExpressionTransformer.transformBinaryExpression((BinaryExpression) expr);
        }
        if (expr instanceof RangeExpression) {
            return rangeExpressionTransformer.transformRangeExpression((RangeExpression) expr);
        }
        if (expr instanceof CastExpression) {
            return castExpressionTransformer.transformCastExpression((CastExpression) expr);
        }
        if (expr instanceof ListExpression) {
            return listExpressionTransformer.transformListExpression((ListExpression) expr);
        }
        if (expr instanceof MapExpression) {
            return mapExpressionTransformer.transformMapExpression((MapExpression) expr);
        }
        if (expr instanceof PrefixExpression || expr instanceof PostfixExpression) {
            Expression increment = binaryExpressionTransformer.transformExtensionPropertyIncrement(expr);
            if (increment != null) return increment;
        }
        return super.transform(expr);
    }

    /**
     * Called by helpers when super.transform() is needed.
     */
    final Expression superTransform(final Expression expr) {
        return super.transform(expr);
    }

    /**
     * Visits a class and then recursively visits its inner classes.
     *
     * @param node the class node to process
     */
    @Override
    public void visitClass(final ClassNode node) {
        ClassNode previous = transformClass;
        transformClass = node;
        super.visitClass(node);
        node.getInnerClasses().forEachRemaining(this::visitClass);
        transformClass = previous;
    }

    @Override
    public void visitMethod(final MethodNode node) {
        boolean unset = transformClass == null;
        if (unset) transformClass = node.getDeclaringClass();
        super.visitMethod(node);
        if (unset) transformClass = null;
    }

    /**
     * Preserves constant field initializers that must remain inline for later compilation phases.
     *
     * @param node the field to process
     */
    @Override
    public void visitField(final FieldNode node) {
        // GROOVY-11624: post-ResolveVisitor, pre-Verifier constant inlining; do not replace initializer expression "x"+WHY with "x".plus(WHY)
        if (node.hasInitialExpression() && node.isStatic() && node.isFinal() && ClassHelper.isStaticConstantInitializerType(node.getType())) {
            node.setInitialValueExpression(ExpressionUtils.transformInlineConstants(node.getInitialExpression(), node.getType()));
        }
        super.visitField(node);
    }

    /**
     * Visits a constructor or method unless it is explicitly marked to skip static compilation.
     *
     * @param node the method or constructor to process
     * @param isConstructor whether {@code node} is a constructor
     */
    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        if (staticCompilationVisitor.isSkipMode(node)) {
            // method has already been visited by a static type checking visitor
            return;
        }
        collectImplicitReturns(node);
        if (!isConstructor) {
            ClosureWriter.markEscapingClosures(node); // GROOVY-12650
        }
        super.visitConstructorOrMethod(node, isConstructor);
    }

    /**
     * Records which multiple assignments are statements whose value is not used.
     *
     * @param stmt the expression statement to process
     */
    @Override
    public void visitExpressionStatement(final ExpressionStatement stmt) {
        Expression expr = stmt.getExpression();
        if (expr instanceof BinaryExpression bin && bin.getLeftExpression() instanceof TupleExpression
                && !implicitReturns.contains(expr)) {
            discardedMultipleAssignments.add(expr);
        }
        super.visitExpressionStatement(stmt);
    }

    /**
     * Records the expressions that become the implicit return value of a closure body.
     *
     * @param code the closure body
     */
    void collectImplicitReturns(final Statement code) {
        if (code != null) { // no parameters: MethodNode would change their static context
            collectImplicitReturns(new MethodNode("doCall", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, code));
        }
    }

    private void collectImplicitReturns(final MethodNode node) {
        new ReturnAdder(returnStatement -> implicitReturns.add(returnStatement.getExpression())).visitMethod(node);
    }

    /**
     * Tells whether the value of a multiple assignment is not used: it is an
     * expression statement that is not the implicit return value of a method or
     * closure.
     *
     * @param expr the multiple assignment
     * @return {@code true} if the value of {@code expr} cannot be observed
     */
    boolean isValueDiscarded(final Expression expr) {
        return discardedMultipleAssignments.contains(expr);
    }
}
