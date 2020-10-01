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
    private boolean holder;

    // br for setting on the LocalVariableTable in the class file
    // these fields should probably go to jvm Operand class
    private Label startLabel = null;
    private Label endLabel = null;
    private boolean dynamicTyped;

    private BytecodeVariable(){
        dynamicTyped = true;
        index=0;
        holder=false;
        prevCurrent=0;
    }
    
    public BytecodeVariable(int index, ClassNode type, String name, int prevCurrent) {
        this.index = index;
        this.type = type;
        this.name = name;
        this.prevCurrent = prevCurrent;
    }

    public String getName() {
        return name;
    }

    public ClassNode getType() {
        return type;
    }
 
    /**
     * @return the stack index for this variable
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return is this local variable shared in other scopes (and so must use a ValueHolder)
     */
    public boolean isHolder() {
        return holder;
    }

    public void setHolder(boolean holder) {
        this.holder = holder;
    }
    
    public Label getStartLabel() {
        return startLabel;
    }

    public void setStartLabel(Label startLabel) {
        this.startLabel = startLabel;
    }

    public Label getEndLabel() {
        return endLabel;
    }

    public void setEndLabel(Label endLabel) {
        this.endLabel = endLabel;
    }

    @Override
    public String toString() {
        return name + "(index=" + index + ",type=" + type + ",holder="+holder+")";
    }

    public void setType(ClassNode type) {
        this.type = type;
        dynamicTyped |= type==ClassHelper.DYNAMIC_TYPE;
    }

    public void setDynamicTyped(boolean b) {
        dynamicTyped = b;
    }
    
    public boolean isDynamicTyped() {
        return dynamicTyped;
    }

    public int getPrevIndex() {
        return prevCurrent;
    }
}
