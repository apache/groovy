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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * This class is a helper for AsmClassGenerator. It manages
 * different aspects of the code of a code block like
 * handling labels, defining variables, and scopes.
 * After a MethodNode is visited clear should be called, for
 * initialization the method init should be used.
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
 *
 * @see org.codehaus.groovy.classgen.AsmClassGenerator
 * @author Jochen Theodorou
 */
public class CompileStack implements Opcodes {
    /**
     * TODO: remove optimization of this.foo -> this.@foo
     *
     */

    // state flag
    private boolean clear=true;
    // current scope
    private VariableScope scope;
    // current label for continue
    private Label continueLabel;
    // current label for break
    private Label breakLabel;
    // available variables on stack
    private Map stackVariables = new HashMap();
    // index of the last variable on stack
    private int currentVariableIndex = 1;
    // index for the next variable on stack
    private int nextVariableIndex = 1;
    // currently temporary variables in use
    private final LinkedList temporaryVariables = new LinkedList();
    // overall used variables for a method/constructor
    private final LinkedList usedVariables = new LinkedList();
    // map containing named labels of parenting blocks
    private Map superBlockNamedLabels = new HashMap();
    // map containing named labels of current block
    private Map currentBlockNamedLabels = new HashMap();
    // list containing finally blocks
    // such a block is created by synchronized or finally and
    // must be called for break/continue/return
    private LinkedList<BlockRecorder> finallyBlocks = new LinkedList<>();
    private final LinkedList<BlockRecorder> visitedBlocks = new LinkedList<>();

    private Label thisStartLabel, thisEndLabel;

//    private MethodVisitor mv;

    // helper to handle different stack based variables
    private final LinkedList stateStack = new LinkedList();

    // handle different states for the implicit "this"
    private final LinkedList<Boolean> implicitThisStack = new LinkedList<>();
    // handle different states for being on the left hand side
    private final LinkedList<Boolean> lhsStack = new LinkedList<>();
    {
        implicitThisStack.add(false);
        lhsStack.add(false);
    }

    // defines the first variable index usable after
    // all parameters of a method
    private int localVariableOffset;
    // this is used to store the goals for a "break foo" call
    // in a loop where foo is a label.
    private final Map namedLoopBreakLabel = new HashMap();
    // this is used to store the goals for a "continue foo" call
    // in a loop where foo is a label.
    private final Map namedLoopContinueLabel = new HashMap();
    private String className;
    private final LinkedList<ExceptionTableEntry> typedExceptions = new LinkedList<>();
    private final LinkedList<ExceptionTableEntry> untypedExceptions = new LinkedList<>();
    // stores if on left-hand-side during compilation
    private boolean lhs;
    // stores if implicit or explicit this is used.
    private boolean implicitThis;
    private final WriterController controller;
    private boolean inSpecialConstructorCall;

    protected static class LabelRange {
        public Label start;
        public Label end;
    }

    public static class BlockRecorder {
        private boolean isEmpty = true;
        public Runnable excludedStatement;
        public final LinkedList<LabelRange> ranges;
        public BlockRecorder() {
            ranges = new LinkedList<>();
        }
        public BlockRecorder(Runnable excludedStatement) {
            this();
            this.excludedStatement = excludedStatement;
        }
        public void startRange(Label start) {
            LabelRange range = new LabelRange();
            range.start = start;
            ranges.add(range);
            isEmpty = false;
        }
        public void closeRange(Label end) {
            ranges.getLast().end = end;
        }
    }

    private static class ExceptionTableEntry {
        Label start,end,goal;
        String sig;
    }

    private class StateStackElement {
        final VariableScope scope;
        final Label continueLabel;
        final Label breakLabel;
        final Map stackVariables;
        final Map currentBlockNamedLabels;
        final LinkedList<BlockRecorder> finallyBlocks;
        final boolean inSpecialConstructorCall;

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

    public CompileStack(WriterController wc) {
        this.controller = wc;
    }

    public void pushState() {
        stateStack.add(new StateStackElement());
        stackVariables = new HashMap(stackVariables);
        finallyBlocks = new LinkedList(finallyBlocks);
    }

    private void popState() {
        if (stateStack.isEmpty()) {
             throw new GroovyBugError("Tried to do a pop on the compile stack without push.");
        }
        StateStackElement element = (StateStackElement) stateStack.removeLast();
        scope = element.scope;
        continueLabel = element.continueLabel;
        breakLabel = element.breakLabel;
        stackVariables = element.stackVariables;
        finallyBlocks = element.finallyBlocks;
        inSpecialConstructorCall = element.inSpecialConstructorCall;
    }

    public Label getContinueLabel() {
        return continueLabel;
    }

    public Label getBreakLabel() {
        return breakLabel;
    }

    public void removeVar(int tempIndex) {
        final BytecodeVariable head = (BytecodeVariable) temporaryVariables.removeFirst();
        if (head.getIndex() != tempIndex) {
            temporaryVariables.addFirst(head);
            MethodNode methodNode = controller.getMethodNode();
            if (methodNode==null) {
                methodNode = controller.getConstructorNode();
            }
            throw new GroovyBugError(
                    "In method "+ (methodNode!=null?methodNode.getText():"<unknown>") + ", " +
                    "CompileStack#removeVar: tried to remove a temporary " +
                    "variable with index "+ tempIndex + " in wrong order. " +
                    "Current temporary variables=" + temporaryVariables);
        }
    }

    private void setEndLabels(){
        Label endLabel = new Label();
        controller.getMethodVisitor().visitLabel(endLabel);
        for (Iterator iter = stackVariables.values().iterator(); iter.hasNext();) {
            BytecodeVariable var = (BytecodeVariable) iter.next();
            var.setEndLabel(endLabel);
        }
        thisEndLabel = endLabel;
    }

    public void pop() {
        setEndLabels();
        popState();
    }

    public VariableScope getScope() {
        return scope;
    }

    /**
     * creates a temporary variable.
     *
     * @param var defines type and name
     * @param store defines if the toplevel argument of the stack should be stored
     * @return the index used for this temporary variable
     */
    public int defineTemporaryVariable(Variable var, boolean store) {
        return defineTemporaryVariable(var.getName(), var.getType(),store);
    }

    public BytecodeVariable getVariable(String variableName ) {
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
    public BytecodeVariable getVariable(String variableName, boolean mustExist) {
        if (variableName.equals("this")) return BytecodeVariable.THIS_VARIABLE;
        if (variableName.equals("super")) return BytecodeVariable.SUPER_VARIABLE;
        BytecodeVariable v = (BytecodeVariable) stackVariables.get(variableName);
        if (v == null && mustExist)
            throw new GroovyBugError("tried to get a variable with the name " + variableName + " as stack variable, but a variable with this name was not created");
        return v;
    }

    /**
     * creates a temporary variable.
     *
     * @param name defines type and name
     * @param store defines if the top-level argument of the stack should be stored
     * @return the index used for this temporary variable
     */
    public int defineTemporaryVariable(String name,boolean store) {
        return defineTemporaryVariable(name, ClassHelper.DYNAMIC_TYPE,store);
    }

    /**
     * creates a temporary variable.
     *
     * @param name defines the name
     * @param node defines the node
     * @param store defines if the top-level argument of the stack should be stored
     * @return the index used for this temporary variable
     */
    public int defineTemporaryVariable(String name, ClassNode node, boolean store) {
        BytecodeVariable answer = defineVar(name, node, false, false);
        temporaryVariables.addFirst(answer); // TRICK: we add at the beginning so when we find for remove or get we always have the last one
        usedVariables.removeLast();

        if (store) controller.getOperandStack().storeVar(answer);

        return answer.getIndex();
    }

    private void resetVariableIndex(boolean isStatic) {
        temporaryVariables.clear();
        if (!isStatic) {
            currentVariableIndex=1;
            nextVariableIndex=1;
        } else {
            currentVariableIndex=0;
            nextVariableIndex=0;
        }
    }

    /**
     * Clears the state of the class. This method should be called
     * after a MethodNode is visited. Note that a call to init will
     * fail if clear is not called before
     */
    public void clear() {
        if (stateStack.size()>1) {
            int size = stateStack.size()-1;
            throw new GroovyBugError("the compile stack contains "+size+" more push instruction"+(size==1?"":"s")+" than pops.");
        }
        if (lhsStack.size()>1) {
            int size = lhsStack.size()-1;
            throw new GroovyBugError("lhs stack is supposed to be empty, but has " +
                                     size + " elements left.");
        }
        if (implicitThisStack.size()>1) {
            int size = implicitThisStack.size()-1;
            throw new GroovyBugError("implicit 'this' stack is supposed to be empty, but has " +
                                     size + " elements left.");
        }
        clear = true;
        MethodVisitor mv = controller.getMethodVisitor();
        // br experiment with local var table so debuggers can retrieve variable names
        if (true) {//AsmClassGenerator.CREATE_DEBUG_INFO) {
            if (thisEndLabel==null) setEndLabels();

            if (!scope.isInStaticContext()) {
                // write "this"
                mv.visitLocalVariable("this", className, null, thisStartLabel, thisEndLabel, 0);
            }

            for (Iterator iterator = usedVariables.iterator(); iterator.hasNext();) {
                BytecodeVariable v = (BytecodeVariable) iterator.next();
                ClassNode t = v.getType();
                if (v.isHolder()) t = ClassHelper.REFERENCE_TYPE;
                String type = BytecodeHelper.getTypeDescription(t);
                Label start = v.getStartLabel();
                Label end = v.getEndLabel();
                mv.visitLocalVariable(v.getName(), type, null, start, end, v.getIndex());
            }
        }

        //exception table writing
        for (ExceptionTableEntry ep : typedExceptions) {
            mv.visitTryCatchBlock(ep.start, ep.end, ep.goal, ep.sig);
        }
        //exception table writing
        for (ExceptionTableEntry ep : untypedExceptions) {
            mv.visitTryCatchBlock(ep.start, ep.end, ep.goal, ep.sig);
        }


        pop();
        typedExceptions.clear();
        untypedExceptions.clear();
        stackVariables.clear();
        usedVariables.clear();
        scope = null;
        finallyBlocks.clear();
        resetVariableIndex(false);
        superBlockNamedLabels.clear();
        currentBlockNamedLabels.clear();
        namedLoopBreakLabel.clear();
        namedLoopContinueLabel.clear();
        continueLabel=null;
        breakLabel=null;
        thisStartLabel=null;
        thisEndLabel=null;
        mv = null;
    }

    public void addExceptionBlock (Label start, Label end, Label goal,
                                   String sig)
    {
        // this code is in an extra method to avoid
        // lazy initialization issues
        ExceptionTableEntry ep = new ExceptionTableEntry();
        ep.start = start;
        ep.end = end;
        ep.sig = sig;
        ep.goal = goal;
        if (sig==null) {
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
    public void init(VariableScope el, Parameter[] parameters) {
        if (!clear) throw new GroovyBugError("CompileStack#init called without calling clear before");
        clear=false;
        pushVariableScope(el);
        defineMethodVariables(parameters,el.isInStaticContext());
        this.className = BytecodeHelper.getTypeDescription(controller.getClassNode());
    }

    /**
     * Causes the state-stack to add an element and sets
     * the given scope as new current variable scope. Creates
     * a element for the state stack so pop has to be called later
     */
    public void pushVariableScope(VariableScope el) {
        pushState();
        scope = el;
        superBlockNamedLabels = new HashMap(superBlockNamedLabels);
        superBlockNamedLabels.putAll(currentBlockNamedLabels);
        currentBlockNamedLabels = new HashMap();
    }

    /**
     * Should be called when descending into a loop that defines
     * also a scope. Calls pushVariableScope and prepares labels
     * for a loop structure. Creates a element for the state stack
     * so pop has to be called later, TODO: @Deprecate
     */
    public void pushLoop(VariableScope el, String labelName) {
        pushVariableScope(el);
        continueLabel = new Label();
        breakLabel = new Label();
        if (labelName != null) {
            initLoopLabels(labelName);
        }
    }

    /**
     * Should be called when descending into a loop that defines
     * also a scope. Calls pushVariableScope and prepares labels
     * for a loop structure. Creates a element for the state stack
     * so pop has to be called later
     */
    public void pushLoop(VariableScope el, List<String> labelNames) {
        pushVariableScope(el);
        continueLabel = new Label();
        breakLabel = new Label();
        if (labelNames != null) {
            for (String labelName : labelNames) {
                initLoopLabels(labelName);
            }
        }
    }

    private void initLoopLabels(String labelName) {
        namedLoopBreakLabel.put(labelName,breakLabel);
        namedLoopContinueLabel.put(labelName,continueLabel);
    }

    /**
     * Should be called when descending into a loop that does
     * not define a scope. Creates a element for the state stack
     * so pop has to be called later, TODO: @Deprecate
     */
    public void pushLoop(String labelName) {
        pushState();
        continueLabel = new Label();
        breakLabel = new Label();
        initLoopLabels(labelName);
    }

    /**
     * Should be called when descending into a loop that does
     * not define a scope. Creates a element for the state stack
     * so pop has to be called later
     */
    public void pushLoop(List<String> labelNames) {
        pushState();
        continueLabel = new Label();
        breakLabel = new Label();
        if (labelNames != null) {
            for (String labelName : labelNames) {
                initLoopLabels(labelName);
            }
        }
    }

    /**
     * Used for <code>break foo</code> inside a loop to end the
     * execution of the marked loop. This method will return the
     * break label of the loop if there is one found for the name.
     * If not, the current break label is returned.
     */
    public Label getNamedBreakLabel(String name) {
        Label label = getBreakLabel();
        Label endLabel = null;
        if (name!=null) endLabel = (Label) namedLoopBreakLabel.get(name);
        if (endLabel!=null) label = endLabel;
        return label;
    }

    /**
     * Used for <code>continue foo</code> inside a loop to continue
     * the execution of the marked loop. This method will return
     * the break label of the loop if there is one found for the
     * name. If not, getLabel is used.
     */
    public Label getNamedContinueLabel(String name) {
        Label label = getLabel(name);
        Label endLabel = null;
        if (name!=null) endLabel = (Label) namedLoopContinueLabel.get(name);
        if (endLabel!=null) label = endLabel;
        return label;
    }

    /**
     * Creates a new break label and a element for the state stack
     * so pop has to be called later
     */
    public Label pushSwitch(){
        pushState();
        breakLabel = new Label();
        return breakLabel;
    }

    /**
     * because a boolean Expression may not be evaluated completely
     * it is important to keep the registers clean
     */
    public void pushBooleanExpression(){
        pushState();
    }

    private BytecodeVariable defineVar(String name, ClassNode type, boolean holder, boolean useReferenceDirectly) {
        int prevCurrent = currentVariableIndex;
        makeNextVariableID(type,useReferenceDirectly);
        int index = currentVariableIndex;
        if (holder && !useReferenceDirectly) index = localVariableOffset++;
        BytecodeVariable answer = new BytecodeVariable(index, type, name, prevCurrent);
        usedVariables.add(answer);
        answer.setHolder(holder);
        return answer;
    }

    private void makeLocalVariablesOffset(Parameter[] paras, boolean isInStaticContext) {
        resetVariableIndex(isInStaticContext);

        for (Parameter para : paras) {
            makeNextVariableID(para.getType(), false);
        }
        localVariableOffset = nextVariableIndex;

        resetVariableIndex(isInStaticContext);
    }

    private void defineMethodVariables(Parameter[] paras, boolean isInStaticContext) {
        Label startLabel  = new Label();
        thisStartLabel = startLabel;
        controller.getMethodVisitor().visitLabel(startLabel);

        makeLocalVariablesOffset(paras,isInStaticContext);

        for (Parameter para : paras) {
            String name = para.getName();
            BytecodeVariable answer;
            ClassNode type = para.getType();
            if (para.isClosureSharedVariable()) {
                boolean useExistingReference = para.getNodeMetaData(ClosureWriter.UseExistingReference.class) != null;
                answer = defineVar(name, para.getOriginType(), true, useExistingReference);
                answer.setStartLabel(startLabel);
                if (!useExistingReference) {
                    controller.getOperandStack().load(type, currentVariableIndex);
                    controller.getOperandStack().box();

                    // GROOVY-4237, the original variable should always appear
                    // in the variable index, otherwise some programs get into
                    // trouble. So we define a dummy variable for the packaging
                    // phase and let it end right away before the normal
                    // reference will be used
                    Label newStart = new Label();
                    controller.getMethodVisitor().visitLabel(newStart);
                    BytecodeVariable var = new BytecodeVariable(currentVariableIndex, para.getOriginType(), name, currentVariableIndex);
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

    private void createReference(BytecodeVariable reference) {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitTypeInsn(NEW, "groovy/lang/Reference");
        mv.visitInsn(DUP_X1);
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKESPECIAL, "groovy/lang/Reference", "<init>", "(Ljava/lang/Object;)V", false);
        mv.visitVarInsn(ASTORE, reference.getIndex());
    }

    private static void pushInitValue(ClassNode type, MethodVisitor mv) {
        if (ClassHelper.isPrimitiveType(type)) {
            if (type== ClassHelper.long_TYPE) {
                mv.visitInsn(LCONST_0);
            } else if (type== ClassHelper.double_TYPE) {
                mv.visitInsn(DCONST_0);
            } else if (type== ClassHelper.float_TYPE) {
                mv.visitInsn(FCONST_0);
            } else {
                mv.visitLdcInsn(0);
            }
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
    public BytecodeVariable defineVariable(Variable v, boolean initFromStack) {
        return defineVariable(v, v.getOriginType(), initFromStack);
    }

    public BytecodeVariable defineVariable(Variable v, ClassNode variableType, boolean initFromStack) {
        String name = v.getName();
        BytecodeVariable answer = defineVar(name, variableType, v.isClosureSharedVariable(), v.isClosureSharedVariable());
        stackVariables.put(name, answer);

        MethodVisitor mv = controller.getMethodVisitor();
        Label startLabel  = new Label();
        answer.setStartLabel(startLabel);
        ClassNode type = answer.getType().redirect();
        OperandStack operandStack = controller.getOperandStack();

        if (!initFromStack) {
            if (ClassHelper.isPrimitiveType(v.getOriginType()) && ClassHelper.getWrapper(v.getOriginType()) == variableType) {
                pushInitValue(v.getOriginType(), mv);
                operandStack.push(v.getOriginType());
                operandStack.box();
                operandStack.remove(1);
            } else {
                pushInitValue(type, mv);
            }
        }
        operandStack.push(answer.getType());
        if (answer.isHolder())  {
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
    public boolean containsVariable(String name) {
        return stackVariables.containsKey(name);
    }

    /**
     * Calculates the index of the next free register stores it
     * and sets the current variable index to the old value
     */
    private void makeNextVariableID(ClassNode type, boolean useReferenceDirectly) {
        currentVariableIndex = nextVariableIndex;
        if ((type== ClassHelper.long_TYPE || type== ClassHelper.double_TYPE) && !useReferenceDirectly) {
            nextVariableIndex++;
        }
        nextVariableIndex++;
    }

    /**
     * Returns the label for the given name
     */
    public Label getLabel(String name) {
        if (name==null) return null;
        Label l = (Label) superBlockNamedLabels.get(name);
        if (l==null) l = createLocalLabel(name);
        return l;
    }

    /**
     * creates a new named label
     */
    public Label createLocalLabel(String name) {
        Label l = (Label) currentBlockNamedLabels.get(name);
        if (l==null) {
            l = new Label();
            currentBlockNamedLabels.put(name,l);
        }
        return l;
    }

    public void applyFinallyBlocks(Label label, boolean isBreakLabel) {
        // first find the state defining the label. That is the state
        // directly after the state not knowing this label. If no state
        // in the list knows that label, then the defining state is the
        // current state.
        StateStackElement result = null;
        for (ListIterator iter = stateStack.listIterator(stateStack.size()); iter.hasPrevious();) {
            StateStackElement element = (StateStackElement) iter.previous();
            if (!element.currentBlockNamedLabels.values().contains(label)) {
                if (isBreakLabel && element.breakLabel != label) {
                    result = element;
                    break;
                }
                if (!isBreakLabel && element.continueLabel != label) {
                    result = element;
                    break;
                }
            }
        }

        List<BlockRecorder> blocksToRemove;
        if (result==null) {
            // all Blocks do know the label, so use all finally blocks
            blocksToRemove = (List<BlockRecorder>) Collections.EMPTY_LIST;
        } else {
            blocksToRemove = result.finallyBlocks;
        }

        List<BlockRecorder> blocks = new LinkedList<>(finallyBlocks);
        blocks.removeAll(blocksToRemove);
        applyBlockRecorder(blocks);
    }


    private void applyBlockRecorder(List<BlockRecorder> blocks) {
        if (blocks.isEmpty() || blocks.size() == visitedBlocks.size()) return;

        MethodVisitor mv = controller.getMethodVisitor();
        Label newStart = new Label();

        for (BlockRecorder fb : blocks) {
            if (visitedBlocks.contains(fb)) continue;

            Label end = new Label();
            mv.visitInsn(NOP);
            mv.visitLabel(end);

            fb.closeRange(end);

            // we exclude the finally block from the exception table
            // here to avoid double visiting of finally statements
            fb.excludedStatement.run();

            fb.startRange(newStart);
        }

        mv.visitInsn(NOP);
        mv.visitLabel(newStart);
    }

    public void applyBlockRecorder() {
        applyBlockRecorder(finallyBlocks);
    }

    public boolean hasBlockRecorder() {
        return !finallyBlocks.isEmpty();
    }

    public void pushBlockRecorder(BlockRecorder recorder) {
        pushState();
        finallyBlocks.addFirst(recorder);
    }

    public void pushBlockRecorderVisit(BlockRecorder finallyBlock) {
        visitedBlocks.add(finallyBlock);
    }

    public void popBlockRecorderVisit(BlockRecorder finallyBlock) {
        visitedBlocks.remove(finallyBlock);
    }

    public void writeExceptionTable(BlockRecorder block, Label goal, String sig) {
        if (block.isEmpty) return;
        MethodVisitor mv = controller.getMethodVisitor();
        for (LabelRange range : block.ranges) {
            mv.visitTryCatchBlock(range.start, range.end, goal, sig);
        }
    }

//    public MethodVisitor getMethodVisitor() {
//        return mv;
//    }

    public boolean isLHS() {
        return lhs;
    }

    public void pushLHS(boolean lhs) {
        lhsStack.add(lhs);
        this.lhs = lhs;
    }

    public void popLHS() {
        lhsStack.removeLast();
        this.lhs = lhsStack.getLast();
    }

    public void pushImplicitThis(boolean implicitThis) {
        implicitThisStack.add(implicitThis);
        this.implicitThis = implicitThis;
    }

    public boolean isImplicitThis() {
        return implicitThis;
    }

    public void popImplicitThis() {
        implicitThisStack.removeLast();
        this.implicitThis = implicitThisStack.getLast();
    }

    public boolean isInSpecialConstructorCall() {
        return inSpecialConstructorCall;
    }

    public void pushInSpecialConstructorCall() {
        pushState();
        inSpecialConstructorCall = true;
    }
}
