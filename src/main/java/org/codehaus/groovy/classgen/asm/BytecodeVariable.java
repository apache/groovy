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

/**
 * Represents compile time variable metadata while compiling a method.
 */
public class BytecodeVariable {

    public static final BytecodeVariable THIS_VARIABLE = new BytecodeVariable();
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

    private BytecodeVariable() {
        index = 0;
        prevCurrent = 0;
        dynamicTyped = true;
    }

    public BytecodeVariable(final int index, final ClassNode type, final String name, final int prevCurrent) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.prevCurrent = prevCurrent;
    }

    /**
     * @return the stack index for this variable
     */
    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ClassNode getType() {
        return type;
    }

    public void setType(final ClassNode type) {
        this.type = type;
        dynamicTyped = dynamicTyped || ClassHelper.isDynamicTyped(type);
    }

    public int getPrevIndex() {
        return prevCurrent;
    }

    public boolean isDynamicTyped() {
        return dynamicTyped;
    }

    public void setDynamicTyped(final boolean b) {
        dynamicTyped = b;
    }

    /**
     * @return is this local variable shared in other scopes (and so must use a ValueHolder)
     */
    public boolean isHolder() {
        return holder;
    }

    public void setHolder(final boolean holder) {
        this.holder = holder;
    }

    public Label getStartLabel() {
        return startLabel;
    }

    public void setStartLabel(final Label startLabel) {
        this.startLabel = startLabel;
    }

    public Label getEndLabel() {
        return endLabel;
    }

    public void setEndLabel(final Label endLabel) {
        this.endLabel = endLabel;
    }

    @Override
    public String toString() {
        return name + "(index=" + index + ",type=" + type + ",holder="+holder+")";
    }
}
