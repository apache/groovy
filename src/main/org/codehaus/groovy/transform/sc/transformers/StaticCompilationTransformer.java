/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform.sc.transformers;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesTypeChooser;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.Types;

import java.util.*;

/**
 * Some expressions use symbols as aliases to method calls (<<, +=, ...). In static compilation,
 * if such a method call is found, we transform the original binary expression into a method
 * call expression so that the call gets statically compiled.
 *
 * @author Cedric Champeau
 */
public class StaticCompilationTransformer extends ClassCodeExpressionTransformer {

    protected static final ClassNode BYTECODE_ADAPTER_CLASS = ClassHelper.make(ScriptBytecodeAdapter.class);
    protected final static Map<Integer, MethodNode> BYTECODE_BINARY_ADAPTERS = Collections.unmodifiableMap(new HashMap<Integer, MethodNode>() {{
        put(Types.COMPARE_EQUAL, BYTECODE_ADAPTER_CLASS.getMethods("compareEqual").get(0));
        put(Types.COMPARE_GREATER_THAN, BYTECODE_ADAPTER_CLASS.getMethods("compareGreaterThan").get(0));
        put(Types.COMPARE_GREATER_THAN_EQUAL, BYTECODE_ADAPTER_CLASS.getMethods("compareGreaterThanEqual").get(0));
        put(Types.COMPARE_LESS_THAN, BYTECODE_ADAPTER_CLASS.getMethods("compareLessThan").get(0));
        put(Types.COMPARE_LESS_THAN_EQUAL, BYTECODE_ADAPTER_CLASS.getMethods("compareLessThanEqual").get(0));
        put(Types.COMPARE_NOT_EQUAL, BYTECODE_ADAPTER_CLASS.getMethods("compareNotEqual").get(0));
        put(Types.COMPARE_TO, BYTECODE_ADAPTER_CLASS.getMethods("compareTo").get(0));

    }});


    private ClassNode classNode;
    private final SourceUnit unit;

    private final StaticTypesTypeChooser typeChooser = new StaticTypesTypeChooser();

    // various helpers in order to avoid a potential very big class
    private final StaticMethodCallExpressionTransformer staticMethodCallExpressionTransformer = new StaticMethodCallExpressionTransformer(this);
    private final ConstructorCallTransformer constructorCallTransformer = new ConstructorCallTransformer(this);
    private final MethodCallExpressionTransformer methodCallExpressionTransformer = new MethodCallExpressionTransformer(this);
    private final BinaryExpressionTransformer binaryExpressionTransformer = new BinaryExpressionTransformer(this);
    private final ClosureExpressionTransformer closureExpressionTransformer = new ClosureExpressionTransformer(this);
    private final BooleanExpressionTransformer booleanExpressionTransformer = new BooleanExpressionTransformer(this);

    public StaticCompilationTransformer(final SourceUnit unit) {
        this.unit = unit;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    public StaticTypesTypeChooser getTypeChooser() {
        return typeChooser;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    @Override
    public void visitClassCodeContainer(final Statement code) {
        super.visitClassCodeContainer(code);
    }

    @Override
    public Expression transform(Expression expr) {
        if (expr instanceof StaticMethodCallExpression) {
            return staticMethodCallExpressionTransformer.transformStaticMethodCallExpression((StaticMethodCallExpression) expr);
        }
        if (expr instanceof BinaryExpression) {
            return binaryExpressionTransformer.transformBinaryExpression((BinaryExpression)expr);
        }
        if (expr instanceof MethodCallExpression) {
            return methodCallExpressionTransformer.transformMethodCallExpression((MethodCallExpression) expr);
        }
        if (expr instanceof ClosureExpression) {
            return closureExpressionTransformer.transformClosureExpression((ClosureExpression) expr);
        }
        if (expr instanceof ConstructorCallExpression) {
            return constructorCallTransformer.transformConstructorCall((ConstructorCallExpression) expr);
        }
        if (expr instanceof BooleanExpression) {
            return booleanExpressionTransformer.transformBooleanExpression((BooleanExpression)expr);
        }
        return super.transform(expr);
    }

    /**
     * Called by helpers when super.transform() is needed.
     */
    final Expression superTransform(Expression expr) {
        return super.transform(expr);
    }
    
    @Override
    public void visitClass(final ClassNode node) {
        ClassNode prec = classNode;
        classNode = node;
        super.visitClass(node);
        Iterator<InnerClassNode> innerClasses = classNode.getInnerClasses();
        while (innerClasses.hasNext()) {
            InnerClassNode innerClassNode = innerClasses.next();
            visitClass(innerClassNode);
        }
        classNode = prec;
    }
}
