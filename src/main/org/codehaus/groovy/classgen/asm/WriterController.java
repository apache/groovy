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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.InterfaceHelperClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class WriterController {
    private AsmClassGenerator acg;
    private MethodVisitor methodVisitor;
    private CompileStack compileStack;
    private OperandStack operandStack;
    private ClassNode classNode;
    private CallSiteWriter callSiteWriter;
    private ClassVisitor cv;
    private ClosureWriter closureWriter;
    private String internalClassName;
    private InvocationWriter invocationWriter;
    private BinaryExpressionHelper binaryExpHelper;
    private AssertionWriter assertionWriter;
    private String internalBaseClassName;
    private ClassNode outermostClass;
    private MethodNode methodNode;
    private SourceUnit sourceUnit;
    private ConstructorNode constructorNode;
    private GeneratorContext context;
    private InterfaceHelperClassNode interfaceClassLoadingClass;
    
    public void init(AsmClassGenerator asmClassGenerator, GeneratorContext gcon, ClassVisitor cv, ClassNode cn) {
        this.classNode = cn;
        this.outermostClass = null;
        this.internalClassName = BytecodeHelper.getClassInternalName(classNode);
        this.callSiteWriter = new CallSiteWriter(this);
        this.invocationWriter = new InvocationWriter(this);
        this.binaryExpHelper = new BinaryExpressionHelper(this);
        this.operandStack = new OperandStack(this);
        this.assertionWriter = new AssertionWriter(this);
        this.closureWriter = new ClosureWriter(this);
        this.internalBaseClassName = BytecodeHelper.getClassInternalName(classNode.getSuperClass());
        this.acg = asmClassGenerator;
        this.sourceUnit = acg.getSourceUnit();
        this.context = gcon;
        this.compileStack = new CompileStack(this);
        this.cv = cv;
    }

    public AsmClassGenerator getAcg() {
        return acg;
    }

    public void setMethodVisitor(MethodVisitor methodVisitor) {
        this.methodVisitor = methodVisitor;
    }

    public MethodVisitor getMethodVisitor() {
        return methodVisitor;
    }

    public CompileStack getCompileStack() {
        return compileStack;
    }

    public OperandStack getOperandStack() {
        return operandStack;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public CallSiteWriter getCallSiteWriter() {
        return callSiteWriter;
    }

    public ClassVisitor getClassVisitor() {
        return cv;
    }

    public ClosureWriter getClosureWriter() {
        return closureWriter;
    }
    
    public ClassVisitor getCv() {
        return cv;
    }

    public String getInternalClassName() {
        return internalClassName;
    }

    public InvocationWriter getInvocationWriter() {
        return invocationWriter;
    }

    public BinaryExpressionHelper getBinaryExpHelper() {
        return binaryExpHelper;
    }

    public AssertionWriter getAssertionWriter() {
        return assertionWriter;
    }

    public String getInternalBaseClassName() {
        return internalBaseClassName;
    }
    
    public MethodNode getMethodNode() {
        return methodNode;
    }
    
    public void setMethodNode(MethodNode mn) {
        methodNode = mn;
        constructorNode = null;
    }
    
    public ConstructorNode getConstructorNode(){
        return constructorNode;
    }
    
    public void setConstructorNode(ConstructorNode cn) {
        constructorNode = cn;
        methodNode = null;
    }
    
    public boolean isNotClinit() {
        return methodNode == null || !methodNode.getName().equals("<clinit>");
    }

    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public boolean isStaticContext() {
        if (compileStack!=null && compileStack.getScope()!=null) {
            return compileStack.getScope().isInStaticContext();
        }
        if (!isInClosure()) return false;
        if (constructorNode != null) return false;
        return classNode.isStaticClass() || methodNode.isStatic();
    }

    public boolean isInClosure() {
        return classNode.getOuterClass() != null
                && classNode.getSuperClass() == ClassHelper.CLOSURE_TYPE;
    }    
    
    public boolean isInClosureConstructor() {
        return constructorNode != null
        && classNode.getOuterClass() != null
        && classNode.getSuperClass() == ClassHelper.CLOSURE_TYPE;
    }

    public boolean isNotExplicitThisInClosure(boolean implicitThis) {
        return implicitThis || !isInClosure();
    }


    public boolean isStaticMethod() {
        return methodNode != null && methodNode.isStatic();
    }

    public ClassNode getReturnType() {
        if (methodNode != null) {
            return methodNode.getReturnType();
        } else if (constructorNode != null) {
            return constructorNode.getReturnType();
        } else {
            throw new GroovyBugError("I spotted a return that is neither in a method nor in a constructor... I can not handle that");
        }
    }

    public boolean isStaticConstructor() {
        return methodNode != null && methodNode.getName().equals("<clinit>");
    }

    public boolean isConstructor() {
        return constructorNode!=null;
    }

    /**
     * @return true if we are in a script body, where all variables declared are no longer
     *         local variables but are properties
     */
    public boolean isInScriptBody() {
        if (classNode.isScriptBody()) {
            return true;
        } else {
            return classNode.isScript() && methodNode != null && methodNode.getName().equals("run");
        }
    }
    
    public String getClassName() {
        String className;
        if (!classNode.isInterface() || interfaceClassLoadingClass == null) {
            className = internalClassName;
        } else {
            className = BytecodeHelper.getClassInternalName(interfaceClassLoadingClass);
        }
        return className;
    }
    
    public ClassNode getOutermostClass() {
        if (outermostClass == null) {
            outermostClass = classNode;
            while (outermostClass instanceof InnerClassNode) {
                outermostClass = outermostClass.getOuterClass();
            }
        }
        return outermostClass;
    }

    public GeneratorContext getContext() {
        return context;
    }

    public void setInterfaceClassLoadingClass(InterfaceHelperClassNode ihc) {
        interfaceClassLoadingClass = ihc;
    }
    
    public InterfaceHelperClassNode getInterfaceClassLoadingClass() {
        return interfaceClassLoadingClass;
    }
}
