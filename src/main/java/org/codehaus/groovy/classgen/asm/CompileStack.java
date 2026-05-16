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
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NOP;
import static org.objectweb.asm.Opcodes.SWAP;

/**
 * Manages different aspects of the code of a code block like handling labels,
 * defining variables, and scopes. After a MethodNode is visited clear should be
 * called, for initialization the method init should be used.
 * <p>
 * Some Notes:
 * <ul>
 * <li> every push method will require a later pop call
 * <li> method parameters may define a category 2 variable, so
 *      don't ignore the type stored in the variable object
 * <li> the index of the variable may not be as assumed when
 *      the variable is a parameter of a method because the
 *      parameter may be used in a closure, so don't ignore
 *      the stored variable index
 * <li> the names of temporary variables can be ignored. The names
 *      are only used for debugging and do not conflict with each
 *      other or normal variables. For accessing, the index of the
 *      variable must be used.
 * <li> never mix temporary and normal variables by changes to this class.
 *      While the name is very important for a normal variable, it is only a
 *      helper construct for temporary variables. That means for example a
 *      name for a temporary variable can be used multiple times without
 *      conflict. So mixing them both may lead to the problem that a normal
 *      or temporary variable is hidden or even removed.  That must not happen!
 * </ul>
 *
 * @see org.codehaus.groovy.classgen.AsmClassGenerator
 */
public class CompileStack {
    // TODO: remove optimization of this.foo -> this.@foo

    /** state flag */
    private boolean clear = true;
    /** current scope */
    private VariableScope scope;
    /** current label for continue */
    private Label continueLabel;
    /** current label for break */
    private Label breakLabel;
    /** available variables on stack */
    private Map<String, BytecodeVariable> stackVariables = new HashMap<>();
    /** index of the last variable on stack */
    private int currentVariableIndex = 1;
    /** index for the next variable on stack */
    private int nextVariableIndex = 1;
    /** currently temporary variables in use */
    private final Deque<BytecodeVariable> temporaryVariables = new LinkedList<>();
    /** overall used variables for a method/constructor */
    private final Deque<BytecodeVariable> usedVariables = new LinkedList<>();
    /** map containing named labels of parenting blocks */
    private Map<String, Label> superBlockNamedLabels = new HashMap<>();
    /** map containing named labels of current block */
    private Map<String, Label> currentBlockNamedLabels = new HashMap<>();
    /**
     * list containing finally blocks
     * <p>
     * such a block is created by synchronized or finally and
     * must be called for break/continue/return
     */
    private LinkedList<BlockRecorder> finallyBlocks = new LinkedList<>();
    private final List<BlockRecorder> visitedBlocks = new LinkedList<>();

    /** helper to handle different stack based variables */
    private final Deque<StateStackElement> stateStack = new LinkedList<>();

    /** handle different states for the implicit "this" */
    private final Deque<Boolean> implicitThisStack = new LinkedList<>();
    /** handle different states for being on the left hand side */
    private final Deque<Boolean> lhsStack = new LinkedList<>();
    {
        implicitThisStack.add(Boolean.FALSE);
        lhsStack.add(Boolean.FALSE);
    }

    private String className;
    /** first variable index usable after all parameters of a method */
    private int localVariableOffset;

    private Label thisStartLabel, thisEndLabel;
    /** goals for a "break foo" call in a loop where foo is a label. */
    private final Map<String, Label> namedLoopBreakLabel = new HashMap<>();
    /** goals for a "continue foo" call in a loop where foo is a label. */
    private final Map<String, Label> namedLoopContinueLabel = new HashMap<>();
    private final Deque<ExceptionTableEntry> typedExceptions = new LinkedList<>();
    private final Deque<ExceptionTableEntry> untypedExceptions = new LinkedList<>();
    /** stores if on left-hand-side during compilation */
    private boolean lhs;
    /** stores if implicit or explicit this is used. */
    private boolean implicitThis;
    private boolean inSpecialConstructorCall;

    private final WriterController controller;

    /**
     * Represents a label range for exception handling or other scoping.
     */
    protected static class LabelRange {
        /** Start label of the recorded range. */
        public Label start;
        /** End label of the recorded range. */
        public Label end;
    }

    /**
     * Records a block for finally or other special handling.
     */
    public static class BlockRecorder {
        private boolean isEmpty = true;
        /** Callback used when visiting the block outside its recorded range. */
        public Runnable excludedStatement;
        /** Label ranges covered by this recorder. */
        public final LinkedList<LabelRange> ranges = new LinkedList<>();

        /**
         * Creates an empty block recorder.
         */
        public BlockRecorder() {
        }

        /**
         * Creates a block recorder with an excluded statement.
         *
         * @param excludedStatement the statement to exclude from the block
         */
        public BlockRecorder(final Runnable excludedStatement) {
            this.excludedStatement = excludedStatement;
        }

        /**
         * Starts a new range at the given label.
         *
         * @param start the start label
         */
        public void startRange(final Label start) {
            LabelRange range = new LabelRange();
            range.start = start;
            ranges.add(range);
            isEmpty = false;
        }

        /**
         * Closes the current range at the given label.
         *
         * @param end the end label
         */
        public void closeRange(final Label end) {
            ranges.getLast().end = end;
        }
    }

    private static class ExceptionTableEntry {
        /** Start label of the protected range. */
        Label start, end, goal;
        /** Internal name of the caught exception type, or {@code null} for catch-all. */
        String sig;
    }

    private class StateStackElement {
        /** Variable scope active when the state was pushed. */
        final VariableScope scope;
        /** Continue label active when the state was pushed. */
        final Label continueLabel;
        /** Break label active when the state was pushed. */
        final Label breakLabel;
        /** Stack variables visible when the state was pushed. */
        final Map<String, BytecodeVariable> stackVariables;
        /** Named labels for the current block when the state was pushed. */
        final Map<String, Label> currentBlockNamedLabels;
        /** Finally-block recorders active when the state was pushed. */
        final LinkedList<BlockRecorder> finallyBlocks;
        /** Whether the saved state is inside a special constructor call. */
        final boolean inSpecialConstructorCall;

        /** Captures the current compile-stack state. */
        StateStackElement() {
            scope = CompileStack.this.scope;
            continueLabel = CompileStack.this.continueLabel;
            breakLabel = CompileStack.this.breakLabel;
            stackVariables = CompileStack.this.stackVariables;
            currentBlockNamedLabels = CompileStack.this.currentBlockNamedLabels;
            finallyBlocks = CompileStack.this.finallyBlocks;
            inSpecialConstructorCall = CompileStack.this.inSpecialConstructorCall;
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Creates a CompileStack managed by the given controller.
     *
     * @param controller the writer controller for the current compilation
     */
    public CompileStack(final WriterController controller) {
        this.controller = controller;
    }

    /** Returns the current break label, or {@code null} if not inside a breakable construct. */
    public Label getBreakLabel() {
        return breakLabel;
    }

    /** Returns the current continue label, or {@code null} if not inside a loop. */
    public Label getContinueLabel() {
        return continueLabel;
    }

    /** Returns the current variable scope. */
    public VariableScope getScope() {
        return scope;
    }

    /**
     * Saves the current compilation state onto an internal stack so that it can
     * be restored by a later call to {@link #pop()}. Every {@code push*} method
     * calls this internally; it is also exposed for callers that need a bare
     * state snapshot without additional label setup.
     */
    public void pushState() {
        stateStack.add(new StateStackElement());
        stackVariables = new HashMap<>(stackVariables);
        finallyBlocks = new LinkedList<>(finallyBlocks);
    }

    private void popState() {
        if (stateStack.isEmpty()) {
            throw new GroovyBugError("Tried to do a pop on the compile stack without push.");
        }
        StateStackElement element = stateStack.removeLast();
        scope = element.scope;
        breakLabel = element.breakLabel;
        continueLabel = element.continueLabel;
        stackVariables = element.stackVariables;
        finallyBlocks = element.finallyBlocks;
        inSpecialConstructorCall = element.inSpecialConstructorCall;
    }

    /**
     * Indicates that the specified temporary variable is no longer used.
     */
    public void removeVar(final int variableIndex) {
        BytecodeVariable head = temporaryVariables.removeFirst();
        if (head.getIndex() != variableIndex) {
            temporaryVariables.addFirst(head);
            MethodNode methodNode = controller.getMethodNode();
            if (methodNode == null) {
                methodNode = controller.getConstructorNode();
            }
            throw new GroovyBugError(
                    "In method "+ (methodNode!=null?methodNode.getText():"<unknown>") + ", " +
                    "CompileStack#removeVar: tried to remove a temporary " +
                    "variable with index " + variableIndex + " in wrong order. " +
                    "Current temporary variables=" + temporaryVariables);
        }
    }

    private void setEndLabels() {
        Label endLabel = new Label();
        controller.getMethodVisitor().visitLabel(endLabel);
        for (BytecodeVariable var : stackVariables.values()) {
            var.setEndLabel(endLabel);
        }
        thisEndLabel = endLabel;
    }

    /**
     * Restores the compilation state that was saved by the matching {@code push*} call,
     * and emits end-labels for all local variables declared within the scope that
     * is being popped.
     */
    public void pop() {
        setEndLabels();
        popState();
    }

    /**
     * Creates a temporary variable.
     *
     * @param var specifies name and type
     * @param store defines if the toplevel argument of the stack should be stored
     * @return the index used for this temporary variable
     */
    public int defineTemporaryVariable(final Variable var, final boolean store) {
        return defineTemporaryVariable(var.getName(), var.getType(), store);
    }

    /**
     * Returns a variable by name, throwing a {@link org.codehaus.groovy.GroovyBugError}
     * if it does not exist. Convenience overload of
     * {@link #getVariable(String, boolean)} with {@code mustExist = true}.
     *
     * @param variableName the name to look up
     * @return the corresponding {@link BytecodeVariable}
     */
    public BytecodeVariable getVariable(final String variableName) {
        return getVariable(variableName, true);
    }

    /**
     * Returns a normal variable.
     * <p>
     * If <code>mustExist</code> is true and the normal variable doesn't exist,
     * then this method will throw a GroovyBugError. It is not the intention of
     * this method to let this happen! And the exception should not be used for
     * flow control - it is just acting as an assertion. If the exception is thrown
     * then it indicates a bug in the class using CompileStack.
     * This method can also not be used to return a temporary variable.
     * Temporary variables are not normal variables.
     *
     * @param variableName name of the variable
     * @param mustExist    throw exception if variable does not exist
     * @return the normal variable or null if not found (and <code>mustExist</code> not true)
     */
    public BytecodeVariable getVariable(final String variableName, final boolean mustExist) {
        if ("this".equals(variableName)) return BytecodeVariable.THIS_VARIABLE;
        if ("super".equals(variableName)) return BytecodeVariable.SUPER_VARIABLE;
        BytecodeVariable v = stackVariables.get(variableName);
        if (v == null && mustExist)
            throw new GroovyBugError("tried to get a variable with the name " + variableName + " as stack variable, but a variable with this name was not created");
        return v;
    }

    /**
     * Creates a temporary variable.
     *
     * @param name defines type and name
     * @param store defines if the top-level argument of the stack should be stored
     * @return the index used for this temporary variable
     */
    public int defineTemporaryVariable(final String name, final boolean store) {
        return defineTemporaryVariable(name, ClassHelper.dynamicType(), store);
    }

    /**
     * Creates a temporary variable.
     *
     * @param name the variable name
     * @param type the variable type
     * @param store indicates if the top-level argument of the stack should be stored
     * @return the index used for this temporary variable
     */
    public int defineTemporaryVariable(final String name, final ClassNode type, final boolean store) {
        BytecodeVariable answer = defineVar(name, type, false, false);
        temporaryVariables.addFirst(answer); // TRICK: we add at the beginning so when we find for remove or get we always have the last one
        usedVariables.removeLast();

        if (store) controller.getOperandStack().storeVar(answer);

        return answer.getIndex();
    }

    private void resetVariableIndex(final boolean isStatic) {
        temporaryVariables.clear();
        if (!isStatic) {
            currentVariableIndex = 1;
            nextVariableIndex = 1;
        } else {
            currentVariableIndex = 0;
            nextVariableIndex = 0;
        }
    }

    /**
     * Clears the state of the class. This method should be called
     * after a MethodNode is visited. Note that a call to init will
     * fail if clear is not called before
     */
    public void clear() {
        if (stateStack.size() > 1) {
            int size = stateStack.size() - 1;
            throw new GroovyBugError("state stack contains " + size + " more push instruction" + (size == 1 ? "" : "s") + " than pops.");
        }
        if (lhsStack.size() > 1) {
            int size = lhsStack.size() - 1;
            throw new GroovyBugError("lhs stack is supposed to be empty, but has " + size + " element" + (size == 1 ? "" : "s") + " left.");
        }
        if (implicitThisStack.size() > 1) {
            int size = implicitThisStack.size() - 1;
            throw new GroovyBugError("implicit 'this' stack is supposed to be empty, but has " + size + " element" + (size == 1 ? "" : "s") + " left.");
        }
        clear = true;
        MethodVisitor mv = controller.getMethodVisitor();
        if (AsmClassGenerator.CREATE_DEBUG_INFO) {
            if (thisEndLabel == null) setEndLabels();

            if (!scope.isInStaticContext()) {
                mv.visitLocalVariable("this", className, null, thisStartLabel, thisEndLabel, 0);
            }
            for (BytecodeVariable v : usedVariables) {
                String type = BytecodeHelper.getTypeDescription(v.isHolder() ? ClassHelper.REFERENCE_TYPE : v.getType());
                Label startLabel = v.getStartLabel(), endLabel = v.getEndLabel();
                if (endLabel == null) endLabel = startLabel; // only occurs for '_' placeholder
                mv.visitLocalVariable(v.getName(), type, null, startLabel, endLabel, v.getIndex());
                // JSR 308: local variable type annotations
                ClassNode t = v.getType();
                String typePath = ""; // ?
                do {
                    for (AnnotationNode a : t.getTypeAnnotations()) {
                        type = BytecodeHelper.getTypeDescription(a.getClassNode());
                        var av = mv.visitLocalVariableAnnotation(TypeReference.LOCAL_VARIABLE << 24, TypePath.fromString(typePath),
                            new Label[]{startLabel}, new Label[]{endLabel}, new int[]{v.getIndex()}, type, a.hasRuntimeRetention());
                        if (av != null) {
                            controller.getAcg().visitAnnotationAttributes(a, av);
                            av.visitEnd();
                        }
                    }
                    typePath += "[";
                    t = t.getComponentType();
                } while (t != null); // array
            }
        }

        // exception table writing
        for (ExceptionTableEntry ep : typedExceptions) {
            mv.visitTryCatchBlock(ep.start, ep.end, ep.goal, ep.sig);
        }
        for (ExceptionTableEntry ep : untypedExceptions) {
            mv.visitTryCatchBlock(ep.start, ep.end, ep.goal, ep.sig);
        }

        popState();
        typedExceptions.clear();
        untypedExceptions.clear();
        stackVariables.clear();
        usedVariables.clear();
        finallyBlocks.clear();
        resetVariableIndex(false);
        superBlockNamedLabels.clear();
        currentBlockNamedLabels.clear();
        namedLoopBreakLabel.clear();
        namedLoopContinueLabel.clear();
        breakLabel = null;
        continueLabel = null;
        thisStartLabel = null;
        thisEndLabel = null;
        className = null;
        scope = null;
    }

    /**
     * Adds a typed or untyped exception handler to the exception table for
     * the current method. Typed entries (non-null {@code sig}) are emitted
     * before untyped ones to ensure correct handler ordering in the bytecode.
     *
     * @param start the start label of the guarded range
     * @param end   the end label of the guarded range
     * @param goal  the label of the handler block
     * @param sig   the internal name of the caught exception type, or {@code null}
     *              for a catch-all handler
     */
    public void addExceptionBlock(final Label start, final Label end, final Label goal, final String sig) {
        // this code is in an extra method to avoid
        // lazy initialization issues
        ExceptionTableEntry ep = new ExceptionTableEntry();
        ep.start = start;
        ep.end = end;
        ep.sig = sig;
        ep.goal = goal;
        if (sig == null) {
            untypedExceptions.add(ep);
        } else {
            typedExceptions.add(ep);
        }
    }

    /**
     * initializes this class for a MethodNode. This method will
     * automatically define variables for the method parameters
     * and will create references if needed.  The created variables
     * can be accessed by calling getVariable().
     *
     */
    public void init(final VariableScope scope, final Parameter[] parameters) {
        if (!clear) throw new GroovyBugError("CompileStack#init called without calling clear before");
        clear = false;
        pushVariableScope(scope);
        defineMethodVariables(parameters, scope.isInStaticContext());
        this.className = BytecodeHelper.getTypeDescription(controller.getClassNode());
    }

    /**
     * Causes the state-stack to add an element and sets
     * the given scope as new current variable scope. Creates
     * an element for the state stack so pop has to be called later
     */
    public void pushVariableScope(final VariableScope scope) {
        pushState();
        this.scope = scope;
        superBlockNamedLabels = new HashMap<>(superBlockNamedLabels);
        superBlockNamedLabels.putAll(currentBlockNamedLabels);
        currentBlockNamedLabels = new HashMap<>();
    }

    /**
     * Should be called when descending into a loop that defines
     * also a scope. Calls pushVariableScope and prepares labels
     * for a loop structure. Creates an element for the state stack
     * so pop has to be called later
     */
    public void pushLoop(final VariableScope scope, final List<String> labelNames) {
        pushVariableScope(scope);
        continueLabel = new Label();
        breakLabel = new Label();
        if (labelNames != null) {
            for (String labelName: labelNames) {
                if (labelName == null) continue;
                namedLoopBreakLabel.put(labelName, breakLabel);
                namedLoopContinueLabel.put(labelName, continueLabel);
            }
        }
    }

    /**
     * Should be called when descending into a loop that defines
     * also a scope. Calls pushVariableScope and prepares labels
     * for a loop structure. Creates a element for the state stack
     * so pop has to be called later.
     */
    @Deprecated(since = "6.0.0")
    public void pushLoop(final VariableScope scope, final String labelName) {
        pushLoop(scope, Collections.singletonList(labelName));
    }

    /**
     * Should be called when descending into a loop that does not define a scope
     * Creates a element for the state stack so pop has to be called later.
     */
    public void pushLoop(final List<String> labelNames) {
        pushState();
        continueLabel = new Label();
        breakLabel = new Label();
        if (labelNames != null) {
            for (String labelName: labelNames) {
                if (labelName == null) continue;
                namedLoopBreakLabel.put(labelName, breakLabel);
                namedLoopContinueLabel.put(labelName, continueLabel);
            }
        }
    }

    /**
     * Should be called when descending into a loop that does not define a scope.
     * Creates a element for the state stack so pop has to be called later.
     */
    @Deprecated(since = "6.0.0")
    public void pushLoop(final String labelName) {
        pushLoop(Collections.singletonList(labelName));
    }

    /**
     * Creates a new break label and an element for the state stack so pop has
     * to be called later.
     *
     * @return the break label
     */
    public Label pushSwitch() {
        pushState();
        breakLabel = new Label();
        return breakLabel;
    }

    /**
     * Creates a new break label and an element for the state stack so pop has
     * to be called later.
     *
     * @return the break label
     *
     * @since 6.0.0
     */
    public Label pushBreakable(final List<String> labelNames) {
        pushState();
        Label namedBreakLabel = new Label();
        if (labelNames != null) {
            for (String labelName: labelNames) {
                if (labelName == null) continue;
                namedLoopBreakLabel.put(labelName, namedBreakLabel);
            }
        }
        return namedBreakLabel;
    }

    /**
     * @return the {@code break} label for name
     */
    public Label getNamedBreakLabel(final String name) {
        Label label;
        if (name == null) {
            label = getBreakLabel();
            if (label == null) throw new GroovyBugError("cannot break");
        } else {
            label = namedLoopBreakLabel.get(name);
            if (label == null) throw new GroovyBugError("undefined break label '" + name + "'");
        }
        return label;
    }

    /**
     * @return the {@code continue} label for name
     */
    public Label getNamedContinueLabel(final String name) {
        Label label;
        if (name == null) {
            label = getContinueLabel();
            if (label == null) throw new GroovyBugError("cannot continue");
        } else {
            label = namedLoopContinueLabel.get(name);
            if (label == null) {
                label = superBlockNamedLabels.get(name);
                if (label == null) throw new GroovyBugError("undefined continue label '" + name + "'");
            }
        }
        return label;
    }

    /**
     * Returns the label for the given name.
     */
    public Label getLabel(final String name) {
        if (name == null) return null;
        var label = superBlockNamedLabels.get(name);
        if (label == null) {
            label = createLocalLabel(name);
        }
        return label;
    }

    /**
     * Creates or returns the label for the given name.
     */
    public Label createLocalLabel(final String name) {
        if (name == null) return null;
        return currentBlockNamedLabels.computeIfAbsent(name, k -> new Label());
    }

    /**
     * Because a boolean Expression may not be evaluated completely
     * it is important to keep the registers clean.
     *
     * @deprecated Use {@link #pushState()} directly.
     */
    @Deprecated(since = "5.0.0")
    public void pushBooleanExpression() {
        pushState();
    }

    private BytecodeVariable defineVar(final String name, final ClassNode type, final boolean holder, final boolean useReferenceDirectly) {
        int prevCurrent = currentVariableIndex;
        makeNextVariableID(type, useReferenceDirectly);
        int index = currentVariableIndex;
        if (holder && !useReferenceDirectly) index = localVariableOffset++;
        BytecodeVariable answer = new BytecodeVariable(index, type, name, prevCurrent);
        usedVariables.add(answer);
        answer.setHolder(holder);
        return answer;
    }

    private void makeLocalVariablesOffset(final Parameter[] paras, final boolean isInStaticContext) {
        resetVariableIndex(isInStaticContext);

        for (Parameter para : paras) {
            makeNextVariableID(para.getType(), false);
        }
        localVariableOffset = nextVariableIndex;

        resetVariableIndex(isInStaticContext);
    }

    private void defineMethodVariables(final Parameter[] params, final boolean isInStaticContext) {
        Label startLabel  = new Label();
        thisStartLabel = startLabel;
        controller.getMethodVisitor().visitLabel(startLabel);

        makeLocalVariablesOffset(params,isInStaticContext);

        for (Parameter param : params) {
            String name = param.getName();
            BytecodeVariable answer;
            ClassNode type = param.getType();
            if (param.isClosureSharedVariable()) {
                boolean useExistingReference = param.getNodeMetaData(ClosureWriter.UseExistingReference.class) != null;
                answer = defineVar(name, param.getOriginType(), true, useExistingReference);
                answer.setStartLabel(startLabel);
                if (!useExistingReference) {
                    controller.getOperandStack().load(type, currentVariableIndex);
                    controller.getOperandStack().box();

                    // GROOVY-4237: the original variable should always appear
                    // in the variable index, otherwise some programs get into
                    // trouble; so define a dummy variable for the packaging
                    // phase and let it end right away before the normal
                    // reference will be used
                    Label newStart = new Label();
                    controller.getMethodVisitor().visitLabel(newStart);
                    BytecodeVariable var = new BytecodeVariable(currentVariableIndex, param.getOriginType(), name, currentVariableIndex);
                    var.setStartLabel(startLabel);
                    var.setEndLabel(newStart);
                    usedVariables.add(var);
                    answer.setStartLabel(newStart);

                    createReference(answer);
                }
            } else {
                answer = defineVar(name, type, false, false);
                answer.setStartLabel(startLabel);
            }
            stackVariables.put(name, answer);
        }

        nextVariableIndex = localVariableOffset;
    }

    /**
     * Wraps the current stack value in a {@code groovy.lang.Reference} for the given variable slot.
     *
     * @param reference the variable that will receive the created reference
     */
    void createReference(final BytecodeVariable reference) {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitTypeInsn(NEW, "groovy/lang/Reference");
        mv.visitInsn(DUP_X1);
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKESPECIAL, "groovy/lang/Reference", "<init>", "(Ljava/lang/Object;)V", false);
        mv.visitVarInsn(ASTORE, reference.getIndex());
    }

    /**
     * Pushes the JVM default initial value for the supplied type.
     *
     * @param type the variable type to initialize
     * @param mv the method visitor receiving the bytecode
     */
    static void pushInitValue(final ClassNode type, final MethodVisitor mv) {
        if (ClassHelper.isPrimitiveDouble(type)) {
            mv.visitInsn(DCONST_0);
        } else if (ClassHelper.isPrimitiveFloat(type)) {
            mv.visitInsn(FCONST_0);
        } else if (ClassHelper.isPrimitiveLong(type)) {
            mv.visitInsn(LCONST_0);
        } else if (ClassHelper.isPrimitiveType(type)) {
            mv.visitInsn(ICONST_0);
        } else {
            mv.visitInsn(ACONST_NULL);
        }
    }

    /**
     * Defines a new Variable using an AST variable.
     * @param initFromStack if true the last element of the
     *                      stack will be used to initialize
     *                      the new variable. If false null
     *                      will be used.
     */
    public BytecodeVariable defineVariable(final Variable v, final boolean initFromStack) {
        return defineVariable(v, v.getOriginType(), initFromStack);
    }

    /**
     * Defines a variable for the given AST variable using an explicitly specified
     * bytecode type (which may differ from the declared type, e.g. when widening
     * for closure-shared variables).
     *
     * @param v             the AST variable to define
     * @param variableType  the bytecode type to assign to the slot
     * @param initFromStack if {@code true} the top of the operand stack is stored
     *                      into the new slot; otherwise a default/null value is used
     * @return the created {@link BytecodeVariable}
     */
    public BytecodeVariable defineVariable(final Variable v, final ClassNode variableType, final boolean initFromStack) {
        String name = v.getName();
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        BytecodeVariable answer = defineVar(name, variableType, v.isClosureSharedVariable(), v.isClosureSharedVariable());
        BytecodeVariable before = stackVariables.put(name, answer);
        Label startLabel = new Label();
        answer.setStartLabel(startLabel);
        if (before != null) {//GROOVY-10303
            before.setEndLabel(startLabel);
        }

        ClassNode type = answer.getType();
        if (!initFromStack) {
            if (ClassHelper.isPrimitiveType(v.getOriginType()) && ClassHelper.getWrapper(v.getOriginType()).equals(variableType)) {
                pushInitValue(v.getOriginType(), mv);
                operandStack.push(v.getOriginType());
                operandStack.box();
                operandStack.remove(1);
            } else {
                pushInitValue(type.redirect(), mv);
            }
        }
        operandStack.push(type);
        if (answer.isHolder()) {
            operandStack.box();
            operandStack.remove(1);
            createReference(answer);
        } else {
            operandStack.storeVar(answer);
        }

        mv.visitLabel(startLabel);
        return answer;
    }

    /**
     * @param name the name of the variable of interest
     * @return true if a variable is already defined
     */
    public boolean containsVariable(final String name) {
        return stackVariables.containsKey(name);
    }

    /**
     * Calculates the index of the next free register stores it
     * and sets the current variable index to the old value
     */
    private void makeNextVariableID(final ClassNode type, final boolean useReferenceDirectly) {
        currentVariableIndex = nextVariableIndex;
        if (!useReferenceDirectly && (ClassHelper.isPrimitiveLong(type) || ClassHelper.isPrimitiveDouble(type))) {
            nextVariableIndex += 1;
        }
        nextVariableIndex += 1;
    }

    /**
     * Applies any pending finally blocks on the path to {@code label}.
     * Walks the state stack to determine which finally blocks are between the
     * current position and the target label and inlines their bytecode.
     *
     * @param label        the target label (break or continue destination)
     * @param isBreakLabel {@code true} for a break label, {@code false} for continue
     */
    public void applyFinallyBlocks(final Label label, final boolean isBreakLabel) {
        StateStackElement before = null;
        search: {
            StateStackElement sse = null;
            for (StateStackElement element : stateStack) {
                if ((isBreakLabel ? element.breakLabel : element.continueLabel) == label
                        || element.currentBlockNamedLabels.containsValue(label)) {
                    before = sse; // the state before label was declared
                    break search;
                }
                sse = element;
            }
            if (isBreakLabel) {
                String name = null; // try to recover the break label's name
                for (Map.Entry<String,Label> e : namedLoopBreakLabel.entrySet()) {
                    if (e.getValue() == label) {
                        name = e.getKey();
                        break;
                    }
                }
                for (var it = stateStack.descendingIterator(); it.hasNext(); ) {
                    if (it.next().currentBlockNamedLabels.containsKey(name)) {
                        before = it.hasNext() ? it.next() : null;
                        break;
                    }
                }
            }
        }

        var blockRecorders = new LinkedList<>(finallyBlocks);
        if (before != null) {
            blockRecorders.removeAll(before.finallyBlocks);
        }
        applyBlockRecorder(blockRecorders);
    }

    private void applyBlockRecorder(final Collection<BlockRecorder> blockRecorders) {
        if (blockRecorders.isEmpty() || blockRecorders.size() == visitedBlocks.size()) return;

        MethodVisitor mv = controller.getMethodVisitor();
        Label start = new Label();

        for (BlockRecorder recorder : blockRecorders) {
            if (visitedBlocks.contains(recorder)) continue;

            Label end = new Label();
            mv.visitInsn(NOP);
            mv.visitLabel(end);

            recorder.closeRange(end);

            // we exclude the finally block from the exception table
            // here to avoid double visiting of finally statements
            recorder.excludedStatement.run();

            recorder.startRange(start);
        }

        mv.visitInsn(NOP);
        mv.visitLabel(start);
    }

    /**
     * Applies all currently active finally blocks for a normal (non-jump) fall-through.
     */
    public void applyBlockRecorder() {
        applyBlockRecorder(finallyBlocks);
    }

    /** Returns {@code true} if there are any active finally/synchronized blocks on the stack. */
    public boolean hasBlockRecorder() {
        return !finallyBlocks.isEmpty();
    }

    /**
     * Pushes a new {@link BlockRecorder} (finally or synchronized guard) onto the
     * block-recorder stack and saves state so that {@link #pop()} will remove it.
     *
     * @param recorder the recorder to push
     */
    public void pushBlockRecorder(final BlockRecorder recorder) {
        pushState();
        finallyBlocks.addFirst(recorder);
    }

    /**
     * Marks {@code finallyBlock} as currently being visited so that recursive
     * finally-block application does not re-enter it.
     *
     * @param finallyBlock the block being visited
     */
    public void pushBlockRecorderVisit(final BlockRecorder finallyBlock) {
        visitedBlocks.add(finallyBlock);
    }

    /**
     * Removes {@code finallyBlock} from the visited-block set after its inline
     * emission is complete.
     *
     * @param finallyBlock the block that finished being visited
     */
    public void popBlockRecorderVisit(final BlockRecorder finallyBlock) {
        visitedBlocks.remove(finallyBlock);
    }

    /**
     * Writes the exception-table entries for all label ranges recorded in {@code block}.
     *
     * @param block the block recorder containing the try-range labels
     * @param goal  the handler label
     * @param sig   the internal name of the caught type, or {@code null} for catch-all
     */
    public void writeExceptionTable(final BlockRecorder block, final Label goal, final String sig) {
        if (block.isEmpty) return;
        MethodVisitor mv = controller.getMethodVisitor();
        for (LabelRange range : block.ranges) {
            mv.visitTryCatchBlock(range.start, range.end, goal, sig);
        }
    }

    /** Returns {@code true} if the current expression is being compiled as a left-hand side. */
    public boolean isLHS() {
        return lhs;
    }

    /**
     * Pushes a new left-hand-side flag onto the LHS stack.
     *
     * @param lhs {@code true} if the next expression is compiled as an assignment target
     */
    public void pushLHS(final boolean lhs) {
        lhsStack.add(lhs);
        this.lhs = lhs;
    }

    /** Pops the top left-hand-side flag, restoring the previous LHS state. */
    public void popLHS() {
        lhsStack.removeLast();
        lhs = lhsStack.getLast();
    }

    /** Returns {@code true} if the current {@code this} reference is implicit (no explicit qualifier). */
    public boolean isImplicitThis() {
        return implicitThis;
    }

    /**
     * Pushes a new implicit-this flag onto the stack.
     *
     * @param implicitThis {@code true} if {@code this} is used implicitly in the current context
     */
    public void pushImplicitThis(final boolean implicitThis) {
        implicitThisStack.add(implicitThis);
        this.implicitThis = implicitThis;
    }

    /** Pops the top implicit-this flag, restoring the previous state. */
    public void popImplicitThis() {
        implicitThisStack.removeLast();
        implicitThis = implicitThisStack.getLast();
    }

    /**
     * Returns {@code true} if the current context is inside a special constructor call
     * ({@code super(...)} or {@code this(...)}).
     */
    public boolean isInSpecialConstructorCall() {
        return inSpecialConstructorCall;
    }

    /**
     * Enters a special constructor call context ({@code super(...)} or {@code this(...)}).
     * Pushes state so that {@link #pop()} will restore the previous context.
     */
    public void pushInSpecialConstructorCall() {
        pushState();
        inSpecialConstructorCall = true;
    }
}
