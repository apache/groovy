/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ast.builder

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.io.ReaderSource
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException

/**
 * Transformation to capture ASTBuilder from code statements.
 * 
 * The AstBuilder "from code" approach is used with a single Closure
 * parameter. This transformation converts the ClosureExpression back
 * into source code and rewrites the AST so that the "from string" 
 * builder is invoked on the source. In order for this to work, the 
 * closure source must be given a goto label. It is the "from string" 
 * approach's responsibility to remove the BlockStatement created
 * by the label. 
 *
 * @author Hamlet D'Arcy
 */

@GroovyASTTransformation (phase = CompilePhase.SEMANTIC_ANALYSIS)
public class AstBuilderTransformation implements ASTTransformation {

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {

        // todo : are there other import types that can be specified?
        def transformer = new AstBuilderInvocationTrap(
                sourceUnit.getAST().imports,
                sourceUnit.getAST().importPackages,
                sourceUnit.source,
                sourceUnit
        )
        nodes?.each {ASTNode it ->
            if (!it instanceof AnnotationNode && !it instanceof ClassNode) {
                it.visit(transformer)
            }
        }
        sourceUnit.getAST()?.visit(transformer)
        sourceUnit.getAST()?.getStatementBlock()?.visit(transformer)
        sourceUnit.getAST()?.getClasses()?.each {ClassNode classNode ->

            classNode.methods.each {MethodNode node ->
                node?.code?.visit(transformer)
            }

            try {
                classNode.constructors.each {MethodNode node ->
                    node?.code?.visit(transformer)
                }
            } catch (MissingPropertyException ignored) {
                // todo: inner class nodes don't have a constructors field available
            }

            // all properties are also always fields
            classNode.fields.each {FieldNode node ->
                node?.initialValueExpression?.visit(transformer)
            }

            try {
                classNode.objectInitializers.each {Statement node ->
                    node?.visit(transformer)
                }
            } catch (MissingPropertyException ignored) {
                // todo: inner class nodes don't have a objectInitializers field available
            }

            // todo: is there anything to do with the module ???
        }
        sourceUnit.getAST()?.getMethods()?.each { MethodNode node ->
            node?.parameters?.defaultValue?.each {
                it?.visit(transformer)
            }
            node?.code?.visit(transformer)
        }
    }
}

/**
 * This class traps invocations of AstBuilder.build(CompilePhase, boolean, Closure) and converts
 * the contents of the closure into expressions by reading the source of the Closure and sending
 * that as a String to AstBuilder.build(String, CompilePhase, boolean) at runtime.
 */
private class AstBuilderInvocationTrap extends CodeVisitorSupport {

    private final List<String> factoryTargets = []
    private final ReaderSource source
    private final SourceUnit sourceUnit

    /**
     * Creates the trap and captures all the ways in which a class may be referenced via imports. 
     * @param imports
     *      all the imports from the source
     * @param importPackages
     *      all the imported packages from the source
     * @param source
     *      the reader source that contains source for the SourceUnit
     * @param sourceUnit
     *      the source unit being compiled. Used for error messages. 
     */
    def AstBuilderInvocationTrap(List<ImportNode> imports, List<String> importPackages, ReaderSource source, SourceUnit sourceUnit) {
        if (!source) throw new IllegalArgumentException("Null: source")
        if (!sourceUnit) throw new IllegalArgumentException("Null: sourceUnit")
        this.source = source
        this.sourceUnit = sourceUnit

        // factory type may be references as fully qualified, an import, or an alias
        factoryTargets << "org.codehaus.groovy.ast.builder.AstBuilder"  //default package
        imports?.each {ImportNode importStatement ->
            if (importStatement.type.name == "org.codehaus.groovy.ast.builder.AstBuilder") {
                factoryTargets << importStatement.alias
            }
        }
        if (importPackages.contains('org.codehaus.groovy.ast.builder.')) {
            factoryTargets << 'AstBuilder'
        }
    }

    /**
    * Reports an error back to the source unit. 
    * @param  msg
    *       the error message
    * @param expr
    *       the expression that caused the error message. 
    */ 
    private void addError(String msg, ASTNode expr) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        sourceUnit.getErrorCollector().addErrorAndContinue(
          new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), sourceUnit)
        );
    }


    /**
    * Attempts to find AstBuilder 'from code' invocations. When found, converts them into calls
    * to the 'from string' approach. 
    * 
    * @param call
    *       the method call expression that may or may not be an AstBuilder 'from code' invocation. 
    */ 
    public void visitMethodCallExpression(MethodCallExpression call) {

        if (isBuildInvocation(call)) {

            def closureExpression = call.arguments.expressions?.find { it instanceof ClosureExpression }
            def otherArgs = call.arguments.expressions?.findAll { !(it instanceof ClosureExpression) }

            String source = convertClosureToSource(closureExpression)

            // parameter order is build(CompilePhase, boolean, String) 
            otherArgs << new ConstantExpression(source)
            call.arguments = new ArgumentListExpression(otherArgs)
            call.method = new ConstantExpression('buildFromBlock')
            call.spreadSafe = false     //todo: is this correct?
            call.safe = false           //todo: is this correct?
            call.implicitThis = false
        } else {
            // continue normal tree walking
            call.getObjectExpression().visit(this);
            call.getMethod().visit(this);
            call.getArguments().visit(this);
        }
    }

    /**
     * Looks for method calls on the AstBuilder class called build that take
     * a Closure as parameter. This is all needed b/c build is overloaded.
     * @param call
     *      the method call expression, may not be null
     */
    private boolean isBuildInvocation(MethodCallExpression call) {
        if (call == null) throw new IllegalArgumentException('Null: call')

        // is method name correct?
        if (call.method instanceof ConstantExpression && 'buildFromCode' == call.method?.value) {

            // is method object correct type?
            String name = call.objectExpression?.type?.name
            if (name && factoryTargets.contains(name)) {

                // is one of the arguments a closure?
                if (call.arguments && call.arguments instanceof TupleExpression) {
                    if (call.arguments.expressions?.find { it instanceof ClosureExpression }) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Converts a ClosureExpression into the String source.
     * @param expression    a closure
     * @return              the source the closure was created from 
     */
    private String convertClosureToSource(ClosureExpression expression) {
        if (expression == null) throw new IllegalArgumentException('Null: expression')
        
        def lineRange = (expression.lineNumber..expression.lastLineNumber)

        def source = lineRange.collect {
            def line = source.getLine(it, null)
            if (line == null) {
                addError(
                        "Error calculating source code for expression. Trying to read line $it from ${source.class}",
                        expression
                )
            }
            if (it == expression.lastLineNumber) {
                line = line.substring(0, expression.lastColumnNumber - 1)
            }
            if (it == expression.lineNumber) {
                line = line.substring(expression.columnNumber - 1)
            }
            return line
        }?.join('\n')?.trim()   //restoring line breaks is important b/c of lack of semicolons

        if (!source.startsWith('{')) {
            addError(
                    'Error converting ClosureExpression into source code. ' +
                    "Closures must start with {. Found: $source",
                    expression
            )
        }
        
        return source
    }
}