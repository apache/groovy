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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Reduction;
import org.codehaus.groovy.syntax.parser.ParserException;
import org.objectweb.asm.Constants;

import java.io.Reader;
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
                convertClassDef(node);
                break;

            default:
                onUnknownAST(node);
        }
    }

    protected void convertClassDef(AST classDef) {
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
                    superClass = extractFirstChildText(node);
                    break;

                case IMPLEMENTS_CLAUSE:
                    interfaces = extractInterfaces(node);
                    break;

                case OBJBLOCK:
                    objectBlock = node;
                    break;

                default:
                    onUnknownAST(node);
            }
        }

        classNode = new ClassNode(name, modifiers, superClass, interfaces, mixins);

        processObjectBlock(objectBlock);
        module.addClass(classNode);
    }

    protected void processObjectBlock(AST objectBlock) {
        for (AST node = objectBlock.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case OBJBLOCK:
                    processObjectBlock(node);
                    break;

                case METHOD_DEF:
                    processMethodDef(node);
                    break;

                default:
                    onUnknownAST(node);
            }
        }
    }

    protected void processMethodDef(AST methodDef) {
        String name = null;

        // TODO read the modifiers
        int modifiers = Constants.ACC_PUBLIC;

        String returnType = null;
        Parameter[] parameters = {};
        Statement code = null;

        for (AST node = methodDef.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case IDENT:
                    name = node.getText();
                    break;

                case TYPE:
                    returnType = extractFirstChildText(node);
                    break;

                case PARAMETERS:
                    parameters = extractParameters(node);
                    break;

                case SLIST:
                    code = extractCode(node);
                    break;

                default:
                    onUnknownAST(node);
            }
        }

        classNode.addMethod(name, modifiers, returnType, parameters, code);
    }

    protected String[] extractInterfaces(AST node) {
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

    protected Parameter[] extractParameters(AST node) {
        return new Parameter[0]; /** TODO */
    }


    protected Statement extractCode(AST code) {
        BlockStatement block = new BlockStatement();

        for (AST node = code.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case METHOD_CALL:
                    block.addStatement(methodCall(node));
                    break;

                default:
                    onUnknownAST(node);
            }
        }
        return block;
    }

    protected Statement methodCall(AST code) {
        AST node = code.getFirstChild();

        Expression objectExpression = VariableExpression.THIS_EXPRESSION;
        if (node.getType() == EXPR) {
            objectExpression = extractExpression(node);
            node = node.getNextSibling();
        }

        assertNodeType(IDENT, node);
        String name = node.getText();

        List expressionList = new ArrayList();

        for (node = node.getNextSibling(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case EXPR:
                    expressionList.add(extractExpression(node));
                    break;

                default:
                    onUnknownAST(node);
            }

        }

        MethodCallExpression expression = new MethodCallExpression(objectExpression, name, new ArgumentListExpression(expressionList));
        return new ExpressionStatement(expression);
    }

    protected Expression extractExpression(AST expression) {
        for (AST node = expression.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case STRING_LITERAL:
                    return new ConstantExpression(node.getText());

                default:
                    onUnknownAST(node);
            }
        }
        return null;
    }


    protected String extractFirstChildText(AST node) {
        return node.getFirstChild().getText();
    }

    protected void assertNodeType(int type, AST node) {
        if (node.getType() != type) {
            throw new RuntimeException("Unexpected node type: " + node.getType() + " found at node: " + node);
        }
    }

    protected void onUnknownAST(AST ast) {
        throw new RuntimeException("Unknown type: " + ast.getType() + " at node: " + ast);
    }

    protected void dumpTree(AST ast) {
        dump(ast);
        for (AST node = ast.getFirstChild(); node != null; node = node.getNextSibling()) {
            dump(node);
        }
    }

    protected void dump(AST node) {
        System.out.println("Type: " + node.getType() + " text: " + node.getText());
    }
}
