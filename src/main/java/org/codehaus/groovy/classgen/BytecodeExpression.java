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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.objectweb.asm.MethodVisitor;

/**
 * Represents some custom bytecode generation by the compiler
 */
public abstract class BytecodeExpression extends Expression {
    public static final BytecodeExpression NOP = new BytecodeExpression() {
        public void visit(MethodVisitor visitor) {
            //do nothing             
        }
    };

    public BytecodeExpression() {
    }
    
    public BytecodeExpression(ClassNode type) {
        super.setType(type);
    }
    
    public void accept(GroovyCodeVisitor visitor) {
        visitor.visitBytecodeExpression(this);
    }

    public abstract void visit(MethodVisitor mv);

    public Expression transformExpression(ExpressionTransformer transformer) {
        return this;
    }
}
