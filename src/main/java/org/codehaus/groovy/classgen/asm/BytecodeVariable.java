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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents compile time variable metadata while compiling a method.
 */
public class BytecodeVariable {

    /**
     * Sentinel metadata for the implicit {@code this} receiver.
     */
    public static final BytecodeVariable THIS_VARIABLE = new BytecodeVariable();
    /**
     * Sentinel metadata for the implicit {@code super} receiver.
     */
    public static final BytecodeVariable SUPER_VARIABLE = new BytecodeVariable();

    private final int index;
    private ClassNode type;
    private String name;
    private final int prevCurrent;
    private boolean dynamicTyped;
    private boolean holder;

    // br for setting on the LocalVariableTable in the class file
    // these fields should probably go to jvm Operand class
    private Label startLabel;
    private Label endLabel;

    // GROOVY-12242: flow-scoped pattern variables are visible over several
    // disjoint bytecode regions (e.g. condition + then-arm, then again after
    // the if). Each completed region is kept here and emitted as its own
    // LocalVariableTable entry; startLabel/endLabel describe the currently
    // open region only. Ordinary locals never close a range, so this stays
    // null for them and emission is unchanged.
    private List<Label[]> closedRanges;
    private boolean rangeClosed;

    private BytecodeVariable() {
        index = 0;
        prevCurrent = 0;
        dynamicTyped = true;
    }

    /**
     * Creates bytecode metadata for a local variable slot.
     *
     * @param index the JVM local-variable index
     * @param type the variable type
     * @param name the variable name
     * @param prevCurrent the previous current-variable index
     */
    public BytecodeVariable(final int index, final ClassNode type, final String name, final int prevCurrent) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.prevCurrent = prevCurrent;
        this.dynamicTyped = ClassHelper.isDynamicTyped(type);
    }

    /**
     * @return the stack index for this variable
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the source-level variable name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the variable type
     */
    public ClassNode getType() {
        return type;
    }

    /**
     * Updates the tracked variable type.
     *
     * @param type the new variable type
     */
    public void setType(final ClassNode type) {
        this.type = type;
        dynamicTyped = dynamicTyped || ClassHelper.isDynamicTyped(type);
    }

    /**
     * @return the previous current-variable index
     */
    public int getPrevIndex() {
        return prevCurrent;
    }

    /**
     * @return whether the variable uses dynamic typing
     */
    public boolean isDynamicTyped() {
        return dynamicTyped;
    }

    /**
     * Marks whether the variable should be treated as dynamically typed.
     *
     * @param dynamicTyped {@code true} if the variable is dynamically typed
     */
    public void setDynamicTyped(final boolean dynamicTyped) {
        this.dynamicTyped = dynamicTyped;
    }

    /**
     * @return is this local variable shared in other scopes (and so must use a ValueHolder)
     */
    public boolean isHolder() {
        return holder;
    }

    /**
     * Marks whether this variable must be stored through a holder object.
     *
     * @param holder {@code true} if the variable is closure-shared
     */
    public void setHolder(final boolean holder) {
        this.holder = holder;
    }

    /**
     * @return the start label used for local-variable table metadata
     */
    public Label getStartLabel() {
        return startLabel;
    }

    /**
     * Sets the start label used for local-variable table metadata.
     *
     * @param startLabel the starting label
     */
    public void setStartLabel(final Label startLabel) {
        this.startLabel = startLabel;
    }

    /**
     * @return the end label used for local-variable table metadata
     */
    public Label getEndLabel() {
        return endLabel;
    }

    /**
     * Sets the end label used for local-variable table metadata.
     *
     * @param endLabel the ending label
     */
    public void setEndLabel(final Label endLabel) {
        this.endLabel = endLabel;
    }

    /**
     * Ends the currently open local-variable-table range at the given label,
     * recording it for later emission. Used when flow scoping hides this
     * variable (GROOVY-12242); a later {@link #reopenRange} starts a new range
     * if the variable becomes visible again.
     *
     * @param end the label at which visibility ends; ignored if {@code null}
     *            or if no range is open
     */
    public void closeRange(final Label end) {
        if (!rangeClosed && startLabel != null && end != null) {
            if (closedRanges == null) closedRanges = new ArrayList<>(2);
            closedRanges.add(new Label[]{startLabel, end});
            rangeClosed = true;
        }
    }

    /**
     * Starts a new local-variable-table range at the given label after a
     * {@link #closeRange}. The end label is reset to the start and extended
     * by subsequent scope pops; a range that is never extended has zero
     * width and is not emitted.
     *
     * @param start the label at which visibility resumes; ignored if
     *              {@code null} or if no range was closed
     */
    public void reopenRange(final Label start) {
        if (rangeClosed && start != null) {
            startLabel = start;
            endLabel = start;
            rangeClosed = false;
        }
    }

    /**
     * @return whether the current local-variable-table range has been closed
     *         without being reopened (the variable is hidden by flow scoping)
     */
    public boolean isRangeClosed() {
        return rangeClosed;
    }

    /**
     * @return the completed local-variable-table ranges as {start, end} label
     *         pairs, oldest first; empty for variables never hidden
     */
    public List<Label[]> getClosedRanges() {
        return closedRanges == null ? Collections.emptyList() : closedRanges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name + "(index=" + index + ",type=" + type + ",holder="+holder+")";
    }
}
