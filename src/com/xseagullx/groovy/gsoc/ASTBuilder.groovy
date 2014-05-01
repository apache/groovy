package com.xseagullx.groovy.gsoc

import groovyjarjarasm.asm.Opcodes
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.NotNull
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.tree.TerminalNode
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

import java.lang.reflect.Modifier

class ASTBuilder extends GroovyBaseListener {
    ModuleNode moduleNode

    private SourceUnit sourceUnit
    private ClassLoader classLoader

    ASTBuilder(SourceUnit sourceUnit, ClassLoader classLoader) {
        this.classLoader = classLoader
        this.sourceUnit = sourceUnit
        moduleNode = new ModuleNode(sourceUnit)

        def lexer = new GroovyLexer(new ANTLRInputStream(sourceUnit.source.reader))
        CommonTokenStream tokens = new CommonTokenStream(lexer)
        def parser = new GroovyParser(tokens)
        ParseTree tree = parser.compilationUnit()

        try {
            new ParseTreeWalker().walk(this, tree);
        }
        catch (CompilationFailedException ignored) {
            // Compilation failed.
        }
    }

    @Override void exitImportStatement(@NotNull GroovyParser.ImportStatementContext ctx) {
        ImportNode node
        if (ctx.getChild(ctx.childCount - 1).text == '*') {
            moduleNode.addStarImport(ctx.IDENTIFIER().join('.') + '.')
            node = moduleNode.starImports.last()
        }
        else {
            moduleNode.addImport(ctx.IDENTIFIER()[-1].text, ClassHelper.make(ctx.IDENTIFIER().join('.')))
            node = moduleNode.imports.last()
            setupNodeLocation(node.type, ctx)
        }
        setupNodeLocation(node, ctx)
    }

    @Override void enterPackageDefinition(@NotNull GroovyParser.PackageDefinitionContext ctx) {
        moduleNode.packageName = ctx.IDENTIFIER().join('.') + '.'
        setupNodeLocation(moduleNode.package, ctx)
    }

    @Override void exitClassDeclaration(@NotNull GroovyParser.ClassDeclarationContext ctx) {
        def classNode = new ClassNode("${moduleNode.packageName ?: ""}${ctx.IDENTIFIER()}", Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        setupNodeLocation(classNode, ctx)
        moduleNode.addClass(classNode)
        classNode.modifiers = parseClassModifiers(ctx.classModifiers())
        classNode.syntheticPublic = (classNode.modifiers & Opcodes.ACC_SYNTHETIC) != 0
        classNode.modifiers &= ~Opcodes.ACC_SYNTHETIC // FIXME Magic with synthetic modifier.

        parseMembers(classNode, ctx.classMember())
    }

    def parseMembers(ClassNode classNode, List<GroovyParser.ClassMemberContext> ctx) {
        for (member in ctx) {
            def memberContext = member.children[0]
            assert memberContext instanceof GroovyParser.ConstructorDeclarationContext ||
                    memberContext instanceof GroovyParser.MethodDeclarationContext ||
                    memberContext instanceof GroovyParser.FieldDeclarationContext

            // This inspection is suppressed cause I use Runtime multimethods dispatching mechanics of Groovy.
            //noinspection GroovyAssignabilityCheck
            parseMember(classNode, memberContext)
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static def parseMember(ClassNode classNode, GroovyParser.ConstructorDeclarationContext ctx) {
        int modifiers = ctx.VISIBILITY_MODIFIER() ? parseVisibilityModifiers(ctx.VISIBILITY_MODIFIER()) : Opcodes.ACC_PUBLIC

        def constructorNode = classNode.addConstructor(modifiers, parseParameters(ctx.argumentDeclarationList()), [] as ClassNode[], parseBlockStatement(ctx.blockStatement()))
        setupNodeLocation(constructorNode, ctx)
        constructorNode.syntheticPublic = ctx.VISIBILITY_MODIFIER() == null
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Statements.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Statement parseBlockStatement(GroovyParser.BlockStatementContext ctx) {
        def statement = new BlockStatement()
        if (!ctx)
            return statement

        ctx.statement().each {
            statement.addStatement(parseStatement(it))
        }
        statement
    }

    static Statement parseStatement(GroovyParser.StatementContext ctx) {
        throw new RuntimeException("Unsupported statement type! $ctx")
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static Statement parseStatement(GroovyParser.ExpressionStatementContext ctx) {
        setupNodeLocation(new ExpressionStatement(parseExpression(ctx.expression())), ctx)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Expressions.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Expression parseExpression(GroovyParser.ExpressionContext ctx) {
        throw new RuntimeException("Unsupported expression type! $ctx")
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static Expression parseExpression(GroovyParser.BinaryExpressionContext ctx) {
        def op = createToken(ctx.getChild(1) as TerminalNode)
        def expression
        def left = parseExpression(ctx.expression(0))
        def right = null // Will be initialized later, in switch. We should handle as and instanceof creating
        // ClassExpression for given IDENTIFIERS. So, switch should fall through.
        //noinspection GroovyFallthrough
        switch (op.type) {
            case Types.RANGE_OPERATOR:
                right = parseExpression(ctx.expression(1))
                expression = new RangeExpression(left, right, !op.text.endsWith('<'))
                break;
            case Types.KEYWORD_AS:
                def classNode = setupNodeLocation(ClassHelper.make(ctx.IDENTIFIER().text), ctx.IDENTIFIER().symbol)
                expression = CastExpression.asExpression(classNode, left)
                break;
            case Types.KEYWORD_INSTANCEOF:
                def classNode = setupNodeLocation(ClassHelper.make(ctx.IDENTIFIER().text), ctx.IDENTIFIER().symbol)
                right = new ClassExpression(classNode)
            default:
                if (!right)
                    right = parseExpression(ctx.expression(1))
                expression = new BinaryExpression(left, op, right)
                break
        }

        expression.columnNumber = op.startColumn
        expression.lastColumnNumber = op.startColumn + op.text.length()
        expression.lineNumber = op.startLine
        expression.lastLineNumber = op.startLine
        expression
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static Expression parseExpression(GroovyParser.UnaryExpressionContext ctx) {
        def node = null
        def op = ctx.getChild(0) as TerminalNode
        switch (op.text) {
            case '-' : node = new UnaryMinusExpression(parseExpression(ctx.expression())); break
            case '+' : node = new UnaryPlusExpression(parseExpression(ctx.expression())); break
            case '!' : node = new NotExpression(parseExpression(ctx.expression())); break
            case '~' : node = new BitwiseNegationExpression(parseExpression(ctx.expression())); break
            default: assert false, "There is no $op.text handler."; break
        }

        node.columnNumber = op.symbol.charPositionInLine + 1
        node.lineNumber = op.symbol.line
        node.lastLineNumber = op.symbol.line
        node.lastColumnNumber = op.symbol.charPositionInLine + 1 + op.text.length()
        node
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static Expression parseExpression(GroovyParser.VariableExpressionContext ctx) {
        setupNodeLocation(new VariableExpression(ctx.IDENTIFIER().text), ctx)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static Expression parseExpression(GroovyParser.FieldAccessExpressionContext ctx) {
        def op = ctx.getChild(1) as TerminalNode
        def text = ctx.IDENTIFIER().text
        def left = parseExpression(ctx.expression())
        def right = new ConstantExpression(text)
        def node
        if (op.text == '.@')
            node = new AttributeExpression(left, right)
        else {
            node = new PropertyExpression(left, right, ctx.getChild(1).text in ['?.', '*.'])
        }
        setupNodeLocation(node, ctx)
        node.spreadSafe = op.text == '*.'
        node
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static PrefixExpression parseExpression(GroovyParser.PrefixExpressionContext ctx) {
        setupNodeLocation(new PrefixExpression(createToken(ctx.getChild(0) as TerminalNode), parseExpression(ctx.expression())), ctx)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static PostfixExpression parseExpression(GroovyParser.PostfixExpressionContext ctx) {
        setupNodeLocation(new PostfixExpression(parseExpression(ctx.expression()), createToken(ctx.getChild(1) as TerminalNode)), ctx)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static ConstantExpression parseExpression(GroovyParser.ConstantExpressionContext ctx) {
        def val = ctx.NUMBER() ? Integer.parseInt(ctx.NUMBER().text) : ctx.text[1..-2]
        setupNodeLocation(new ConstantExpression(val, true), ctx)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static Expression parseExpression(GroovyParser.NullExpressionContext ctx) {
        setupNodeLocation(new ConstantExpression(null), ctx)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static Expression parseExpression(GroovyParser.AssignmentExpressionContext ctx) {
        def left = parseExpression(ctx.expression(0)) // TODO reference to AntlrParserPlugin line 2304 for error handling.
        def right = parseExpression(ctx.expression(1))
        setupNodeLocation(new BinaryExpression(left, createToken(ctx.getChild(1) as TerminalNode), right), ctx)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static Expression parseExpression(GroovyParser.DeclarationExpressionContext ctx) {
        def left = new VariableExpression(ctx.IDENTIFIER().text)
        def col = ctx.start.charPositionInLine + 1 // FIXME Why assignment token location is it's first occurrence.
        def token = new Token(Types.ASSIGN, '=', ctx.start.line, col)
        def right = ctx.childCount == 2 ? new EmptyExpression() : parseExpression(ctx.expression())

        setupNodeLocation(new DeclarationExpression(left, token, right), ctx)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static Expression parseExpression(GroovyParser.CallExpressionContext ctx) {
        def argumentListExpression = createArgumentList(ctx.argumentList())

        if (ctx.expression() instanceof GroovyParser.VariableExpressionContext)
            new MethodCallExpression(new VariableExpression("this"), new ConstantExpression(ctx.expression().text), argumentListExpression)
        else
            new MethodCallExpression(parseExpression(ctx.expression()), new ConstantExpression("call"), argumentListExpression)
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    static Expression parseExpression(GroovyParser.MethodCallExpressionContext ctx) {
        def method = new ConstantExpression(ctx.IDENTIFIER().text)
        ArgumentListExpression argumentListExpression = createArgumentList(ctx.argumentList())
        def expression = new MethodCallExpression(parseExpression(ctx.expression()), method, argumentListExpression)
        expression.implicitThis = false
        def op = ctx.getChild(1) as TerminalNode
        expression.spreadSafe = op.text == '*.'
        expression.safe = op.text == '?.'
        expression
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // End of Expressions.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static ArgumentListExpression createArgumentList(GroovyParser.ArgumentListContext ctx) {
        def argumentListExpression = new ArgumentListExpression()
        ctx?.expression()?.each {
            argumentListExpression.addExpression(parseExpression(it))
        }
        argumentListExpression
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    def parseMember(ClassNode classNode, GroovyParser.MethodDeclarationContext ctx) {
        //noinspection GroovyAssignabilityCheck
        def (int modifiers, boolean hasVisibilityModifier) = parseModifiers(ctx.memberModifier(), Opcodes.ACC_PUBLIC)
        def statement = parseBlockStatement(ctx.blockStatement())

        def params = parseParameters(ctx.argumentDeclarationList())
        def methodNode = classNode.addMethod(ctx.IDENTIFIER().text, modifiers, parseTypeDeclaration(ctx.typeDeclaration()), params, [] as ClassNode[], statement)
        setupNodeLocation(methodNode, ctx)
        methodNode.syntheticPublic = !hasVisibilityModifier
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    def parseMember(ClassNode classNode, GroovyParser.FieldDeclarationContext ctx) {
        //noinspection GroovyAssignabilityCheck
        def (int modifiers, boolean hasVisibilityModifier) = parseModifiers(ctx.memberModifier())

        def typeDeclaration = parseTypeDeclaration(ctx.typeDeclaration())
        if (hasVisibilityModifier)
            setupNodeLocation(classNode.addField(ctx.IDENTIFIER().text, modifiers, typeDeclaration, null), ctx)
        else { // no visibility specified. Generate property node.
            def propertyModifier = modifiers | Opcodes.ACC_PUBLIC
            def propertyNode = classNode.addProperty(ctx.IDENTIFIER().text, propertyModifier, typeDeclaration, null, null, null)
            propertyNode.field.modifiers = modifiers | Opcodes.ACC_PRIVATE
            propertyNode.field.synthetic = true
            setupNodeLocation(propertyNode.field, ctx)
            setupNodeLocation(propertyNode, ctx)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Utility methods.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Token createToken(TerminalNode node) {
        def text = node.text
        new Token(node.text == '..<' || node.text == '..' ? Types.RANGE_OPERATOR : Types.lookup(text, Types.ANY),
            text, node.symbol.line, node.symbol.charPositionInLine + 1)
    }

    static ClassNode parseTypeDeclaration(GroovyParser.TypeDeclarationContext ctx) {
        !ctx || ctx.KW_DEF() ? ClassHelper.OBJECT_TYPE : setupNodeLocation(ClassHelper.make(ctx.IDENTIFIER().text), ctx)
    }

    static Parameter[] parseParameters(GroovyParser.ArgumentDeclarationListContext ctx) {
        ctx.argumentDeclaration().collect {
            setupNodeLocation(new Parameter(parseTypeDeclaration(it.typeDeclaration()), it.IDENTIFIER().text), it)
        }
    }

    /**
     * Sets location(lineNumber, colNumber, lastLineNumber, lastColumnNumber) for node using standard context information.
     * Note: this method is implemented to be closed over ASTNode. It returns same node as it received in arguments.
     * @param astNode Node to be modified.
     * @param ctx Context from which information is obtained.
     * @return Modified astNode.
     */
    static <T extends ASTNode> T setupNodeLocation(T astNode, ParserRuleContext ctx) {
        astNode.lineNumber = ctx.start.line
        astNode.columnNumber = ctx.start.charPositionInLine + 1
        astNode.lastLineNumber = ctx.stop.line
        astNode.lastColumnNumber = ctx.stop.charPositionInLine + 1 + ctx.stop.text.length()
        astNode
    }

    static <T extends ASTNode> T setupNodeLocation(T astNode, org.antlr.v4.runtime.Token token) {
        astNode.lineNumber = token.line
        astNode.columnNumber = token.charPositionInLine + 1
        astNode.lastLineNumber = token.line
        astNode.lastColumnNumber = token.charPositionInLine + 1 + token.text.length()
        astNode
    }

    int parseClassModifiers(@NotNull GroovyParser.ClassModifiersContext ctx) {
        int modifier = parseVisibilityModifiers(ctx.VISIBILITY_MODIFIER(), Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC);
        modifier |= parseModifier(ctx.KW_ABSTRACT(), Opcodes.ACC_ABSTRACT)
        modifier |= parseModifier(ctx.KW_FINAL(), Opcodes.ACC_FINAL)
        modifier |= parseModifier(ctx.KW_STATIC(), Opcodes.ACC_STATIC)
        modifier |= parseModifier(ctx.KW_STRICTFP(), Opcodes.ACC_STRICT)
        modifier
    }

    int checkModifierDuplication(int modifier, int opcode, TerminalNode node) {
        if (!(modifier & opcode))
            modifier | opcode
        else {
            def symbol = node.symbol

            def line = symbol.line
            def col = symbol.charPositionInLine + 1
            sourceUnit.addError(new SyntaxException("Cannot repeat modifier: $symbol.text at line: $line column: $col. File: $sourceUnit.name", line, col))
            modifier
        }
    }

    int parseModifier(Collection<TerminalNode> nodes, int opcode) {
        if (!nodes)
            return 0

        if (nodes.size() > 1) {
            def modifier = nodes[1].symbol

            def line = modifier.line
            def col = modifier.charPositionInLine + 1
            sourceUnit.addError(new SyntaxException("Cannot repeat modifier: $modifier.text at line: $line column: $col. File: $sourceUnit.name", line, col))
        }
        opcode
    }

    /**
     * Traverse through modifiers, and combine them in one int value. Raise an error if there is multiple occurrences of same modifier.
     * @param ctxList modifiers list.
     * @param defaultVisibilityModifier Default visibility modifier. Can be null. Applied if providen, and no visibility modifier exists in the ctxList.
     * @return tuple of int modifier and boolean flag, signalising visibility modifiers presence(true if there is visibility modifier in list, false otherwise).
     * @see #checkModifierDuplication(int, int, org.antlr.v4.runtime.tree.TerminalNode)
     */
    def parseModifiers(List<GroovyParser.MemberModifierContext> ctxList, Integer defaultVisibilityModifier = null) {
        int modifiers = 0;
        boolean hasVisibilityModifier = false;
        ctxList.each {
            def child = (it.getChild(0) as TerminalNode)
            switch (child.symbol.type) {
                case GroovyLexer.KW_STATIC: modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_STATIC, child); break
                case GroovyLexer.KW_ABSTRACT: modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_ABSTRACT, child); break
                case GroovyLexer.KW_FINAL: modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_FINAL, child); break
                case GroovyLexer.KW_NATIVE: modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_NATIVE, child); break
                case GroovyLexer.KW_SYNCHRONIZED: modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_SYNCHRONIZED, child); break
                case GroovyLexer.KW_TRANSIENT: modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_TRANSIENT, child); break
                case GroovyLexer.KW_VOLATILE: modifiers |= checkModifierDuplication(modifiers, Opcodes.ACC_VOLATILE, child); break
                case GroovyLexer.VISIBILITY_MODIFIER:
                    modifiers |= parseVisibilityModifiers(child)
                    hasVisibilityModifier = true
                    break
            }
        }
        if (!hasVisibilityModifier && defaultVisibilityModifier != null)
            modifiers |= defaultVisibilityModifier

        [modifiers, hasVisibilityModifier]
    }

    void reportError(String text, int line, int col) {
        sourceUnit.addError(new SyntaxException(text, line, col))
    }

    static int parseVisibilityModifiers(TerminalNode modifier) {
        assert modifier.symbol.type == GroovyLexer.VISIBILITY_MODIFIER
        switch (modifier.symbol.text) {
            case "public": Opcodes.ACC_PUBLIC; break
            case "private": Opcodes.ACC_PRIVATE; break
            case "protected": Opcodes.ACC_PROTECTED; break
            default: throw new AssertionError("$modifier.symbol.text is not a valid visibility modifier!")
        }
    }

    int parseVisibilityModifiers(List<TerminalNode> modifiers, int defaultValue) {
        if (!modifiers)
            return defaultValue

        if (modifiers.size() > 1) {
            def modifier = modifiers[1].symbol

            def line = modifier.line
            def col = modifier.charPositionInLine + 1

            reportError("Cannot specify modifier: $modifier.text when access scope has already been defined at line: $line column: $col. File: $sourceUnit.name", line, col)
        }

        parseVisibilityModifiers(modifiers[0])
    }
}
