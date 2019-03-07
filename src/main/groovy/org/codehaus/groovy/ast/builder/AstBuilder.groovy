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
package org.codehaus.groovy.ast.builder

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase

/**
 * The AstBuilder provides several ways to build an abstract syntax tree (AST) of Groovy code.
 *
 * You can convert a String into AST using the buildFromString method.
 * You can convert code into AST using the buildFromCode method.
 * You can use the AST DSL with the buildFromSpec method. 
 *
 * For more information, see the resources on the Groovy wiki pages. 
 */
@CompileStatic
class AstBuilder {

    /**
     * Builds AST based on the code within the  {@link Closure}  parameter.
     *
     * This method <strong>must</strong> be invoked at compile time and never at runtime, because
     * an ASTTransformation must be run to support it. If you receive an IllegalStateException then
     * you most likely need to add stronger typing. For instance, this will not work:
     * <code>
     *      def builder = new AstBuilder()
     *      builder.buildFromCode {*             // some code
     *}* </code>
     * While this code will:
     * <code>
     *      new AstBuilder().buildFromCode {*             // some code
     *}* </code>
     *
     * The compiler rewrites buildFromCode invocations into  {@link AstBuilder#buildFromString(CompilePhase, boolean, String)}
     * invocations. An exception raised during AST generation will show a stack trace from  {@link AstBuilder#buildFromString(CompilePhase, boolean, String)}
     * and not from  {@link AstBuilder#buildFromCode(CompilePhase, boolean, Closure)} .
     *
     * The compiler saves the source code of the closure as a String within the Java class file. The String source
     * of the closure will be visible and un-obfuscated within the class file. If your Closure parameter contains
     * sensitive data such as a hard-coded password then that data is free to be seen by anyone with the class file.
     * Do not store sensitive data within the closure parameter.
     *
     * @param phase
     *      the  {@link CompilePhase}  the AST will be targeted towards. Default is  {@link CompilePhase#CLASS_GENERATION}
     * @param statementsOnly
     *      when true, only the script statements are returned. WHen false, you will
     *      receive back a Script class node also. Default is true.
     * @param block
     *      the code that will be converted
     * @returns a List of  {@link ASTNode} .
     * @throws IllegalStateException*      this method may not be invoked at runtime. It works via a compile-time transformation
     *      of the closure source code into a String, which is sent to the  {@link AstBuilder#buildFromString(CompilePhase, boolean, String)}
     *      method. The buildFromCode() method must be invoked against a strongly typed AstBuilder.
     */
    List<ASTNode> buildFromCode(CompilePhase phase = CompilePhase.CLASS_GENERATION, boolean statementsOnly = true, Closure block) {
        throw new IllegalStateException("""AstBuilder.build(CompilePhase, boolean, Closure):List<ASTNode> should never be called at runtime.
Are you sure you are using it correctly?
""")
    }


    /**
     * Builds AST based on the code within the String parameter.
     *
     * @param phase
     *      the  {@link CompilePhase}  the AST will be targeted towards. Default is  {@link CompilePhase#CLASS_GENERATION}
     * @param statementsOnly
     *      when true, only the script statements are returned. WHen false, you will
     *      receive back a Script class node also. Default is true.
     * @param source
     *      The source code String that will be compiled.
     * @returns a List of  {@link ASTNode} .
     * @throws IllegalArgumentException*      if source is null or empty
     */
    List<ASTNode> buildFromString(CompilePhase phase = CompilePhase.CLASS_GENERATION, boolean statementsOnly = true, String source) {
        if (!source || "" == source.trim()) throw new IllegalArgumentException("A source must be specified")
        return new AstStringCompiler().compile(source, phase, statementsOnly);
    }

    /**
     * Builds AST based on the code within the String parameter. The parameter is assumed to be 
     * a code block which is not legal Groovy code. A goto label is affixed to the block, compiled, 
     * and the resulting BlockStatement wrapper is removed before returning a result. 
     * @param phase
     *      the  {@link CompilePhase}  the AST will be targeted towards. Default is  {@link CompilePhase#CLASS_GENERATION}
     * @param statementsOnly
     *      when true, only the script statements are returned. WHen false, you will
     *      receive back a Script class node also. Default is true.
     * @param source
     *      The source code String that will be compiled. The string must be a block wrapped in curly braces. 
     * @returns a List of  {@link ASTNode} .
     * @throws IllegalArgumentException*      if source is null or empty
     */
    private List<ASTNode> buildFromBlock(CompilePhase phase = CompilePhase.CLASS_GENERATION, boolean statementsOnly = true, String source) {
        if (!source || "" == source.trim()) throw new IllegalArgumentException("A source must be specified")
        def labelledSource = "__synthesized__label__${System.currentTimeMillis()}__:" + source
        List<ASTNode> result = new AstStringCompiler().compile(labelledSource, phase, statementsOnly)
        // find the block statement from the result, and unwrap it from one level.
        result.collect { node ->
            if (node instanceof BlockStatement) {
                ((BlockStatement) node).statements[0] //unwrap the artifact of pre-pending the goto label
            } else {
                node
            }
        }
    }

    /**
     * Builds AST based on the DSL data within the Closure parameter.
     * @param specification
     *      the contents to create
     */
    List<ASTNode> buildFromSpec(@DelegatesTo(AstSpecificationCompiler) Closure specification) {
        if (specification == null) throw new IllegalArgumentException('Null: specification')
        def properties = new AstSpecificationCompiler(specification)
        return properties.expression
    }
}
