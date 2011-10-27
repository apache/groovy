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

import groovy.transform.CompileStatic;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.asm.*;
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
        this.binaryExprHelper = new BinaryExpressionMultiTypeDispatcher(this);
    }

    @Override
    public void setMethodNode(final MethodNode mn) {
        isInStaticallyCheckedMethod = mn != null && !mn.getAnnotations(ClassHelper.make(CompileStatic.class)).isEmpty();
        super.setMethodNode(mn);
    }

    @Override
    public void setConstructorNode(final ConstructorNode cn) {
        isInStaticallyCheckedMethod = false;
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
    public BinaryExpressionHelper getBinaryExpHelper() {
        if (isInStaticallyCheckedMethod) {
            return binaryExprHelper;
        } else {
            return super.getBinaryExpHelper();
        }
    }
}
