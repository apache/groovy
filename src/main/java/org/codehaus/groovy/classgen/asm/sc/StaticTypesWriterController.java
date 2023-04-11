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
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
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
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.ClassVisitor;

import static org.codehaus.groovy.transform.sc.StaticCompilationVisitor.isStaticallyCompiled;

/**
 * An alternative {@link org.codehaus.groovy.classgen.asm.WriterController} which handles static types and method
 * dispatch. In case of a "mixed mode" where only some methods are annotated with {@link groovy.transform.TypeChecked}
 * then this writer will delegate to the classic writer controller.
 */
public class StaticTypesWriterController extends DelegatingController {

    protected boolean isInStaticallyCheckedMethod;

    private LambdaWriter lambdaWriter;
    private ClosureWriter closureWriter;
    private StaticTypesTypeChooser typeChooser;
    private StaticInvocationWriter invocationWriter;
    private StaticTypesCallSiteWriter callSiteWriter;
    private StaticTypesStatementWriter statementWriter;
    private UnaryExpressionHelper unaryExpressionHelper;
    private BinaryExpressionMultiTypeDispatcher binaryExpressionHelper;
    private MethodReferenceExpressionWriter methodReferenceExpressionWriter;

    public StaticTypesWriterController(final WriterController controller) {
        super(controller);
    }

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

    @Override
    public void setMethodNode(final MethodNode mn) {
        isInStaticallyCheckedMethod = isStaticallyCompiled(mn);
        super.setMethodNode(mn);
    }

    @Override
    public void setConstructorNode(final ConstructorNode cn) {
        isInStaticallyCheckedMethod = isStaticallyCompiled(cn);
        super.setConstructorNode(cn);
    }
    
    @Override
    public boolean isFastPath() {
        if (isInStaticallyCheckedMethod) return true;
        return super.isFastPath();
    }
    
    @Override
    public CallSiteWriter getCallSiteWriter() {
        MethodNode methodNode = getMethodNode();
        if (isInStaticallyCheckedMethod && (methodNode == null
                || !Boolean.TRUE.equals(methodNode.getNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION)))) {
            return callSiteWriter;
        }
        return super.getCallSiteWriter();
    }

    public CallSiteWriter getRegularCallSiteWriter() {
        return super.getCallSiteWriter();
    }

    @Override
    public StatementWriter getStatementWriter() {
        if (isInStaticallyCheckedMethod) {
            return statementWriter;
        } else {
            return super.getStatementWriter();
        }
    }
    
    @Override
    public TypeChooser getTypeChooser() {
        if (isInStaticallyCheckedMethod) {
            return typeChooser;
        } else {
            return super.getTypeChooser();
        }
    }

    @Override
    public InvocationWriter getInvocationWriter() {
        if (isInStaticallyCheckedMethod) {
            return invocationWriter;
        } else {
            return super.getInvocationWriter();
        }
    }

    public InvocationWriter getRegularInvocationWriter() {
        return super.getInvocationWriter();
    }

    @Override
    public BinaryExpressionHelper getBinaryExpressionHelper() {
        if (isInStaticallyCheckedMethod) {
            return binaryExpressionHelper;
        } else {
            return super.getBinaryExpressionHelper();
        }
    }

    @Override
    public MethodReferenceExpressionWriter getMethodReferenceExpressionWriter() {
        if (isInStaticallyCheckedMethod) {
            return methodReferenceExpressionWriter;
        }

        return super.getMethodReferenceExpressionWriter();
    }

    @Override
    public UnaryExpressionHelper getUnaryExpressionHelper() {
        if (isInStaticallyCheckedMethod) {
            return unaryExpressionHelper;
        }
        return super.getUnaryExpressionHelper();
    }

    @Override
    public ClosureWriter getClosureWriter() {
        if (isInStaticallyCheckedMethod) {
            return closureWriter;
        }
        return super.getClosureWriter();
    }

    @Override
    public LambdaWriter getLambdaWriter() {
        if (isInStaticallyCheckedMethod) {
            return lambdaWriter;
        }
        return super.getLambdaWriter();
    }
}
