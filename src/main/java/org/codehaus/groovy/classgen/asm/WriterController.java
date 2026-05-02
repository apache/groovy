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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InterfaceHelperClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.asm.indy.IndyBinHelper;
import org.codehaus.groovy.classgen.asm.indy.IndyCallSiteWriter;
import org.codehaus.groovy.classgen.asm.indy.InvokeDynamicWriter;
import org.codehaus.groovy.classgen.asm.util.LoggableClassVisitor;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.ast.tools.ClassNodeUtils.getNestHost;
import static org.codehaus.groovy.ast.ClassHelper.isGeneratedFunction;

public class WriterController {
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
    private MethodNode methodNode;
    private ConstructorNode constructorNode;
    private GeneratorContext context;
    private InterfaceHelperClassNode interfaceClassLoadingClass;
    /**
     * Controls whether the bytecode generator should emit optimized fast-path code
     * for integer operations. When enabled, the compiler generates specialized
     * numeric handling paths for better performance with primitive integers.
     * Disabled when invokedynamic is enabled, since JIT compilation provides
     * comparable or better optimization.
     */
    public boolean optimizeForInt = true;
    private StatementWriter statementWriter;
    private boolean fastPath;
    private TypeChooser typeChooser;
    private int bytecodeVersion = CompilerConfiguration.DEFAULT.getBytecodeVersion();
    private int lineNumber = -1;
    private int helperMethodIndex = 0;
    private List<String> superMethodNames = new ArrayList<>();
    private MethodPointerExpressionWriter methodPointerExpressionWriter;
    private MethodReferenceExpressionWriter methodReferenceExpressionWriter;

    /**
     * Initializes this controller with compilation context and ASM infrastructure.
     * Must be called exactly once before any bytecode generation operations.
     * Sets up all supporting writers (call site, closure, lambda, etc.) and
     * configures optimization strategies based on compiler configuration.
     *
     * @param asmClassGenerator the class generator managing the overall compilation
     * @param gcon the compilation context tracking state across the compilation unit
     * @param cv the ASM ClassVisitor for emitting bytecode directives
     * @param cn the ClassNode being compiled
     */
    public void init(final AsmClassGenerator asmClassGenerator, final GeneratorContext gcon, final ClassVisitor cv, final ClassNode cn) {
        CompilerConfiguration config = cn.getCompileUnit().getConfig();
        Map<String,Boolean> optOptions = config.getOptimizationOptions();
        boolean invokedynamic = true;
        if (optOptions.isEmpty()) {
            // IGNORE
        } else if (Boolean.FALSE.equals(optOptions.get("all"))) {
            invokedynamic = false;
            this.optimizeForInt = false;
            // set other optimizations options to false here
        } else {
            if (!config.isIndyEnabled()) invokedynamic = false;
            if (Boolean.FALSE.equals(optOptions.get("int"))) this.optimizeForInt = false;
            // set other optimizations options to false here
        }
        if (invokedynamic) this.optimizeForInt = false;

        this.classNode = cn;
        this.internalClassName = BytecodeHelper.getClassInternalName(cn);

        this.bytecodeVersion = config.getBytecodeVersion();

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
        if (this.optimizeForInt) {
            this.fastPathBinaryExpHelper = new BinaryExpressionMultiTypeDispatcher(this);
            // TODO: replace with a real fast path unary expression helper when available
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
        this.internalBaseClassName = BytecodeHelper.getClassInternalName(cn.getSuperClass());
        this.acg = asmClassGenerator;
        this.context = gcon;
        this.compileStack = new CompileStack(this);
        this.cv = createClassVisitor(cv, config);
        if (this.optimizeForInt) {
            this.statementWriter = new OptimizingStatementWriter(this);
        } else {
            this.statementWriter = new StatementWriter(this);
        }
        this.typeChooser = new StatementMetaTypeChooser();
    }

    private static ClassVisitor createClassVisitor(final ClassVisitor cv, final CompilerConfiguration config) {
        if (!config.isLogClassgen() || cv instanceof LoggableClassVisitor) {
            return cv;
        }
        return new LoggableClassVisitor(cv, config);
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the AsmClassGenerator managing overall compilation for this class.
     * Provides access to source units, compiler configuration, and other
     * compilation-wide state.
     */
    public AsmClassGenerator getAcg() {
        return acg;
    }

    /**
     * Returns the ASM ClassVisitor for emitting class-level bytecode directives.
     *
     * @deprecated Use {@link #getClassVisitor()} instead for clarity
     */
    @Deprecated
    public ClassVisitor getCv() {
        return cv;
    }

    /**
     * Returns the ASM ClassVisitor for emitting class-level bytecode directives.
     * This is the primary interface for writing class metadata, fields, methods,
     * and attributes to the compiled bytecode.
     */
    public ClassVisitor getClassVisitor() {
        return cv;
    }

    /**
     * Returns the ASM MethodVisitor for emitting bytecode instructions within
     * the currently-active method or constructor. Null if no method is active.
     */
    public MethodVisitor getMethodVisitor() {
        return methodVisitor;
    }

    /**
     * Sets the ASM MethodVisitor for the currently-active method or constructor.
     * Called before each method body compilation to direct bytecode emission.
     *
     * @param methodVisitor the visitor for the current method, or null to deactivate
     */
    public void setMethodVisitor(final MethodVisitor methodVisitor) {
        this.methodVisitor = methodVisitor;
    }

    /**
     * Returns the GeneratorContext tracking compilation state across
     * the entire compilation unit, including helper classes and shared resources.
     */
    public GeneratorContext getContext() {
        return context;
    }

    /**
     * Returns the CompileStack managing local variable slots, scope boundaries,
     * and control flow labels during method compilation.
     */
    public CompileStack getCompileStack() {
        return compileStack;
    }

    /**
     * Returns the OperandStack managing the JVM operand stack, tracking stack
     * depth and type information for bytecode verification and optimization.
     */
    public OperandStack getOperandStack() {
        return operandStack;
    }

    /**
     * Returns the SourceUnit containing the source code being compiled.
     * Provides access to source URLs, encoding, and error reporting infrastructure.
     */
    public SourceUnit getSourceUnit() {
        return getAcg().getSourceUnit();
    }

    /**
     * Returns the TypeChooser used to select appropriate type representations
     * for expressions during compilation, supporting both dynamic and typed paths.
     */
    public TypeChooser getTypeChooser() {
        return typeChooser;
    }

    /**
     * Returns the appropriate UnaryExpressionHelper for the current code path.
     * Selects fast-path specialized handling when {@link #isFastPath()} is true,
     * otherwise delegates to the general-purpose unary expression handler.
     * Fast-path optimization is controlled by {@link #optimizeForInt}.
     *
     * @return the unary expression writer for the active compilation mode
     */
    public UnaryExpressionHelper getUnaryExpressionHelper() {
        if (fastPath) {
            return fastPathUnaryExpressionHelper;
        } else {
            return unaryExpressionHelper;
        }
    }

    /**
     * Returns the appropriate BinaryExpressionHelper for the current code path.
     * Selects fast-path specialized handling when {@link #isFastPath()} is true,
     * otherwise delegates to the general-purpose binary expression handler.
     * Fast-path optimization is controlled by {@link #optimizeForInt}.
     *
     * @return the binary expression writer for the active compilation mode
     */
    public BinaryExpressionHelper getBinaryExpressionHelper() {
        if (fastPath) {
            return fastPathBinaryExpHelper;
        } else {
            return binaryExpHelper;
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the AssertionWriter for compiling Groovy assert statements
     * into bytecode that evaluates conditions and raises AssertionError.
     */
    public AssertionWriter getAssertionWriter() {
        return assertionWriter;
    }

    /**
     * Returns the CallSiteWriter responsible for generating dynamic call site
     * infrastructure. Behavior depends on bytecode strategy: either invokedynamic
     * for modern JVMs or traditional call-site caching for broader compatibility.
     */
    public CallSiteWriter getCallSiteWriter() {
        return callSiteWriter;
    }

    /**
     * Returns the CallSiteWriter for a specific expression.
     * Currently delegates to the default CallSiteWriter; reserved for
     * future expression-specific optimization strategies.
     *
     * @param expression the expression being processed
     * @return the appropriate call site writer for this expression
     *
     * @since 6.0.0
     */
    public CallSiteWriter getCallSiteWriterFor(final Expression expression) {
        return callSiteWriter;
    }

    /**
     * Returns the ClosureWriter for compiling Groovy closure literals into
     * inner classes implementing GroovyObject and supporting variable capture.
     */
    public ClosureWriter getClosureWriter() {
        return closureWriter;
    }

    /**
     * Returns the LambdaWriter for compiling Java-style lambda expressions
     * (using the {@code ->} operator) into functional interfaces.
     */
    public LambdaWriter getLambdaWriter() {
        return lambdaWriter;
    }

    /**
     * Returns the StatementWriter for compiling Groovy statements into bytecode.
     * Selection depends on optimization mode: OptimizingStatementWriter for
     * {@link #optimizeForInt}, otherwise generic StatementWriter.
     */
    public StatementWriter getStatementWriter() {
        return statementWriter;
    }

    /**
     * Returns the InvocationWriter for compiling method calls and dynamic
     * function invocations into appropriate bytecode patterns.
     */
    public InvocationWriter getInvocationWriter() {
        return invocationWriter;
    }

    /**
     * Returns the MethodPointerExpressionWriter for compiling method pointer
     * expressions (e.g., {@code String.&length}) into method reference objects.
     */
    public MethodPointerExpressionWriter getMethodPointerExpressionWriter() {
        return methodPointerExpressionWriter;
    }

    /**
     * Returns the MethodReferenceExpressionWriter for compiling method reference
     * expressions compatible with Java functional interface targets.
     */
    public MethodReferenceExpressionWriter getMethodReferenceExpressionWriter() {
        return methodReferenceExpressionWriter;
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the internal (JVM) name of the class being compiled.
     * For interface types with a helper loading class, returns the helper class name;
     * otherwise returns the standard internal class name.
     * Format: package segments separated by '/', e.g., "java/lang/String"
     *
     * @return the fully-qualified internal class name
     */
    public String getClassName() {
        String className;
        if (!classNode.isInterface() || interfaceClassLoadingClass == null) {
            className = internalClassName;
        } else {
            className = BytecodeHelper.getClassInternalName(interfaceClassLoadingClass);
        }
        return className;
    }

    /**
     * Returns the ClassNode representing the class currently being compiled.
     * Contains the complete AST structure including fields, methods, and metadata.
     * This is immutable for the duration of the compilation process.
     *
     * @return the ClassNode for this compilation unit
     */
    public ClassNode getClassNode() {
        return classNode;
    }

    /**
     * Returns the MethodNode for the method currently being compiled, or null
     * if no method is active or a constructor is being compiled instead.
     *
     * @return the current MethodNode, or null if none is active
     */
    public MethodNode getMethodNode() {
        return methodNode;
    }

    /**
     * Sets the MethodNode for the method currently being compiled.
     * Automatically clears any active constructor node, since only one can be active.
     * Used during compilation to track which method's bytecode is being generated.
     *
     * @param methodNode the MethodNode to compile, or null to deactivate
     */
    public void setMethodNode(final MethodNode methodNode) {
        this.methodNode = methodNode;
        this.constructorNode = null;
    }

    /**
     * Returns the ConstructorNode for the constructor currently being compiled, or null
     * if no constructor is active or a method is being compiled instead.
     *
     * @return the current ConstructorNode, or null if none is active
     */
    public ConstructorNode getConstructorNode() {
        return constructorNode;
    }

    /**
     * Sets the ConstructorNode for the constructor currently being compiled.
     * Automatically clears any active method node, since only one can be active.
     * Used during compilation to track which constructor's bytecode is being generated.
     *
     * @param constructorNode the ConstructorNode to compile, or null to deactivate
     */
    public void setConstructorNode(final ConstructorNode constructorNode) {
        this.constructorNode = constructorNode;
        this.methodNode = null;
    }

    /**
     * Returns the type of the 'this' reference in the current context.
     * For regular methods, returns the enclosing class. For closures and lambdas
     * (generated function wrappers), traverses the outer class chain to find the
     * actual 'this' class, skipping intermediate generated wrapper classes.
     *
     * @return the ClassNode representing 'this' in the current context
     */
    public ClassNode getThisType() {
        ClassNode thisType = getClassNode();
        while (isGeneratedFunction(thisType)) {
            thisType = thisType.getOuterClass();
        }
        return thisType;
    }

    /**
     * Returns the declared return type of the current method or constructor.
     * The return type reflects the method signature from the source code.
     *
     * @return the return type ClassNode
     * @throws GroovyBugError if called outside method or constructor context
     */
    public ClassNode getReturnType() {
        if (methodNode != null) {
            return methodNode.getReturnType();
        } else if (constructorNode != null) {
            return constructorNode.getReturnType();
        } else {
            throw new GroovyBugError("I spotted a return that is neither in a method nor in a constructor... I can not handle that");
        }
    }

    /**
     * Returns the outermost enclosing class in the nesting hierarchy.
     * For top-level classes, returns the class itself. For nested classes,
     * traverses outward to find the top-level class.
     *
     * @return the outermost ClassNode in the nest
     */
    public ClassNode getOutermostClass() {
        return getNestHost(classNode);
    }

    /**
     * Returns the internal (JVM) name of the class being compiled.
     * The internal name format uses '/' as package separator, e.g., "org/groovy/MyClass".
     * This corresponds to the formal name used in bytecode class descriptors.
     *
     * @return the fully-qualified internal class name
     */
    public String getInternalClassName() {
        return internalClassName;
    }

    /**
     * Returns the internal (JVM) name of the immediate superclass.
     * The internal name format uses '/' as package separator, e.g., "java/lang/Object".
     * Typically "java/lang/Object" for classes without explicit superclass,
     * or the superclass name for classes with explicit inheritance.
     *
     * @return the fully-qualified internal name of the parent class
     */
    public String getInternalBaseClassName() {
        return internalBaseClassName;
    }

    /**
     * Returns the list of method names defined by the superclass.
     * Used during method resolution to detect method overrides, generate
     * appropriate method-missing handlers, and verify method signature compatibility.
     * The list is mutable; modifications affect subsequent resolution queries.
     *
     * @return a list of superclass method names
     */
    public List<String> getSuperMethodNames() {
        return superMethodNames;
    }

    /**
     * Returns the InterfaceHelperClassNode used for interface static method loading,
     * or null if the compiled class is not an interface or needs no helper class.
     * Interface helper classes bridge the gap between interface static methods
     * and their actual implementation in Java 8+ runtime environments.
     *
     * @return the helper class node, or null if not applicable
     */
    public InterfaceHelperClassNode getInterfaceClassLoadingClass() {
        return interfaceClassLoadingClass;
    }

    /**
     * Sets the InterfaceHelperClassNode for interface static method loading.
     * Called during interface compilation to register the synthesized helper class
     * that provides access to static interface methods in compatible runtimes.
     * The helper class allows proper method lookup and invocation for interface statics.
     *
     * @param ihc the helper class node, or null to deactivate
     */
    public void setInterfaceClassLoadingClass(final InterfaceHelperClassNode ihc) {
        interfaceClassLoadingClass = ihc;
    }

    /**
     * Determines whether the current execution context is statically scoped.
     * Returns true when compiling inside a static method or static class initializer.
     * For instance constructors, checks whether we're in a special constructor call
     * (e.g., super or this call) which requires static semantics.
     *
     * @return true if executing in static context, false for instance context
     * @throws IllegalStateException if called outside valid compilation scope
     */
    public boolean isStaticContext() {
        if (isConstructor()) { // GROOVY-11483
            return compileStack.isInSpecialConstructorCall();
        }
        if (compileStack.getScope() != null) {
            return compileStack.getScope().isInStaticContext();
        }
        throw new IllegalStateException("out-of-scope static check");
    }

    /**
     * Returns true if the current method is explicitly declared as static.
     * Returns false if no method is active or the method is instance-scoped.
     *
     * @return true if the current method has the static modifier
     */
    public boolean isStaticMethod() {
        return methodNode != null && methodNode.isStatic();
    }

    /**
     * Returns true if the current method is NOT a static class initializer (&lt;clinit&gt;).
     * Equivalent to: no method is active OR the method is not a static constructor.
     * Used to guard code that should not execute in class initialization.
     *
     * @return true if not in static initializer context
     */
    public boolean isNotClinit() {
        return methodNode == null || !methodNode.isStaticConstructor();
    }

    /**
     * Returns true if the current method is a static class initializer (&lt;clinit&gt;).
     * The static initializer runs once when the class is loaded by the JVM.
     * Returns false if no method is active or the method is not static.
     *
     * @return true if the current method is the static class initializer
     */
    public boolean isStaticConstructor() {
        return methodNode != null && methodNode.isStaticConstructor();
    }

    /**
     * Returns true if a constructor is currently being compiled.
     * False if a regular instance or static method is active, or if no method is active.
     *
     * @return true if in constructor compilation context
     */
    public boolean isConstructor() {
        return constructorNode != null;
    }

    /**
     * Returns true if the class being compiled is a generated function wrapper
     * (e.g., an inner class synthesized for a closure or lambda expression).
     * Generated functions have an outer class and are marked as generated.
     *
     * @return true if compiling a closure or lambda's wrapper class
     */
    public boolean isInGeneratedFunction() {
        return classNode.getOuterClass() != null && isGeneratedFunction(classNode);
    }

    /**
     * Returns true if compiling a constructor within a generated function class.
     * Combines two checks: that we're in constructor context AND in a generated function.
     * This condition indicates we're initializing a closure or lambda wrapper.
     *
     * @return true if in a generated function's constructor
     */
    public boolean isInGeneratedFunctionConstructor() {
        return isConstructor() && isInGeneratedFunction();
    }

    /**
     * @return true if we are in a script body, where all variables declared are no longer
     *         local variables but are properties
     */
    public boolean isInScriptBody() {
        return classNode.isScriptBody() || (methodNode != null && methodNode.isScriptBody());
    }

    /**
     * Returns true if the bytecode generator should emit optimized fast-path code
     * for integer operations. Corresponds to the {@link #optimizeForInt} configuration
     * which is disabled when invokedynamic is enabled.
     *
     * @return true if integer-optimized fast-path code generation is enabled
     */
    public boolean shouldOptimizeForInt() {
        return optimizeForInt;
    }

    /**
     * Switches compilation to fast-path mode for specialized numeric handling.
     * In fast-path mode, the compiler emits optimized bytecode for common operations
     * on primitive integer types, bypassing dynamic method invocation overhead.
     * Also resets line number tracking to allow proper debug attribute synchronization.
     */
    public void switchToFastPath() {
        fastPath = true;
        resetLineNumber();
    }

    /**
     * Switches compilation to slow-path mode, reverting to general-purpose bytecode generation.
     * Slow-path mode uses dynamic method dispatch for all operations, suitable when
     * type information is insufficient for safe optimization or type specialization
     * is not beneficial. Also resets line number tracking.
     */
    public void switchToSlowPath() {
        fastPath = false;
        resetLineNumber();
    }

    /**
     * Returns true if the compiler is currently in fast-path mode,
     * emitting specialized bytecode for primitive type operations.
     * Use this to determine whether expression helpers should apply optimizations.
     *
     * @return true if fast-path compilation mode is active
     */
    public boolean isFastPath() {
        return fastPath;
    }

    /**
     * Returns the source line number currently being compiled.
     * Line numbers are tracked for debug attribute generation in the bytecode,
     * allowing debuggers and profilers to map bytecode instructions to source lines.
     * Returns -1 when no valid line number is active.
     *
     * @return the current source line number, or -1 if invalid/not set
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Resets the tracked line number to -1 (invalid).
     * Called when switching compilation modes or after line number emission
     * to avoid stale line number information in subsequent bytecode.
     */
    public void resetLineNumber() {
        setLineNumber(-1);
    }

    /**
     * Updates the current source line number being compiled.
     * The line number is used for debug information (LineNumberTable attribute)
     * and must be set before emitting bytecode for the corresponding source line.
     *
     * @param lineNumber the new source line number, or -1 to reset
     */
    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Emits a line number bytecode directive if the provided line number differs
     * from the currently-tracked line number and is positive (valid).
     * Creates an ASM label at the current instruction position and records the
     * line number mapping, enabling debuggers to correlate bytecode with source.
     * Only emits if a MethodVisitor is currently active.
     *
     * @param lineNumber the source line number to emit; must be positive
     */
    public void visitLineNumber(final int lineNumber) {
        if (lineNumber > 0 && lineNumber != this.lineNumber) {
            setLineNumber(lineNumber);

            MethodVisitor mv = getMethodVisitor();
            if (mv != null) {
                Label label = new Label();
                mv.visitLabel(label);
                mv.visitLineNumber(lineNumber, label);
            }
        }
    }

    /**
     * Returns the bytecode version (target JVM version) for classes being compiled.
     * Examples: V1_8 (Java 8), V11 (Java 11), V17 (Java 17), etc.
     * Determined from compiler configuration and affects class file format
     * and available bytecode features in generated code.
     *
     * @return the target bytecode version constant from ASM
     */
    public int getBytecodeVersion() {
        return bytecodeVersion;
    }

    /**
     * Returns the next sequential index for an internally-generated helper method.
     * Each invocation increments the counter, ensuring unique names for synthesized methods.
     * Used to generate unique helper method names for method references, closure bridges,
     * type adapters, and other compiler-generated synthetic methods.
     * Helper method names typically follow the pattern: {@code $static$<index>} or similar.
     *
     * @return a unique positive helper method index, auto-incremented
     */
    public int getNextHelperMethodIndex() {
        return helperMethodIndex += 1;
    }
}
