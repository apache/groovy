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
package transforms.global

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.transform.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import java.lang.annotation.*
import org.codehaus.groovy.ast.builder.AstBuilder

/**
* This ASTTransformation adds a static getCompiledTime() : String method to every class.  
*
* @author Hamlet D'Arcy
*/ 
@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
public class CompiledAtASTTransformation implements ASTTransformation {

    private static final compileTime = new Date().toString()

    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {

        List classes = sourceUnit.ast?.classes
        classes?.each { ClassNode clazz ->
            clazz.addMethod(makeMethod())
        }
    }

    /**
    *  OpCodes should normally be referenced, but in a standalone example I don't want to have to include
    * the jar at compile time. 
    */ 
    MethodNode makeMethod() {
        def ast = new AstBuilder().buildFromSpec {
            method('getCompiledTime', /*OpCodes.ACC_PUBLIC*/1 | /*OpCodes.ACC_STATIC*/8, String) {
                parameters {}
                exceptions {}
                block { 
                    returnStatement {
                        constant(compileTime) 
                    }
                }
                annotations {}
            }
        }
        ast[0]
    }
}
