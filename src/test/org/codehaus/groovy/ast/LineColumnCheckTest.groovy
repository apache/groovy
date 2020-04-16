/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership. The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.ast

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
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
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
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Tests the LineColumn information in file with path specified by the prefix TEST_FILE_PREFIX.
 * The base version contains tests that should work with both the antlr2 and antlr4 parser.
 * The suffixed versions work with just the respective parser. In general, the antlr4 parser
 * has more accurate line/column information in a number of situations.
 *
 * The file in the specified path should look like:
 *
 * ###<testname>:::
 * <source code from which the AST will be built>
 * :::<expected AST output>
 *
 * The section above can be repeated for every new TestCase
 *
 * The AST output from the visitor is quite big. Also for small source code snippets. Therefore
 * it is possible to only specify the nodes that you want to check and separate them with a semicolon.
 * A semicolon is also needed when you begin with a new line.
 * Example:
 * [TryCatchStatement,(1:1),(9:2)][BlockStatement,(1:5),(3:3)];
 * [CatchStatement,(3:3),(5:3)][BlockStatement,(3:12),(5:3)];
 * [CatchStatement,(5:3),(7:3)][BlockStatement,(5:12),(7:3)];
 * [BlockStatement,(7:3),(9:2)][BlockStatement,(7:11),(9:2)]
 *
 * [<NodeType>,(<line>:<column>),(<lastLine>:<lastColumn>)]
 */
@RunWith(Parameterized)
final class LineColumnCheckTest extends ASTTest {

    static final String TEST_FILE_PREFIX = './src/test/org/codehaus/groovy/ast/LineColumnCheck'

    private LineCheckVisitor visitor
    private String name
    private String source
    private String[] expected

    @Parameterized.Parameters(name = 'Test {0}: Source: {1} Expected: {2}')
    static Iterable<Object[]> data() {
        List testdata = extractData("${TEST_FILE_PREFIX}.txt")
        //flip if condition as per below and swap antlr2/4 ordering once antlr4 is the default
        //if (System.getProperty('groovy.antlr4') != 'false') {
        testdata += extractData("${TEST_FILE_PREFIX}_antlr4.txt")
        testdata
    }

    private static List extractData(String test_file_path) {
        String content = new File(test_file_path).text
        String[] tests = content.split('###')
        tests = tests.drop(1) // remove apache header
        List testdata = []
        for (String test : tests) {
            testdata << (test.split(':::').collect { it.trim() } as Object[])
        }
        testdata
    }

    LineColumnCheckTest(String name, String source, String expected) {
        this.name = name
        this.source = source
        this.expected = expected.split(';')
    }

    @Before
    void setUp() {
        visitor = new LineCheckVisitor()
    }

    @Test
    void testLineColumnInfo() {
        visitor.visitModuleNode(getAST(source))
        String was = visitor.getASTString()
        //comment out next line to view the output of the visitor
        //println(name + ': ' + was)
        for (String anExpected : expected) {
            // FIXME
            // def ii = 17      // <1>
            // Object ii = 17   // <2>
            //
            // The class node `Object` is cached, but its node position will be configured at any places where it appears, e.g. <2>
            // Though `def` is an alias of `Object`, its node position will NOT be configured.
            // Since `ClassHelper.OBJECT_TYPE` and `ClassHelper.DYNAMIC_TYPE` are global variable and are assigned to a same value `ClassHelper.makeCached(Object.class)`,
            // if the node position of `ClassHelper.OBJECT_TYPE` is configured, the node position of `ClassHelper.DYNAMIC_TYPE` will change too,
            // but the node position of `ClassHelper.DYNAMIC_TYPE` can not be reset to a correct value as `def` will not be configured, e.g. <1>
            if (source.contains('def ii = 17') && '[ExpressionStatement,(2:1),(2:12)][ClassNode,(-1:-1),(-1:-1)][DeclarationExpression,(2:1),(2:12)]' == anExpected.trim()) {
                continue
            }

            assertTrue("'" + anExpected + "' not found in '" + was + "'", was.indexOf(anExpected.trim()) != -1)
        }
    }
}

/**
 *
 * Visitor to write for each visited node a string like:
 * [<NodeType>,(<line>:<column>),(<lastLine>:<lastColumn>)]
 *
 */
class LineCheckVisitor extends ClassCodeVisitorSupport {

    private StringBuffer astString = new StringBuffer()

    String getASTString() {
        astString.toString()
    }

    protected void visitStatement(Statement statement) {
        visitNode(statement)
    }

    protected void visitType(ClassNode node) {
        visitNode(node)
        visitGenerics(node)
    }

    protected void visitTypes(ClassNode[] classNodes) {
        if (classNodes != null) {
            for (ClassNode classNode : classNodes) {
                visitType(classNode)
            }
        }
    }

    protected void visitGenerics(ClassNode node) {
        if (node.isUsingGenerics()) {
            GenericsType[] generics = node.getGenericsTypes()
            if(generics == null) return
            for (GenericsType genericType : generics) {
                visitNode(genericType)
                visitType(genericType.getType())
                if (genericType.getLowerBound() != null) {
                    visitType(genericType.getLowerBound())
                }
                visitTypes(genericType.getUpperBounds())
            }
        }
    }

    protected void visitNodes(ASTNode[] nodes) {
        if (nodes != null) {
            for (ASTNode node : nodes) {
                visitNode(node)
            }
        }
    }

    protected void visitNode(ASTNode node) {
        String nodeName = node.getClass().getName()
        //get classname without package
        nodeName = nodeName.substring(nodeName.lastIndexOf('.') + 1,nodeName.length())
        astString.append('[')
        astString.append(nodeName)
        astString.append(',(')
        astString.append(node.getLineNumber())
        astString.append(':')
        astString.append(node.getColumnNumber())
        astString.append('),(')
        astString.append(node.getLastLineNumber())
        astString.append(':')
        astString.append(node.getLastColumnNumber())
        astString.append(')]')
        //String of each node looks like: [AssertStatement,(1:1),(1:20)]
    }

    SourceUnit getSourceUnit() {
        null
    }

    void visitModuleNode(ModuleNode moduleNode) {

        //visit imports like import java.io.File and import java.io.File as MyFile
        for (ImportNode importNode : moduleNode.getImports()) {
            visitNode(importNode.getType())
        }

        //visit static imports like import java.lang.Math.*
        for (ImportNode importNode : moduleNode.getStaticStarImports().values()) {
            visitNode(importNode.getType())
        }

        //visit static imports like import java.lang.Math.cos
        for (ImportNode importNode : moduleNode.getStaticImports().values()) {
            visitNode(importNode.getType())
        }

        for (ClassNode classNode : moduleNode.getClasses()) {
            if (!classNode.isScript()) {
                visitClass(classNode)
            } else {
                for (MethodNode method : moduleNode.getMethods()) {
                    visitMethod(method)
                }
            }
        }
        //visit Statements that are not inside a class
        if (!moduleNode.getStatementBlock().isEmpty()) {
            visitBlockStatement(moduleNode.getStatementBlock())
        }
    }

    void visitClass(ClassNode node) {
        visitType(node)
        visitType(node.getUnresolvedSuperClass())
        visitTypes(node.getInterfaces())
        super.visitClass(node)
    }

    void visitAnnotations(AnnotatedNode node) {
        List<AnnotationNode> annotationMap = node.getAnnotations()
        if (annotationMap.isEmpty()) return
        visitNode(node)
        for (AnnotationNode annotationNode : annotationMap) {
            visitNode(annotationNode)
        }
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        visitAnnotations(node)
        analyseMethodHead(node)
        Statement code = node.getCode()

        visitClassCodeContainer(code)
    }

    private void analyseMethodHead(MethodNode node) {
        visitNode(node.getReturnType())
        analyseParameters(node.getParameters())
        visitNodes(node.getExceptions())
    }

    private void analyseParameters(Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            visitType(parameter.getOriginType())
            if (parameter.hasInitialExpression()) {
                parameter.getInitialExpression().visit(this)
            }
        }
    }

    void visitConstructor(ConstructorNode node) {
        visitNode(node)
        super.visitConstructor(node)
    }

    void visitMethod(MethodNode node) {
        visitNode(node)
        super.visitMethod(node)
    }

    void visitField(FieldNode node) {
        // Do not visit fields which are manually added due to optimization
        if (!node.getName().startsWith('$')) {
            visitType(node.getOriginType())
            visitNode(node)
            super.visitField(node)
        }
    }

    void visitProperty(PropertyNode node) {
        // do nothing, also visited as FieldNode
    }

    /*
     * Statements
     *
     * Statements not written here are visited in ClassCodeVisitorSupport and call there
     * visitStatement(Statement statement) which is overridden in this class
     */

    /*
     * Expressions
     */
    void visitMethodCallExpression(MethodCallExpression call) {
        visitNode(call)
        super.visitMethodCallExpression(call)
    }

    void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        visitNode(call)
        super.visitStaticMethodCallExpression(call)
    }

    void visitConstructorCallExpression(ConstructorCallExpression call) {
        visitNode(call)
        visitType(call.getType())
        super.visitConstructorCallExpression(call)
    }

    void visitBinaryExpression(BinaryExpression expression) {
        visitNode(expression)
        super.visitBinaryExpression(expression)
    }

    void visitTernaryExpression(TernaryExpression expression) {
        visitNode(expression)
        super.visitTernaryExpression(expression)
    }

    void visitPostfixExpression(PostfixExpression expression) {
        visitNode(expression)
        super.visitPostfixExpression(expression)
    }

    void visitPrefixExpression(PrefixExpression expression) {
        visitNode(expression)
        super.visitPrefixExpression(expression)
    }

    void visitBooleanExpression(BooleanExpression expression) {
        visitNode(expression)
        super.visitBooleanExpression(expression)
    }

    void visitNotExpression(NotExpression expression) {
        visitNode(expression)
        super.visitNotExpression(expression)
    }

    void visitClosureExpression(ClosureExpression expression) {
        visitNode(expression)
        super.visitClosureExpression(expression)
    }

    void visitTupleExpression(TupleExpression expression) {
        visitNode(expression)
        super.visitTupleExpression(expression)
    }

    void visitListExpression(ListExpression expression) {
        visitNode(expression)
        super.visitListExpression(expression)
    }

    void visitArrayExpression(ArrayExpression expression) {
        visitNode(expression)
        visitNode(expression.getElementType())
        super.visitArrayExpression(expression)
    }

    void visitMapExpression(MapExpression expression) {
        visitNode(expression)
        super.visitMapExpression(expression)
    }

    void visitMapEntryExpression(MapEntryExpression expression) {
        visitNode(expression)
        super.visitMapEntryExpression(expression)
    }

    void visitRangeExpression(RangeExpression expression) {
        visitNode(expression)
        super.visitRangeExpression(expression)
    }

    void visitSpreadExpression(SpreadExpression expression) {
        visitNode(expression)
        super.visitSpreadExpression(expression)
    }

    void visitSpreadMapExpression(SpreadMapExpression expression) {
        visitNode(expression)
        super.visitSpreadMapExpression(expression)
    }

    void visitMethodPointerExpression(MethodPointerExpression expression) {
        visitNode(expression)
        super.visitMethodPointerExpression(expression)
    }

    void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        visitNode(expression)
        super.visitBitwiseNegationExpression(expression)
    }

    void visitCastExpression(CastExpression expression) {
        visitNode(expression)
        visitType(expression.getType())
        super.visitCastExpression(expression)
    }

    void visitConstantExpression(ConstantExpression expression) {
        visitNode(expression)
        super.visitConstantExpression(expression)
    }

    void visitClassExpression(ClassExpression expression) {
        visitNode(expression)
        super.visitClassExpression(expression)
    }

    void visitVariableExpression(VariableExpression expression) {
        visitNode(expression)
        super.visitVariableExpression(expression)
    }

    void visitDeclarationExpression(DeclarationExpression expression) {
        //visitNode(expression) is visited afterwards in BinaryExpression. Because
        //super.visitDeclarationExpression calls visitBinaryExpression
        visitType(expression.getLeftExpression().getType())
        super.visitDeclarationExpression(expression)
    }

    void visitPropertyExpression(PropertyExpression expression) {
        visitNode(expression)
        super.visitPropertyExpression(expression)
    }

    void visitAttributeExpression(AttributeExpression expression) {
        visitNode(expression)
        super.visitAttributeExpression(expression)
    }

    void visitFieldExpression(FieldExpression expression) {
        visitNode(expression)
        super.visitFieldExpression(expression)
    }

    void visitGStringExpression(GStringExpression expression) {
        visitNode(expression)
        super.visitGStringExpression(expression)
    }

    void visitArgumentlistExpression(ArgumentListExpression ale) {
        //visitNode(ale) is visited afterwards in TupleExpression. Because
        //super.visitArgumentlistExpression calls visitTupleExpression
        super.visitArgumentlistExpression(ale)
    }

    void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitNode(expression)
        super.visitShortTernaryExpression(expression)
    }

    void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        visitNode(expression)
        super.visitUnaryPlusExpression(expression)
    }

    void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        visitNode(expression)
        super.visitUnaryMinusExpression(expression)
    }

    void visitClosureListExpression(ClosureListExpression cle) {
        visitNode(cle)
        super.visitClosureListExpression(cle)
    }
}
