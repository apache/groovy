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
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Numbers;
import org.codehaus.groovy.syntax.Reduction;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.syntax.parser.ASTHelper;
import org.codehaus.groovy.syntax.parser.ParserException;
import org.objectweb.asm.Constants;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A parser plugin which adapts the JSR Antlr Parser to the Groovy runtime
 *
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @version $Revision$
 */
public class AntlrParserPlugin extends ASTHelper implements ParserPlugin, GroovyTokenTypes {
    private static final Type OBJECT_TYPE = new Type("java.lang.Object", true);

    private AST ast;
    private ClassNode classNode;


    public Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
        ast = null;

        setController(sourceUnit);

        GroovyLexer lexer = new GroovyLexer(reader);
        GroovyRecognizer parser = GroovyRecognizer.make(lexer);
        parser.setFilename(sourceUnit.getName());

        // start parsing at the compilationUnit rule
        try {
            parser.compilationUnit();
        }
        catch (RecognitionException e) {
            sourceUnit.addException(e);
        }
        catch (TokenStreamException e) {
            sourceUnit.addException(e);
        }

        ast = parser.getAST();

        return null; //new Reduction(Tpken.EOF);
    }

    public ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException {
        setClassLoader(classLoader);
        makeModule();
        try {
            convertGroovy(ast);
        }
        catch (ASTRuntimeException e) {
            throw new ASTParserException(e);
        }
        return output;
    }

    /**
     * Converts the Antlr AST to the Groovy AST
     */
    protected void convertGroovy(AST node) {
        while (node != null) {
            int type = node.getType();
            switch (type) {
                case PACKAGE_DEF:
                    packageDef(node);
                    break;

                case IMPORT:
                    importDef(node);
                    break;

                case CLASS_DEF:
                    classDef(node);
                    break;

                case METHOD_DEF:
                    methodDef(node);
                    break;

                default:
                    {
                        Statement statement = statement(node);
                        output.addStatement(statement);
                    }
            }
            node = node.getNextSibling();
        }
    }

    // Top level control structures
    //-------------------------------------------------------------------------

    protected void packageDef(AST packageDef) {
        AST node = packageDef.getFirstChild();
        if (isType(ANNOTATIONS, node)) {
            node = node.getNextSibling();
        }
        String name = qualifiedName(node);
        setPackageName(name);
    }

    protected void importDef(AST importNode) {
        AST node = importNode.getFirstChild();
        if (isType(LITERAL_as, node)) {
            AST dotNode = node.getFirstChild();
            AST packageNode = dotNode.getFirstChild();
            AST classNode = packageNode.getNextSibling();
            String packageName = qualifiedName(packageNode);
            String name = qualifiedName(classNode);
            String alias = identifier(dotNode.getNextSibling());
            importClass(packageName, name, alias);
        }
        else {
            AST packageNode = node.getFirstChild();
            String packageName = qualifiedName(packageNode);
            AST nameNode = packageNode.getNextSibling();
            if (isType(STAR, nameNode)) {
                importPackageWithStar(packageName);
            }
            else {
                String name = qualifiedName(nameNode);
                importClass(packageName, name, name);
            }
        }
    }

    protected void classDef(AST classDef) {
        List annotations = new ArrayList();
        AST node = classDef.getFirstChild();
        int modifiers = Constants.ACC_PUBLIC;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            node = node.getNextSibling();
        }

        String name = identifier(node);
        node = node.getNextSibling();

        String superClass = null;
        if (isType(EXTENDS_CLAUSE, node)) {
            superClass = typeName(node);
            node = node.getNextSibling();
        }
        if (superClass == null) {
            superClass = "java.lang.Object";
        }

        String[] interfaces = {};
        if (isType(IMPLEMENTS_CLAUSE, node)) {
            interfaces = interfaces(node);
            node = node.getNextSibling();
        }

        // TODO read mixins
        MixinNode[] mixins = {};

        addNewClassName(name);
        String fullClassName = dot(getPackageName(), name);
        classNode = new ClassNode(fullClassName, modifiers, superClass, interfaces, mixins);
        classNode.addAnnotations(annotations);
        configureAST(classNode, classDef);

        assertNodeType(OBJBLOCK, node);
        objectBlock(node);
        output.addClass(classNode);
        classNode = null;
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

                case CTOR_IDENT:
                    constructorDef(node);
                    break;

                case VARIABLE_DEF:
                    fieldDef(node);
                    break;

                default:
                    unknownAST(node);
            }
        }
    }

    protected void methodDef(AST methodDef) {
        List annotations = new ArrayList();
        AST node = methodDef.getFirstChild();
        int modifiers = Constants.ACC_PUBLIC;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            node = node.getNextSibling();
        }

        String returnType = null;

        if (isType(TYPE, node)) {
            returnType = typeName(node);
            node = node.getNextSibling();
        }

        String name = identifier(node);
        if (classNode != null) {
            if (classNode.getNameWithoutPackage().equals(name)) {
                throw new ASTRuntimeException(methodDef, "Invalid constructor format. Try remove the 'def' expression?");
            }
        }
        node = node.getNextSibling();

        assertNodeType(PARAMETERS, node);
        Parameter[] parameters = parameters(node);
        node = node.getNextSibling();

        assertNodeType(SLIST, node);
        Statement code = statementList(node);

            MethodNode methodNode = new MethodNode(name, modifiers, returnType, parameters, code);
            methodNode.addAnnotations(annotations);
            configureAST(methodNode, methodDef);
            if (classNode != null) {
                classNode.addMethod(methodNode);
            }
            else {
                output.addMethod(methodNode);
            }
    }

    protected void constructorDef(AST constructorDef) {
        List annotations = new ArrayList();
        AST node = constructorDef.getFirstChild();
        int modifiers = Constants.ACC_PUBLIC;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            node = node.getNextSibling();
        }

        assertNodeType(PARAMETERS, node);
        Parameter[] parameters = parameters(node);
        node = node.getNextSibling();

        assertNodeType(SLIST, node);
        Statement code = statementList(node);

        ConstructorNode constructorNode = classNode.addConstructor(modifiers, parameters, code);
        constructorNode.addAnnotations(annotations);
        configureAST(constructorNode, constructorDef);
    }

    protected void fieldDef(AST fieldDef) {
        List annotations = new ArrayList();
        AST node = fieldDef.getFirstChild();

        int modifiers = 0;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            node = node.getNextSibling();
        }

        String type = null;
        if (isType(TYPE, node)) {
            type = typeName(node);
            node = node.getNextSibling();
        }

        String name = identifier(node);
        node = node.getNextSibling();

        Expression initialValue = null;
        if (node != null) {
            assertNodeType(ASSIGN, node);
            initialValue = expression(node);
        }


        FieldNode fieldNode = new FieldNode(name, modifiers, type, classNode, initialValue);
        fieldNode.addAnnotations(annotations);
        configureAST(fieldNode, fieldDef);

        // lets check for a property annotation first
        if (fieldNode.getAnnotations("Property") != null) {
            // lets set the modifiers on the field
            int fieldModifiers = 0;
            int flags = Constants.ACC_STATIC | Constants.ACC_TRANSIENT | Constants.ACC_VOLATILE | Constants.ACC_FINAL;

            // lets pass along any other modifiers we need
            fieldModifiers |= (modifiers & flags);
            fieldNode.setModifiers(fieldModifiers);

            if (!hasVisibility(modifiers)) {
                modifiers |= Constants.ACC_PUBLIC;
            }
            classNode.addProperty(new PropertyNode(fieldNode, modifiers, null, null));
        }
        else {
            /*
            if (!hasVisibility(modifiers)) {
                modifiers |= Constants.ACC_PRIVATE;
                fieldNode.setModifiers(modifiers);
            }
            */
            fieldNode.setModifiers(modifiers);

            classNode.addField(fieldNode);
        }
    }

    protected String[] interfaces(AST node) {
        List interfaceList = new ArrayList();
        for (AST implementNode = node.getFirstChild(); implementNode != null; implementNode = implementNode.getNextSibling()) {
            interfaceList.add(resolveTypeName(implementNode.getText()));
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

    protected Parameter parameter(AST paramNode) {
        List annotations = new ArrayList();
        AST node = paramNode.getFirstChild();

        int modifiers = 0;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            node = node.getNextSibling();
        }
        assertNodeType(TYPE, node);
        String type = typeName(node);

        node = node.getNextSibling();

        String name = identifier(node);

        Expression defaultValue = null;
        node = node.getNextSibling();
        if (node != null) {
            defaultValue = expression(node);
        }
        Parameter parameter = new Parameter(type, name, defaultValue);
        // TODO
        //configureAST(parameter, paramNode);
        //parameter.addAnnotations(annotations);
        return parameter;
    }

    protected int modifiers(AST modifierNode, List annotations, int defaultModifiers) {
        assertNodeType(MODIFIERS, modifierNode);

        boolean access = false;
        int answer = 0;

        for (AST node = modifierNode.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                // annotations
                case ANNOTATION:
                    annotations.add(annotation(node));
                    break;


                    // core access scope modifiers
                case LITERAL_private:
                    answer |= Constants.ACC_PRIVATE;
                    access = setAccessTrue(node, access);
                    break;

                case LITERAL_protected:
                    answer |= Constants.ACC_PROTECTED;
                    access = setAccessTrue(node, access);
                    break;

                case LITERAL_public:
                    answer |= Constants.ACC_PUBLIC;
                    access = setAccessTrue(node, access);
                    break;

                    // other modifiers
                case ABSTRACT:
                    answer |= Constants.ACC_ABSTRACT;
                    break;

                case FINAL:
                    answer |= Constants.ACC_FINAL;
                    break;

                case LITERAL_native:
                    answer |= Constants.ACC_NATIVE;
                    break;

                case LITERAL_static:
                    answer |= Constants.ACC_STATIC;
                    break;

                case STRICTFP:
                    answer |= Constants.ACC_STRICT;
                    break;

                case LITERAL_synchronized:
                    answer |= Constants.ACC_SYNCHRONIZED;
                    break;

                case LITERAL_transient:
                    answer |= Constants.ACC_TRANSIENT;
                    break;

                case LITERAL_volatile:
                    answer |= Constants.ACC_VOLATILE;
                    break;

                case LITERAL_def:
                    // ignore def
                    break;

                default:
                    unknownAST(node);
            }
        }
        if (!access) {
            answer |= defaultModifiers;
        }
        return answer;
    }

    protected boolean setAccessTrue(AST node, boolean access) {
        if (!access) {
            return true;
        }
        else {
            throw new ASTRuntimeException(node, "Cannot specify modifier: " + node.getText() + " when access scope has already been defined");
        }
    }

    protected AnnotationNode annotation(AST annotationNode) {
        AST node = annotationNode.getFirstChild();
        String name = identifier(node);
        AnnotationNode annotatedNode = new AnnotationNode(name);
        while (true) {
            node = node.getNextSibling();
            if (isType(ANNOTATION_MEMBER_VALUE_PAIR, node)) {
                AST memberNode = node.getFirstChild();
                String param = identifier(memberNode);
                Expression expression = expression(memberNode.getNextSibling());
                annotatedNode.addMember(param, expression);
            }
            else {
                break;
            }
        }
        return annotatedNode;
    }



    // Statements
    //-------------------------------------------------------------------------

    protected Statement statement(AST node) {
        Statement statement = null;
        int type = node.getType();
        switch (type) {
            case SLIST:
            case LITERAL_finally:
                statement = statementList(node);
                break;

            case METHOD_CALL:
                statement = methodCall(node);
                break;

            case VARIABLE_DEF:
                statement = variableDef(node);
                break;


            case LABELED_STAT:
                statement = labelledStatement(node);
                break;

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
        }
        return statement;
    }

    protected Statement statementList(AST code) {
        return statementListNoChild(code.getFirstChild());
    }

    protected Statement statementListNoChild(AST node) {
        BlockStatement block = new BlockStatement();
        for (; node != null; node = node.getNextSibling()) {
            block.addStatement(statement(node));
        }
        return block;
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

            type = type(typeNode);
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
        // this node could be a BREAK node
        //assertNodeType(SLIST, node);
        Statement ifBlock = statement(node);

        Statement elseBlock = EmptyStatement.INSTANCE;
        node = node.getNextSibling();
        if (node != null) {
            elseBlock = statement(node);
        }
        return new IfStatement(booleanExpression, ifBlock, elseBlock);
    }

    protected Statement labelledStatement(AST labelNode) {
        AST node = labelNode.getFirstChild();
        String label = identifier(node);
        Statement statement = statement(node.getNextSibling());
        statement.setStatementLabel(label);
        return statement;
    }

    protected Statement methodCall(AST code) {
        Expression expression = methodCallExpression(code);
        return new ExpressionStatement(expression);
    }

    protected Statement variableDef(AST variableDef) {
        AST node = variableDef.getFirstChild();
        String type = null;
        if (isType(MODIFIERS, node)) {
            node = node.getNextSibling();
        }
        if (isType(TYPE, node)) {
            type = typeName(node);
            node = node.getNextSibling();
        }

        String name = identifier(node);
        node = node.getNextSibling();

        Expression leftExpression = new VariableExpression(name, type);

        Expression rightExpression = ConstantExpression.NULL;
        if (node != null) {
            assertNodeType(ASSIGN, node);

            rightExpression = expression(node.getFirstChild());
        }
        Token token = makeToken(Types.ASSIGN, variableDef);

        // TODO should we have a variable declaration statement?
        return new ExpressionStatement(new BinaryExpression(leftExpression, token, rightExpression));
    }

    protected Statement returnStatement(AST node) {
        AST exprNode = node.getFirstChild();
        if (exprNode == null) {
            exprNode = node.getNextSibling();
        }
        if (exprNode != null) {
            Expression expression = expression(exprNode);
            if (expression instanceof ConstantExpression) {
                ConstantExpression constantExpr = (ConstantExpression) expression;
                if (constantExpr.getValue() == null) {
                    return ReturnStatement.RETURN_NULL_OR_VOID;
                }
            }
            return new ReturnStatement(expression);
        }
        else {
            return ReturnStatement.RETURN_NULL_OR_VOID;
        }
    }

    protected Statement switchStatement(AST switchNode) {
        AST node = switchNode.getFirstChild();
        Expression expression = expression(node);
        Statement defaultStatement = EmptyStatement.INSTANCE;

        List list = new ArrayList();
        for (node = node.getNextSibling(); isType(CASE_GROUP, node); node = node.getNextSibling()) {
            AST child = node.getFirstChild();
            if (isType(LITERAL_case, child)) {
                list.add(caseStatement(child));
            }
            else {
                defaultStatement = statement(child.getNextSibling());
            }
        }
        if (node != null) {
            unknownAST(node);
        }
        return new SwitchStatement(expression, list, defaultStatement);
    }

    protected CaseStatement caseStatement(AST node) {
        Expression expression = expression(node.getFirstChild());
        Statement statement = statement(node.getNextSibling());
        CaseStatement answer = new CaseStatement(expression, statement);
        configureAST(answer, node);
        return answer;
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
            throw new ASTRuntimeException(node, "No expression available");
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

            case ELIST:
                return expressionList(node);

            case CLOSED_BLOCK:
                return closureExpression(node);

            case SUPER_CTOR_CALL:
                return superMethodCallExpression(node);

            case METHOD_CALL:
                return methodCallExpression(node);

            case LITERAL_new:
                return constructorCallExpression(node.getFirstChild());

            case CTOR_CALL:
                return constructorCallExpression(node);

            case QUESTION:
                return ternaryExpression(node);

            case OPTIONAL_ARG:
                return binaryExpression(Types.NAVIGATE, node);

            case DOT:
                return dotExpression(node);

            case IDENT:
            case LITERAL_boolean:
            case LITERAL_byte:
            case LITERAL_char:
            case LITERAL_double:
            case LITERAL_float:
            case LITERAL_int:
            case LITERAL_long:
            case LITERAL_short:
                return variableExpression(node);

            case LIST_CONSTRUCTOR:
                return listExpression(node);

            case MAP_CONSTRUCTOR:
                return mapExpression(node);

            case LABELED_ARG:
                return mapEntryExpression(node);

            case INDEX_OP:
                return indexExpression(node);

            case LITERAL_instanceof:
                return instanceofExpression(node);

            case LITERAL_as:
                return asExpression(node);

            case TYPECAST:
                return castExpression(node);

                // literals

            case LITERAL_true:
                return ConstantExpression.TRUE;

            case LITERAL_false:
                return ConstantExpression.FALSE;

            case LITERAL_null:
                return ConstantExpression.NULL;

            case STRING_LITERAL:
                return new ConstantExpression(node.getText());

            case STRING_CONSTRUCTOR:
                return gstring(node);

            case NUM_DOUBLE:
            case NUM_FLOAT:
            case NUM_BIG_DECIMAL:
                return decimalExpression(node);

            case NUM_BIG_INT:
            case NUM_INT:
            case NUM_LONG:
                return integerExpression(node);

            case LITERAL_this:
                return VariableExpression.THIS_EXPRESSION;

            case LITERAL_super:
                return VariableExpression.SUPER_EXPRESSION;


                // Unary expressions
            case LNOT:
                return new NotExpression(expression(node.getFirstChild()));

            case UNARY_MINUS:
                return negateExpression(node);

            case BNOT:
                return new BitwiseNegExpression(expression(node.getFirstChild()));

            case UNARY_PLUS:
                return expression(node.getFirstChild());


                // Prefix expressions
            case INC:
                return prefixExpression(node, Types.PLUS_PLUS);

            case DEC:
                return prefixExpression(node, Types.MINUS_MINUS);

                // Postfix expressions
            case POST_INC:
                return postfixExpression(node, Types.PLUS_PLUS);

            case POST_DEC:
                return postfixExpression(node, Types.MINUS_MINUS);

                
                // Binary expressions

            case ASSIGN:
                return binaryExpression(Types.ASSIGN, node);

            case EQUAL:
                return binaryExpression(Types.COMPARE_EQUAL, node);

            case NOT_EQUAL:
                return binaryExpression(Types.COMPARE_NOT_EQUAL, node);

            case COMPARE_TO:
                return binaryExpression(Types.COMPARE_TO, node);

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

                 case ???:
                 return binaryExpression(Types.LOGICAL_AND_EQUAL, node);

                 case ???:
                 return binaryExpression(Types.LOGICAL_OR_EQUAL, node);

                 */

            case LAND:
                return binaryExpression(Types.LOGICAL_AND, node);

            case LOR:
                return binaryExpression(Types.LOGICAL_OR, node);

            case BAND:
                return binaryExpression(Types.BITWISE_AND, node);

            case BAND_ASSIGN:
                return binaryExpression(Types.BITWISE_AND_EQUAL, node);

            case BOR:
                return binaryExpression(Types.BITWISE_OR, node);

            case BOR_ASSIGN:
                return binaryExpression(Types.BITWISE_OR_EQUAL, node);

            case BXOR:
                return binaryExpression(Types.BITWISE_XOR, node);

            case BXOR_ASSIGN:
                return binaryExpression(Types.BITWISE_XOR_EQUAL, node);


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


            case STAR_STAR:
                return binaryExpression(Types.POWER, node);

            case STAR_STAR_ASSIGN:
                return binaryExpression(Types.POWER_EQUAL, node);


            case DIV:
                return binaryExpression(Types.DIVIDE, node);

            case DIV_ASSIGN:
                return binaryExpression(Types.DIVIDE_EQUAL, node);


            case MOD:
                return binaryExpression(Types.MOD, node);

            case MOD_ASSIGN:
                return binaryExpression(Types.MOD_EQUAL, node);

            case SL:
                return binaryExpression(Types.LEFT_SHIFT, node);

            case SL_ASSIGN:
                return binaryExpression(Types.LEFT_SHIFT_EQUAL, node);

            case SR:
                return binaryExpression(Types.RIGHT_SHIFT, node);

            case SR_ASSIGN:
                return binaryExpression(Types.RIGHT_SHIFT_EQUAL, node);

            case BSR:
                return binaryExpression(Types.RIGHT_SHIFT_UNSIGNED, node);

            case BSR_ASSIGN:
                return binaryExpression(Types.RIGHT_SHIFT_UNSIGNED_EQUAL, node);

                // Regex
            case REGEX_FIND:
                return binaryExpression(Types.FIND_REGEX, node);

            case REGEX_MATCH:
                return binaryExpression(Types.MATCH_REGEX, node);


                // Ranges
            case RANGE_INCLUSIVE:
                return rangeExpression(node, true);

            case RANGE_EXCLUSIVE:
                return rangeExpression(node, false);

            default:
                unknownAST(node);
        }
        return null;
    }

    protected Expression ternaryExpression(AST ternaryNode) {
        AST node = ternaryNode.getFirstChild();
        BooleanExpression booleanExpression = booleanExpression(node);
        node = node.getNextSibling();
        Expression left = expression(node);
        Expression right = expression(node.getNextSibling());
        return new TernaryExpression(booleanExpression, left, right);
    }

    protected Expression variableExpression(AST node) {
        String text = node.getText();

        // TODO we might wanna only try to resolve the name if we are
        // on the left hand side of an expression or before a dot?
        String newText = resolveTypeName(text, false);
        if (newText == null) {
            return new VariableExpression(text);
        }
        else {
            return new ClassExpression(newText);
        }
    }

    protected Expression rangeExpression(AST rangeNode, boolean inclusive) {
        AST node = rangeNode.getFirstChild();
        Expression left = expression(node);
        Expression right = expression(node.getNextSibling());
        return new RangeExpression(left, right, inclusive);
    }

    protected Expression listExpression(AST listNode) {
        List expressions = new ArrayList();
        AST elist = listNode.getFirstChild();
        assertNodeType(ELIST, elist);

        AST node = elist.getFirstChild();
        if (isType(LABELED_ARG, node)) {
            do {
                expressions.add(mapEntryExpression(node));
                node = node.getNextSibling();
            }
            while (isType(LABELED_ARG, node));

            return new MapExpression(expressions);
        }
        else {
            while (node != null) {
                expressions.add(expression(node));
                node = node.getNextSibling();
            }
            return new ListExpression(expressions);
        }
    }

    /**
     * Typically only used for map constructors I think?
     */
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


    protected Expression instanceofExpression(AST node) {
        AST leftNode = node.getFirstChild();
        Expression leftExpression = expression(leftNode);

        AST rightNode = leftNode.getNextSibling();
        String typeName = resolvedName(rightNode);
        Expression rightExpression = new ClassExpression(typeName);

        return new BinaryExpression(leftExpression, makeToken(Types.KEYWORD_INSTANCEOF, node), rightExpression);
    }

    protected Expression asExpression(AST node) {
        AST leftNode = node.getFirstChild();
        Expression leftExpression = expression(leftNode);

        AST rightNode = leftNode.getNextSibling();
        String typeName = resolvedName(rightNode);

        return CastExpression.asExpression(typeName, leftExpression);
    }

    protected Expression castExpression(AST castNode) {
        AST node = castNode.getFirstChild();
        String typeName = resolvedName(node);

        AST expressionNode = node.getNextSibling();
        Expression expression = expression(expressionNode);

        return new CastExpression(typeName, expression);
    }


    protected Expression indexExpression(AST indexNode) {
        AST leftNode = indexNode.getFirstChild();
        Expression leftExpression = expression(leftNode);

        AST rightNode = leftNode.getNextSibling();
        Expression rightExpression = expression(rightNode);

        return new BinaryExpression(leftExpression, makeToken(Types.LEFT_SQUARE_BRACKET, indexNode), rightExpression);
    }

    protected Expression binaryExpression(int type, AST node) {
        Token token = makeToken(type, node);

        AST leftNode = node.getFirstChild();
        Expression leftExpression = expression(leftNode);
        AST rightNode = leftNode.getNextSibling();
        if (rightNode == null) {
            rightNode = leftNode.getFirstChild();
        }
        if (rightNode == null) {
            throw new NullPointerException("No rightNode associated with binary expression");
        }
        Expression rightExpression = expression(rightNode);
        return new BinaryExpression(leftExpression, token, rightExpression);
    }

    protected Expression prefixExpression(AST node, int token) {
        Expression expression = expression(node.getFirstChild());
        return new PrefixExpression(makeToken(token, node), expression);
    }

    protected Expression postfixExpression(AST node, int token) {
        Expression expression = expression(node.getFirstChild());
        return new PostfixExpression(expression, makeToken(token, node));
    }

    protected BooleanExpression booleanExpression(AST node) {
        BooleanExpression booleanExpression = new BooleanExpression(expression(node));
        configureAST(booleanExpression, node);
        return booleanExpression;
    }

    protected Expression dotExpression(AST node) {
        // lets decide if this is a propery invocation or a method call
        AST leftNode = node.getFirstChild();
        if (leftNode != null) {
            AST identifierNode = leftNode.getNextSibling();
            if (identifierNode != null) {
                Expression leftExpression = expression(leftNode);
                if (isType(SELECT_SLOT, identifierNode)) {
                    String field = identifier(identifierNode.getFirstChild());
                    return new AttributeExpression(leftExpression, field);
                }
                String property = identifier(identifierNode);
                return new PropertyExpression(leftExpression, property);
            }
        }
        return methodCallExpression(node);
    }

    protected Expression superMethodCallExpression(AST methodCallNode) {
        AST node = methodCallNode.getFirstChild();

        String name = "super";
        Expression objectExpression = VariableExpression.SUPER_EXPRESSION;

        Expression arguments = arguments(node);
        MethodCallExpression expression = new MethodCallExpression(objectExpression, name, arguments);
        configureAST(expression, methodCallNode);
        return expression;
    }


    protected Expression methodCallExpression(AST methodCallNode) {
        AST node = methodCallNode.getFirstChild();
        if (isType(METHOD_CALL, node)) {
            // sometimes method calls get wrapped in method calls for some wierd reason
            return methodCallExpression(node);
        }

        Expression objectExpression = VariableExpression.THIS_EXPRESSION;
        AST elist = null;
        if (isType(DOT, node)) {
            AST objectNode = node.getFirstChild();
            elist = node.getNextSibling();

            objectExpression = expression(objectNode);

            node = objectNode.getNextSibling();
        }

        String name = null;
        if (isType(LITERAL_super, node)) {
            name = "super";
            if (objectExpression == VariableExpression.THIS_EXPRESSION) {
                objectExpression = VariableExpression.SUPER_EXPRESSION;
            }
        }
        else if (isType(LITERAL_new, node)) {
            // TODO for some reason the parser wraps this in a method call if
            // there is an appended closure
            return constructorCallExpression(node);
        }
        else if (isPrimitiveTypeLiteral(node)) {
            throw new ASTRuntimeException(node, "Primitive type literal: " + node.getText()
                    + " cannot be used as a method name; maybe you need to use a double || with a closure expression? {|int x| ..} rather than {int x|...}");
        }
        else {
            name = identifier(node);
        }

        if (elist == null) {
            elist = node.getNextSibling();
        }

        Expression arguments = arguments(elist);
        MethodCallExpression expression = new MethodCallExpression(objectExpression, name, arguments);
        configureAST(expression, methodCallNode);
        return expression;
    }

    protected Expression constructorCallExpression(AST node) {
        if (isType(CTOR_CALL, node) || isType(LITERAL_new, node)) {
            node = node.getFirstChild();
        }
        AST constructorCallNode = node;

        String name = resolvedName(node);
        AST elist = node.getNextSibling();

        if (isType(ARRAY_DECLARATOR, elist)) {
            Expression size = expression(elist.getFirstChild());
            return new ArrayExpression(name, size);
        }
        Expression arguments = arguments(elist);
        ConstructorCallExpression expression = new ConstructorCallExpression(name, arguments);
        configureAST(expression, constructorCallNode);
        return expression;
    }

    protected Expression arguments(AST elist) {
        List expressionList = new ArrayList();
        boolean namedArguments = false;
        for (AST node = elist; node != null; node = node.getNextSibling()) {
            if (isType(ELIST, node)) {
                for (AST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    namedArguments |= addArgumentExpression(child, expressionList);
                }
            }
            else {
                namedArguments |= addArgumentExpression(node, expressionList);
            }
        }
        if (namedArguments) {
            if (!expressionList.isEmpty()) {
                // lets remove any non-MapEntryExpression instances
                // such as if the last expression is a ClosureExpression
                // so lets wrap the named method calls in a Map expression
                List argumentList = new ArrayList();
                for (Iterator iter = expressionList.iterator(); iter.hasNext();) {
                    Expression expression = (Expression) iter.next();
                    if (!(expression instanceof MapEntryExpression)) {
                        argumentList.add(expression);
                    }
                }
                if (!argumentList.isEmpty()) {
                    expressionList.removeAll(argumentList);
                    MapExpression mapExpression = new MapExpression(expressionList);
                    argumentList.add(0, mapExpression);
                    return new ArgumentListExpression(argumentList);
                }
            }
            return new NamedArgumentListExpression(expressionList);
        }
        else {
            return new ArgumentListExpression(expressionList);
        }
    }

    protected boolean addArgumentExpression(AST node, List expressionList) {
        Expression expression = expression(node);
        expressionList.add(expression);
        return expression instanceof MapEntryExpression;
    }

    protected Expression expressionList(AST node) {
        List expressionList = new ArrayList();
        for (AST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            expressionList.add(expression(child));
        }
        if (expressionList.size() == 1) {
            return (Expression) expressionList.get(0);
        }
        else {
            return new ListExpression(expressionList);
        }
    }

    protected ClosureExpression closureExpression(AST node) {
        AST paramNode = node.getFirstChild();
        Parameter[] parameters = Parameter.EMPTY_ARRAY;
        AST codeNode = paramNode;
        if (isType(PARAMETERS, paramNode) || isType(IMPLICIT_PARAMETERS, paramNode)) {
            parameters = parameters(paramNode);
            codeNode = paramNode.getNextSibling();
        }
        Statement code = statementListNoChild(codeNode);
        return new ClosureExpression(parameters, code);
    }

    protected Expression negateExpression(AST negateExpr) {
        AST node = negateExpr.getFirstChild();

        // if we are a number literal then lets just parse it
        // as the negation operator on MIN_INT causes rounding to a long
        String text = node.getText();
        switch (node.getType()) {
            case NUM_DOUBLE:
            case NUM_FLOAT:
            case NUM_BIG_DECIMAL:
                return new ConstantExpression(Numbers.parseDecimal("-" + text));

            case NUM_BIG_INT:
            case NUM_INT:
            case NUM_LONG:
                return new ConstantExpression(Numbers.parseInteger("-" + text));

            default:
                return new NegationExpression(expression(node));
        }
    }

    protected ConstantExpression decimalExpression(AST node) {
        String text = node.getText();
        return new ConstantExpression(Numbers.parseDecimal(text));
    }

    protected ConstantExpression integerExpression(AST node) {
        String text = node.getText();
        return new ConstantExpression(Numbers.parseInteger(text));
    }

    protected Expression gstring(AST gstringNode) {
        List strings = new ArrayList();
        List values = new ArrayList();

        StringBuffer buffer = new StringBuffer();

        for (AST node = gstringNode.getFirstChild(); node != null; node = node.getNextSibling()) {
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

                case DOT:
                    {
                        Expression expression = expression(node);
                        values.add(expression);
                        buffer.append("$");
                        buffer.append(expression.getText());
                    }
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
                    unknownAST(node);
            }
        }
        return new GStringExpression(buffer.toString(), strings, values);
    }

    protected Type type(AST typeNode) {
        // TODO intern types?
        return new Type(resolvedName(typeNode.getFirstChild()));
    }

    protected String qualifiedName(AST qualifiedNameNode) {
        if (isType(IDENT, qualifiedNameNode)) {
            return qualifiedNameNode.getText();
        }
        assertNodeType(DOT, qualifiedNameNode);

        AST node = qualifiedNameNode.getFirstChild();
        StringBuffer buffer = new StringBuffer();
        boolean first = true;

        for (; node != null; node = node.getNextSibling()) {
            if (first) {
                first = false;
            }
            else {
                buffer.append(".");
            }
            buffer.append(qualifiedName(node));
        }
        return buffer.toString();
    }

    protected String typeName(AST typeNode) {
        String answer = null;
        AST node = typeNode.getFirstChild();
        if (node != null) {
            if (isType(INDEX_OP, node) || isType(ARRAY_DECLARATOR, node)) {
                return resolveTypeName(node.getFirstChild().getText()) + "[]";
            }
            answer = resolveTypeName(node.getText());
            node = node.getNextSibling();
            if (isType(INDEX_OP, node) || isType(ARRAY_DECLARATOR, node)) {
                return answer + "[]";
            }
        }
        return answer;
    }

    /**
     * Performs a name resolution to see if the given name is a type from imports,
     * aliases or newly created classes
     */
    protected String resolveTypeName(String name, boolean safe) {
        if (name == null) {
            return null;
        }
        return resolveNewClassOrName(name, safe);
    }

    /**
     * Performs a name resolution to see if the given name is a type from imports,
     * aliases or newly created classes
     */
    protected String resolveTypeName(String name) {
        return resolveTypeName(name, true);
    }

    /**
     * Extracts an identifier from the Antlr AST and then performs a name resolution
     * to see if the given name is a type from imports, aliases or newly created classes
     */
    protected String resolvedName(AST node) {
        if (isType(TYPE, node)) {
            node = node.getFirstChild();
        }
        String answer = null;
        if (isType(DOT, node)) {
            answer = qualifiedName(node);
        }
        else if (isPrimitiveTypeLiteral(node)) {
            answer = node.getText();
        }
        else if (isType(INDEX_OP, node) || isType(ARRAY_DECLARATOR, node)) {
            AST child = node.getFirstChild();
            String text = resolvedName(child);
            // TODO sometimes we have ARRAY_DECLARATOR->typeName
            // and sometimes we have typeName->ARRAY_DECLARATOR
            // so here's a little fudge while we be more consistent in the Antlr
            if (text.endsWith("[]")) {
                return text;
            }
            return text + "[]";
        }
        else {
            String identifier = identifier(node);
            answer = resolveTypeName(identifier);
        }
        AST nextSibling = node.getNextSibling();
        if (isType(INDEX_OP, nextSibling) || isType(ARRAY_DECLARATOR, node)) {
            return answer + "[]";
        }
        else {
            return answer;
        }
    }

    protected boolean isPrimitiveTypeLiteral(AST node) {
        int type = node.getType();
        switch (type) {
            case LITERAL_boolean:
            case LITERAL_byte:
            case LITERAL_char:
            case LITERAL_double:
            case LITERAL_float:
            case LITERAL_int:
            case LITERAL_long:
            case LITERAL_short:
                return true;

            default:
                return false;
        }
    }

    /**
     * Extracts an identifier from the Antlr AST
     */
    protected String identifier(AST node) {
        assertNodeType(IDENT, node);
        return node.getText();
    }

    protected String label(AST labelNode) {
        AST node = labelNode.getFirstChild();
        if (node == null) {
            return null;
        }
        return identifier(node);
    }



    // Helper methods
    //-------------------------------------------------------------------------


    /**
     * Returns true if the modifiers flags contain a visibility modifier
     */
    protected boolean hasVisibility(int modifiers) {
        return (modifiers & (Constants.ACC_PRIVATE | Constants.ACC_PROTECTED | Constants.ACC_PUBLIC)) != 0;
    }

    protected void configureAST(ASTNode node, AST ast) {
        node.setColumnNumber(ast.getColumn());
        node.setLineNumber(ast.getLine());

        // TODO we could one day store the Antlr AST on the Groovy AST
        // node.setCSTNode(ast);
    }

    protected static Token makeToken(int typeCode, AST node) {
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
            throw new ASTRuntimeException(node, "No child node available in AST when expecting type: " + type);
        }
        if (node.getType() != type) {
            throw new ASTRuntimeException(node, "Unexpected node type: " + node.getType() + " found when expecting type: " + type);
        }
    }

    protected void notImplementedYet(AST node) {
        throw new ASTRuntimeException(node, "AST node not implemented yet for type: " + node.getType());
    }

    protected void unknownAST(AST node) {
        throw new ASTRuntimeException(node, "Unknown type: " + node.getType());
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
