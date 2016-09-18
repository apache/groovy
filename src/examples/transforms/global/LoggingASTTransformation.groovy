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

import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import java.lang.annotation.Annotation
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.ExpressionStatement

/**
* This ASTTransformation adds a start and stop message to every single method call. 
*
* @author Hamlet D'Arcy
*/ 
@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
public class LoggingASTTransformation implements ASTTransformation {


    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        List methods = sourceUnit.getAST()?.getMethods()
        methods?.each { MethodNode method ->
            Statement startMessage = createPrintlnAst("Starting $method.name")
            Statement endMessage = createPrintlnAst("Ending $method.name")

            List existingStatements = method.getCode().getStatements()
            existingStatements.add(0, startMessage)
            existingStatements.add(endMessage)
        }
    }

    /**
    * Creates the AST for a println invocation. 
    */ 
    private Statement createPrintlnAst(String message) {
        return new ExpressionStatement(
            new MethodCallExpression(
                new VariableExpression("this"),
                new ConstantExpression("println"),
                new ArgumentListExpression(
                    new ConstantExpression(message)
                )
            )
        )
    }
}
