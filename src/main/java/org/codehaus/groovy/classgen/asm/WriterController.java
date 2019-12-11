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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InterfaceHelperClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.asm.indy.IndyBinHelper;
import org.codehaus.groovy.classgen.asm.indy.IndyCallSiteWriter;
import org.codehaus.groovy.classgen.asm.indy.InvokeDynamicWriter;
import org.codehaus.groovy.classgen.asm.util.LoggableClassVisitor;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.util.SystemUtil.getBooleanSafe;

public class WriterController {

    private final boolean LOG_CLASSGEN = getBooleanSafe("groovy.log.classgen");

    private AsmClassGenerator acg;
    private MethodVisitor methodVisitor;
    private CompileStack compileStack;
    private OperandStack operandStack;
    private ClassNode classNode;
    private CallSiteWriter callSiteWriter;
    private ClassVisitor cv;
    private ClosureWriter closureWriter;
    private LambdaWriter lambdaWriter;
    private String internalClassName;
    private InvocationWriter invocationWriter;
    private BinaryExpressionHelper binaryExpHelper, fastPathBinaryExpHelper;
    private UnaryExpressionHelper unaryExpressionHelper, fastPathUnaryExpressionHelper;
    private AssertionWriter assertionWriter;
    private String internalBaseClassName;
    private ClassNode outermostClass;
    private MethodNode methodNode;
    private SourceUnit sourceUnit;
    private ConstructorNode constructorNode;
    private GeneratorContext context;
    private InterfaceHelperClassNode interfaceClassLoadingClass;
    public boolean optimizeForInt = true;
    private StatementWriter statementWriter;
    private boolean fastPath = false;
    private TypeChooser typeChooser;
    private int bytecodeVersion = Opcodes.V1_8;
    private int lineNumber = -1;
    private int helperMethodIndex = 0;
    private List<String> superMethodNames = new ArrayList<>();
    private MethodPointerExpressionWriter methodPointerExpressionWriter;
    private MethodReferenceExpressionWriter methodReferenceExpressionWriter;

    public void init(final AsmClassGenerator asmClassGenerator, final GeneratorContext gcon, final ClassVisitor cv, final ClassNode cn) {
        CompilerConfiguration config = cn.getCompileUnit().getConfig();
        Map<String,Boolean> optOptions = config.getOptimizationOptions();
        boolean invokedynamic = false;
        if (optOptions.isEmpty()) {
            // IGNORE
        } else if (Boolean.FALSE.equals(optOptions.get("all"))) {
            optimizeForInt = false;
            // set other optimizations options to false here
        } else {
            if (Boolean.TRUE.equals(optOptions.get(CompilerConfiguration.INVOKEDYNAMIC))) invokedynamic = true;
            if (Boolean.FALSE.equals(optOptions.get("int"))) optimizeForInt = false;
            if (invokedynamic) optimizeForInt = false;
            // set other optimizations options to false here
        }
        this.classNode = cn;
        this.outermostClass = null;
        this.internalClassName = BytecodeHelper.getClassInternalName(classNode);

        bytecodeVersion = chooseBytecodeVersion(invokedynamic, config.isPreviewFeatures(), config.getTargetBytecode());

        if (invokedynamic) {
            this.invocationWriter = new InvokeDynamicWriter(this);
            this.callSiteWriter = new IndyCallSiteWriter(this);
            this.binaryExpHelper = new IndyBinHelper(this);
        } else {
            this.callSiteWriter = new CallSiteWriter(this);
            this.invocationWriter = new InvocationWriter(this);
            this.binaryExpHelper = new BinaryExpressionHelper(this);
        }

        this.unaryExpressionHelper = new UnaryExpressionHelper(this);
        if (optimizeForInt) {
            this.fastPathBinaryExpHelper = new BinaryExpressionMultiTypeDispatcher(this);
            // todo: replace with a real fast path unary expression helper when available
            this.fastPathUnaryExpressionHelper = new UnaryExpressionHelper(this);
        } else {
            this.fastPathBinaryExpHelper = this.binaryExpHelper;
            this.fastPathUnaryExpressionHelper = new UnaryExpressionHelper(this);
        }

        this.operandStack = new OperandStack(this);
        this.assertionWriter = new AssertionWriter(this);
        this.closureWriter = new ClosureWriter(this);
        this.lambdaWriter = new LambdaWriter(this);
        this.methodPointerExpressionWriter = new MethodPointerExpressionWriter(this);
        this.methodReferenceExpressionWriter = new MethodReferenceExpressionWriter(this);
        this.internalBaseClassName = BytecodeHelper.getClassInternalName(classNode.getSuperClass());
        this.acg = asmClassGenerator;
        this.sourceUnit = acg.getSourceUnit();
        this.context = gcon;
        this.compileStack = new CompileStack(this);
        this.cv = this.createClassVisitor(cv);
        if (optimizeForInt) {
            this.statementWriter = new OptimizingStatementWriter(this);
        } else {
            this.statementWriter = new StatementWriter(this);
        }
        this.typeChooser = new StatementMetaTypeChooser();
    }

    private ClassVisitor createClassVisitor(final ClassVisitor cv) {
        if (!LOG_CLASSGEN) {
            return cv;
        }
        if (cv instanceof LoggableClassVisitor) {
            return cv;
        }
        return new LoggableClassVisitor(cv);
    }

    private static int chooseBytecodeVersion(final boolean invokedynamic, final boolean previewFeatures, final String targetBytecode) {
        Integer bytecodeVersion = CompilerConfiguration.JDK_TO_BYTECODE_VERSION_MAP.get(targetBytecode);

        if (invokedynamic && bytecodeVersion < Opcodes.V1_8) {
            return Opcodes.V1_8;
        } else {
            if (null != bytecodeVersion) {
                return previewFeatures ? bytecodeVersion | Opcodes.V_PREVIEW : bytecodeVersion;
            }
        }

        throw new GroovyBugError("Bytecode version ["+targetBytecode+"] is not supported by the compiler");
    }

    public AsmClassGenerator getAcg() {
        return acg;
    }

    public void setMethodVisitor(final MethodVisitor methodVisitor) {
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

    public LambdaWriter getLambdaWriter() {
        return lambdaWriter;
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

    public BinaryExpressionHelper getBinaryExpressionHelper() {
        if (fastPath) {
            return fastPathBinaryExpHelper;
        } else {
            return binaryExpHelper;
        }
    }

    public MethodPointerExpressionWriter getMethodPointerExpressionWriter() {
        return methodPointerExpressionWriter;
    }

    public MethodReferenceExpressionWriter getMethodReferenceExpressionWriter() {
        return methodReferenceExpressionWriter;
    }

    public UnaryExpressionHelper getUnaryExpressionHelper() {
        if (fastPath) {
            return fastPathUnaryExpressionHelper;
        } else {
            return unaryExpressionHelper;
        }
    }

    public AssertionWriter getAssertionWriter() {
        return assertionWriter;
    }

    public TypeChooser getTypeChooser() {
        return typeChooser;
    }

    public String getInternalBaseClassName() {
        return internalBaseClassName;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public void setMethodNode(final MethodNode methodNode) {
        this.methodNode = methodNode;
        this.constructorNode = null;
    }

    public ConstructorNode getConstructorNode(){
        return constructorNode;
    }

    public void setConstructorNode(final ConstructorNode constructorNode) {
        this.constructorNode = constructorNode;
        this.methodNode = null;
    }

    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public boolean isStaticContext() {
        if (compileStack != null && compileStack.getScope() != null) {
            return compileStack.getScope().isInStaticContext();
        }
        if (!isInClosure()) return false;
        if (isConstructor()) return false;
        return classNode.isStaticClass() || isStaticMethod();
    }

    public boolean isInClosure() {
        return classNode.getOuterClass() != null
                && classNode.getSuperClass().equals(ClassHelper.CLOSURE_TYPE)
                && classNode.implementsAnyInterfaces(ClassHelper.GENERATED_CLOSURE_Type, ClassHelper.GENERATED_LAMBDA_TYPE);
    }

    public boolean isInClosureConstructor() {
        return isConstructor() && isInClosure();
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

    public boolean isNotClinit() {
        return methodNode == null || !methodNode.getName().equals("<clinit>");
    }

    public boolean isStaticConstructor() {
        return methodNode != null && methodNode.getName().equals("<clinit>");
    }

    public boolean isConstructor() {
        return constructorNode != null;
    }

    /**
     * @return true if we are in a script body, where all variables declared are no longer
     *         local variables but are properties
     */
    public boolean isInScriptBody() {
        return classNode.isScriptBody() || (classNode.isScript() && methodNode != null && methodNode.getName().equals("run"));
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
            List<ClassNode> outers = classNode.getOuterClasses();
            outermostClass = !outers.isEmpty() ? outers.get(outers.size() - 1) : classNode;
        }
        return outermostClass;
    }

    public GeneratorContext getContext() {
        return context;
    }

    public void setInterfaceClassLoadingClass(final InterfaceHelperClassNode ihc) {
        interfaceClassLoadingClass = ihc;
    }

    public InterfaceHelperClassNode getInterfaceClassLoadingClass() {
        return interfaceClassLoadingClass;
    }

    public boolean shouldOptimizeForInt() {
        return optimizeForInt;
    }

    public StatementWriter getStatementWriter() {
        return statementWriter;
    }

    public void switchToFastPath() {
        fastPath = true;
        resetLineNumber();
    }

    public void switchToSlowPath() {
        fastPath = false;
        resetLineNumber();
    }

    public boolean isFastPath() {
        return fastPath;
    }

    public int getBytecodeVersion() {
        return bytecodeVersion;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void resetLineNumber() {
        setLineNumber(-1);
    }

    public int getNextHelperMethodIndex() {
        return helperMethodIndex += 1;
    }

    public List<String> getSuperMethodNames() {
        return superMethodNames;
    }
}
