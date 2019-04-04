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
package org.codehaus.groovy.transform.tailrec

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TailRecursive
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.ReturnAdder
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * Handles generation of code for the @TailRecursive annotation.
 *
 * It's doing its work in the earliest possible compile phase
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class TailRecursiveASTTransformation extends AbstractASTTransformation {

    private static final Class MY_CLASS = TailRecursive
    private static final ClassNode MY_TYPE = new ClassNode(MY_CLASS)
    static final String MY_TYPE_NAME = '@' + MY_TYPE.nameWithoutPackage
    private final HasRecursiveCalls hasRecursiveCalls = new HasRecursiveCalls()
    private final TernaryToIfStatementConverter ternaryToIfStatement = new TernaryToIfStatementConverter()


    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)

        MethodNode method = nodes[1] as MethodNode

        if (method.isAbstract()) {
            addError("Annotation $MY_TYPE_NAME cannot be used for abstract methods.", method)
            return
        }

        if (hasAnnotation(method, ClassHelper.make(Memoized))) {
            ClassNode memoizedClassNode = ClassHelper.make(Memoized)
            for (AnnotationNode annotationNode in method.annotations) {
                if (annotationNode.classNode == MY_TYPE)
                    break
                if (annotationNode.classNode == memoizedClassNode) {
                    addError("Annotation $MY_TYPE_NAME must be placed before annotation @Memoized.", annotationNode)
                    return
                }
            }
        }

        if (!hasRecursiveMethodCalls(method)) {
            AnnotationNode annotationNode = method.getAnnotations(ClassHelper.make(TailRecursive))[0]
            addError("No recursive calls detected. You must remove annotation ${MY_TYPE_NAME}.", annotationNode)
            return
        }

        transformToIteration(method, source)
        ensureAllRecursiveCallsHaveBeenTransformed(method)
    }

    private boolean hasAnnotation(MethodNode methodNode, ClassNode annotation) {
        List annots = methodNode.getAnnotations(annotation)
        annots != null && annots.size() > 0
    }


    private void transformToIteration(MethodNode method, SourceUnit source) {
        if (method.isVoidMethod()) {
            transformVoidMethodToIteration(method)
        } else {
            transformNonVoidMethodToIteration(method, source)
        }
    }

    private void transformVoidMethodToIteration(MethodNode method) {
        addError('Void methods are not supported by @TailRecursive yet.', method)
    }

    private void transformNonVoidMethodToIteration(MethodNode method, SourceUnit source) {
        addMissingDefaultReturnStatement(method)
        replaceReturnsWithTernariesToIfStatements(method)
        wrapMethodBodyWithWhileLoop(method)

        Map<String, Map> nameAndTypeMapping = name2VariableMappingFor(method)
        replaceAllAccessToParams(method, nameAndTypeMapping)
        addLocalVariablesForAllParameters(method, nameAndTypeMapping) //must happen after replacing access to params

        Map<Integer, Map> positionMapping = position2VariableMappingFor(method)
        replaceAllRecursiveReturnsWithIteration(method, positionMapping)
        repairVariableScopes(source, method)
    }

    private void repairVariableScopes(SourceUnit source, MethodNode method) {
        new VariableScopeVisitor(source).visitClass(method.declaringClass)
    }

    @SuppressWarnings('Instanceof')
    private void replaceReturnsWithTernariesToIfStatements(MethodNode method) {
        Closure<Boolean> whenReturnWithTernary = { ASTNode node ->
            if (!(node instanceof ReturnStatement)) {
                return false
            }
            ((ReturnStatement) node).expression instanceof TernaryExpression
        }
        Closure<Statement> replaceWithIfStatement = { ReturnStatement statement ->
            ternaryToIfStatement.convert(statement)
        }
        StatementReplacer replacer = new StatementReplacer(when: whenReturnWithTernary, replaceWith: replaceWithIfStatement)
        replacer.replaceIn(method.code)

    }

    private void addLocalVariablesForAllParameters(MethodNode method, Map<String, Map> nameAndTypeMapping) {
        BlockStatement code = method.code as BlockStatement
        nameAndTypeMapping.each { String paramName, Map localNameAndType ->
            code.statements.add(0, AstHelper.createVariableDefinition(
                    (String) localNameAndType['name'],
                    (ClassNode) localNameAndType['type'],
                    new VariableExpression(paramName, (ClassNode) localNameAndType['type'])
            ))
        }
    }

    private void replaceAllAccessToParams(MethodNode method, Map<String, Map> nameAndTypeMapping) {
        new VariableAccessReplacer(nameAndTypeMapping: nameAndTypeMapping).replaceIn(method.code)
    }

    // Public b/c there are tests for this method
    Map<String, Map> name2VariableMappingFor(MethodNode method) {
        Map<String, Map> nameAndTypeMapping = [:]
        method.parameters.each { Parameter param ->
            String paramName = param.name
            ClassNode paramType = param.type as ClassNode
            String iterationVariableName = iterationVariableName(paramName)
            nameAndTypeMapping[paramName] = [name: iterationVariableName, type: paramType]
        }
        nameAndTypeMapping
    }

    // Public b/c there are tests for this method
    Map<Integer, Map> position2VariableMappingFor(MethodNode method) {
        Map<Integer, Map> positionMapping = [:]
        method.parameters.eachWithIndex { Parameter param, int index ->
            String paramName = param.name
            ClassNode paramType = param.type as ClassNode
            String iterationVariableName = this.iterationVariableName(paramName)
            positionMapping[index] = [name: iterationVariableName, type: paramType]
        }
        positionMapping
    }

    private String iterationVariableName(String paramName) {
        '_' + paramName + '_'
    }

    private void replaceAllRecursiveReturnsWithIteration(MethodNode method, Map positionMapping) {
        replaceRecursiveReturnsOutsideClosures(method, positionMapping)
        replaceRecursiveReturnsInsideClosures(method, positionMapping)
    }

    @SuppressWarnings('Instanceof')
    private void replaceRecursiveReturnsOutsideClosures(MethodNode method, Map<Integer, Map> positionMapping) {
        Closure<Boolean> whenRecursiveReturn = { Statement statement, boolean inClosure ->
            if (inClosure)
                return false
            if (!(statement instanceof ReturnStatement)) {
                return false
            }
            Expression inner = ((ReturnStatement) statement).expression
            if (!(inner instanceof MethodCallExpression) && !(inner instanceof StaticMethodCallExpression)) {
                return false
            }
            isRecursiveIn(inner, method)
        }
        Closure<Statement> replaceWithContinueBlock = { ReturnStatement statement ->
            new ReturnStatementToIterationConverter().convert(statement, positionMapping)
        }
        def replacer = new StatementReplacer(when: whenRecursiveReturn, replaceWith: replaceWithContinueBlock)
        replacer.replaceIn(method.code)
    }

    @SuppressWarnings('Instanceof')
    private void replaceRecursiveReturnsInsideClosures(MethodNode method, Map<Integer, Map> positionMapping) {
        Closure<Boolean> whenRecursiveReturn = { Statement statement, boolean inClosure ->
            if (!inClosure)
                return false
            if (!(statement instanceof ReturnStatement)) {
                return false
            }
            Expression inner = ((ReturnStatement) statement).expression
            if (!(inner instanceof MethodCallExpression) && !(inner instanceof StaticMethodCallExpression)) {
                return false
            }
            isRecursiveIn(inner, method)
        }
        Closure<Statement> replaceWithThrowLoopException = { ReturnStatement statement ->
            new ReturnStatementToIterationConverter(recurStatement: AstHelper.recurByThrowStatement()).convert(statement, positionMapping)
        }
        StatementReplacer replacer = new StatementReplacer(when: whenRecursiveReturn, replaceWith: replaceWithThrowLoopException)
        replacer.replaceIn(method.code)
    }

    private void wrapMethodBodyWithWhileLoop(MethodNode method) {
        new InWhileLoopWrapper().wrap(method)
    }

    private void addMissingDefaultReturnStatement(MethodNode method) {
        new ReturnAdder().visitMethod(method)
        new ReturnAdderForClosures().visitMethod(method)
    }

    private void ensureAllRecursiveCallsHaveBeenTransformed(MethodNode method) {
        List<Expression> remainingRecursiveCalls = new CollectRecursiveCalls().collect(method)
        for (Expression expression : remainingRecursiveCalls) {
            addError("Recursive call could not be transformed by @TailRecursive. Maybe it's not a tail call.", expression)
        }
    }

    private boolean hasRecursiveMethodCalls(MethodNode method) {
        hasRecursiveCalls.test(method)
    }

    @SuppressWarnings('Instanceof')
    private boolean isRecursiveIn(Expression methodCall, MethodNode method) {
        if (methodCall instanceof MethodCallExpression)
            return new RecursivenessTester().isRecursive(method, (MethodCallExpression) methodCall)
        if (methodCall instanceof StaticMethodCallExpression)
            return new RecursivenessTester().isRecursive(method, (StaticMethodCallExpression) methodCall)
    }
}