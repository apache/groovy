/**
 *
 * Copyright 2004 James Strachan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package org.codehaus.groovy.antlr;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Type;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Reduction;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.syntax.parser.ParserException;
import org.objectweb.asm.Constants;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Revision$
 */
public class AntlrParserPlugin extends ParserPlugin implements GroovyTokenTypes {
    private static final Type OBJECT_TYPE = new Type("java.lang.Object", true);

    private AST ast;
    private ModuleNode module;
    private ClassNode classNode;


    public Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
        ast = null;

        GroovyLexer lexer = new GroovyLexer(reader);
        GroovyRecognizer parser = GroovyRecognizer.make(lexer);
        parser.setFilename(sourceUnit.getName());

        // start parsing at the compilationUnit rule
        try {
            parser.compilationUnit();
        }
        catch (RecognitionException e) {
            // TODO
            throw new RuntimeException(e);
            //throw new CompilationFailedException(e.getMessage() new Token(-1, e.getFilename(), e.getLine(), e.getColumn()));
        }
        catch (TokenStreamException e) {
            // TODO
            throw new RuntimeException(e);
            ///throw new CompilationFailedException(e.getMessage(), Token.EOF);
        }

        ast = parser.getAST();

        return null; //new Reduction(Tpken.EOF);
    }

    public ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException {
        module = new ModuleNode(sourceUnit);
        convertGroovy(ast);
        return module;
    }

    /**
     * Converts the Antlr AST to the Groovy AST
     */
    protected void convertGroovy(AST node) {
        int type = node.getType();
        switch (type) {
            case CLASS_DEF:
                classDef(node);
                break;

            default:
                onUnknownAST(node);
        }
    }

    protected void classDef(AST classDef) {
        String name = null;

        // TODO read the modifiers
        int modifiers = Constants.ACC_PUBLIC;
        String superClass = null;
        String[] interfaces = {};

        // TODO read mixins
        MixinNode[] mixins = {};

        AST objectBlock = null;
        for (AST node = classDef.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case IDENT:
                    name = node.getText();
                    break;

                case EXTENDS_CLAUSE:
                    superClass = getFirstChildText(node);
                    break;

                case IMPLEMENTS_CLAUSE:
                    interfaces = interfaces(node);
                    break;

                case OBJBLOCK:
                    objectBlock = node;
                    break;

                default:
                    onUnknownAST(node);
            }
        }

        classNode = new ClassNode(name, modifiers, superClass, interfaces, mixins);
        configureAST(classNode, classDef);

        objectBlock(objectBlock);
        module.addClass(classNode);
    }

    protected void objectBlock(AST objectBlock) {
        for (AST node = objectBlock.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case OBJBLOCK:
                    objectBlock(node);
                    break;

                case METHOD_DEF:
                    methodDef(node);
                    break;

                case VARIABLE_DEF:
                    fieldDef(node);
                    break;

                default:
                    onUnknownAST(node);
            }
        }
    }

    protected void methodDef(AST methodDef) {
        String name = null;

        AST node = methodDef.getFirstChild();
        int modifiers = Constants.ACC_PUBLIC;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, modifiers);
            node = node.getNextSibling();
        }

        String returnType = null;

        if (isType(TYPE, node)) {
            returnType = getFirstChildText(node);
            node = node.getNextSibling();
        }

        name = identifier(node);
        node = node.getNextSibling();

        assertNodeType(PARAMETERS, node);
        Parameter[] parameters = parameters(node);
        node = node.getNextSibling();

        assertNodeType(SLIST, node);
        Statement code = statement(node);

        MethodNode methodNode = classNode.addMethod(name, modifiers, returnType, parameters, code);
        configureAST(methodNode, methodDef);
    }

    protected void fieldDef(AST fieldDef) {
        String type = null;
        Expression initialValue = null;

        AST node = fieldDef.getFirstChild();

        int modifiers = Constants.ACC_PRIVATE;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, modifiers);
            node = node.getNextSibling();
        }

        if (isType(TYPE, node)) {
            type = getFirstChildText(node);
            node = node.getNextSibling();
        }

        String name = identifier(node);

        node = node.getNextSibling();

        if (node != null) {
            assertNodeType(ASSIGN, node);
            initialValue = expression(node);
        }

        FieldNode fieldNode = classNode.addField(name, modifiers, type, initialValue);
        configureAST(fieldNode, fieldDef);
    }

    protected String[] interfaces(AST node) {
        List interfaceList = new ArrayList();
        for (AST implementNode = node.getFirstChild(); implementNode != null; implementNode = implementNode.getNextSibling()) {
            interfaceList.add(implementNode.getText());
        }
        String[] interfaces = {};
        if (!interfaceList.isEmpty()) {
            interfaces = new String[interfaceList.size()];
            interfaceList.toArray(interfaces);

        }
        return interfaces;
    }

    protected Parameter[] parameters(AST parametersNode) {
        AST node = parametersNode.getFirstChild();
        if (node == null) {
            return Parameter.EMPTY_ARRAY;
        }
        else {
            List parameters = new ArrayList();
            do {
                parameters.add(parameter(node));
                node = node.getNextSibling();
            }
            while (node != null);
            Parameter[] answer = new Parameter[parameters.size()];
            parameters.toArray(answer);
            return answer;
        }
    }

    protected Parameter parameter(AST node) {
        node = node.getFirstChild();

        int modifiers = 0;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, modifiers);
            node = node.getNextSibling();
        }
        assertNodeType(TYPE, node);
        String type = getFirstChildText(node);

        node = node.getNextSibling();

        String name = identifier(node);

        Expression defaultValue = null;
        node = node.getNextSibling();
        if (node != null) {
            defaultValue = expression(node);
        }
        Parameter parameter = new Parameter(type, name, defaultValue);
        // TODO
        //configureAST(parameter, node);
        return parameter;
    }

    protected int modifiers(AST node, int defaultModifiers) {
        // TODO!
        return defaultModifiers;
    }

    protected Statement statement(AST code) {
        BlockStatement block = new BlockStatement();

        for (AST node = code.getFirstChild(); node != null; node = node.getNextSibling()) {
            Statement statement = null;
            int type = node.getType();
            switch (type) {
                case METHOD_CALL:
                case IDENT:
                    statement = methodCall(node);
                    break;

                case VARIABLE_DEF:
                    statement = variableDef(node);
                    break;

                case SLIST:
                    statement = statement(node);
                    break;

                case LABELED_STAT:
                    statement = labelledStatement(node);

                case LITERAL_assert:
                    statement = assertStatement(node);
                    break;

                case LITERAL_break:
                    statement = breakStatement(node);
                    break;

                case LITERAL_continue:
                    statement = continueStatement(node);
                    break;

                case LITERAL_if:
                    statement = ifStatement(node);
                    break;

                case LITERAL_for:
                    statement = forStatement(node);
                    break;

                case LITERAL_return:
                    statement = returnStatement(node);
                    break;

                case LITERAL_synchronized:
                    statement = synchronizedStatement(node);
                    break;

                case LITERAL_switch:
                    statement = switchStatement(node);
                    break;

                case LITERAL_with:
                    statement = withStatement(node);
                    break;

                case LITERAL_try:
                    statement = tryStatement(node);
                    break;

                case LITERAL_throw:
                    statement = throwStatement(node);
                    break;

                case LITERAL_while:
                    statement = whileStatement(node);
                    break;

                default:
                    statement = new ExpressionStatement(expression(node));
            }
            if (statement != null) {
                configureAST(statement, node);
                block.addStatement(statement);
            }
        }

        // TODO check for dumb expression rule
        return block;
    }


    // Statements
    //-------------------------------------------------------------------------

    protected Statement assertStatement(AST assertNode) {
        AST node = assertNode.getFirstChild();
        BooleanExpression booleanExpression = booleanExpression(node);
        Expression messageExpression = null;

        node = node.getNextSibling();
        if (node != null) {
            messageExpression = expression(node);
        }
        else {
            messageExpression = ConstantExpression.NULL;
        }
        return new AssertStatement(booleanExpression, messageExpression);
    }

    protected Statement breakStatement(AST node) {
        return new BreakStatement(label(node));
    }

    protected Statement continueStatement(AST node) {
        return new ContinueStatement(label(node));
    }

    protected Statement forStatement(AST forNode) {
        AST inNode = forNode.getFirstChild();
        AST variableNode = inNode.getFirstChild();
        AST collectionNode = variableNode.getNextSibling();

        Type type = OBJECT_TYPE;
        if (isType(VARIABLE_DEF, variableNode)) {
            AST typeNode = variableNode.getFirstChild();
            assertNodeType(TYPE, typeNode);

            // TODO intern types?
            type = new Type(identifier(typeNode.getFirstChild()));
            variableNode = typeNode.getNextSibling();
        }
        String variable = identifier(variableNode);

        Expression collectionExpression = expression(collectionNode);
        Statement block = statement(inNode.getNextSibling());

        return new ForStatement(variable, type, collectionExpression, block);
    }

    protected Statement ifStatement(AST ifNode) {
        AST node = ifNode.getFirstChild();
        assertNodeType(EXPR, node);
        BooleanExpression booleanExpression = booleanExpression(node);

        node = node.getNextSibling();
        assertNodeType(SLIST, node);
        Statement ifBlock = statement(node);

        Statement elseBlock = EmptyStatement.INSTANCE;
        node = node.getNextSibling();
        if (isType(SLIST, node)) {
            elseBlock = statement(node);
        }
        return new IfStatement(booleanExpression, ifBlock, elseBlock);
    }

    protected Statement labelledStatement(AST node) {
        notImplementedYet(node);
        return null; /** TODO */
    }

    protected Statement methodCall(AST code) {
        MethodCallExpression expression = methodCallExpression(code);
        return new ExpressionStatement(expression);
    }

    protected Statement variableDef(AST variableDef) {
        AST node = variableDef.getFirstChild();
        String type = null;
        if (isType(TYPE, node)) {
            type = getFirstChildText(node);
            node = node.getNextSibling();
        }

        String name = identifier(node);
        node = node.getNextSibling();

        Expression leftExpression = new VariableExpression(name, type);

        assertNodeType(ASSIGN, node);
        Token token = makeToken(Types.ASSIGN, node);

        Expression rightExpression = expression(node.getFirstChild());

        // TODO should we have a variable declaration statement?
        return new ExpressionStatement(new BinaryExpression(leftExpression, token, rightExpression));
    }

    protected Statement returnStatement(AST node) {
        Expression expression = null;
        AST exprNode = node.getFirstChild();
        if (exprNode == null) {
            exprNode = node.getNextSibling();
        }
        if (exprNode != null) {
            expression = expression(exprNode);
        }
        else {
            expression = ConstantExpression.NULL;
        }
        return new ReturnStatement(expression);
    }

    protected Statement switchStatement(AST node) {
        notImplementedYet(node);
        return null; /** TODO */
    }

    protected Statement synchronizedStatement(AST syncNode) {
        AST node = syncNode.getFirstChild();
        Expression expression = expression(node);
        Statement code = statement(node.getNextSibling());
        return new SynchronizedStatement(expression, code);
    }

    protected Statement throwStatement(AST node) {
        AST expressionNode = node.getFirstChild();
        if (expressionNode == null) {
            expressionNode = node.getNextSibling();
        }
        if (expressionNode == null) {
            throw new RuntimeException("No expression available: " + description(node));
        }
        return new ThrowStatement(expression(expressionNode));
    }

    protected Statement tryStatement(AST tryStatementNode) {
        AST tryNode = tryStatementNode.getFirstChild();
        Statement tryStatement = statement(tryNode);
        Statement finallyStatement = EmptyStatement.INSTANCE;
        AST node = tryNode.getNextSibling();

        // lets do the catch nodes
        List catches = new ArrayList();
        for (; node != null && isType(LITERAL_catch, node); node = node.getNextSibling()) {
            catches.add(catchStatement(node));
        }

        if (isType(LITERAL_finally, node)) {
            finallyStatement = statement(node);
            node = node.getNextSibling();
        }

        TryCatchStatement tryCatchStatement = new TryCatchStatement(tryStatement, finallyStatement);
        for (Iterator iter = catches.iterator(); iter.hasNext();) {
            CatchStatement statement = (CatchStatement) iter.next();
            tryCatchStatement.addCatch(statement);
        }
        return tryCatchStatement;
    }

    protected CatchStatement catchStatement(AST catchNode) {
        AST node = catchNode.getFirstChild();
        Parameter parameter = parameter(node);
        String exceptionType = parameter.getType();
        String variable = parameter.getName();
        /*
        String exceptionType = null;
        if (isType(TYPE, node)) {
            exceptionType = getFirstChildText(node);
            node = node.getNextSibling();
        }
        String variable = identifier(node);
        */
        node = node.getNextSibling();
        Statement code = statement(node);
        CatchStatement answer = new CatchStatement(exceptionType, variable, code);
        configureAST(answer, catchNode);
        return answer;
    }

    protected Statement whileStatement(AST whileNode) {
        AST node = whileNode.getFirstChild();
        assertNodeType(EXPR, node);
        BooleanExpression booleanExpression = booleanExpression(node);

        node = node.getNextSibling();
        assertNodeType(SLIST, node);
        Statement block = statement(node);
        return new WhileStatement(booleanExpression, block);
    }

    protected Statement withStatement(AST node) {
        notImplementedYet(node);
        return null; /** TODO */
    }



    // Expressions
    //-------------------------------------------------------------------------

    protected Expression expression(AST node) {
        Expression expression = expressionSwitch(node);
        configureAST(expression, node);
        return expression;
    }

    protected Expression expressionSwitch(AST node) {
        int type = node.getType();
        switch (type) {
            case EXPR:
                return expression(node.getFirstChild());

            case METHOD_CALL:
                return methodCallExpression(node);

            case LITERAL_new:
                return constructorCallExpression(node.getFirstChild());

            case CTOR_CALL:
                return constructorCallExpression(node);

            case DOT:
                return dotExpression(node);

            case IDENT:
                return new VariableExpression(node.getText());

            case LIST_CONSTRUCTOR:
                return listExpression(node);

            case MAP_CONSTRUCTOR:
                return mapExpression(node);

                // literals

            case STRING_LITERAL:
                return new ConstantExpression(node.getText());

            case STRING_CONSTRUCTOR:
                return gstring(node);

            case NUM_BIG_DECIMAL:
                return new ConstantExpression(new BigDecimal(node.getText()));

            case NUM_BIG_INT:
                return new ConstantExpression(new BigInteger(node.getText()));

            case NUM_DOUBLE:
                return new ConstantExpression(Double.valueOf(node.getText()));

            case NUM_FLOAT:
                return new ConstantExpression(Float.valueOf(node.getText()));

            case NUM_INT:
                return new ConstantExpression(Integer.valueOf(node.getText()));

            case NUM_LONG:
                return new ConstantExpression(Long.valueOf(node.getText()));

            case LITERAL_this:
                return VariableExpression.THIS_EXPRESSION;


                // Binary expressions

            case ASSIGN:
                return binaryExpression(Types.ASSIGN, node);

            case EQUAL:
                return binaryExpression(Types.COMPARE_EQUAL, node);

            case NOT_EQUAL:
                return binaryExpression(Types.COMPARE_NOT_EQUAL, node);

            case LE:
                return binaryExpression(Types.COMPARE_LESS_THAN_EQUAL, node);

            case LT:
                return binaryExpression(Types.COMPARE_LESS_THAN, node);

            case GT:
                return binaryExpression(Types.COMPARE_GREATER_THAN, node);

            case GE:
                return binaryExpression(Types.COMPARE_GREATER_THAN_EQUAL, node);

                /**
                 * TODO treble equal?
                 return binaryExpression(Types.COMPARE_IDENTICAL, node);
                 */

            case PLUS:
                return binaryExpression(Types.PLUS, node);

            case PLUS_ASSIGN:
                return binaryExpression(Types.PLUS_EQUAL, node);


            case MINUS:
                return binaryExpression(Types.MINUS, node);

            case MINUS_ASSIGN:
                return binaryExpression(Types.MINUS_EQUAL, node);


            case STAR:
                return binaryExpression(Types.MULTIPLY, node);

            case STAR_ASSIGN:
                return binaryExpression(Types.MULTIPLY_EQUAL, node);


            case DIV:
                return binaryExpression(Types.DIVIDE, node);

            case DIV_ASSIGN:
                return binaryExpression(Types.DIVIDE_EQUAL, node);


            case MOD:
                return binaryExpression(Types.MOD, node);

            case MOD_ASSIGN:
                return binaryExpression(Types.MOD_EQUAL, node);

            default:
                onUnknownAST(node);
        }
        return null;
    }

    protected Expression listExpression(AST listNode) {
        List expressions = new ArrayList();
        AST elist = listNode.getFirstChild();
        assertNodeType(ELIST, elist);

        for (AST node = elist.getFirstChild(); node != null; node = node.getNextSibling()) {
            expressions.add(expression(node));
        }
        return new ListExpression(expressions);
    }

    protected Expression mapExpression(AST mapNode) {
        List expressions = new ArrayList();
        AST elist = mapNode.getFirstChild();
        assertNodeType(ELIST, elist);

        for (AST node = elist.getFirstChild(); node != null; node = node.getNextSibling()) {
            expressions.add(mapEntryExpression(node));
        }
        return new MapExpression(expressions);
    }

    protected MapEntryExpression mapEntryExpression(AST node) {
        AST keyNode = node.getFirstChild();
        Expression keyExpression = expression(keyNode);
        AST valueNode = keyNode.getNextSibling();
        Expression valueExpression = expression(valueNode);
        return new MapEntryExpression(keyExpression, valueExpression);
    }

    protected Expression binaryExpression(int type, AST node) {
        Token token = makeToken(type, node);

        AST leftNode = node.getFirstChild();
        Expression leftExpression = expression(leftNode);
        AST rightNode = leftNode.getFirstChild();
        if (rightNode == null) {
            rightNode = leftNode.getNextSibling();
        }
        if (rightNode == null) {
            throw new NullPointerException("No rightNode associated with binary expression");
        }
        Expression rightExpression = expression(rightNode);
        return new BinaryExpression(leftExpression, token, rightExpression);
    }

    protected BooleanExpression booleanExpression(AST node) {
        BooleanExpression booleanExpression = new BooleanExpression(expression(node));
        configureAST(booleanExpression, node);
        return booleanExpression;
    }

    protected Expression dotExpression(AST node) {
        // lets decide if this is a propery invocation or a method call
        AST elist = node.getNextSibling();
        if (elist != null) {
            return methodCallExpression(node);
        }
        else {
            AST leftNode = node.getFirstChild();
            Expression leftExpression = expression(leftNode);

            String property = identifier(leftNode.getNextSibling());
            return new PropertyExpression(leftExpression, property);
        }
    }

    protected MethodCallExpression methodCallExpression(AST node) {
        if (isType(METHOD_CALL, node)) {
            node = node.getFirstChild();
        }
        AST methodCallNode = node;

        Expression objectExpression = VariableExpression.THIS_EXPRESSION;
        AST elist = null;
        if (isType(DOT, node)) {
            AST objectNode = node.getFirstChild();
            elist = node.getNextSibling();

            objectExpression = expression(objectNode);

            node = objectNode.getNextSibling();
        }
        String name = identifier(node);

        if (elist == null) {
            elist = node.getNextSibling();
        }

        ArgumentListExpression arguments = arguments(elist);

        MethodCallExpression expression = new MethodCallExpression(objectExpression, name, arguments);
        configureAST(expression, methodCallNode);
        return expression;
    }

    protected ConstructorCallExpression constructorCallExpression(AST node) {
        if (isType(CTOR_CALL, node)) {
            node = node.getFirstChild();
        }
        AST constructorCallNode = node;

        String name = identifier(node);
        AST elist = node.getNextSibling();

        ArgumentListExpression arguments = arguments(elist);
        ConstructorCallExpression expression = new ConstructorCallExpression(name, arguments);
        configureAST(expression, constructorCallNode);
        return expression;
    }

    protected ArgumentListExpression arguments(AST elist) {
        AST node;
        List expressionList = new ArrayList();

        for (node = elist; node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case ELIST:
                    for (AST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                        expressionList.add(expression(child));
                    }
                    break;

                case EXPR:
                    expressionList.add(expression(node));
                    break;

                case CLOSED_BLOCK:
                    expressionList.add(closureExpression(node));
                    break;

                default:
                    onUnknownAST(node);
            }

        }
        ArgumentListExpression arguments = new ArgumentListExpression(expressionList);
        return arguments;
    }

    protected Object closureExpression(AST node) {
        AST paramNode = node.getFirstChild();
        Parameter[] parameters = parameters(paramNode);
        AST codeNode = paramNode.getNextSibling();
        Statement code = statement(codeNode);
        return new ClosureExpression(parameters, code);
    }

    protected Expression gstring(AST expression) {
        List strings = new ArrayList();
        List values = new ArrayList();

        StringBuffer buffer = new StringBuffer();


        for (AST node = expression.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            String text = null;
            switch (type) {

                case STRING_LITERAL:
                    text = node.getText();
                    strings.add(new ConstantExpression(text));
                    buffer.append(text);
                    break;

                    // TODO is this correct?
                case IDENT:
                    text = node.getText();
                    values.add(new VariableExpression(text));
                    buffer.append("$");
                    buffer.append(text);
                    break;

                case SLIST:
                    {
                        Expression valueExpression = expression(node.getFirstChild());
                        values.add(valueExpression);
                        buffer.append("${");
                        buffer.append(valueExpression.getText());
                        buffer.append("}");
                    }
                    break;

                default:
                    onUnknownAST(node);
            }
        }
        return new GStringExpression(buffer.toString(), strings, values);
    }

    protected String label(AST labelNode) {
        AST node = labelNode.getFirstChild();
        if (node == null) {
            return null;
        }
        return identifier(node);
    }

    protected String identifier(AST node) {
        assertNodeType(IDENT, node);
        return node.getText();
    }



    // Helper methods
    //-------------------------------------------------------------------------

    protected void configureAST(ASTNode node, AST ast) {
        node.setColumnNumber(ast.getColumn());
        node.setLineNumber(ast.getLine());

        // TODO we could one day store the Antlr AST on the Groovy AST
        // node.setCSTNode(ast);
    }

    protected Token makeToken(int typeCode, AST node) {
        return Token.newSymbol(typeCode, node.getLine(), node.getColumn());
    }

    protected String getFirstChildText(AST node) {
        AST child = node.getFirstChild();
        return child != null ? child.getText() : null;
    }


    protected boolean isType(int typeCode, AST node) {
        return node != null && node.getType() == typeCode;
    }

    protected void assertNodeType(int type, AST node) {
        if (node == null) {
            throw new RuntimeException("No child node available in AST when expecting type: " + type);
        }
        if (node.getType() != type) {
            throw new RuntimeException("Unexpected node type: " + node.getType() + " found " + description(node));
        }
    }

    protected void notImplementedYet(AST node) {
        throw new RuntimeException("AST node not implemented yet for type: " + node.getType() + description(node));
    }

    protected void onUnknownAST(AST node) {
        throw new RuntimeException("Unknown type: " + node.getType() + description(node));
    }

    protected String description(AST node) {
        return " at node: " + node + " at line: " + node.getLine() + " column: " + node.getColumn();
    }

    protected void dumpTree(AST ast) {
        for (AST node = ast.getFirstChild(); node != null; node = node.getNextSibling()) {
            dump(node);
        }
    }

    protected void dump(AST node) {
        System.out.println("Type: " + node.getType() + " text: " + node.getText());
    }
}
