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
package org.apache.groovy.parser.antlr4.util

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.GroovyClassVisitor
import org.codehaus.groovy.ast.GroovyCodeVisitor
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.PackageNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ClosureListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.LambdaExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.MethodReferenceExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.SpreadExpression
import org.codehaus.groovy.ast.expr.SpreadMapExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.UnaryPlusExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.BreakStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.ContinueStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.classgen.Verifier
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.io.ReaderSource

import java.lang.reflect.Modifier

/**
 * Generate the groovy source according to the AST.
 * It is useful to verify the equality of new and old parser.
 */
@CompileStatic
class AstDumper {
    private final ModuleNode ast

    AstDumper(final ModuleNode ast) {
        this.ast = ast
    }

    /**
     * Generate the groovy source code according the AST
     *
     * @return the groovy source code
     */
    String gen() {
        try (StringWriter out = new StringWriter()) {
            AstNodeToScriptVisitor visitor = new AstNodeToScriptVisitor(out, true, true)

            new LinkedList<ClassNode>(this.ast?.classes ?: Collections.<ClassNode>emptyList()).sort { c1, c2 -> c1.name <=> c2.name }?.each {
                visitor.call(new SourceUnit((String) null, (ReaderSource) null, null, null, null) {
                    @Override
                    ModuleNode getAST() {
                        return AstDumper.this.ast
                    }
                }, null, it)
            }

            return out.toString().replaceAll(/([\w_$]+)@[0-9a-z]+/, '$1@<hashcode>')
        }
    }
}

/**
 * *****************************************************
 * In order to solve the "Egg & Chicken" problem,
 * we have to copy the source code(instead of invoking it): subprojects/groovy-console/src/main/groovy/groovy/inspect/swingui/AstNodeToScriptAdapter.groovy
 * *****************************************************
 *
 * An adapter from ASTNode tree to source code.
 */
@CompileStatic
class AstNodeToScriptVisitor implements CompilationUnit.IPrimaryClassNodeOperation, GroovyClassVisitor, GroovyCodeVisitor {

    private final Writer _out
    Stack<String> classNameStack = new Stack<String>()
    String _indent = ''
    boolean readyToIndent = true
    boolean showScriptFreeForm
    boolean showScriptClass
    boolean scriptHasBeenVisited

    AstNodeToScriptVisitor(Writer writer, boolean showScriptFreeForm = true, boolean showScriptClass = true) {
        this._out = writer
        this.showScriptFreeForm = showScriptFreeForm
        this.showScriptClass = showScriptClass
        this.scriptHasBeenVisited = false
    }

    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {

        visitPackage(source?.getAST()?.getPackage())

        visitAllImports(source)

        if (showScriptFreeForm && !scriptHasBeenVisited) {
            scriptHasBeenVisited = true
            source?.getAST()?.getStatementBlock()?.visit(this)
        }
        if (showScriptClass || !classNode.isScript()) {
            visitClass classNode
        }
    }

    private def visitAllImports(SourceUnit source) {
        boolean staticImportsPresent = false
        boolean importsPresent = false

        source?.getAST()?.getStaticImports()?.values()?.each {
            visitImport(it)
            staticImportsPresent = true
        }
        source?.getAST()?.getStaticStarImports()?.values()?.each {
            visitImport(it)
            staticImportsPresent = true
        }

        if (staticImportsPresent) {
            printDoubleBreak()
        }

        source?.getAST()?.getImports()?.each {
            visitImport(it)
            importsPresent = true
        }
        source?.getAST()?.getStarImports()?.each {
            visitImport(it)
            importsPresent = true
        }
        if (importsPresent) {
            printDoubleBreak()
        }
    }


    void print(parameter) {
        def output = parameter.toString()

        if (readyToIndent) {
            _out.print _indent
            readyToIndent = false
            while (output.startsWith(' ')) {
                output = output[1..-1]  // trim left
            }
        }
        if (_out.toString().endsWith(' ')) {
            if (output.startsWith(' ')) {
                output = output[1..-1]
            }
        }
        _out.print output
    }

    def println(parameter) {
        throw new UnsupportedOperationException('Wrong API')
    }

    def indented(Closure block) {
        String startingIndent = _indent
        _indent = _indent + '    '
        block()
        _indent = startingIndent
    }

    def printLineBreak() {
        if (!_out.toString().endsWith('\n')) {
            _out.print '\n'
        }
        readyToIndent = true
    }

    def printDoubleBreak() {
        if (_out.toString().endsWith('\n\n')) {
            // do nothing
        } else if (_out.toString().endsWith('\n')) {
            _out.print '\n'
        } else {
            _out.print '\n'
            _out.print '\n'
        }
        readyToIndent = true
    }

    void visitPackage(PackageNode packageNode) {

        if (packageNode) {

            packageNode.annotations?.each {
                visitAnnotationNode(it)
                printLineBreak()
            }

            if (packageNode.text.endsWith('.')) {
                print packageNode.text[0..-2]
            } else {
                print packageNode.text
            }
            printDoubleBreak()
        }
    }

    void visitImport(ImportNode node) {
        if (node) {
            node.annotations?.each {
                visitAnnotationNode(it)
                printLineBreak()
            }
            print node.text
            printLineBreak()
        }
    }

    @Override
    void visitClass(ClassNode node) {

        classNameStack.push(node.name)

        node?.annotations?.each {
            visitAnnotationNode(it)
            printLineBreak()
        }

        visitModifiers(node.modifiers)
        if (node.isInterface()) print node.name
        else print "class $node.name"
        visitGenerics node?.genericsTypes
        boolean first = true
        node.unresolvedInterfaces?.each {
            if (!first) {
                print ', '
            } else {
                print ' implements '
            }
            first = false
            visitType it
        }
        print ' extends '
        visitType node.unresolvedSuperClass
        print ' { '
        printDoubleBreak()

        indented {
            node?.properties?.each { visitProperty(it) }
            printLineBreak()
            node?.fields?.each { visitField(it) }
            printDoubleBreak()
            node?.declaredConstructors?.each { visitConstructor(it) }
            printLineBreak()
            visitObjectInitializerBlocks(node)
            printLineBreak()
            node?.methods?.each { visitMethod(it) }
        }
        print '}'
        printLineBreak()
        classNameStack.pop()
    }

    private void visitObjectInitializerBlocks(ClassNode node) {
        for (Statement stmt : node.getObjectInitializerStatements()) {
            print '{'
            printLineBreak()
            indented {
                stmt.visit(this)
            }
            printLineBreak()
            print '}'
            printDoubleBreak()
        }
    }

    private void visitGenerics(GenericsType[] generics) {

        if (generics) {
            print '<'
            boolean first = true
            generics.each { GenericsType it ->
                if (!first) {
                    print ', '
                }
                first = false
                print it.name
                if (it.upperBounds) {
                    print ' extends '
                    boolean innerFirst = true
                    it.upperBounds.each { ClassNode upperBound ->
                        if (!innerFirst) {
                            print ' & '
                        }
                        innerFirst = false
                        visitType upperBound
                    }
                }
                if (it.lowerBound) {
                    print ' super '
                    visitType it.lowerBound
                }
            }
            print '>'
        }
    }

    @Override
    void visitConstructor(ConstructorNode node) {
        visitMethod(node)
    }

    private String visitParameters(parameters) {
        boolean first = true

        parameters.each { Parameter it ->
            if (!first) {
                print ', '
            }
            first = false

            it.annotations?.each {
                visitAnnotationNode(it)
                print(' ')
            }

            visitModifiers(it.modifiers)
            visitType it.type
            print ' ' + it.name
            if (it.initialExpression && !(it.initialExpression instanceof EmptyExpression)) {
                print ' = '
                it.initialExpression.visit this
            }
        }
    }

    @Override
    void visitMethod(MethodNode node) {
        node?.annotations?.each {
            visitAnnotationNode(it)
            printLineBreak()
        }

        visitModifiers(node.modifiers)
        if (node.name == '<init>') {
            print "${classNameStack.peek()}("
            visitParameters(node.parameters)
            print ') {'
            printLineBreak()
        } else if (node.name == '<clinit>') {
            print '{ ' // will already have 'static' from modifiers
            printLineBreak()
        } else {
            visitType node.returnType
            print " $node.name("
            visitParameters(node.parameters)
            print ')'
            if (node.exceptions) {
                boolean first = true
                print ' throws '
                node.exceptions.each {
                    if (!first) {
                        print ', '
                    }
                    first = false
                    visitType it
                }
            }
            print ' {'
            printLineBreak()
        }

        indented {
            node?.code?.visit(this)
        }
        printLineBreak()
        print '}'
        printDoubleBreak()
    }

    private void visitModifiers(int modifiers) {
        String mods = Modifier.toString(modifiers)
        mods = mods ? mods + ' ' : mods
        print mods
    }

    @Override
    void visitField(FieldNode node) {
        node?.annotations?.each {
            visitAnnotationNode(it)
            printLineBreak()
        }
        visitModifiers(node.modifiers)
        visitType node.type
        print " $node.name "
        // do not print initial expression, as this is executed as part of the constructor, unless on static constant
        Expression exp = node.initialValueExpression
        if (exp instanceof ConstantExpression) exp = Verifier.transformToPrimitiveConstantIfPossible(exp)
        ClassNode type = exp?.type
        if (Modifier.isStatic(node.modifiers) && Modifier.isFinal(node.getModifiers())
                && exp instanceof ConstantExpression
                && type == node.type
                && ClassHelper.isStaticConstantInitializerType(type)) {
            // GROOVY-5150: final constants may be initialized directly
            print ' = '
            if (ClassHelper.STRING_TYPE == type) {
                print "'" + node.initialValueExpression.text.replaceAll("'", "\\\\'") + "'"
            } else if (ClassHelper.char_TYPE == type) {
                print "'${node.initialValueExpression.text}'"
            } else {
                print node.initialValueExpression.text
            }
        }
        printLineBreak()
    }

    void visitAnnotationNode(AnnotationNode node) {
        print '@' + node?.classNode?.name
        if (node?.members) {
            print '('
            boolean first = true
            node.members.each { String name, Expression value ->
                if (first) {
                    first = false
                } else {
                    print ', '
                }
                print name + ' = '
                value.visit(this)
            }
            print ')'
        }

    }

    @Override
    void visitProperty(PropertyNode node) {
        // is a FieldNode, avoid double dispatch
    }

    @Override
    void visitBlockStatement(BlockStatement block) {
        if (printStatementLabels(block)) {
            print '{'
            printLineBreak()
            indented {
                block?.statements?.each {
                    it.visit(this)
                    printLineBreak()
                }
            }
            print '}'
            printLineBreak()
        } else {
            block?.statements?.each {
                it.visit(this)
                printLineBreak()
            }
        }
        if (!_out.toString().endsWith('\n')) {
            printLineBreak()
        }
    }

    @Override
    void visitForLoop(ForStatement statement) {
        printStatementLabels(statement)
        print 'for ('
        if (statement?.variable != ForStatement.FOR_LOOP_DUMMY) {
            visitParameters([statement.variable])
            print ' : '
        }

        if (statement?.collectionExpression instanceof ListExpression) {
            statement?.collectionExpression?.visit this
        } else {
            statement?.collectionExpression?.visit this
        }
        print ') {'
        printLineBreak()
        indented {
            statement?.loopBlock?.visit this
        }
        print '}'
        printLineBreak()
    }

    @Override
    void visitIfElse(IfStatement ifElse) {
        printStatementLabels(ifElse)
        print 'if ('
        ifElse?.booleanExpression?.visit this
        print ') {'
        printLineBreak()
        indented {
            ifElse?.ifBlock?.visit this
        }
        printLineBreak()
        if (ifElse?.elseBlock && !(ifElse.elseBlock instanceof EmptyStatement)) {
            print '} else {'
            printLineBreak()
            indented {
                ifElse?.elseBlock?.visit this
            }
            printLineBreak()
        }
        print '}'
        printLineBreak()
    }

    @Override
    void visitExpressionStatement(ExpressionStatement statement) {
        statement.expression.visit this
    }

    @Override
    void visitReturnStatement(ReturnStatement statement) {
        printLineBreak()
        print 'return '
        statement.getExpression().visit(this)
        printLineBreak()
    }

    @Override
    void visitSwitch(SwitchStatement statement) {
        printStatementLabels(statement)
        print 'switch ('
        statement?.expression?.visit this
        print ') {'
        printLineBreak()
        indented {
            statement?.caseStatements?.each {
                visitCaseStatement it
            }
            if (statement?.defaultStatement) {
                print 'default: '
                printLineBreak()
                statement?.defaultStatement?.visit this
            }
        }
        print '}'
        printLineBreak()
    }

    @Override
    void visitCaseStatement(CaseStatement statement) {
        print 'case '
        statement?.expression?.visit this
        print ':'
        printLineBreak()
        indented {
            statement?.code?.visit this
        }
    }

    @Override
    void visitBreakStatement(BreakStatement statement) {
        print 'break'
        if (statement?.label) {
            print ' ' + statement.label
        }
        printLineBreak()
    }

    @Override
    void visitContinueStatement(ContinueStatement statement) {
        print 'continue'
        if (statement?.label) {
            print ' ' + statement.label
        }
        printLineBreak()
    }

    @Override
    void visitMethodCallExpression(MethodCallExpression expression) {

        Expression objectExp = expression.getObjectExpression()
        if (objectExp instanceof VariableExpression) {
            visitVariableExpression(objectExp, false)
        } else {
            objectExp.visit(this)
        }
        if (expression.spreadSafe) {
            print '*'
        }
        if (expression.safe) {
            print '?'
        }
        print '.'
        Expression method = expression.getMethod()
        if (method instanceof ConstantExpression) {
            visitConstantExpression(method, true)
        } else {
            method.visit(this)
        }
        expression.getArguments().visit(this)
    }

    @Override
    void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
        print expression?.ownerType?.name + '.' + expression?.method
        if (expression?.arguments instanceof VariableExpression || expression?.arguments instanceof MethodCallExpression) {
            print '('
            expression?.arguments?.visit this
            print ')'
        } else {
            expression?.arguments?.visit this
        }
    }

    @Override
    void visitConstructorCallExpression(ConstructorCallExpression expression) {
        if (expression?.isSuperCall()) {
            print 'super'
        } else if (expression?.isThisCall()) {
            print 'this '
        } else {
            print 'new '
            visitType expression?.type
        }
        expression?.arguments?.visit this
    }

    @Override
    void visitBinaryExpression(BinaryExpression expression) {
        expression?.leftExpression?.visit this
        print " $expression.operation.text "
        expression.rightExpression.visit this

        if (expression?.operation?.text == '[') {
            print ']'
        }
    }

    @Override
    void visitPostfixExpression(PostfixExpression expression) {
        print '('
        expression?.expression?.visit this
        print ')'
        print expression?.operation?.text
    }

    @Override
    void visitPrefixExpression(PrefixExpression expression) {
        print expression?.operation?.text
        print '('
        expression?.expression?.visit this
        print ')'
    }


    @Override
    void visitClosureExpression(ClosureExpression expression) {
        print '{ '
        if (expression?.parameters) {
            visitParameters(expression?.parameters)
            print ' ->'
        }
        printLineBreak()
        indented {
            expression?.code?.visit this
        }
        print '}'
    }

    @Override
    void visitLambdaExpression(LambdaExpression expression) {
        print '( '
        if (expression?.parameters) {
            visitParameters(expression?.parameters)
        }
        print ') -> {'
        printLineBreak()
        indented {
            expression?.code?.visit this
        }
        print '}'
    }

    @Override
    void visitTupleExpression(TupleExpression expression) {
        print '('
        visitExpressionsAndCommaSeparate(expression?.expressions)
        print ')'
    }

    @Override
    void visitRangeExpression(RangeExpression expression) {
        print '('
        expression?.from?.visit this
        print '..'
        expression?.to?.visit this
        print ')'
    }

    @Override
    void visitPropertyExpression(PropertyExpression expression) {
        expression?.objectExpression?.visit this
        if (expression?.spreadSafe) {
            print '*'
        } else if (expression?.isSafe()) {
            print '?'
        }
        print '.'
        if (expression?.property instanceof ConstantExpression) {
            visitConstantExpression((ConstantExpression) expression?.property, true)
        } else {
            expression?.property?.visit this
        }
    }

    @Override
    void visitAttributeExpression(AttributeExpression attributeExpression) {
        visitPropertyExpression attributeExpression
    }

    @Override
    void visitFieldExpression(FieldExpression expression) {
        print expression?.field?.name
    }

    void visitConstantExpression(ConstantExpression expression, boolean unwrapQuotes = false) {
        if (expression.value instanceof String && !unwrapQuotes) {
            // string reverse escaping is very naive
            def escaped = ((String) expression.value).replaceAll('\n', '\\\\n').replaceAll("'", "\\\\'")
            print "'$escaped'"
        } else {
            print expression.value
        }
    }

    @Override
    void visitClassExpression(ClassExpression expression) {
        print expression.text
    }

    void visitVariableExpression(VariableExpression expression, boolean spacePad = true) {

        if (spacePad) {
            print ' ' + expression.name + ' '
        } else {
            print expression.name
        }
    }

    @Override
    void visitDeclarationExpression(DeclarationExpression expression) {
        // handle multiple assignment expressions
        if (expression?.leftExpression instanceof ArgumentListExpression) {
            print 'def '
            visitArgumentlistExpression((ArgumentListExpression) expression?.leftExpression, true)
            print " $expression.operation.text "
            expression.rightExpression.visit this

            if (expression?.operation?.text == '[') {
                print ']'
            }
        } else {
            visitType expression?.leftExpression?.type
            visitBinaryExpression expression // is a BinaryExpression
        }
    }

    @Override
    void visitGStringExpression(GStringExpression expression) {
        print '"' + expression.text + '"'
    }

    @Override
    void visitSpreadExpression(SpreadExpression expression) {
        print '*'
        expression?.expression?.visit this
    }

    @Override
    void visitNotExpression(NotExpression expression) {
        print '!('
        expression?.expression?.visit this
        print ')'
    }

    @Override
    void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        print '-('
        expression?.expression?.visit this
        print ')'
    }

    @Override
    void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        print '+('
        expression?.expression?.visit this
        print ')'
    }

    @Override
    void visitCastExpression(CastExpression expression) {
        print '(('
        expression?.expression?.visit this
        print ') as '
        visitType(expression?.type)
        print ')'

    }

    /**
     * Prints out the type, safely handling arrays.
     * @param classNode
     */
    void visitType(ClassNode classNode) {
        def name = classNode.name
        if (name =~ /^\[+L/ && name.endsWith(';')) {
            int numDimensions = name.indexOf('L')
            print "${classNode.name[(numDimensions + 1)..-2]}" + ('[]' * numDimensions)
        } else {
            print name
        }
        visitGenerics classNode?.genericsTypes
    }

    void visitArgumentlistExpression(ArgumentListExpression expression, boolean showTypes = false) {
        print '('
        int count = expression?.expressions?.size()
        expression.expressions.each {
            if (showTypes) {
                visitType it.type
                print ' '
            }
            if (it instanceof VariableExpression) {
                visitVariableExpression it, false
            } else if (it instanceof ConstantExpression) {
                visitConstantExpression it, false
            } else {
                it.visit this
            }
            count--
            if (count) print ', '
        }
        print ')'
    }

    @Override
    void visitBytecodeExpression(BytecodeExpression expression) {
        print '/*BytecodeExpression*/'
        printLineBreak()
    }


    @Override
    void visitMapExpression(MapExpression expression) {
        print '['
        if (expression?.mapEntryExpressions?.size() == 0) {
            print ':'
        } else {
            visitExpressionsAndCommaSeparate((List) expression?.mapEntryExpressions)
        }
        print ']'
    }

    @Override
    void visitMapEntryExpression(MapEntryExpression expression) {
        if (expression?.keyExpression instanceof SpreadMapExpression) {
            print '*'            // is this correct?
        } else {
            expression?.keyExpression?.visit this
        }
        print ': '
        expression?.valueExpression?.visit this
    }

    @Override
    void visitListExpression(ListExpression expression) {
        print '['
        visitExpressionsAndCommaSeparate(expression?.expressions)
        print ']'
    }

    @Override
    void visitTryCatchFinally(TryCatchStatement statement) {
        printStatementLabels(statement)
        print 'try {'
        printLineBreak()
        indented {
            statement?.tryStatement?.visit this
        }
        printLineBreak()
        print '} '
        printLineBreak()
        statement?.catchStatements?.each { CatchStatement catchStatement ->
            visitCatchStatement(catchStatement)
        }
        print 'finally { '
        printLineBreak()
        indented {
            statement?.finallyStatement?.visit this
        }
        print '} '
        printLineBreak()
    }

    @Override
    void visitThrowStatement(ThrowStatement statement) {
        print 'throw '
        statement?.expression?.visit this
        printLineBreak()
    }

    @Override
    void visitSynchronizedStatement(SynchronizedStatement statement) {
        printStatementLabels(statement)
        print 'synchronized ('
        statement?.expression?.visit this
        print ') {'
        printLineBreak()
        indented {
            statement?.code?.visit this
        }
        print '}'
    }

    @Override
    void visitTernaryExpression(TernaryExpression expression) {
        expression?.booleanExpression?.visit this
        print ' ? '
        expression?.trueExpression?.visit this
        print ' : '
        expression?.falseExpression?.visit this
    }

    @Override
    void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression)
    }

    @Override
    void visitBooleanExpression(BooleanExpression expression) {
        expression?.expression?.visit this
    }

    @Override
    void visitWhileLoop(WhileStatement statement) {
        printStatementLabels(statement)
        print 'while ('
        statement?.booleanExpression?.visit this
        print ') {'
        printLineBreak()
        indented {
            statement?.loopBlock?.visit this
        }
        printLineBreak()
        print '}'
        printLineBreak()
    }

    @Override
    void visitDoWhileLoop(DoWhileStatement statement) {
        printStatementLabels(statement)
        print 'do {'
        printLineBreak()
        indented {
            statement?.loopBlock?.visit this
        }
        print '} while ('
        statement?.booleanExpression?.visit this
        print ')'
        printLineBreak()
    }

    @Override
    void visitCatchStatement(CatchStatement statement) {
        print 'catch ('
        visitParameters([statement.variable])
        print ') {'
        printLineBreak()
        indented {
            statement.code?.visit this
        }
        print '} '
        printLineBreak()
    }

    @Override
    void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        print '~('
        expression?.expression?.visit this
        print ') '
    }

    @Override
    void visitAssertStatement(AssertStatement statement) {
        print 'assert '
        statement?.booleanExpression?.visit this
        print ' : '
        statement?.messageExpression?.visit this
    }

    @Override
    void visitClosureListExpression(ClosureListExpression expression) {
        boolean first = true
        expression?.expressions?.each {
            if (!first) {
                print ';'
            }
            first = false
            it.visit this
        }
    }

    @Override
    void visitMethodPointerExpression(MethodPointerExpression expression) {
        expression?.expression?.visit this
        print '.&'
        expression?.methodName?.visit this
    }

    @Override
    void visitMethodReferenceExpression(MethodReferenceExpression expression) {
        expression?.expression?.visit this
        print '::'
        expression?.methodName?.visit this
    }

    @Override
    void visitArrayExpression(ArrayExpression expression) {
        print 'new '
        visitType expression?.elementType
        print '['
        visitExpressionsAndCommaSeparate(expression?.sizeExpression)
        print ']'
    }

    private void visitExpressionsAndCommaSeparate(List<? super Expression> expressions) {
        boolean first = true
        expressions?.each {
            if (!first) {
                print ', '
            }
            first = false
            ((ASTNode) it).visit this
        }
    }

    @Override
    void visitSpreadMapExpression(SpreadMapExpression expression) {
        print '*:'
        expression?.expression?.visit this
    }

    /**
     * Prints all labels for the given statement.  The labels will be printed on a single
     * line and line break will be added.
     *
     * @param statement for which to print labels
     * @return {@code true} if the statement had labels to print, else {@code false}
     */
    private boolean printStatementLabels(Statement statement) {
        List<String> labels = statement?.statementLabels
        if (labels == null || labels.isEmpty()) {
            return false
        }
        for (String label : labels) {
            print label + ':'
            printLineBreak()
        }
        return true
    }

}
