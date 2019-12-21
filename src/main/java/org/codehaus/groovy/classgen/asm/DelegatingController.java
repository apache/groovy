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

    public DelegatingController(WriterController normalController) {
        this.delegationController = normalController;
    }

    @Override
    public void init(final AsmClassGenerator asmClassGenerator, final GeneratorContext gcon, final ClassVisitor cv, final ClassNode cn) {
        delegationController.init(asmClassGenerator, gcon, cv, cn);
    }

    @Override
    public void setMethodNode(final MethodNode mn) {
        delegationController.setMethodNode(mn);
    }

    @Override
    public void setConstructorNode(final ConstructorNode cn) {
        delegationController.setConstructorNode(cn);
    }

    @Override
    public boolean isFastPath() {
        return delegationController.isFastPath();
    }

    @Override
    public CallSiteWriter getCallSiteWriter() {
        return delegationController.getCallSiteWriter();
    }

    @Override
    public StatementWriter getStatementWriter() {
        return delegationController.getStatementWriter();
    }

    @Override
    public TypeChooser getTypeChooser() {
        return delegationController.getTypeChooser();
    }

    @Override
    public AsmClassGenerator getAcg() {
        return delegationController.getAcg();
    }

    @Override
    public AssertionWriter getAssertionWriter() {
        return delegationController.getAssertionWriter();
    }

    @Override
    public BinaryExpressionHelper getBinaryExpressionHelper() {
        return delegationController.getBinaryExpressionHelper();
    }

    @Override
    public UnaryExpressionHelper getUnaryExpressionHelper() {
        return delegationController.getUnaryExpressionHelper();
    }

    @Override
    public String getClassName() {
        return delegationController.getClassName();
    }

    @Override
    public ClassNode getClassNode() {
        return delegationController.getClassNode();
    }

    @Override
    public ClassVisitor getClassVisitor() {
        return delegationController.getClassVisitor();
    }

    @Override
    public ClosureWriter getClosureWriter() {
        return delegationController.getClosureWriter();
    }

    @Override
    public LambdaWriter getLambdaWriter() {
        return delegationController.getLambdaWriter();
    }

    @Override
    public MethodPointerExpressionWriter getMethodPointerExpressionWriter() {
        return delegationController.getMethodPointerExpressionWriter();
    }

    @Override
    public MethodReferenceExpressionWriter getMethodReferenceExpressionWriter() {
        return delegationController.getMethodReferenceExpressionWriter();
    }

    @Override
    public CompileStack getCompileStack() {
        return delegationController.getCompileStack();
    }

    @Override
    public ConstructorNode getConstructorNode() {
        return delegationController.getConstructorNode();
    }

    @Override
    public GeneratorContext getContext() {
        return delegationController.getContext();
    }

    @Override
    public ClassVisitor getCv() {
        return delegationController.getCv();
    }

    @Override
    public InterfaceHelperClassNode getInterfaceClassLoadingClass() {
        return delegationController.getInterfaceClassLoadingClass();
    }

    @Override
    public String getInternalBaseClassName() {
        return delegationController.getInternalBaseClassName();
    }

    @Override
    public String getInternalClassName() {
        return delegationController.getInternalClassName();
    }

    @Override
    public InvocationWriter getInvocationWriter() {
        return delegationController.getInvocationWriter();
    }

    @Override
    public MethodNode getMethodNode() {
        return delegationController.getMethodNode();
    }

    @Override
    public MethodVisitor getMethodVisitor() {
        return delegationController.getMethodVisitor();
    }

    @Override
    public OperandStack getOperandStack() {
        return delegationController.getOperandStack();
    }

    @Override
    public ClassNode getOutermostClass() {
        return delegationController.getOutermostClass();
    }

    @Override
    public ClassNode getReturnType() {
        return delegationController.getReturnType();
    }

    @Override
    public SourceUnit getSourceUnit() {
        return delegationController.getSourceUnit();
    }

    @Override
    public boolean isConstructor() {
        return delegationController.isConstructor();
    }

    @Override
    public boolean isInGeneratedFunction() {
        return delegationController.isInGeneratedFunction();
    }

    @Override
    public boolean isInGeneratedFunctionConstructor() {
        return delegationController.isInGeneratedFunctionConstructor();
    }

    @Override
    public boolean isNotClinit() {
        return delegationController.isNotClinit();
    }

    @Override
    public boolean isInScriptBody() {
        return delegationController.isInScriptBody();
    }

    @Override
    public boolean isStaticConstructor() {
        return delegationController.isStaticConstructor();
    }

    @Override
    public boolean isStaticContext() {
        return delegationController.isStaticContext();
    }

    @Override
    public boolean isStaticMethod() {
        return delegationController.isStaticMethod();
    }

    @Override
    public void setInterfaceClassLoadingClass(InterfaceHelperClassNode ihc) {
        delegationController.setInterfaceClassLoadingClass(ihc);
    }

    @Override
    public void setMethodVisitor(MethodVisitor methodVisitor) {
        delegationController.setMethodVisitor(methodVisitor);
    }

    @Override
    public boolean shouldOptimizeForInt() {
        return delegationController.shouldOptimizeForInt();
    }

    @Override
    public void switchToFastPath() {
        delegationController.switchToFastPath();
    }

    @Override
    public void switchToSlowPath() {
        delegationController.switchToSlowPath();
    }

    @Override
    public int getBytecodeVersion() {
        return delegationController.getBytecodeVersion();
    }

    @Override
    public void setLineNumber(int n) {
        delegationController.setLineNumber(n);
    }

    @Override
    public int getLineNumber() {
        return delegationController.getLineNumber();
    }

    @Override
    public void resetLineNumber() {
        delegationController.resetLineNumber();
    }
}
