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
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.MixinNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression
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
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression
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
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

/**
 * Handles parsing the properties from the closure into values that can be referenced.
 *
 * This object is very stateful and not threadsafe. It accumulates expressions in the 
 * 'expression' field as they are found and executed within the DSL. 
 *
 * Note: this class consists of many one-line method calls. A better implementation
 * might be to take a declarative approach and replace the one-liners with map entries. 
 */
class AstSpecificationCompiler implements GroovyInterceptable {

    private final List<ASTNode> expression = []

    /**
     * Creates the DSL compiler.
     */
    AstSpecificationCompiler(@DelegatesTo(AstSpecificationCompiler) Closure spec) {
        spec.delegate = this
        spec()
    }

    /**
     * Gets the current generated expression.
     */
    List<ASTNode> getExpression() {
        return expression
    }

    /**
     * This method takes a List of Classes (a "spec"), and makes sure that the expression field
     * contains those classes. It is a safety mechanism to enforce that the DSL is being called
     * properly.
     *
     * @param methodName
     *   the name of the method within the DSL that is being invoked. Used in creating error messages.
     * @param spec
     *   the list of Class objects that the method expects to have in the expression field when invoked.
     * @return
     *   the portions of the expression field that adhere to the spec.
     */
    private List<ASTNode> enforceConstraints(String methodName, List<Class> spec) {

        // enforce that the correct # arguments was passed
        if (spec.size() != expression.size()) {
            throw new IllegalArgumentException("$methodName could not be invoked. Expected to receive parameters $spec but found ${expression?.collect { it.class }}")
        }

        // enforce types and collect result
        (0..(spec.size() - 1)).collect { int it ->
            def actualClass = expression[it].class
            def expectedClass = spec[it]
            if (!expectedClass.isAssignableFrom(actualClass)) {
                throw new IllegalArgumentException("$methodName could not be invoked. Expected to receive parameters $spec but found ${expression?.collect { it.class }}")
            }
            expression[it]
        }
    }

    /**
     * This method helps you take Closure parameters to a method and bundle them into
     * constructor calls to a specific ASTNode subtype.
     * @param name
     *       name of object being constructed, used to create helpful error message.
     * @param argBlock
     *       the actual parameters being specified for the node
     * @param constructorStatement
     *       the type specific construction code that will be run
     */
    @CompileStatic
    private void captureAndCreateNode(String name, @DelegatesTo(AstSpecificationCompiler) Closure argBlock, Closure constructorStatement) {
        if (!argBlock) throw new IllegalArgumentException("nodes of type $name require arguments to be specified")

        def oldProps = new ArrayList(expression)
        expression.clear()
        new AstSpecificationCompiler(argBlock)
        def result = constructorStatement(expression) // invoke custom constructor for node
        expression.clear()
        expression.addAll(oldProps)
        ((List) expression).add(result)
    }

    /**
     * Helper method to convert a DSL invocation into an ASTNode instance.
     *
     * @param target
     *       the class you are going to create
     * @param typeAlias
     *       the DSL keyword that was used to invoke this type
     * @param ctorArgs
     *       a specification of what arguments the constructor expects
     * @param argBlock
     *       the single closure argument used during invocation
     */
    private void makeNode(Class target, String typeAlias, List<Class<? super ASTNode>> ctorArgs, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode(target.class.simpleName, argBlock) {
            target.newInstance(*enforceConstraints(typeAlias, ctorArgs))
        }
    }

    /**
     * Helper method to convert a DSL invocation with a list of parameters specified
     * in a Closure into an ASTNode instance.
     *
     * @param target
     *       the class you are going to create
     * @param argBlock
     *       the single closure argument used during invocation
     */
    private void makeNodeFromList(Class target, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        //todo: add better error handling?
        captureAndCreateNode(target.simpleName, argBlock) {
            target.newInstance(new ArrayList(expression))
        }
    }

    /**
     * Helper method to convert a DSL invocation with a String parameter into a List of ASTNode instances.
     *
     * @param argBlock
     *       the single closure argument used during invocation
     * @param input
     *       the single String argument used during invocation
     */
    private void makeListOfNodes(@DelegatesTo(AstSpecificationCompiler) Closure argBlock, String input) {
        captureAndCreateNode(input, argBlock) {
            new ArrayList(expression)
        }
    }

    /**
     * Helper method to convert a DSL invocation with a String parameter into an Array of ASTNode instances.
     *
     * @param argBlock
     *       the single closure argument used during invocation
     * @param target
     *       the target type
     */
    private void makeArrayOfNodes(Object target, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode(target.class.simpleName, argBlock) {
            expression.toArray(target)
        }
    }

    /**
     * Helper method to convert a DSL invocation into an ASTNode instance when a Class parameter is specified.
     *
     * @param target
     *       the class you are going to create
     * @param alias
     *       the DSL keyword that was used to invoke this type
     * @param spec
     *       the list of Classes that you expect to be present as parameters
     * @param argBlock
     *       the single closure argument used during invocation
     * @param type
     *       a type parameter
     */
    private void makeNodeWithClassParameter(Class target, String alias, List<Class> spec, @DelegatesTo(AstSpecificationCompiler) Closure argBlock, Class type) {
        captureAndCreateNode(target.class.simpleName, argBlock) {
            expression.add(0, ClassHelper.make(type))
            target.newInstance(*enforceConstraints(alias, spec))
        }
    }

    private void makeNodeWithStringParameter(Class target, String alias, List<Class> spec, @DelegatesTo(AstSpecificationCompiler) Closure argBlock, String text) {
        captureAndCreateNode(target.class.simpleName, argBlock) {
            expression.add(0, text)
            target.newInstance(*enforceConstraints(alias, spec))
        }
    }

    /**
     * Creates a CastExpression.
     */
    void cast(Class type, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNodeWithClassParameter(CastExpression, 'cast', [ClassNode, Expression], argBlock, type)
    }

    /**
     * Creates an ConstructorCallExpression.
     */
    void constructorCall(Class type, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNodeWithClassParameter(ConstructorCallExpression, 'constructorCall', [ClassNode, Expression], argBlock, type)
    }

    /**
     * Creates a MethodCallExpression.
     */
    void methodCall(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(MethodCallExpression, 'methodCall', [Expression, Expression, Expression], argBlock)
    }

    /**
     * Creates an AnnotationConstantExpression.
     */
    void annotationConstant(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(AnnotationConstantExpression, 'annotationConstant', [AnnotationNode], argBlock)
    }

    /**
     * Creates a PostfixExpression.
     */
    void postfix(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(PostfixExpression, 'postfix', [Expression, Token], argBlock)
    }

    /**
     * Creates a FieldExpression.
     */
    void field(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(FieldExpression, 'field', [FieldNode], argBlock)
    }

    /**
     * Creates a MapExpression.
     */
    void map(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNodeFromList(MapExpression, argBlock)
    }

    /**
     * Creates a TupleExpression.
     */
    void tuple(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNodeFromList(TupleExpression, argBlock)
    }

    /**
     * Creates a MapEntryExpression.
     */
    void mapEntry(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(MapEntryExpression, 'mapEntry', [Expression, Expression], argBlock)
    }

    /**
     * Creates a gString.
     */
    void gString(String verbatimText, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNodeWithStringParameter(GStringExpression, 'gString', [String, List, List], argBlock, verbatimText)
    }


    /**
     * Creates a methodPointer.
     */

    void methodPointer(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(MethodPointerExpression, 'methodPointer', [Expression, Expression], argBlock)
    }

    /**
     * Creates a property.
     */
    void property(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(PropertyExpression, 'property', [Expression, Expression], argBlock)
    }

    /**
     * Creates a RangeExpression.
     */
    void range(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(RangeExpression, 'range', [Expression, Expression, Boolean], argBlock)
    }

    /**
     * Creates EmptyStatement.
     */
    void empty() {
        expression << EmptyStatement.INSTANCE
    }

    /**
     * Creates a label.
     */
    void label(String label) {
        expression << label
    }

    /**
     * Creates an ImportNode.
     */
    void importNode(Class target, String alias = null) {
        expression << new ImportNode(ClassHelper.make(target), alias)
    }

    /**
     * Creates a CatchStatement.
     */
    void catchStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(CatchStatement, 'catchStatement', [Parameter, Statement], argBlock)
    }

    /**
     * Creates a ThrowStatement.
     */
    void throwStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(ThrowStatement, 'throwStatement', [Expression], argBlock)
    }

    /**
     * Creates a SynchronizedStatement.
     */
    void synchronizedStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(SynchronizedStatement, 'synchronizedStatement', [Expression, Statement], argBlock)
    }

    /**
     * Creates a ReturnStatement.
     */
    void returnStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(ReturnStatement, 'returnStatement', [Expression], argBlock)
    }

    /**
     * Creates a TernaryExpression.
     */

    private void ternary(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(TernaryExpression, 'ternary', [BooleanExpression, Expression, Expression], argBlock)
    }


    /**
     * Creates an ElvisOperatorExpression.
     */
    void elvisOperator(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(ElvisOperatorExpression, 'elvisOperator', [Expression, Expression], argBlock)
    }

    /**
     * Creates a BreakStatement.
     */
    void breakStatement(String label = null) {
        if (label) {
            expression << new BreakStatement(label)
        } else {
            expression << new BreakStatement()
        }
    }

    /**
     * Creates a ContinueStatement.
     */
    void continueStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock = null) {
        if (!argBlock) {
            expression << new ContinueStatement()
        } else {
            makeNode(ContinueStatement, 'continueStatement', [String], argBlock)
        }
    }

    /**
     * Create a CaseStatement.
     */
    void caseStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(CaseStatement, 'caseStatement', [Expression, Statement], argBlock)
    }

    /**
     * Creates a BlockStatement.
     */
    void defaultCase(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        block(argBlock) // same as arg block
    }

    /**
     * Creates a PrefixExpression.
     */
    void prefix(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(PrefixExpression, 'prefix', [Token, Expression], argBlock)
    }

    /**
     * Creates a NotExpression.
     */
    void not(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(NotExpression, 'not', [Expression], argBlock)
    }

    /**
     * Creates a DynamicVariable.
     */
    void dynamicVariable(String variable, boolean isStatic = false) {
        expression << new DynamicVariable(variable, isStatic)
    }

    /**
     * Creates a ClassNode[].
     */
    void exceptions(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeArrayOfNodes([] as ClassNode[], argBlock)
    }

    /**
     * Designates a list of AnnotationNodes.
     */
    void annotations(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, "List<AnnotationNode>")
    }


    /**
     * Designates a list of MethodNodes.
     */
    void methods(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, "List<MethodNode>")
    }

    /**
     * Designates a list of ConstructorNodes.
     */
    void constructors(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, "List<ConstructorNode>")
    }

    /**
     * Designates a list of {@code PropertyNode}s.
     */
    void properties(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, "List<PropertyNode>")
    }

    /**
     * Designates a list of {@code FieldNode}s.
     */
    void fields(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, "List<FieldNode>")
    }

    /**
     * Designates a list of ConstantExpressions.
     */

    void strings(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, "List<ConstantExpression>")
    }

    /**
     * Designates a list of Expressions.
     */

    void values(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, "List<Expression>")
    }

    /**
     * Creates a boolean value.
     */
    void inclusive(boolean value) {
        expression << value
    }

    /**
     * Creates a ConstantExpression.
     */
    void constant(Object value) {
        expression << new ConstantExpression(value)
    }

    /**
     * Creates an IfStatement.
     */
    void ifStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(IfStatement, 'ifStatement', [BooleanExpression, Statement, Statement], argBlock)
    }

    /**
     * Creates a SpreadExpression.
     */
    void spread(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(SpreadExpression, 'spread', [Expression], argBlock)
    }

    /**
     * Creates a SpreadMapExpression.
     */
    void spreadMap(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(SpreadMapExpression, 'spreadMap', [Expression], argBlock)
    }

    /**
     * Creates a WhileStatement.
     */
    void whileStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(WhileStatement, 'whileStatement', [BooleanExpression, Statement], argBlock)
    }

    /**
     * Create a ForStatement.
     */
    void forStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(ForStatement, 'forStatement', [Parameter, Expression, Statement], argBlock)
    }

    /**
     * Creates a ClosureListExpression.
     */
    void closureList(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNodeFromList(ClosureListExpression, argBlock)
    }

    /**
     * Creates a DeclarationExpression.
     */
    void declaration(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(DeclarationExpression, 'declaration', [Expression, Token, Expression], argBlock)
    }

    /**
     * Creates a ListExpression.
     */
    void list(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNodeFromList(ListExpression, argBlock)
    }

    /**
     * Creates a BitwiseNegationExpression.
     */
    void bitwiseNegation(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(BitwiseNegationExpression, 'bitwiseNegation', [Expression], argBlock)
    }

    /**
     * Creates a ClosureExpression.
     */
    void closure(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(ClosureExpression, 'closure', [Parameter[], Statement], argBlock)
    }

    /**
     * Creates a BooleanExpression.
     */
    void booleanExpression(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(BooleanExpression, 'booleanExpression', [Expression], argBlock)
    }

    /**
     * Creates a BinaryExpression.
     */
    void binary(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(BinaryExpression, 'binary', [Expression, Token, Expression], argBlock)
    }

    /**
     * Creates a UnaryPlusExpression.
     */
    void unaryPlus(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(UnaryPlusExpression, 'unaryPlus', [Expression], argBlock)
    }

    /**
     * Creates a ClassExpression.
     */
    void classExpression(Class type) {
        expression << new ClassExpression(ClassHelper.make(type))
    }

    /**
     * Creates a UnaryMinusExpression
     */
    void unaryMinus(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(UnaryMinusExpression, 'unaryMinus', [Expression], argBlock)
    }

    /**
     * Creates an AttributeExpression.
     */
    void attribute(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(AttributeExpression, 'attribute', [Expression, Expression], argBlock)
    }

    /**
     * Creates an ExpressionStatement.
     */
    void expression(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNode(ExpressionStatement, 'expression', [Expression], argBlock)
    }

    /**
     * Creates a NamedArgumentListExpression.
     */
    void namedArgumentList(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeNodeFromList(NamedArgumentListExpression, argBlock)
    }

    /**
     * Creates a ClassNode[].
     */
    void interfaces(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, "List<ClassNode>")
    }

    /**
     * Creates a MixinNode[].
     */
    void mixins(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, "List<MixinNode>")
    }

    /**
     * Creates a GenericsTypes[].
     */
    void genericsTypes(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, "List<GenericsTypes>")
    }

    /**
     * Creates a ClassNode.
     */
    void classNode(Class target) {
        expression << ClassHelper.make(target, false)
    }

    /**
     * Creates a Parameter[].
     */
    void parameters(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeArrayOfNodes([] as Parameter[], argBlock)
    }

    /**
     * Creates a BlockStatement.
     */
    void block(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("BlockStatement", argBlock) {
            return new BlockStatement(new ArrayList(expression), new VariableScope())
        }
    }

    /**
     * Creates a Parameter.
     */
    void parameter(Map<String, Class> args, @DelegatesTo(AstSpecificationCompiler) Closure argBlock = null) {
        if (!args) throw new IllegalArgumentException()
        if (args.size() > 1) throw new IllegalArgumentException()

        //todo: add better error handling?
        if (argBlock) {
            args.each { name, type ->
                captureAndCreateNode("Parameter", argBlock) {
                    new Parameter(ClassHelper.make(type), name, expression[0])
                }
            }
        } else {
            args.each { name, type ->
                expression << (new Parameter(ClassHelper.make(type), name))
            }
        }
    }

    /**
     * Creates an ArrayExpression.
     */
    void array(Class type, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("ArrayExpression", argBlock) {
            new ArrayExpression(ClassHelper.make(type), new ArrayList(expression))
        }
    }

    /**
     * Creates a GenericsType.
     */
    void genericsType(Class type, @DelegatesTo(AstSpecificationCompiler) Closure argBlock = null) {
        if (argBlock) {
            captureAndCreateNode("GenericsType", argBlock) {
                new GenericsType(ClassHelper.make(type), expression[0] as ClassNode[], expression[1])
            }
        } else {
            expression << new GenericsType(ClassHelper.make(type))
        }
    }

    /**
     * Creates a list of upperBound ClassNodes.
     */
    void upperBound(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        makeListOfNodes(argBlock, 'List<ClassNode>')
    }

    /**
     * Create lowerBound ClassNode.
     */
    void lowerBound(Class target) {
        expression << ClassHelper.make(target)
    }

    /**
     * Creates a 2 element list of name and Annotation. Used with Annotation Members.
     */
    void member(String name, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("Annotation Member", argBlock) {
            [name, expression[0]]
        }
    }

    /**
     * Creates an ArgumentListExpression.
     */
    void argumentList(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        if (!argBlock) {
            expression << new ArgumentListExpression()
        } else {
            makeNodeFromList(ArgumentListExpression, argBlock)
        }
    }

    /**
     * Creates an AnnotationNode.
     */
    void annotation(Class target, @DelegatesTo(AstSpecificationCompiler) Closure argBlock = null) {
        if (argBlock) {
            //todo: add better error handling
            captureAndCreateNode("ArgumentListExpression", argBlock) {
                def node = new AnnotationNode(ClassHelper.make(target))
                expression?.each {
                    node.addMember(it[0], it[1])
                }
                node
            }
        } else {
            expression << new AnnotationNode(ClassHelper.make(target))
        }
    }

    /**
     * Creates a MixinNode.
     */
    void mixin(String name, int modifiers, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("AttributeExpression", argBlock) {
            if (expression.size() > 1) {
                new MixinNode(name, modifiers, expression[0], new ArrayList(expression[1]) as ClassNode[])
            } else {
                new MixinNode(name, modifiers, expression[0])
            }
        }
    }

    /**
     * Creates a ClassNode
     */
    void classNode(String name, int modifiers, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("ClassNode", argBlock) {
            def result = new ClassNode(name, modifiers,
                    expression[0],
                    new ArrayList(expression[1]) as ClassNode[],
                    new ArrayList(expression[2]) as MixinNode[]
            )
            while (expression.size() > 3) {
                if (!List.isAssignableFrom(expression[3].getClass())) {
                    throw new IllegalArgumentException("Expecting to find list of additional items instead found: " + expression[3].getClass())
                }
                if (expression[3].size() > 0) {
                    def clazz = expression[3][0].getClass()
                    switch (clazz) {
                        case GenericsType:
                            result.setGenericsTypes(new ArrayList(expression[3]) as GenericsType[])
                            break
                        case MethodNode:
                            expression[3].each { result.addMethod(it) }
                            break
                        case ConstructorNode:
                            expression[3].each { result.addConstructor(it) }
                            break
                        case PropertyNode:
                            expression[3].each {
                                it.field.owner = result
                                result.addProperty(it)
                            }
                            break
                        case FieldNode:
                            expression[3].each {
                                it.owner = result
                                result.addField(it)
                            }
                            break
                        case AnnotationNode:
                            result.addAnnotations(new ArrayList(expression[3]))
                            break
                        default:
                            throw new IllegalArgumentException("Unexpected item found in ClassNode spec. Expecting [Field|Method|Property|Constructor|Annotation|GenericsType] but found: $clazz.name")
                    }
                }
                expression.remove(3)
            }
            result
        }
    }

    /**
     * Creates an AssertStatement.
     */
    void assertStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("AssertStatement", argBlock) {
            if (expression.size() < 2) {
                new AssertStatement(*enforceConstraints('assertStatement', [BooleanExpression]))
            } else {
                new AssertStatement(*enforceConstraints('assertStatement', [BooleanExpression, Expression]))
            }
        }
    }

    /**
     * Creates a TryCatchStatement.
     */
    void tryCatch(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("TryCatchStatement", argBlock) {
            def result = new TryCatchStatement(expression[0], expression[1])
            def catchStatements = expression.tail().tail()
            catchStatements.each { statement -> result.addCatch(statement) }
            return result
        }
    }

    /**
     * Creates a VariableExpression.
     */
    void variable(String variable) {
        expression << new VariableExpression(variable)
    }

    /**
     * Creates a MethodNode.
     */
    void method(String name, int modifiers, Class returnType, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("MethodNode", argBlock) {
            //todo: enforce contract
            def result = new MethodNode(name, modifiers, ClassHelper.make(returnType), expression[0], expression[1], expression[2])
            if (expression[3]) {
                result.addAnnotations(new ArrayList(expression[3]))
            }
            result
        }
    }

    /**
     * Creates a token.
     */
    void token(String value) {
        if (value == null) throw new IllegalArgumentException("Null: value")

        def tokenID = Types.lookupKeyword(value)
        if (tokenID == Types.UNKNOWN) {
            tokenID = Types.lookupSymbol(value)
        }
        if (tokenID == Types.UNKNOWN) throw new IllegalArgumentException("could not find token for $value")

        expression << new Token(tokenID, value, -1, -1)
    }

    /**
     * Creates a RangeExpression.
     */
    void range(Range range) {
        if (range == null) throw new IllegalArgumentException('Null: range')
        expression << new RangeExpression(new ConstantExpression(range.getFrom()), new ConstantExpression(range.getTo()), true)
        //default is inclusive
    }

    /**
     * Creates a SwitchStatement.
     */
    void switchStatement(@DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("SwitchStatement", argBlock) {
            def switchExpression = expression.head()
            def caseStatements = expression.tail().tail()
            def defaultExpression = expression.tail().head()
            new SwitchStatement(switchExpression, caseStatements, defaultExpression)
        }
    }

    /**
     * Creates a mapEntry.
     */
    void mapEntry(Map map) {
        map.entrySet().each {
            expression << new MapEntryExpression(
                    new ConstantExpression(it.key),
                    new ConstantExpression(it.value))
        }
    }

    //
    // todo: these methods can still be reduced smaller
    //

    /**
     * Creates a FieldNode.
     */
    void fieldNode(String name, int modifiers, Class type, Class owner, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("FieldNode", argBlock) {
            def annotations = null
            if (expression.size() > 1) {
                annotations = expression[1]
                expression.remove(1)
            }
            expression.add(0, ClassHelper.make(owner))
            expression.add(0, ClassHelper.make(type))
            expression.add(0, modifiers)
            expression.add(0, name)
            def result = new FieldNode(*enforceConstraints('fieldNode', [String, Integer, ClassNode, ClassNode, Expression]))
            if (annotations) {
                result.addAnnotations(new ArrayList(annotations))
            }
            result
        }
    }

    /**
     * Creates an inner class.
     */
    void innerClass(String name, int modifiers, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("InnerClassNode", argBlock) {
            //todo: enforce contract
            new InnerClassNode(
                    expression[0],
                    name,
                    modifiers,
                    expression[1],
                    new ArrayList(expression[2]) as ClassNode[],
                    new ArrayList(expression[3]) as MixinNode[])
        }
    }

    /**
     * Creates a PropertyNode.
     */
    void propertyNode(String name, int modifiers, Class type, Class owner, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        //todo: improve error handling?
        captureAndCreateNode("PropertyNode", argBlock) {
            def annotations = null
            // check if the last expression looks like annotations
            if (List.isAssignableFrom(expression[-1].getClass())) {
                annotations = expression[-1]
                expression.remove(expression.size() - 1)
            }
            def result = new PropertyNode(name, modifiers, ClassHelper.make(type), ClassHelper.make(owner),
                    expression[0],  // initial value (possibly null)
                    expression[1],  // getter block (possibly null)
                    expression[2])  // setter block (possibly null)
            if (annotations) {
                result.addAnnotations(new ArrayList(annotations))
            }
            result
        }
    }

    /**
     * Creates a StaticMethodCallExpression.
     */
    void staticMethodCall(Class target, String name, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("StaticMethodCallExpression", argBlock) {
            expression.add(0, name)
            expression.add(0, ClassHelper.make(target))
            new StaticMethodCallExpression(*enforceConstraints('staticMethodCall', [ClassNode, String, Expression]))
        }
    }

    /**
     * Creates a StaticMethodCallExpression.
     */
    void staticMethodCall(MethodClosure target, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("StaticMethodCallExpression", argBlock) {
            expression.add(0, target.method)
            expression.add(0, ClassHelper.makeWithoutCaching(target.owner.class, false))
            new StaticMethodCallExpression(*enforceConstraints('staticMethodCall', [ClassNode, String, Expression]))
        }
    }

    /**
     * Creates a ConstructorNode.
     */
    void constructor(int modifiers, @DelegatesTo(AstSpecificationCompiler) Closure argBlock) {
        captureAndCreateNode("ConstructorNode", argBlock) {
            def annotations = null
            if (expression.size() > 3) {
                annotations = expression[3]
                expression.remove(3)
            }
            expression.add(0, modifiers)
            def result = new ConstructorNode(*enforceConstraints('constructor', [Integer, Parameter[], ClassNode[], Statement]))
            if (annotations) {
                result.addAnnotations(new ArrayList(annotations))
            }
            result
        }
    }
}
