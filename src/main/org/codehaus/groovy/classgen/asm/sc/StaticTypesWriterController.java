/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.asm.*;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor;
import org.objectweb.asm.ClassVisitor;


/**
 * An alternative {@link org.codehaus.groovy.classgen.asm.WriterController} which handles static types and method
 * dispatch. In case of a "mixed mode" where only some methods are annotated with {@link groovy.transform.TypeChecked}
 * then this writer will delegate to the classic writer controller.
 *
 * @author Cedric Champeau
 */
public class StaticTypesWriterController extends DelegatingController {

    protected boolean isInStaticallyCheckedMethod;
    private StaticTypesCallSiteWriter callSiteWriter;
    private StaticTypesStatementWriter statementWriter;
    private StaticTypesTypeChooser typeChooser;
    private StaticInvocationWriter invocationWriter;
    private BinaryExpressionMultiTypeDispatcher binaryExprHelper;
    private UnaryExpressionHelper unaryExpressionHelper;
    private ClosureWriter closureWriter;

    public StaticTypesWriterController(WriterController normalController) {
        super(normalController);
        isInStaticallyCheckedMethod = false;
    }

    @Override
    public void init(final AsmClassGenerator asmClassGenerator, final GeneratorContext gcon, final ClassVisitor cv, final ClassNode cn) {
        super.init(asmClassGenerator, gcon, cv, cn);
        this.callSiteWriter = new StaticTypesCallSiteWriter(this);
        this.statementWriter = new StaticTypesStatementWriter(this);
        this.typeChooser = new StaticTypesTypeChooser();
        this.invocationWriter = new StaticInvocationWriter(this);
        this.binaryExprHelper = new StaticTypesBinaryExpressionMultiTypeDispatcher(this);
        this.closureWriter = new StaticTypesClosureWriter(this);
        this.unaryExpressionHelper = new StaticTypesUnaryExpressionHelper(this);
    }

    @Override
    public void setMethodNode(final MethodNode mn) {
        updateStaticCompileFlag(mn);
        super.setMethodNode(mn);
    }

    private void updateStaticCompileFlag(final MethodNode mn) {
        ClassNode classNode = getClassNode();
        AnnotatedNode node = mn;
        if (classNode.implementsInterface(ClassHelper.GENERATED_CLOSURE_Type)) {
            node = classNode.getOuterClass();
        }

        isInStaticallyCheckedMethod = mn != null && (
                StaticCompilationVisitor.isStaticallyCompiled(node)
                        || classNode.implementsInterface(ClassHelper.GENERATED_CLOSURE_Type)&&classNode.getNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE)!=null);

/*      if (isInStaticallyCheckedMethod) {
            System.out.println("Entering statically compiled method: "+mn.getDeclaringClass()+"#"+mn);
        } else if (mn!=null) {
            System.out.println("Entering dynamically compiled method: "+mn.getDeclaringClass()+"#"+mn);
        }*/
    }

    @Override
    public void setConstructorNode(final ConstructorNode cn) {
        updateStaticCompileFlag(cn);
        super.setConstructorNode(cn);
    }
    
    @Override
    public boolean isFastPath() {
        if (isInStaticallyCheckedMethod) return true;
        return super.isFastPath();
    }
    
    @Override
    public CallSiteWriter getCallSiteWriter() {
        if (isInStaticallyCheckedMethod) {
            return callSiteWriter;
        } else {
            return super.getCallSiteWriter();
        }
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

    @Override
    public BinaryExpressionHelper getBinaryExpressionHelper() {
        if (isInStaticallyCheckedMethod) {
            return binaryExprHelper;
        } else {
            return super.getBinaryExpressionHelper();
        }
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
}
