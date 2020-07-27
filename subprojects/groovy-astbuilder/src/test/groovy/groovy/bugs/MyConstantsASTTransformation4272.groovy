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
package groovy.bugs

import org.objectweb.asm.Opcodes

import org.codehaus.groovy.ast.ASTNode 
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode 
import org.codehaus.groovy.ast.MethodNode 
import org.codehaus.groovy.ast.Parameter 
import org.codehaus.groovy.ast.builder.AstBuilder 
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit 
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class MyConstantsASTTransformation4272 implements ASTTransformation, Opcodes {
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        ClassNode classNode = nodes[1]

        classNode.addMethod(new MethodNode("willSucceed", ACC_PUBLIC, ClassHelper.boolean_TYPE,
                [] as Parameter[], null, new AstBuilder().buildFromCode { return new Integer("1") }[0]))
                
        classNode.addMethod(new MethodNode("willNotFail", ACC_PUBLIC, ClassHelper.int_TYPE,
                [] as Parameter[], null, new AstBuilder().buildFromCode { return 1 }[0]))

        classNode.addMethod(new MethodNode("willAlsoNotFail", ACC_PUBLIC, ClassHelper.boolean_TYPE,
                [] as Parameter[], null, new AstBuilder().buildFromString("return 1")[0]))
    }
}
