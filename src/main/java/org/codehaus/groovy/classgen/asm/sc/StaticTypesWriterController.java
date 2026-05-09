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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.asm.BinaryExpressionHelper;
import org.codehaus.groovy.classgen.asm.BinaryExpressionMultiTypeDispatcher;
import org.codehaus.groovy.classgen.asm.CallSiteWriter;
import org.codehaus.groovy.classgen.asm.ClosureWriter;
import org.codehaus.groovy.classgen.asm.DelegatingController;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.classgen.asm.LambdaWriter;
import org.codehaus.groovy.classgen.asm.MethodReferenceExpressionWriter;
import org.codehaus.groovy.classgen.asm.StatementWriter;
import org.codehaus.groovy.classgen.asm.TypeChooser;
import org.codehaus.groovy.classgen.asm.UnaryExpressionHelper;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.classgen.asm.indy.sc.IndyStaticTypesMultiTypeDispatcher;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.objectweb.asm.ClassVisitor;

import static org.codehaus.groovy.transform.sc.StaticCompilationVisitor.isStaticallyCompiled;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DYNAMIC_RESOLUTION;

/**
 * An alternative {@link org.codehaus.groovy.classgen.asm.WriterController} which handles static types and method
 * dispatch. In case of a "mixed mode" where only some methods are annotated with {@link groovy.transform.TypeChecked}
 * then this writer will delegate to the classic writer controller.
 */
public class StaticTypesWriterController extends DelegatingController {

    /**
     * Tracks whether the current member is being emitted through the static-compilation fast path.
     */
    protected boolean isInStaticallyCheckedMethod;
    private boolean methodHasDynamicResolution; // GROOVY-11968

    private LambdaWriter lambdaWriter;
    private ClosureWriter closureWriter;
    private StaticTypesTypeChooser typeChooser;
    private StaticInvocationWriter invocationWriter;
    private StaticTypesCallSiteWriter callSiteWriter;
    private StaticTypesStatementWriter statementWriter;
    private UnaryExpressionHelper unaryExpressionHelper;
    private BinaryExpressionMultiTypeDispatcher binaryExpressionHelper;
    private MethodReferenceExpressionWriter methodReferenceExpressionWriter;

    /**
     * Creates a delegating writer controller that switches between dynamic and static code-generation strategies.
     */
    public StaticTypesWriterController(final WriterController controller) {
        super(controller);
    }

    /** {@inheritDoc} */
    @Override
    public void init(final AsmClassGenerator asmClassGenerator, final GeneratorContext gcon, final ClassVisitor cv, final ClassNode cn) {
        super.init(asmClassGenerator, gcon, cv, cn);
        this.callSiteWriter = new StaticTypesCallSiteWriter(this);
        this.statementWriter = new StaticTypesStatementWriter(this);
        this.typeChooser = new StaticTypesTypeChooser();
        this.invocationWriter = new StaticInvocationWriter(this);
        this.closureWriter = new StaticTypesClosureWriter(this);
        this.lambdaWriter = new StaticTypesLambdaWriter(this);
        this.methodReferenceExpressionWriter = new StaticTypesMethodReferenceExpressionWriter(this);
        this.unaryExpressionHelper = new StaticTypesUnaryExpressionHelper(this);

        CompilerConfiguration config = cn.getCompileUnit().getConfig();
        this.binaryExpressionHelper = config.isIndyEnabled()
                ? new IndyStaticTypesMultiTypeDispatcher(this)
                : new StaticTypesBinaryExpressionMultiTypeDispatcher(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setMethodNode(final MethodNode mn) {
        isInStaticallyCheckedMethod = isStaticallyCompiled(mn);
        methodHasDynamicResolution = isInStaticallyCheckedMethod && hasDynamicResolution(mn);
        super.setMethodNode(mn);
    }

    /** {@inheritDoc} */
    @Override
    public void setConstructorNode(final ConstructorNode cn) {
        isInStaticallyCheckedMethod = isStaticallyCompiled(cn);
        methodHasDynamicResolution = isInStaticallyCheckedMethod && hasDynamicResolution(cn);
        super.setConstructorNode(cn);
    }

    /**
     * GROOVY-11968: returns {@code true} when the current statically compiled method
     * contains one or more sub-expressions that will be routed through the regular
     * (non-static) call site writer via {@link #getCallSiteWriterFor}. The regular
     * writer's per-method state must then be initialized at method entry.
     *
     * @since 6.0.0
     */
    public boolean methodHasDynamicResolution() {
        return methodHasDynamicResolution;
    }

    private static boolean hasDynamicResolution(final MethodNode mn) {
        if (mn == null) return false;
        if (mn.getNodeMetaData(DYNAMIC_RESOLUTION) != null) return true;
        if (mn.getCode() == null) return false;
        var scanner = new DynamicResolutionScanner();
        mn.getCode().visit(scanner);
        return scanner.found;
    }

    private static class DynamicResolutionScanner extends CodeVisitorSupport {
        boolean found;

        @Override
        public void visitMethodCallExpression(final MethodCallExpression call) {
            if (isMarked(call)) return;
            super.visitMethodCallExpression(call);
        }

        @Override
        public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
            if (isMarked(call)) return;
            super.visitStaticMethodCallExpression(call);
        }

        @Override
        public void visitPropertyExpression(final PropertyExpression expression) {
            if (isMarked(expression)) return;
            super.visitPropertyExpression(expression);
        }

        @Override
        public void visitAttributeExpression(final AttributeExpression expression) {
            if (isMarked(expression)) return;
            super.visitAttributeExpression(expression);
        }

        @Override
        public void visitVariableExpression(final VariableExpression expression) {
            if (isMarked(expression)) return;
            super.visitVariableExpression(expression);
        }

        private boolean isMarked(final Expression e) {
            if (!found && e.getNodeMetaData(DYNAMIC_RESOLUTION) != null) {
                found = true;
            }
            return found;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFastPath() {
        if (isInStaticallyCheckedMethod) return true;
        return super.isFastPath();
    }

    /** {@inheritDoc} */
    @Override
    public CallSiteWriter getCallSiteWriter() {
        if (isInStaticallyCheckedMethod) {
            return callSiteWriter;
        }
        return super.getCallSiteWriter();
    }

    /** {@inheritDoc} */
    @Override
    public CallSiteWriter getCallSiteWriterFor(final Expression expression) {
        if (expression.getNodeMetaData(DYNAMIC_RESOLUTION) != null) {
            return getRegularCallSiteWriter(); // GROOVY-6263
        }
        return getCallSiteWriter();
    }

    /**
     * Returns the regular dynamic call-site writer used for fallback expressions.
     */
    public CallSiteWriter getRegularCallSiteWriter() {
        return super.getCallSiteWriter();
    }

    /** {@inheritDoc} */
    @Override
    public StatementWriter getStatementWriter() {
        if (isInStaticallyCheckedMethod) {
            return statementWriter;
        } else {
            return super.getStatementWriter();
        }
    }

    /** {@inheritDoc} */
    @Override
    public TypeChooser getTypeChooser() {
        if (isInStaticallyCheckedMethod) {
            return typeChooser;
        } else {
            return super.getTypeChooser();
        }
    }

    /** {@inheritDoc} */
    @Override
    public InvocationWriter getInvocationWriter() {
        if (isInStaticallyCheckedMethod) {
            return invocationWriter;
        } else {
            return super.getInvocationWriter();
        }
    }

    /**
     * Returns the regular dynamic invocation writer used for fallback expressions.
     */
    public InvocationWriter getRegularInvocationWriter() {
        return super.getInvocationWriter();
    }

    /** {@inheritDoc} */
    @Override
    public BinaryExpressionHelper getBinaryExpressionHelper() {
        if (isInStaticallyCheckedMethod) {
            return binaryExpressionHelper;
        } else {
            return super.getBinaryExpressionHelper();
        }
    }

    /** {@inheritDoc} */
    @Override
    public MethodReferenceExpressionWriter getMethodReferenceExpressionWriter() {
        if (isInStaticallyCheckedMethod) {
            return methodReferenceExpressionWriter;
        }

        return super.getMethodReferenceExpressionWriter();
    }

    /** {@inheritDoc} */
    @Override
    public UnaryExpressionHelper getUnaryExpressionHelper() {
        if (isInStaticallyCheckedMethod) {
            return unaryExpressionHelper;
        }
        return super.getUnaryExpressionHelper();
    }

    /** {@inheritDoc} */
    @Override
    public ClosureWriter getClosureWriter() {
        if (isInStaticallyCheckedMethod) {
            return closureWriter;
        }
        return super.getClosureWriter();
    }

    /** {@inheritDoc} */
    @Override
    public LambdaWriter getLambdaWriter() {
        if (isInStaticallyCheckedMethod) {
            return lambdaWriter;
        }
        return super.getLambdaWriter();
    }
}
