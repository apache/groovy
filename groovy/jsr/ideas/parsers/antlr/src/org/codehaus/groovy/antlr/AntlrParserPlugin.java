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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
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
import java.util.List;

/**
 * @version $Revision$
 */
public class AntlrParserPlugin extends ParserPlugin implements GroovyTokenTypes {
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

        assertNodeType(IDENT, node);
        name = node.getText();
        node = node.getNextSibling();

        assertNodeType(PARAMETERS, node);
        Parameter[] parameters = parameters(node);
        node = node.getNextSibling();

        assertNodeType(SLIST, node);
        Statement code = statement(node);

        classNode.addMethod(name, modifiers, returnType, parameters, code);
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

        assertNodeType(IDENT, node);
        String name = node.getText();

        node = node.getNextSibling();

        if (node != null) {
            assertNodeType(ASSIGN, node);
            initialValue = expression(node);
        }

        classNode.addField(name, modifiers, type, initialValue);
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

        assertNodeType(IDENT, node);
        String name = node.getText();

        Expression defaultValue = null;
        node = node.getNextSibling();
        if (node != null) {
            defaultValue = expression(node);
        }
        return new Parameter(type, name, defaultValue);
    }

    protected int modifiers(AST node, int defaultModifiers) {
        // TODO!
        return defaultModifiers;
    }

    protected Statement statement(AST code) {
        BlockStatement block = new BlockStatement();

        for (AST node = code.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case METHOD_CALL:
                    block.addStatement(methodCall(node));
                    break;

                case VARIABLE_DEF:
                    block.addStatement(variableDef(node));
                    break;

                case LITERAL_assert:
                    block.addStatement(assertStatement(node));
                    break;

                case SLIST:
                    block.addStatement(statement(code));
                    break;

                case LITERAL_return:
                    block.addStatement(returnStatement(node));
                    break;

                default:
                    block.addStatement(new ExpressionStatement(expression(node)));
            }
        }

        // TODO check for dumb expression rule
        return block;
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

        assertNodeType(IDENT, node);
        String name = node.getText();
        node = node.getNextSibling();

        Expression leftExpression = new VariableExpression(name, type);

        assertNodeType(ASSIGN, node);
        Token token = makeToken(Types.ASSIGN, node);

        Expression rightExpression = expression(node.getFirstChild());

        // TODO should we have a variable declaration statement?
        return new ExpressionStatement(new BinaryExpression(leftExpression, token, rightExpression));
    }

    private Token makeToken(int typeCode, AST node) {
        return Token.newSymbol(typeCode, node.getLine(), node.getColumn());
    }

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

    protected Expression expression(AST node) {
        int type = node.getType();
        switch (type) {
            case EXPR:
                return expression(node.getFirstChild());

            case METHOD_CALL:
                return methodCallExpression(node);

            case IDENT:
                return new VariableExpression(node.getText());


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
        return new BooleanExpression(expression(node));
    }

    protected MethodCallExpression methodCallExpression(AST code) {
        AST node = code.getFirstChild();

        String name = null;
        Expression objectExpression = VariableExpression.THIS_EXPRESSION;

        AST elist = null;


        if (isType(DOT, node)) {
            AST objectNode = node.getFirstChild();
            elist = node.getNextSibling();

            objectExpression = expression(objectNode);

            node = objectNode.getNextSibling();
            assertNodeType(IDENT, node);
            name = node.getText();
        }
        else {
            assertNodeType(IDENT, node);
            name = node.getText();
            elist = node.getNextSibling();
        }

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

                default:
                    onUnknownAST(node);
            }

        }

        MethodCallExpression expression = new MethodCallExpression(objectExpression, name, new ArgumentListExpression(expressionList));
        return expression;
    }

    private Expression gstring(AST expression) {
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
            throw new RuntimeException("Unexpected node type: " + node.getType() + " found at node: " + node + " at line: " + ast.getLine() + " column: " + ast.getColumn());
        }
    }

    protected void onUnknownAST(AST ast) {
        throw new RuntimeException("Unknown type: " + ast.getType() + " at node: " + ast + " at line: " + ast.getLine() + " column: " + ast.getColumn());
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
