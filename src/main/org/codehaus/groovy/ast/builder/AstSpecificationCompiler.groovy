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

import groovy.transform.PackageScope
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
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
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.ClassHelper

/**
 * Handles parsing the properties from the closure into values that can be referenced.
 * 
 * This object is very stateful and not threadsafe. It accumulates expressions in the 
 * 'expression' field as they are found and executed within the DSL. 
 * 
 * Note: this class consists of many one-line method calls. A better implementation
 * might be to take a declarative approach and replace the one-liners with map entries. 
 * 
 * @author Hamlet D'Arcy
 */
@PackageScope class AstSpecificationCompiler implements GroovyInterceptable {

    private final List<ASTNode> expression = []

    /**
     * Creates the DSL compiler.
     */

    AstSpecificationCompiler(Closure spec) {
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
    private void captureAndCreateNode(String name, Closure argBlock, Closure constructorStatement) {
        if (!argBlock) throw new IllegalArgumentException("nodes of type $name require arguments to be specified")

        def oldProps = new ArrayList(expression)
        expression.clear()
        new AstSpecificationCompiler(argBlock)
        def result = constructorStatement(expression) // invoke custom constructor for node
        expression.clear()
        expression.addAll(oldProps)
        expression.add(result)
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
    private void makeNode(Class target, String typeAlias, List<Class<? super ASTNode>> ctorArgs, Closure argBlock) {
        captureAndCreateNode(target.class.simpleName, argBlock) {
            target.newInstance(
                    * enforceConstraints(typeAlias, ctorArgs)
            )
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
    private void makeNodeFromList(Class target, Closure argBlock) {
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
    private void makeListOfNodes(Closure argBlock, String input) {
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
    private void makeArrayOfNodes(Object target, Closure argBlock) {
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
    private void makeNodeWithClassParameter(Class target, String alias, List<Class> spec, Closure argBlock, Class type) {
        captureAndCreateNode(target.class.simpleName, argBlock) {
            expression.add(0, ClassHelper.make(type))
            target.newInstance(
                    * enforceConstraints(alias, spec)
            )
        }
    }

    private void makeNodeWithStringParameter(Class target, String alias, List<Class> spec, Closure argBlock, String text) {
        captureAndCreateNode(target.class.simpleName, argBlock) {
            expression.add(0, text)
            target.newInstance(
                    * enforceConstraints(alias, spec)
            )
        }
    }


    /**
     * Creates a CastExpression.
     */

    private void cast(Class type, Closure argBlock) {
        makeNodeWithClassParameter(CastExpression, 'cast', [ClassNode, Expression], argBlock, type)
    }


    /**
     * Creates an ConstructorCallExpression.
     */

    private void constructorCall(Class type, Closure argBlock) {
        makeNodeWithClassParameter(ConstructorCallExpression, 'constructorCall', [ClassNode, Expression], argBlock, type)
    }


    /**
     * Creates a MethodCallExpression.
     */

    private void methodCall(Closure argBlock) {
        makeNode(MethodCallExpression, 'methodCall', [Expression, Expression, Expression], argBlock)
    }


    /**
     * Creates an AnnotationConstantExpression.
     */

    private void annotationConstant(Closure argBlock) {
        makeNode(AnnotationConstantExpression, 'annotationConstant', [AnnotationNode], argBlock)
    }


    /**
     * Creates a PostfixExpression.
     */

    private void postfix(Closure argBlock) {
        makeNode(PostfixExpression, 'postfix', [Expression, Token], argBlock)
    }


    /**
     * Creates a FieldExpression.
     */

    private void field(Closure argBlock) {
        makeNode(FieldExpression, 'field', [FieldNode], argBlock)
    }


    /**
     * Creates a MapExpression.
     */

    private void map(Closure argBlock) {
        makeNodeFromList(MapExpression, argBlock)
    }


    /**
     * Creates a TupleExpression.
     */

    private void tuple(Closure argBlock) {
        makeNodeFromList(TupleExpression, argBlock)
    }


    /**
     * Creates a MapEntryExpression.
     */

    private void mapEntry(Closure argBlock) {
        makeNode(MapEntryExpression, 'mapEntry', [Expression, Expression], argBlock)
    }


    /**
     * Creates a gString.
     */

    private void gString(String verbatimText, Closure argBlock) {
        makeNodeWithStringParameter(GStringExpression, 'gString', [String, List, List], argBlock, verbatimText)
    }


    /**
     * Creates a methodPointer.
     */

    private void methodPointer(Closure argBlock) {
        makeNode(MethodPointerExpression, 'methodPointer', [Expression, Expression], argBlock)
    }


    /**
     * Creates a property.
     */

    private void property(Closure argBlock) {
        makeNode(PropertyExpression, 'property', [Expression, Expression], argBlock)
    }


    /**
     * Creates a RangeExpression.
     */

    private void range(Closure argBlock) {
        makeNode(RangeExpression, 'range', [Expression, Expression, Boolean], argBlock)
    }


    /**
     * Creates EmptyStatement.
     */

    private void empty() {
        expression << new EmptyStatement()
    }


    /**
     * Creates a label.
     */

    private void label(String label) {
        expression << label
    }


    /**
     * Creates an ImportNode.
     */

    private void importNode(Class target, String alias = null) {
        expression << new ImportNode(ClassHelper.make(target), alias)
    }


    /**
     * Creates a CatchStatement.
     */

    private void catchStatement(Closure argBlock) {
        makeNode(CatchStatement, 'catchStatement', [Parameter, Statement], argBlock)
    }


    /**
     * Creates a ThrowStatement.
     */

    private void throwStatement(Closure argBlock) {
        makeNode(ThrowStatement, 'throwStatement', [Expression], argBlock)
    }


    /**
     * Creates a SynchronizedStatement.
     */

    private void synchronizedStatement(Closure argBlock) {
        makeNode(SynchronizedStatement, 'synchronizedStatement', [Expression, Statement], argBlock)
    }


    /**
     * Creates a ReturnStatement.
     */

    private void returnStatement(Closure argBlock) {
        makeNode(ReturnStatement, 'returnStatement', [Expression], argBlock)
    }


    /**
     * Creates a TernaryExpression.
     */

    private void ternary(Closure argBlock) {
        makeNode(TernaryExpression, 'ternary', [BooleanExpression, Expression, Expression], argBlock)
    }


    /**
     * Creates an ElvisOperatorExpression.
     */

    private void elvisOperator(Closure argBlock) {
        makeNode(ElvisOperatorExpression, 'elvisOperator', [Expression, Expression], argBlock)
    }


    /**
     * Creates a BreakStatement.
     */

    private void breakStatement(String label = null) {
        if (label) {
            expression << new BreakStatement(label)
        } else {
            expression << new BreakStatement()
        }
    }


    /**
     * Creates a ContinueStatement.
     */

    private void continueStatement(Closure argBlock = null) {
        if (!argBlock) {
            expression << new ContinueStatement()
        } else {
            makeNode(ContinueStatement, 'continueStatement', [String], argBlock)
        }
    }


    /**
     * Create a CaseStatement.
     */

    private void caseStatement(Closure argBlock) {
        makeNode(CaseStatement, 'caseStatement', [Expression, Statement], argBlock)
    }


    /**
     * Creates a BlockStatement.
     */

    private void defaultCase(Closure argBlock) {
        block(argBlock) // same as arg block
    }


    /**
     * Creates a PrefixExpression.
     */

    private void prefix(Closure argBlock) {
        makeNode(PrefixExpression, 'prefix', [Token, Expression], argBlock)
    }


    /**
     * Creates a NotExpression.
     */

    private void not(Closure argBlock) {
        makeNode(NotExpression, 'not', [Expression], argBlock)
    }


    /**
     * Creates a DynamicVariable.
     */

    private void dynamicVariable(String variable, boolean isStatic = false) {
        expression << new DynamicVariable(variable, isStatic)
    }


    /**
     * Creates a ClassNode[].
     */

    private void exceptions(Closure argBlock) {
        makeArrayOfNodes([] as ClassNode[], argBlock)
    }


    /**
     * Designates a list of AnnotationNodes.
     */

    private void annotations(Closure argBlock) {
        makeListOfNodes(argBlock, "List<AnnotationNode>")
    }

    /**
     * Designates a list of ConstantExpressions.
     */

    private void strings(Closure argBlock) {
        makeListOfNodes(argBlock, "List<ConstantExpression>")
    }

    /**
     * Designates a list of Expressions.
     */

    private void values(Closure argBlock) {
        makeListOfNodes(argBlock, "List<Expression>")
    }


    /**
     * Creates a boolean value.
     */

    private void inclusive(boolean value) {
        expression << value
    }


    /**
     * Creates a ConstantExpression.
     */

    private void constant(Object value) {
        expression << new ConstantExpression(value)
    }


    /**
     * Creates an IfStatement.
     */

    private void ifStatement(Closure argBlock) {
        makeNode(IfStatement, 'ifStatement', [BooleanExpression, Statement, Statement], argBlock)
    }


    /**
     * Creates a SpreadExpression.
     */

    private void spread(Closure argBlock) {
        makeNode(SpreadExpression, 'spread', [Expression], argBlock)
    }


    /**
     * Creates a SpreadMapExpression.
     */

    private void spreadMap(Closure argBlock) {
        makeNode(SpreadMapExpression, 'spreadMap', [Expression], argBlock)
    }


    /**
     * Creates a WhileStatement.
     */

    private void whileStatement(Closure argBlock) {
        makeNode(WhileStatement, 'whileStatement', [BooleanExpression, Statement], argBlock)
    }


    /**
     * Create a ForStatement.
     */

    private void forStatement(Closure argBlock) {
        makeNode(ForStatement, 'forStatement', [Parameter, Expression, Statement], argBlock)
    }


    /**
     * Creates a ClosureListExpression.
     */

    private void closureList(Closure argBlock) {
        makeNodeFromList(ClosureListExpression, argBlock)
    }


    /**
     * Creates a DeclarationExpression.
     */

    private void declaration(Closure argBlock) {
        makeNode(DeclarationExpression, 'declaration', [Expression, Token, Expression], argBlock)
    }


    /**
     * Creates a ListExpression.
     */

    private void list(Closure argBlock) {
        makeNodeFromList(ListExpression, argBlock)
    }


    /**
     * Creates a BitwiseNegationExpression.
     */

    private void bitwiseNegation(Closure argBlock) {
        makeNode(BitwiseNegationExpression, 'bitwiseNegation', [Expression], argBlock)
    }


    /**
     * Creates a ClosureExpression.
     */

    private void closure(Closure argBlock) {
        makeNode(ClosureExpression, 'closure', [Parameter[], Statement], argBlock)
    }


    /**
     * Creates a BooleanExpression.
     */

    private void booleanExpression(Closure argBlock) {
        makeNode(BooleanExpression, 'booleanExpression', [Expression], argBlock)
    }


    /**
     * Creates a BinaryExpression.
     */

    private void binary(Closure argBlock) {
        makeNode(BinaryExpression, 'binary', [Expression, Token, Expression], argBlock)
    }


    /**
     * Creates a UnaryPlusExpression.
     */

    private void unaryPlus(Closure argBlock) {
        makeNode(UnaryPlusExpression, 'unaryPlus', [Expression], argBlock)
    }


    /**
     * Creates a ClassExpression.
     */

    private void classExpression(Class type) {
        expression << new ClassExpression(ClassHelper.make(type))
    }


    /**
     * Creates a UnaryMinusExpression
     */

    private void unaryMinus(Closure argBlock) {
        makeNode(UnaryMinusExpression, 'unaryMinus', [Expression], argBlock)
    }


    /**
     * Creates an AttributeExpression.
     */

    private void attribute(Closure argBlock) {
        makeNode(AttributeExpression, 'attribute', [Expression, Expression], argBlock)
    }


    /**
     * Creates an ExpressionStatement.
     */

    private void expression(Closure argBlock) {
        makeNode(ExpressionStatement, 'expression', [Expression], argBlock)
    }


    /**
     * Creates a NamedArgumentListExpression.
     */

    private void namedArgumentList(Closure argBlock) {
        makeNodeFromList(NamedArgumentListExpression, argBlock)
    }


    /**
     * Creates a ClassNode[].
     */

    private void interfaces(Closure argBlock) {
        makeListOfNodes(argBlock, "List<ClassNode>")
    }


    /**
     * Creates a MixinNode[].
     */

    private void mixins(Closure argBlock) {
        makeListOfNodes(argBlock, "List<MixinNode>")
    }


    /**
     * Creates a GenericsTypes[].
     */

    private void genericsTypes(Closure argBlock) {
        makeListOfNodes(argBlock, "List<GenericsTypes>")
    }


    /**
     * Creates a ClassNode.
     */

    private void classNode(Class target) {
        expression << ClassHelper.make(target, false)
    }


    /**
     * Creates a Parameter[].
     */

    private void parameters(Closure argBlock) {
        makeArrayOfNodes([] as Parameter[], argBlock)
    }


    /**
     * Creates a BlockStatement.
     */

    private void block(Closure argBlock) {
        captureAndCreateNode("BlockStatement", argBlock) {
            return new BlockStatement(new ArrayList(expression), new VariableScope())
        }
    }


    /**
     * Creates a Parameter.
     */

    private void parameter(Map<String, Class> args, Closure argBlock = null) {
        if (!args) throw new IllegalArgumentException()
        if (args.size() > 1) throw new IllegalArgumentException()

        //todo: add better error handling?
        if (argBlock) {
            args.each {name, type ->
                captureAndCreateNode("Parameter", argBlock) {
                    new Parameter(ClassHelper.make(type), name, expression[0])
                }
            }
        } else {
            args.each {name, type ->
                expression << (new Parameter(ClassHelper.make(type), name))
            }
        }
    }


    /**
     * Creates an ArrayExpression.
     */

    private void array(Class type, Closure argBlock) {
        captureAndCreateNode("ArrayExpression", argBlock) {
            new ArrayExpression(ClassHelper.make(type), new ArrayList(expression))
        }
    }



    /**
     * Creates a GenericsType.
     */

    private void genericsType(Class type, Closure argBlock = null) {
        if (argBlock) {
            captureAndCreateNode("GenericsType", argBlock) {
                new GenericsType(ClassHelper.make(type), expression[0] as ClassNode[], expression[1])
            }
        } else {
            expression << new GenericsType(ClassHelper.make(type))
        }
    }

    /**
     * Creates a list of ClassNodes.
     */
    private void upperBound(Closure argBlock) {
        makeListOfNodes(argBlock, 'List<ClassNode>')
    }

    /**
     * Creates a list of ClassNodes. 
     */
    private void lowerBound(Class target) {
        expression << ClassHelper.make(target)
    }

    /**
     * Creates a 2 element list of name and Annotation. Used with Annotation Members.
     */

    private void member(String name, Closure argBlock) {
        captureAndCreateNode("Annotation Member", argBlock) {
            [name, expression[0]]
        }
    }


    /**
     * Creates an ArgumentListExpression.
     */

    private void argumentList(Closure argBlock) {
        if (!argBlock) {
            expression << new ArgumentListExpression()
        } else {
            makeNodeFromList(ArgumentListExpression, argBlock)
        }
    }


    /**
     * Creates an AnnotationNode.
     */

    private void annotation(Class target, Closure argBlock = null) {
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

    private void mixin(String name, int modifiers, Closure argBlock) {
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

    private void classNode(String name, int modifiers, Closure argBlock) {
        captureAndCreateNode("ClassNode", argBlock) {
            def result = new ClassNode(name, modifiers,
                    expression[0],
                    new ArrayList(expression[1]) as ClassNode[],
                    new ArrayList(expression[2]) as MixinNode[]
            )
            if (expression[3]) {
                result.setGenericsTypes(new ArrayList(expression[3]) as GenericsType[])
            }
            result
        }
    }


    /**
     * Creates an AssertStatement.
     */

    private void assertStatement(Closure argBlock) {
        captureAndCreateNode("AssertStatement", argBlock) {
            if (expression.size() < 2) {
                new AssertStatement(
                        * enforceConstraints('assertStatement', [BooleanExpression])
                )
            } else {
                new AssertStatement(
                        * enforceConstraints('assertStatement', [BooleanExpression, Expression])
                )
            }
        }
    }


    /**
     * Creates a TryCatchStatement.
     */

    private void tryCatch(Closure argBlock) {
        captureAndCreateNode("TryCatchStatement", argBlock) {
            def result = new TryCatchStatement(expression[0], expression[1])
            def catchStatements = expression.tail().tail()
            catchStatements.each {statement -> result.addCatch(statement) }
            return result
        }
    }


    /**
     * Creates a VariableExpression.
     */

    private void variable(String variable) {
        expression << new VariableExpression(variable)
    }


    /**
     * Creates a MethodNode.
     */

    private void method(String name, int modifiers, Class returnType, Closure argBlock) {
        captureAndCreateNode("MethodNode", argBlock) {
            //todo: enforce contract
            def result = new MethodNode(
                    name, modifiers, ClassHelper.make(returnType), expression[0], expression[1], expression[2]
            )
            if (expression[3]) {
                def annotations = expression[3]
                result.addAnnotations(new ArrayList(annotations))
            }
            result
        }
    }


    /**
     * Creates a token.
     */

    private void token(String value) {
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

    private void range(Range range) {
        if (range == null) throw new IllegalArgumentException('Null: range')
        expression << new RangeExpression(
                new ConstantExpression(range.getFrom()),
                new ConstantExpression(range.getTo()),
                true)   //default is inclusive
    }


    /**
     * Creates a SwitchStatement.
     */

    private void switchStatement(Closure argBlock) {
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

    private void mapEntry(Map map) {
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

    private void fieldNode(String name, int modifiers, Class type, Class owner, Closure argBlock) {
        captureAndCreateNode("FieldNode", argBlock) {
            expression.add(0, ClassHelper.make(owner))
            expression.add(0, ClassHelper.make(type))
            expression.add(0, modifiers)
            expression.add(0, name)
            new FieldNode(
                    * enforceConstraints('fieldNode', [String, Integer, ClassNode, ClassNode, Expression]))
        }
    }



    /**
     * Creates a property.
     */

    private void innerClass(String name, int modifiers, Closure argBlock) {
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

    private void propertyNode(String name, int modifiers, Class type, Class owner, Closure argBlock) {
        //todo: improve error handling?
        captureAndCreateNode("PropertyNode", argBlock) {
            new PropertyNode(name, modifiers, ClassHelper.make(type), ClassHelper.make(owner),
                    expression[0],  // initial value
                    expression[1],  // getter block
                    expression[2])  // setter block
        }
    }



    /**
     * Creates a StaticMethodCallExpression.
     */

    private void staticMethodCall(Class target, String name, Closure argBlock) {
        captureAndCreateNode("StaticMethodCallExpression", argBlock) {
            expression.add(0, name)
            expression.add(0, ClassHelper.make(target))
            new StaticMethodCallExpression(
                    * enforceConstraints('staticMethodCall', [ClassNode, String, Expression])
            )
        }
    }


    /**
     * Creates a StaticMethodCallExpression.
     */

    private void staticMethodCall(MethodClosure target, Closure argBlock) {
        captureAndCreateNode("StaticMethodCallExpression", argBlock) {
            expression.add(0, target.method)
            expression.add(0, ClassHelper.makeWithoutCaching(target.owner.class, false))
            new StaticMethodCallExpression(
                    * enforceConstraints('staticMethodCall', [ClassNode, String, Expression])
            )
        }
    }


    /**
     * Creates a ConstructorNode.
     */

    private void constructor(int modifiers, Closure argBlock) {
        captureAndCreateNode("ConstructorNode", argBlock) {
            expression.add(0, modifiers)
            new ConstructorNode(
                    * enforceConstraints('constructor', [Integer, Parameter[], ClassNode[], Statement])
            )
        }
    }
}
