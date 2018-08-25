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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.LinkedList;

/**
 * Abstract base class for generator of Java class versions of Groovy AST classes
 *
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @author Russel Winder
 */
public abstract class ClassGenerator extends ClassCodeVisitorSupport implements Opcodes {
    // inner classes created while generating bytecode
    protected LinkedList<ClassNode> innerClasses = new LinkedList<>();

    public LinkedList<ClassNode> getInnerClasses() {
        return innerClasses;
    }

    protected SourceUnit getSourceUnit() {
        return null;
    }

    public void visitBytecodeSequence(BytecodeSequence bytecodeSequence) {
    }
}
