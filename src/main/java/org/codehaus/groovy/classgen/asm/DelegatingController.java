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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InterfaceHelperClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * This class will delegate all calls to a WriterController given in the constructor.
 */
public class DelegatingController extends WriterController {

    private final WriterController delegationController;

    /**
     * Creates a delegating controller that forwards all calls to the given controller.
     *
     * @param normalController the controller to delegate to
     */
    public DelegatingController(WriterController normalController) {
        this.delegationController = normalController;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final AsmClassGenerator asmClassGenerator, final GeneratorContext gcon, final ClassVisitor cv, final ClassNode cn) {
        delegationController.init(asmClassGenerator, gcon, cv, cn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMethodNode(final MethodNode mn) {
        delegationController.setMethodNode(mn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConstructorNode(final ConstructorNode cn) {
        delegationController.setConstructorNode(cn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFastPath() {
        return delegationController.isFastPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CallSiteWriter getCallSiteWriter() {
        return delegationController.getCallSiteWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CallSiteWriter getCallSiteWriterFor(final Expression expression) {
        return delegationController.getCallSiteWriterFor(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StatementWriter getStatementWriter() {
        return delegationController.getStatementWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeChooser getTypeChooser() {
        return delegationController.getTypeChooser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AsmClassGenerator getAcg() {
        return delegationController.getAcg();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AssertionWriter getAssertionWriter() {
        return delegationController.getAssertionWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BinaryExpressionHelper getBinaryExpressionHelper() {
        return delegationController.getBinaryExpressionHelper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnaryExpressionHelper getUnaryExpressionHelper() {
        return delegationController.getUnaryExpressionHelper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassName() {
        return delegationController.getClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassNode getClassNode() {
        return delegationController.getClassNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassVisitor getClassVisitor() {
        return delegationController.getClassVisitor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClosureWriter getClosureWriter() {
        return delegationController.getClosureWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LambdaWriter getLambdaWriter() {
        return delegationController.getLambdaWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodPointerExpressionWriter getMethodPointerExpressionWriter() {
        return delegationController.getMethodPointerExpressionWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodReferenceExpressionWriter getMethodReferenceExpressionWriter() {
        return delegationController.getMethodReferenceExpressionWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompileStack getCompileStack() {
        return delegationController.getCompileStack();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConstructorNode getConstructorNode() {
        return delegationController.getConstructorNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeneratorContext getContext() {
        return delegationController.getContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public ClassVisitor getCv() {
        return delegationController.getCv();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InterfaceHelperClassNode getInterfaceClassLoadingClass() {
        return delegationController.getInterfaceClassLoadingClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInternalBaseClassName() {
        return delegationController.getInternalBaseClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInternalClassName() {
        return delegationController.getInternalClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvocationWriter getInvocationWriter() {
        return delegationController.getInvocationWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodNode getMethodNode() {
        return delegationController.getMethodNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodVisitor getMethodVisitor() {
        return delegationController.getMethodVisitor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperandStack getOperandStack() {
        return delegationController.getOperandStack();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassNode getOutermostClass() {
        return delegationController.getOutermostClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassNode getReturnType() {
        return delegationController.getReturnType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceUnit getSourceUnit() {
        return delegationController.getSourceUnit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConstructor() {
        return delegationController.isConstructor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInGeneratedFunction() {
        return delegationController.isInGeneratedFunction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInGeneratedFunctionConstructor() {
        return delegationController.isInGeneratedFunctionConstructor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNotClinit() {
        return delegationController.isNotClinit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInScriptBody() {
        return delegationController.isInScriptBody();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStaticConstructor() {
        return delegationController.isStaticConstructor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStaticContext() {
        return delegationController.isStaticContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStaticMethod() {
        return delegationController.isStaticMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInterfaceClassLoadingClass(InterfaceHelperClassNode ihc) {
        delegationController.setInterfaceClassLoadingClass(ihc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMethodVisitor(MethodVisitor methodVisitor) {
        delegationController.setMethodVisitor(methodVisitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldOptimizeForInt() {
        return delegationController.shouldOptimizeForInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void switchToFastPath() {
        delegationController.switchToFastPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void switchToSlowPath() {
        delegationController.switchToSlowPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBytecodeVersion() {
        return delegationController.getBytecodeVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLineNumber(int n) {
        delegationController.setLineNumber(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLineNumber() {
        return delegationController.getLineNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetLineNumber() {
        delegationController.resetLineNumber();
    }
}
