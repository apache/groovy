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
package org.apache.groovy.parser.antlr4;

import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import groovy.transform.CompileStatic;
import groovy.transform.NonSealed;
import groovy.transform.Sealed;
import groovy.transform.Trait;
import groovy.transform.TupleConstructor;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.groovy.parser.antlr4.internal.DescriptiveErrorStrategy;
import org.apache.groovy.parser.antlr4.internal.atnmanager.AtnManager;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.apache.groovy.util.Maps;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.antlr.EnumHelper;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.EnumConstantClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModifierNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.NodeMetaDataHandler;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.ast.tools.ClosureUtils;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.syntax.Numbers;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static groovy.lang.Tuple.tuple;
import static org.apache.groovy.parser.antlr4.GroovyParser.*;
import static org.apache.groovy.parser.antlr4.util.PositionConfigureUtils.configureAST;
import static org.apache.groovy.parser.antlr4.util.PositionConfigureUtils.configureEndPosition;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.closureX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.listX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.classgen.asm.util.TypeUtil.isPrimitiveType;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;

/**
 * Builds the AST from the parse tree generated by Antlr4.
 */
public class AstBuilder extends GroovyParserBaseVisitor<Object> {

    public AstBuilder(final SourceUnit sourceUnit, final boolean groovydocEnabled, final boolean runtimeGroovydocEnabled) {
        this.sourceUnit = sourceUnit;
        this.moduleNode = new ModuleNode(sourceUnit);
        CharStream charStream = createCharStream(sourceUnit);

        this.lexer = new GroovyLangLexer(charStream);
        this.parser = new GroovyLangParser(new CommonTokenStream(this.lexer));
        this.parser.setErrorHandler(new DescriptiveErrorStrategy(charStream));

        this.groovydocManager = new GroovydocManager(groovydocEnabled, runtimeGroovydocEnabled);
        this.tryWithResourcesASTTransformation = new TryWithResourcesASTTransformation(this);
    }

    private CharStream createCharStream(final SourceUnit sourceUnit) {
        CharStream charStream;

        try {
            charStream = CharStreams.fromReader(
                    new BufferedReader(sourceUnit.getSource().getReader()),
                    sourceUnit.getName());
        } catch (IOException e) {
            throw new RuntimeException("Error occurred when reading source code.", e);
        }

        return charStream;
    }

    private GroovyParserRuleContext buildCST() throws CompilationFailedException {
        GroovyParserRuleContext result;

        try {
            // parsing have to wait util clearing is complete.
            AtnManager.READ_LOCK.lock();
            try {
                final TokenStream tokenStream = parser.getInputStream();
                if (SLL_THRESHOLD >= 0 && tokenStream.size() > SLL_THRESHOLD) {
                    // The more tokens to parse, the more possibility SLL will fail and the more parsing time will waste.
                    // The option `groovy.antlr4.sll.threshold` could be tuned for better parsing performance, but it is disabled by default.
                    // If the token count is greater than `groovy.antlr4.sll.threshold`, use LL directly.
                    result = buildCST(PredictionMode.LL);
                } else {
                    try {
                        result = buildCST(PredictionMode.SLL);
                    } catch (Throwable t) {
                        // if some syntax error occurred in the lexer, no need to retry the powerful LL mode
                        if (t instanceof GroovySyntaxError && GroovySyntaxError.LEXER == ((GroovySyntaxError) t).getSource()) {
                            throw t;
                        }

                        tokenStream.seek(0);
                        result = buildCST(PredictionMode.LL);
                    }
                }
            } finally {
                AtnManager.READ_LOCK.unlock();
            }
        } catch (Throwable t) {
            throw convertException(t);
        }

        return result;
    }

    private GroovyParserRuleContext buildCST(final PredictionMode predictionMode) {
        parser.getInterpreter().setPredictionMode(predictionMode);

        if (PredictionMode.SLL.equals(predictionMode)) {
            this.removeErrorListeners();
        } else {
            this.addErrorListeners();
        }

        return parser.compilationUnit();
    }

    private CompilationFailedException convertException(final Throwable t) {
        CompilationFailedException cfe;

        if (t instanceof CompilationFailedException) {
            cfe = (CompilationFailedException) t;
        } else if (t instanceof ParseCancellationException) {
            cfe = createParsingFailedException(t.getCause());
        } else {
            cfe = createParsingFailedException(t);
        }

        return cfe;
    }

    public ModuleNode buildAST() {
        try {
            return (ModuleNode) this.visit(this.buildCST());
        } catch (Throwable t) {
            throw convertException(t);
        }
    }

    @Override
    public ModuleNode visitCompilationUnit(final CompilationUnitContext ctx) {
        this.visit(ctx.packageDeclaration());

        for (ASTNode node : this.visitScriptStatements(ctx.scriptStatements())) {
            if (node instanceof DeclarationListStatement) { // local variable declaration(s)
                for (Statement stmt: ((DeclarationListStatement) node).getDeclarationStatements()) {
                    this.moduleNode.addStatement(stmt);
                }
            } else if (node instanceof Statement) {
                this.moduleNode.addStatement((Statement) node);
            } else if (node instanceof MethodNode) {
                this.moduleNode.addMethod((MethodNode) node);
            }
        }

        for (ClassNode node : this.classNodeList) {
            this.moduleNode.addClass(node);
        }

        if (this.isPackageInfoDeclaration()) {
            ClassNode packageInfo = ClassHelper.make(this.moduleNode.getPackageName() + PACKAGE_INFO);
            if (!this.moduleNode.getClasses().contains(packageInfo)) {
                this.moduleNode.addClass(packageInfo);
            }
        } else if (this.isBlankScript()) {
            // add "return null" if script has no statements/methods/classes
            this.moduleNode.addStatement(ReturnStatement.RETURN_NULL_OR_VOID);
        }

        this.configureScriptClassNode();

        if (this.numberFormatError != null) {
            throw createParsingFailedException(this.numberFormatError.getV2().getMessage(), this.numberFormatError.getV1());
        }

        return this.moduleNode;
    }

    @Override
    public List<ASTNode> visitScriptStatements(final ScriptStatementsContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return ctx.scriptStatement().stream()
                .map(e -> (ASTNode) visit(e))
                .collect(Collectors.toList());
    }

    @Override
    public PackageNode visitPackageDeclaration(final PackageDeclarationContext ctx) {
        String packageName = this.visitQualifiedName(ctx.qualifiedName());
        moduleNode.setPackageName(packageName + DOT_STR);

        PackageNode packageNode = moduleNode.getPackage();

        packageNode.addAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

        return configureAST(packageNode, ctx);
    }

    @Override
    public ImportNode visitImportDeclaration(final ImportDeclarationContext ctx) {
        List<AnnotationNode> annotations = this.visitAnnotationsOpt(ctx.annotationsOpt());

        boolean hasStatic = asBoolean(ctx.STATIC());
        boolean hasStar   = asBoolean(ctx.MUL());
        boolean hasAlias  = asBoolean(ctx.alias);

        ImportNode importNode;

        if (hasStatic) {
            if (hasStar) { // e.g. import static java.lang.Math.*
                String qualifiedName = this.visitQualifiedName(ctx.qualifiedName());
                ClassNode importType = makeClassNode(qualifiedName);
                configureAST(importType, ctx.qualifiedName());

                moduleNode.addStaticStarImport(importType.getText(), importType, annotations);
                importNode = last(moduleNode.getStaticStarImports().values());
            } else { // e.g. import static java.lang.Math.pow
                List<? extends QualifiedNameElementContext> identifierList = ctx.qualifiedName().qualifiedNameElement();
                int identifierListSize = identifierList.size();

                String qualifiedName = identifierList.stream().limit(identifierListSize - 1).map(ParseTree::getText).collect(Collectors.joining(DOT_STR));
                ClassNode importType = makeClassNode(qualifiedName);
                configureAST(importType, ctx.qualifiedName()); // qualifiedName() includes member name
                configureEndPosition(importType, identifierList.get(Math.max(0, identifierListSize - 2)).getStop());

                String memberName = identifierList.get(identifierListSize - 1).getText();
                String simpleName = hasAlias ? ctx.alias.getText() : memberName;

                moduleNode.addStaticImport(importType, memberName, simpleName, annotations);
                importNode = last(moduleNode.getStaticImports().values());
            }
        } else {
            if (hasStar) { // e.g. import java.util.*
                String qualifiedName = this.visitQualifiedName(ctx.qualifiedName());
                moduleNode.addStarImport(qualifiedName + DOT_STR, annotations);
                importNode = last(moduleNode.getStarImports());
            } else { // e.g. import java.util.Map
                String qualifiedName = this.visitQualifiedName(ctx.qualifiedName());
                ClassNode importType = makeClassNode(qualifiedName);
                configureAST(importType, ctx.qualifiedName());

                String simpleName = hasAlias ? ctx.alias.getText()
                                             : last(ctx.qualifiedName().qualifiedNameElement()).getText();

                moduleNode.addImport(simpleName, importType, annotations);
                importNode = last(moduleNode.getImports());
            }
        }

        return configureAST(importNode, ctx);
    }

    private static AnnotationNode makeAnnotationNode(final Class<? extends Annotation> type) {
        AnnotationNode node = new AnnotationNode(ClassHelper.make(type));
        // TODO: source offsets
        return node;
    }

    private static ClassNode makeClassNode(final String name) {
        ClassNode node = ClassHelper.make(name);
        // TODO: shared instances
        return node;
    }

    // statement { -------------------------------------------------------------

    @Override
    public AssertStatement visitAssertStatement(final AssertStatementContext ctx) {
        visitingAssertStatementCount += 1;

        Expression conditionExpression = (Expression) this.visit(ctx.ce);

        if (conditionExpression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) conditionExpression;

            if (binaryExpression.getOperation().getType() == Types.ASSIGN) {
                throw createParsingFailedException("Assignment expression is not allowed in the assert statement", conditionExpression);
            }
        }

        BooleanExpression booleanExpression =
                configureAST(
                        new BooleanExpression(conditionExpression), conditionExpression);

        if (!asBoolean(ctx.me)) {
            return configureAST(
                    new AssertStatement(booleanExpression), ctx);
        }

        AssertStatement result = configureAST(new AssertStatement(booleanExpression,
                        (Expression) this.visit(ctx.me)),
                ctx);

        visitingAssertStatementCount -= 1;

        return result;
    }

    @Override
    public Statement visitConditionalStatement(final ConditionalStatementContext ctx) {
        if (asBoolean(ctx.ifElseStatement())) {
            return configureAST(this.visitIfElseStatement(ctx.ifElseStatement()), ctx);
        } else if (asBoolean(ctx.switchStatement())) {
            return configureAST(this.visitSwitchStatement(ctx.switchStatement()), ctx);
        }

        throw createParsingFailedException("Unsupported conditional statement", ctx);
    }

    @Override
    public IfStatement visitIfElseStatement(final IfElseStatementContext ctx) {
        Expression conditionExpression = this.visitExpressionInPar(ctx.expressionInPar());
        BooleanExpression booleanExpression =
                configureAST(
                        new BooleanExpression(conditionExpression), conditionExpression);

        Statement ifBlock =
                this.unpackStatement(
                        (Statement) this.visit(ctx.tb));
        Statement elseBlock =
                this.unpackStatement(
                        asBoolean(ctx.ELSE())
                                ? (Statement) this.visit(ctx.fb)
                                : EmptyStatement.INSTANCE);

        return configureAST(new IfStatement(booleanExpression, ifBlock, elseBlock), ctx);
    }

    @Override
    public Statement visitLoopStmtAlt(final LoopStmtAltContext ctx) {
        switchExpressionRuleContextStack.push(ctx);
        visitingLoopStatementCount += 1;
        try {
            return configureAST((Statement) this.visit(ctx.loopStatement()), ctx);
        } finally {
            switchExpressionRuleContextStack.pop();
            visitingLoopStatementCount -= 1;
        }
    }

    @Override
    public ForStatement visitForStmtAlt(final ForStmtAltContext ctx) {
        Tuple2<Parameter, Expression> controlTuple = this.visitForControl(ctx.forControl());

        Statement loopBlock = this.unpackStatement((Statement) this.visit(ctx.statement()));

        return configureAST(
                new ForStatement(controlTuple.getV1(), controlTuple.getV2(), asBoolean(loopBlock) ? loopBlock : EmptyStatement.INSTANCE),
                ctx);
    }

    @Override
    public Tuple2<Parameter, Expression> visitForControl(final ForControlContext ctx) {
        if (asBoolean(ctx.enhancedForControl())) { // e.g. for(int i in 0..<10) {}
            return this.visitEnhancedForControl(ctx.enhancedForControl());
        }

        if (asBoolean(ctx.classicalForControl())) { // e.g. for(int i = 0; i < 10; i++) {}
            return this.visitClassicalForControl(ctx.classicalForControl());
        }

        throw createParsingFailedException("Unsupported for control: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitForInit(final ForInitContext ctx) {
        if (!asBoolean(ctx)) {
            return EmptyExpression.INSTANCE;
        }

        if (asBoolean(ctx.localVariableDeclaration())) {
            DeclarationListStatement declarationListStatement = this.visitLocalVariableDeclaration(ctx.localVariableDeclaration());
            List<DeclarationExpression> declarationExpressions = declarationListStatement.getDeclarationExpressions();

            if (declarationExpressions.size() == 1) {
                return configureAST((Expression) declarationExpressions.get(0), ctx);
            } else {
                return configureAST(new ClosureListExpression((List) declarationExpressions), ctx);
            }
        }

        if (asBoolean(ctx.expressionList())) {
            return this.translateExpressionList(ctx.expressionList());
        }

        throw createParsingFailedException("Unsupported for init: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitForUpdate(final ForUpdateContext ctx) {
        if (!asBoolean(ctx)) {
            return EmptyExpression.INSTANCE;
        }

        return this.translateExpressionList(ctx.expressionList());
    }

    private Expression translateExpressionList(final ExpressionListContext ctx) {
        List<Expression> expressionList = this.visitExpressionList(ctx);

        if (expressionList.size() == 1) {
            return configureAST(expressionList.get(0), ctx);
        } else {
            return configureAST(new ClosureListExpression(expressionList), ctx);
        }
    }

    @Override
    public Tuple2<Parameter, Expression> visitEnhancedForControl(final EnhancedForControlContext ctx) {
        Parameter parameter = new Parameter(this.visitType(ctx.type()), this.visitVariableDeclaratorId(ctx.variableDeclaratorId()).getName());
        ModifierManager modifierManager = new ModifierManager(this, this.visitVariableModifiersOpt(ctx.variableModifiersOpt()));
        modifierManager.processParameter(parameter);
        configureAST(parameter, ctx.variableDeclaratorId());
        return tuple(parameter, (Expression) this.visit(ctx.expression()));
    }

    @Override
    public Tuple2<Parameter, Expression> visitClassicalForControl(final ClassicalForControlContext ctx) {
        ClosureListExpression closureListExpression = new ClosureListExpression();

        closureListExpression.addExpression(this.visitForInit(ctx.forInit()));
        closureListExpression.addExpression(asBoolean(ctx.expression()) ? (Expression) this.visit(ctx.expression()) : EmptyExpression.INSTANCE);
        closureListExpression.addExpression(this.visitForUpdate(ctx.forUpdate()));

        return tuple(ForStatement.FOR_LOOP_DUMMY, closureListExpression);
    }

    @Override
    public WhileStatement visitWhileStmtAlt(final WhileStmtAltContext ctx) {
        Tuple2<BooleanExpression, Statement> conditionAndBlock = createLoopConditionExpressionAndBlock(ctx.expressionInPar(), ctx.statement());

        return configureAST(
                new WhileStatement(conditionAndBlock.getV1(), asBoolean(conditionAndBlock.getV2()) ? conditionAndBlock.getV2() : EmptyStatement.INSTANCE),
                ctx);
    }

    @Override
    public DoWhileStatement visitDoWhileStmtAlt(final DoWhileStmtAltContext ctx) {
        Tuple2<BooleanExpression, Statement> conditionAndBlock = createLoopConditionExpressionAndBlock(ctx.expressionInPar(), ctx.statement());

        return configureAST(
                new DoWhileStatement(conditionAndBlock.getV1(), asBoolean(conditionAndBlock.getV2()) ? conditionAndBlock.getV2() : EmptyStatement.INSTANCE),
                ctx);
    }

    private Tuple2<BooleanExpression, Statement> createLoopConditionExpressionAndBlock(final ExpressionInParContext eipc, final StatementContext sc) {
        Expression conditionExpression = this.visitExpressionInPar(eipc);

        BooleanExpression booleanExpression =
                configureAST(
                        new BooleanExpression(conditionExpression),
                        conditionExpression
                );

        Statement loopBlock = this.unpackStatement((Statement) this.visit(sc));

        return tuple(booleanExpression, loopBlock);
    }

    @Override
    public Statement visitTryCatchStatement(final TryCatchStatementContext ctx) {
        boolean resourcesExists = asBoolean(ctx.resources());
        boolean catchExists = asBoolean(ctx.catchClause());
        boolean finallyExists = asBoolean(ctx.finallyBlock());

        if (!(resourcesExists || catchExists || finallyExists)) {
            throw createParsingFailedException("Either a catch or finally clause or both is required for a try-catch-finally statement", ctx);
        }

        TryCatchStatement tryCatchStatement =
                new TryCatchStatement((Statement) this.visit(ctx.block()),
                        this.visitFinallyBlock(ctx.finallyBlock()));

        if (resourcesExists) {
            this.visitResources(ctx.resources()).forEach(tryCatchStatement::addResource);
        }

        ctx.catchClause().stream().map(this::visitCatchClause)
                .reduce(new LinkedList<>(), (r, e) -> {
                    r.addAll(e); // merge several LinkedList<CatchStatement> instances into one LinkedList<CatchStatement> instance
                    return r;
                })
                .forEach(tryCatchStatement::addCatch);

        return configureAST(
                tryWithResourcesASTTransformation.transform(
                        configureAST(tryCatchStatement, ctx)),
                ctx);
    }

    @Override
    public List<ExpressionStatement> visitResources(final ResourcesContext ctx) {
        return this.visitResourceList(ctx.resourceList());
    }

    @Override
    public List<ExpressionStatement> visitResourceList(final ResourceListContext ctx) {
        return ctx.resource().stream().map(this::visitResource).collect(Collectors.toList());
    }

    @Override
    public ExpressionStatement visitResource(final ResourceContext ctx) {
        if (asBoolean(ctx.localVariableDeclaration())) {
            List<ExpressionStatement> declarationStatements = this.visitLocalVariableDeclaration(ctx.localVariableDeclaration()).getDeclarationStatements();

            if (declarationStatements.size() > 1) {
                throw createParsingFailedException("Multi resources can not be declared in one statement", ctx);
            }

            return declarationStatements.get(0);
        } else if (asBoolean(ctx.expression())) {
            Expression expression = (Expression) this.visit(ctx.expression());
            boolean isVariableDeclaration = expression instanceof BinaryExpression
                    && Types.ASSIGN == ((BinaryExpression) expression).getOperation().getType()
                    && ((BinaryExpression) expression).getLeftExpression() instanceof VariableExpression;
            boolean isVariableAccess = expression instanceof VariableExpression;

            if (!(isVariableDeclaration || isVariableAccess)) {
                throw createParsingFailedException("Only variable declarations or variable access are allowed to declare resource", ctx);
            }
            BinaryExpression assignmentExpression;

            if (isVariableDeclaration) {
                assignmentExpression = (BinaryExpression) expression;
            } else if (isVariableAccess) {
                assignmentExpression = tryWithResourcesASTTransformation.transformResourceAccess(expression);
            } else {
                throw createParsingFailedException("Unsupported resource declaration", ctx);
            }

            return configureAST(
                    new ExpressionStatement(
                            configureAST(
                                    new DeclarationExpression(
                                            configureAST(
                                                    new VariableExpression(assignmentExpression.getLeftExpression().getText()),
                                                    assignmentExpression.getLeftExpression()
                                            ),
                                            assignmentExpression.getOperation(),
                                            assignmentExpression.getRightExpression()
                                    ), ctx)
                    ), ctx);
        }

        throw createParsingFailedException("Unsupported resource declaration: " + ctx.getText(), ctx);
    }

    /**
     * Multi-catch(1..*) clause will be unpacked to several normal catch clauses, so the return type is List
     *
     * @param ctx the parse tree
     * @return a list of CatchStatement instances
     */
    @Override
    public List<CatchStatement> visitCatchClause(final CatchClauseContext ctx) {
        return this.visitCatchType(ctx.catchType()).stream()
                .map(e -> configureAST(
                        new CatchStatement(
                                new Parameter(e, this.visitIdentifier(ctx.identifier())),
                                this.visitBlock(ctx.block())),
                        ctx))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassNode> visitCatchType(final CatchTypeContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.singletonList(ClassHelper.dynamicType());
        }

        return ctx.qualifiedClassName().stream()
                .map(this::visitQualifiedClassName)
                .collect(Collectors.toList());
    }

    @Override
    public Statement visitFinallyBlock(final FinallyBlockContext ctx) {
        if (!asBoolean(ctx)) {
            return EmptyStatement.INSTANCE;
        }

        return configureAST(
                this.createBlockStatement((Statement) this.visit(ctx.block())),
                ctx);
    }

    @Override
    public SwitchStatement visitSwitchStatement(final SwitchStatementContext ctx) {
        switchExpressionRuleContextStack.push(ctx);
        visitingSwitchStatementCount += 1;
        try {
            List<Statement> statementList =
                    ctx.switchBlockStatementGroup().stream()
                            .map(this::visitSwitchBlockStatementGroup)
                            .reduce(new LinkedList<>(), (r, e) -> {
                                r.addAll(e);
                                return r;
                            });

            List<CaseStatement> caseStatementList = new LinkedList<>();
            List<Statement> defaultStatementList = new LinkedList<>();

            for (Statement e : statementList) {
                if (e instanceof CaseStatement) {
                    caseStatementList.add((CaseStatement) e);
                } else if (isTrue(e, IS_SWITCH_DEFAULT)) {
                    defaultStatementList.add(e);
                }
            }

            int defaultStatementListSize = defaultStatementList.size();
            if (defaultStatementListSize > 1) {
                throw createParsingFailedException("a switch must only have one default branch", defaultStatementList.get(0));
            }

            if (defaultStatementListSize > 0 && last(statementList) instanceof CaseStatement) {
                throw createParsingFailedException("a default branch must only appear as the last branch of a switch", defaultStatementList.get(0));
            }

            return configureAST(
                    new SwitchStatement(
                            this.visitExpressionInPar(ctx.expressionInPar()),
                            caseStatementList,
                            defaultStatementListSize == 0 ? EmptyStatement.INSTANCE : defaultStatementList.get(0)
                    ),
                    ctx);
        } finally {
            switchExpressionRuleContextStack.pop();
            visitingSwitchStatementCount -= 1;
        }
    }

    @Override
    public List<Statement> visitSwitchBlockStatementGroup(final SwitchBlockStatementGroupContext ctx) {
        int labelCount = ctx.switchLabel().size();
        List<Token> firstLabelHolder = new ArrayList<>(1);

        return (List<Statement>) ctx.switchLabel().stream()
                .map(e -> (Object) this.visitSwitchLabel(e))
                .reduce(new ArrayList<Statement>(4), (r, e) -> {
                    Statement statement;
                    List<Statement> statementList = (List<Statement>) r;
                    Tuple2<Token, Expression> tuple = (Tuple2<Token, Expression>) e;
                    switch (tuple.getV1().getType()) {
                      case CASE:
                        if (!asBoolean(statementList)) {
                            firstLabelHolder.add(tuple.getV1());
                        }
                        statement = new CaseStatement(
                                tuple.getV2(),
                                // check whether processing the last label; if yes, block statement should be attached
                                (statementList.size() == labelCount - 1) ? this.visitBlockStatements(ctx.blockStatements()) : EmptyStatement.INSTANCE
                        );
                        statementList.add(configureAST(statement, firstLabelHolder.get(0)));
                        break;
                      case DEFAULT:
                        statement = this.visitBlockStatements(ctx.blockStatements());
                        statement.putNodeMetaData(IS_SWITCH_DEFAULT, Boolean.TRUE);
                        statementList.add(statement);
                        break;
                    }
                    return statementList;
                });
    }

    @Override
    public Tuple2<Token, Expression> visitSwitchLabel(final SwitchLabelContext ctx) {
        if (asBoolean(ctx.CASE())) {
            return tuple(ctx.CASE().getSymbol(), (Expression) this.visit(ctx.expression()));
        } else if (asBoolean(ctx.DEFAULT())) {
            return tuple(ctx.DEFAULT().getSymbol(), EmptyExpression.INSTANCE);
        }

        throw createParsingFailedException("Unsupported switch label: " + ctx.getText(), ctx);
    }

    @Override
    public SynchronizedStatement visitSynchronizedStmtAlt(final SynchronizedStmtAltContext ctx) {
        return configureAST(
                new SynchronizedStatement(this.visitExpressionInPar(ctx.expressionInPar()), this.visitBlock(ctx.block())),
                ctx);
    }

    @Override
    public ReturnStatement visitReturnStmtAlt(final ReturnStmtAltContext ctx) {
        if (switchExpressionRuleContextStack.peek() instanceof SwitchExpressionContext) {
            throw createParsingFailedException("switch expression does not support `return`", ctx);
        }

        return configureAST(new ReturnStatement(asBoolean(ctx.expression())
                        ? (Expression) this.visit(ctx.expression())
                        : ConstantExpression.EMPTY_EXPRESSION),
                ctx);
    }

    @Override
    public ThrowStatement visitThrowStmtAlt(final ThrowStmtAltContext ctx) {
        return configureAST(
                new ThrowStatement((Expression) this.visit(ctx.expression())),
                ctx);
    }

    @Override
    public Statement visitLabeledStmtAlt(final LabeledStmtAltContext ctx) {
        Statement statement = (Statement) this.visit(ctx.statement());

        statement.addStatementLabel(this.visitIdentifier(ctx.identifier()));

        return statement;
    }

    @Override
    public BreakStatement visitBreakStatement(final BreakStatementContext ctx) {
        if (visitingLoopStatementCount == 0 && visitingSwitchStatementCount == 0) {
            throw createParsingFailedException("break statement is only allowed inside loops or switches", ctx);
        }

        if (switchExpressionRuleContextStack.peek() instanceof SwitchExpressionContext) {
            throw createParsingFailedException("switch expression does not support `break`", ctx);
        }

        String label = asBoolean(ctx.identifier())
                ? this.visitIdentifier(ctx.identifier())
                : null;

        return configureAST(new BreakStatement(label), ctx);
    }

    @Override
    public ReturnStatement visitYieldStatement(final YieldStatementContext ctx) {
        ReturnStatement returnStatement = (ReturnStatement) returnS((Expression) this.visit(ctx.expression()));
        returnStatement.putNodeMetaData(IS_YIELD_STATEMENT, Boolean.TRUE);
        return configureAST(returnStatement, ctx);
    }

    @Override
    public ReturnStatement visitYieldStmtAlt(final YieldStmtAltContext ctx) {
        return configureAST(this.visitYieldStatement(ctx.yieldStatement()), ctx);
    }

    @Override
    public ContinueStatement visitContinueStatement(final ContinueStatementContext ctx) {
        if (visitingLoopStatementCount == 0) {
            throw createParsingFailedException("continue statement is only allowed inside loops", ctx);
        }

        if (switchExpressionRuleContextStack.peek() instanceof SwitchExpressionContext) {
            throw createParsingFailedException("switch expression does not support `continue`", ctx);
        }

        String label = asBoolean(ctx.identifier())
                ? this.visitIdentifier(ctx.identifier())
                : null;

        return configureAST(new ContinueStatement(label), ctx);

    }

    @Override
    public Expression visitSwitchExprAlt(final SwitchExprAltContext ctx) {
        return configureAST(this.visitSwitchExpression(ctx.switchExpression()), ctx);
    }

    /**
     * <pre>
     * switch(a) {
     *     case 0, 1  ->   'a';
     *     case 2     ->   'b';
     *     default    ->   'z';
     * }
     * </pre>
     * the above code will be transformed to:
     * <pre>
     * {->
     *     switch(a) {
     *         case 0:
     *         case 1:  return 'a';
     *         case 2:  return 'b';
     *         default: return 'z';
     *     }
     * }()
     * </pre>
     *
     * @param ctx the parse tree
     * @return {@link MethodCallExpression} instance
     */
    @Override
    public MethodCallExpression visitSwitchExpression(final SwitchExpressionContext ctx) {
        switchExpressionRuleContextStack.push(ctx);
        try {
            validateSwitchExpressionLabels(ctx);
            List<Tuple3<List<Statement>, Boolean, Boolean>> statementInfoList =
                    ctx.switchBlockStatementExpressionGroup().stream()
                            .map(e -> this.visitSwitchBlockStatementExpressionGroup(e))
                            .collect(Collectors.toList());

            if (statementInfoList.isEmpty()) {
                throw createParsingFailedException("`case` or `default` branches are expected", ctx.LBRACE());
            }

            Boolean isArrow = statementInfoList.get(0).getV2();
            if (!isArrow && statementInfoList.stream().noneMatch(e -> {
                Boolean hasYieldOrThrowStatement = e.getV3();
                return hasYieldOrThrowStatement;
            })) {
                throw createParsingFailedException("`yield` or `throw` is expected", ctx);
            }

            List<Statement> statementList =
                    statementInfoList.stream().map(e -> e.getV1())
                            .reduce(new LinkedList<>(), (r, e) -> {
                                r.addAll(e);
                                return r;
                            });

            List<CaseStatement> caseStatementList = new LinkedList<>();
            List<Statement> defaultStatementList = new LinkedList<>();

            statementList.forEach(e -> {
                if (e instanceof CaseStatement) {
                    caseStatementList.add((CaseStatement) e);
                } else if (isTrue(e, IS_SWITCH_DEFAULT)) {
                    defaultStatementList.add(e);
                }
            });

            int defaultStatementListSize = defaultStatementList.size();
            if (defaultStatementListSize > 1) {
                throw createParsingFailedException("switch expression should have only one default case, which should appear at last", defaultStatementList.get(0));
            }

            if (defaultStatementListSize > 0 && last(statementList) instanceof CaseStatement) {
                throw createParsingFailedException("default case should appear at last", defaultStatementList.get(0));
            }

            String variableName = "__$$sev" + switchExpressionVariableSeq++;
            Statement declarationStatement = declS(localVarX(variableName), this.visitExpressionInPar(ctx.expressionInPar()));
            SwitchStatement switchStatement = configureAST(
                    new SwitchStatement(
                            varX(variableName),
                            caseStatementList,
                            defaultStatementListSize == 0 ? EmptyStatement.INSTANCE : defaultStatementList.get(0)
                    ),
                    ctx);

            MethodCallExpression callClosure = callX(
                    configureAST(
                            closureX(null, createBlockStatement(declarationStatement, switchStatement)),
                            ctx
                    ), CALL_STR);
            callClosure.setImplicitThis(false);

            return configureAST(callClosure, ctx);
        } finally {
            switchExpressionRuleContextStack.pop();
        }
    }
    private int switchExpressionVariableSeq;

    @Override
    public Tuple3<List<Statement>, Boolean, Boolean> visitSwitchBlockStatementExpressionGroup(SwitchBlockStatementExpressionGroupContext ctx) {
        int labelCnt = ctx.switchExpressionLabel().size();
        List<Token> firstLabelHolder = new ArrayList<>(1);
        final int[] arrowCntHolder = new int[1];

        boolean[] isArrowHolder = new boolean[1];
        boolean[] hasResultStmtHolder = new boolean[1];
        List<Statement> result = (List<Statement>) ctx.switchExpressionLabel().stream()
                .map(e -> (Object) this.visitSwitchExpressionLabel(e))
                .reduce(new ArrayList<Statement>(4), (r, e) -> {
                    List<Statement> statementList = (List<Statement>) r;
                    Tuple3<Token, List<Expression>, Integer> tuple = (Tuple3<Token, List<Expression>, Integer>) e;

                    boolean isArrow = ARROW == tuple.getV3();
                    isArrowHolder[0] = isArrow;
                    if (isArrow) {
                        if (++arrowCntHolder[0] > 1 && !firstLabelHolder.isEmpty()) {
                            throw createParsingFailedException("`case ... ->` does not support falling through cases", firstLabelHolder.get(0));
                        }
                    }

                    boolean isLast = labelCnt - 1 == statementList.size();

                    BlockStatement codeBlock = this.visitBlockStatements(ctx.blockStatements());
                    List<Statement> statements = codeBlock.getStatements();
                    int statementsCnt = statements.size();
                    if (0 == statementsCnt) {
                        throw createParsingFailedException("`yield` is expected", ctx.blockStatements());
                    }

                    if (isArrow && statementsCnt > 1) {
                        throw createParsingFailedException("Expect only 1 statement, but " + statementsCnt + " statements found", ctx.blockStatements());
                    }

                    if (!isArrow) {
                        boolean[] hasYieldHolder = new boolean[1];
                        boolean[] hasThrowHolder = new boolean[1];
                        codeBlock.visit(new CodeVisitorSupport() {
                            @Override
                            public void visitReturnStatement(ReturnStatement statement) {
                                if (isTrue(statement, IS_YIELD_STATEMENT)) {
                                    hasYieldHolder[0] = true;
                                    return;
                                }

                                super.visitReturnStatement(statement);
                            }

                            @Override
                            public void visitThrowStatement(ThrowStatement statement) {
                                hasThrowHolder[0] = true;
                            }
                        });

                        if (hasYieldHolder[0] || hasThrowHolder[0]) {
                            hasResultStmtHolder[0] = true;
                        }

                    }

                    Statement exprOrBlockStatement = statements.get(0);
                    if (exprOrBlockStatement instanceof BlockStatement) {
                        BlockStatement blockStatement = (BlockStatement) exprOrBlockStatement;
                        List<Statement> branchStatementList = blockStatement.getStatements();
                        if (1 == branchStatementList.size()) {
                            exprOrBlockStatement = branchStatementList.get(0);
                        }
                    }

                    if (!(exprOrBlockStatement instanceof ReturnStatement || exprOrBlockStatement instanceof ThrowStatement)) {
                        if (isArrow) {
                            MethodCallExpression callClosure = callX(
                                    configureAST(
                                            closureX(null, exprOrBlockStatement),
                                            exprOrBlockStatement
                                    ), CALL_STR);
                            callClosure.setImplicitThis(false);
                            Expression resultExpr = exprOrBlockStatement instanceof ExpressionStatement
                                    ? ((ExpressionStatement) exprOrBlockStatement).getExpression()
                                    : callClosure;

                            codeBlock = configureAST(
                                    createBlockStatement(configureAST(
                                            returnS(resultExpr),
                                            exprOrBlockStatement
                                    )),
                                    exprOrBlockStatement
                            );
                        }
                    }

                    switch (tuple.getV1().getType()) {
                        case CASE:
                            if (!asBoolean(statementList)) {
                                firstLabelHolder.add(tuple.getV1());
                            }
                            for (int i = 0, n = tuple.getV2().size(); i < n; i += 1) {
                                Expression expr = tuple.getV2().get(i);
                                statementList.add(
                                        configureAST(
                                                new CaseStatement(
                                                        expr,

                                                        // check whether processing the last label. if yes, block statement should be attached.
                                                        (isLast && i == n - 1) ? codeBlock
                                                                : EmptyStatement.INSTANCE
                                                ),
                                                firstLabelHolder.get(0)));
                            }
                            break;
                        case DEFAULT:
                            codeBlock.putNodeMetaData(IS_SWITCH_DEFAULT, Boolean.TRUE);
                            statementList.add(codeBlock);
                            break;
                    }

                    return statementList;
                });

        return tuple(result, isArrowHolder[0], hasResultStmtHolder[0]);
    }

    private void validateSwitchExpressionLabels(SwitchExpressionContext ctx) {
        Map<String, List<SwitchExpressionLabelContext>> acMap =
                ctx.switchBlockStatementExpressionGroup().stream()
                        .flatMap(e -> e.switchExpressionLabel().stream())
                        .collect(Collectors.groupingBy(e -> e.ac.getText()));
        if (acMap.size() > 1) {
            List<SwitchExpressionLabelContext> lastSelcList = acMap.values().stream().reduce((prev, next) -> next).orElse(null);
            throw createParsingFailedException(acMap.keySet().stream().collect(Collectors.joining("` and `", "`", "`")) + " cannot be used together", lastSelcList.get(0).ac);
        }
    }

    @Override
    public Tuple3<Token, List<Expression>, Integer> visitSwitchExpressionLabel(SwitchExpressionLabelContext ctx) {
        final Integer acType = ctx.ac.getType();
        if (asBoolean(ctx.CASE())) {
            return tuple(ctx.CASE().getSymbol(), this.visitExpressionList(ctx.expressionList()), acType);
        } else if (asBoolean(ctx.DEFAULT())) {
            return tuple(ctx.DEFAULT().getSymbol(), Collections.singletonList(EmptyExpression.INSTANCE), acType);
        }

        throw createParsingFailedException("Unsupported switch expression label: " + ctx.getText(), ctx);
    }

    // } statement -------------------------------------------------------------

    @Override
    public ClassNode visitTypeDeclaration(final TypeDeclarationContext ctx) {
        if (asBoolean(ctx.classDeclaration())) { // e.g. class A {}
            ctx.classDeclaration().putNodeMetaData(TYPE_DECLARATION_MODIFIERS, this.visitClassOrInterfaceModifiersOpt(ctx.classOrInterfaceModifiersOpt()));
            return configureAST(this.visitClassDeclaration(ctx.classDeclaration()), ctx);
        }

        throw createParsingFailedException("Unsupported type declaration: " + ctx.getText(), ctx);
    }

    @Override
    public ClassNode visitClassDeclaration(final ClassDeclarationContext ctx) {
        String packageName = Optional.ofNullable(this.moduleNode.getPackageName()).orElse("");
        String className = this.visitIdentifier(ctx.identifier());
        if ("var".equals(className)) {
            throw createParsingFailedException("var cannot be used for type declarations", ctx.identifier());
        }

        boolean isAnnotation = asBoolean(ctx.AT());
        if (isAnnotation) {
            if (asBoolean(ctx.typeParameters())) {
                throw createParsingFailedException("annotation declaration cannot have type parameters", ctx.typeParameters());
            }

            if (asBoolean(ctx.EXTENDS())) {
                throw createParsingFailedException("No extends clause allowed for annotation declaration", ctx.EXTENDS());
            }

            if (asBoolean(ctx.IMPLEMENTS())) {
                throw createParsingFailedException("No implements clause allowed for annotation declaration", ctx.IMPLEMENTS());
            }
        }

        boolean isEnum = asBoolean(ctx.ENUM());
        if (isEnum) {
            if (asBoolean(ctx.typeParameters())) {
                throw createParsingFailedException("enum declaration cannot have type parameters", ctx.typeParameters());
            }

            if (asBoolean(ctx.EXTENDS())) {
                throw createParsingFailedException("No extends clause allowed for enum declaration", ctx.EXTENDS());
            }
        }

        boolean isInterface = (asBoolean(ctx.INTERFACE()) && !isAnnotation);
        if (isInterface) {
            if (asBoolean(ctx.IMPLEMENTS())) {
                throw createParsingFailedException("No implements clause allowed for interface declaration", ctx.IMPLEMENTS());
            }
        }

        ModifierManager modifierManager = new ModifierManager(this, ctx.getNodeMetaData(TYPE_DECLARATION_MODIFIERS));

        Optional<ModifierNode> finalModifier = modifierManager.get(FINAL);
        Optional<ModifierNode> sealedModifier = modifierManager.get(SEALED);
        Optional<ModifierNode> nonSealedModifier = modifierManager.get(NON_SEALED);
        boolean isFinal = finalModifier.isPresent();
        boolean isSealed = sealedModifier.isPresent();
        boolean isNonSealed = nonSealedModifier.isPresent();

        boolean isRecord = asBoolean(ctx.RECORD());
        boolean hasRecordHeader = asBoolean(ctx.formalParameters());
        if (isRecord) {
            if (!hasRecordHeader) {
                throw createParsingFailedException("header declaration of record is expected", ctx.identifier());
            }
            if (asBoolean(ctx.EXTENDS())) {
                throw createParsingFailedException("No extends clause allowed for record declaration", ctx.EXTENDS());
            }
            if (isSealed) {
                throw createParsingFailedException("`sealed` is not allowed for record declaration", sealedModifier.get());
            }
            if (isNonSealed) {
                throw createParsingFailedException("`non-sealed` is not allowed for record declaration", nonSealedModifier.get());
            }
        } else {
            if (hasRecordHeader) {
                throw createParsingFailedException("header declaration is only allowed for record declaration", ctx.formalParameters());
            }
        }

        if (isSealed && isNonSealed) {
            throw createParsingFailedException("type cannot be defined with both `sealed` and `non-sealed`", nonSealedModifier.get());
        }

        if (isFinal && (isSealed || isNonSealed)) {
            throw createParsingFailedException("type cannot be defined with both " + (isSealed ? "`sealed`" : "`non-sealed`") + " and `final`", finalModifier.get());
        }

        if ((isAnnotation || isEnum) && (isSealed || isNonSealed)) {
            ModifierNode mn = isSealed ? sealedModifier.get() : nonSealedModifier.get();
            throw createParsingFailedException("modifier `" + mn.getText() + "` is not allowed for " + (isEnum ? "enum" : "annotation definition"), mn);
        }

        boolean hasPermits = asBoolean(ctx.PERMITS());
        if (!isSealed && hasPermits) {
            throw createParsingFailedException("only sealed type declarations should have `permits` clause", ctx);
        }

        int modifiers = modifierManager.getClassModifiersOpValue();

        boolean syntheticPublic = ((modifiers & Opcodes.ACC_SYNTHETIC) != 0);
        modifiers &= ~Opcodes.ACC_SYNTHETIC;

        ClassNode classNode, outerClass = this.classNodeStack.peek();

        if (isEnum) {
            classNode = EnumHelper.makeEnumNode(
                    asBoolean(outerClass) ? className : packageName + className,
                    modifiers,
                    null,
                    outerClass
            );
        } else if (asBoolean(outerClass)) {
            if (outerClass.isInterface()) modifiers |= Opcodes.ACC_STATIC;
            classNode = new InnerClassNode(
                    outerClass,
                    outerClass.getName() + "$" + className,
                    modifiers,
                    ClassHelper.OBJECT_TYPE.getPlainNodeReference()
            );
        } else {
            classNode = new ClassNode(
                    packageName + className,
                    modifiers,
                    ClassHelper.OBJECT_TYPE.getPlainNodeReference()
            );
        }

        configureAST(classNode, ctx);
        classNode.setSyntheticPublic(syntheticPublic);
        classNode.setGenericsTypes(this.visitTypeParameters(ctx.typeParameters()));
        boolean isInterfaceWithDefaultMethods = (isInterface && this.containsDefaultOrPrivateMethods(ctx));

        if (isSealed) {
            AnnotationNode sealedAnnotationNode = makeAnnotationNode(Sealed.class);
            if (asBoolean(ctx.ps)) {
                ListExpression permittedSubclassesListExpression =
                        listX(Arrays.stream(this.visitTypeList(ctx.ps))
                                .map(ClassExpression::new)
                                .collect(Collectors.toList()));
                sealedAnnotationNode.setMember("permittedSubclasses", permittedSubclassesListExpression);
                configureAST(sealedAnnotationNode, ctx.PERMITS());
                sealedAnnotationNode.setNodeMetaData("permits", Boolean.TRUE);
            }
            classNode.addAnnotation(sealedAnnotationNode);
        } else if (isNonSealed) {
            classNode.addAnnotation(makeAnnotationNode(NonSealed.class));
        }
        if (asBoolean(ctx.TRAIT())) {
            classNode.addAnnotation(makeAnnotationNode(Trait.class));
        }
        classNode.addAnnotations(modifierManager.getAnnotations());
        if (isRecord && classNode.getAnnotations().stream().noneMatch(a ->
                        a.getClassNode().getName().equals(RECORD_TYPE_NAME))) {
            classNode.addAnnotation(new AnnotationNode(ClassHelper.makeWithoutCaching(RECORD_TYPE_NAME))); // TODO: makeAnnotationNode(RecordType.class)
        }

        if (isInterfaceWithDefaultMethods) {
            classNode.putNodeMetaData(IS_INTERFACE_WITH_DEFAULT_METHODS, Boolean.TRUE);
        }
        classNode.putNodeMetaData(CLASS_NAME, className);

        if (asBoolean(ctx.CLASS()) || asBoolean(ctx.TRAIT())) {
            if (asBoolean(ctx.scs)) {
                ClassNode[] scs = this.visitTypeList(ctx.scs);
                if (scs.length > 1) {
                    throw createParsingFailedException("Cannot extend multiple classes", ctx.EXTENDS());
                }
                classNode.setSuperClass(scs[0]);
            }
            classNode.setInterfaces(this.visitTypeList(ctx.is));
            this.checkUsingGenerics(classNode);

        } else if (isInterface) {
            classNode.setModifiers(classNode.getModifiers() | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT);
            classNode.setInterfaces(this.visitTypeList(ctx.scs));
            this.checkUsingGenerics(classNode);
            this.hackMixins(classNode);

        } else if (isEnum || isRecord) {
            classNode.setInterfaces(this.visitTypeList(ctx.is));
            this.checkUsingGenerics(classNode);
            if (isRecord) {
                this.transformRecordHeaderToProperties(ctx, classNode);
            }

        } else if (isAnnotation) {
            classNode.setModifiers(classNode.getModifiers() | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_ANNOTATION);
            classNode.addInterface(ClassHelper.Annotation_TYPE);
            this.hackMixins(classNode);

        } else {
            throw createParsingFailedException("Unsupported class declaration: " + ctx.getText(), ctx);
        }

        this.classNodeStack.push(classNode);
        ctx.classBody().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
        this.visitClassBody(ctx.classBody());
        if (isRecord) {
            classNode.getFields().stream().filter(f -> !isTrue(f, IS_RECORD_GENERATED) && !f.isStatic()).findFirst()
                    .ifPresent(fn -> this.createParsingFailedException("Instance field is not allowed in `record`", fn));
        }
        this.classNodeStack.pop();

        // The first element in classNodeList determines what GCL#parseClass for
        // example will return. So we have to ensure it won't be an inner class.
        if (outerClass == null) {
            this.addToClassNodeList(classNode);
        }
        this.groovydocManager.handle(classNode, ctx);

        return classNode;
    }

    private void addToClassNodeList(final ClassNode classNode) {
        this.classNodeList.add(classNode); // GROOVY-11117: outer class first
        classNode.getInnerClasses().forEachRemaining(this::addToClassNodeList);
    }

    private void checkUsingGenerics(final ClassNode classNode) {
        if (!classNode.isUsingGenerics()) {
            if (!classNode.isEnum() && classNode.getSuperClass().isUsingGenerics()) {
                classNode.setUsingGenerics(true);
            } else if (classNode.getInterfaces() != null) {
                for (ClassNode interfaceNode : classNode.getInterfaces()) {
                    if (interfaceNode.isUsingGenerics()) {
                        classNode.setUsingGenerics(true);
                        break;
                    }
                }
            }
        }
    }

    private void transformRecordHeaderToProperties(final ClassDeclarationContext ctx, final ClassNode classNode) {
        Parameter[] parameters = this.visitFormalParameters(ctx.formalParameters());
        classNode.putNodeMetaData(RECORD_HEADER, parameters);

        final int n = parameters.length;
        for (int i = 0; i < n; i += 1) {
            Parameter parameter = parameters[i];
            FormalParameterContext parameterCtx = parameter.getNodeMetaData(PARAMETER_CONTEXT);
            ModifierManager parameterModifierManager = parameter.getNodeMetaData(PARAMETER_MODIFIER_MANAGER);
            PropertyNode propertyNode = declareProperty(parameterCtx, parameterModifierManager, parameter.getType(), classNode, i,
                    parameter, parameter.getName(), parameter.getModifiers() | Opcodes.ACC_FINAL, parameter.getInitialExpression());
            propertyNode.getField().putNodeMetaData(IS_RECORD_GENERATED, Boolean.TRUE);
        }
    }

    private boolean containsDefaultOrPrivateMethods(final ClassDeclarationContext ctx) {
        List<MethodDeclarationContext> methodDeclarationContextList =
                (List<MethodDeclarationContext>) ctx.classBody().classBodyDeclaration().stream()
                        .map(ClassBodyDeclarationContext::memberDeclaration)
                        .filter(Objects::nonNull)
                        .map(e -> (Object) e.methodDeclaration())
                        .filter(Objects::nonNull).reduce(new LinkedList<MethodDeclarationContext>(), (r, e) -> {
                            MethodDeclarationContext methodDeclarationContext = (MethodDeclarationContext) e;
                            if (createModifierManager(methodDeclarationContext).containsAny(DEFAULT, PRIVATE)) {
                                ((List) r).add(methodDeclarationContext);
                            }
                            return r;
                        });

        return !methodDeclarationContextList.isEmpty();
    }

    @Override
    public Void visitClassBody(final ClassBodyContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        if (asBoolean(ctx.enumConstants())) {
            ctx.enumConstants().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitEnumConstants(ctx.enumConstants());
        }

        ctx.classBodyDeclaration().forEach(e -> {
            e.putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitClassBodyDeclaration(e);
        });

        return null;
    }

    @Override
    public List<FieldNode> visitEnumConstants(final EnumConstantsContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        return ctx.enumConstant().stream()
                .map(e -> {
                    e.putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
                    return this.visitEnumConstant(e);
                })
                .collect(Collectors.toList());
    }

    @Override
    public FieldNode visitEnumConstant(final EnumConstantContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        InnerClassNode anonymousInnerClassNode = null;
        if (asBoolean(ctx.anonymousInnerClassDeclaration())) {
            ctx.anonymousInnerClassDeclaration().putNodeMetaData(ANONYMOUS_INNER_CLASS_SUPER_CLASS, classNode);
            anonymousInnerClassNode = this.visitAnonymousInnerClassDeclaration(ctx.anonymousInnerClassDeclaration());
        }

        FieldNode enumConstant =
                EnumHelper.addEnumConstant(
                        classNode,
                        this.visitIdentifier(ctx.identifier()),
                        createEnumConstantInitExpression(ctx.arguments(), anonymousInnerClassNode));

        enumConstant.addAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

        groovydocManager.handle(enumConstant, ctx);

        return configureAST(enumConstant, ctx);
    }

    private Expression createEnumConstantInitExpression(final ArgumentsContext ctx, final InnerClassNode anonymousInnerClassNode) {
        if (!asBoolean(ctx) && !asBoolean(anonymousInnerClassNode)) {
            return null;
        }

        TupleExpression argumentListExpression = (TupleExpression) this.visitArguments(ctx);
        List<Expression> expressions = argumentListExpression.getExpressions();

        if (expressions.size() == 1) {
            Expression expression = expressions.get(0);

            if (expression instanceof NamedArgumentListExpression) { // e.g. SOME_ENUM_CONSTANT(a: "1", b: "2")
                List<MapEntryExpression> mapEntryExpressionList = ((NamedArgumentListExpression) expression).getMapEntryExpressions();
                ListExpression listExpression =
                        new ListExpression(
                                mapEntryExpressionList.stream()
                                        .map(e -> (Expression) e)
                                        .collect(Collectors.toList()));

                if (asBoolean(anonymousInnerClassNode)) {
                    listExpression.addExpression(
                            configureAST(
                                    new ClassExpression(anonymousInnerClassNode),
                                    anonymousInnerClassNode));
                }

                if (mapEntryExpressionList.size() > 1) {
                    listExpression.setWrapped(true);
                }

                return configureAST(listExpression, ctx);
            }

            if (!asBoolean(anonymousInnerClassNode)) {
                if (expression instanceof ListExpression) {
                    ListExpression listExpression = new ListExpression();
                    listExpression.addExpression(expression);

                    return configureAST(listExpression, ctx);
                }

                return expression;
            }

            ListExpression listExpression = new ListExpression();

            if (expression instanceof ListExpression) {
                ((ListExpression) expression).getExpressions().forEach(listExpression::addExpression);
            } else {
                listExpression.addExpression(expression);
            }

            listExpression.addExpression(
                    configureAST(
                            new ClassExpression(anonymousInnerClassNode),
                            anonymousInnerClassNode));

            return configureAST(listExpression, ctx);
        }

        ListExpression listExpression = new ListExpression(expressions);
        if (asBoolean(anonymousInnerClassNode)) {
            listExpression.addExpression(
                    configureAST(
                            new ClassExpression(anonymousInnerClassNode),
                            anonymousInnerClassNode));
        }

        if (asBoolean(ctx)) {
            listExpression.setWrapped(true);
        }

        return asBoolean(ctx)
                ? configureAST(listExpression, ctx)
                : configureAST(listExpression, anonymousInnerClassNode);
    }

    @Override
    public Void visitClassBodyDeclaration(final ClassBodyDeclarationContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        if (asBoolean(ctx.memberDeclaration())) {
            ctx.memberDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitMemberDeclaration(ctx.memberDeclaration());
        } else if (asBoolean(ctx.block())) {
            Statement statement = this.visitBlock(ctx.block());
            if (asBoolean(ctx.STATIC())) { // e.g. static { }
                classNode.addStaticInitializerStatements(Collections.singletonList(statement), false);
            } else { // e.g. { }
                classNode.addObjectInitializerStatements(configureAST(this.createBlockStatement(statement), statement));
            }
        }

        return null;
    }

    @Override
    public Void visitMemberDeclaration(final MemberDeclarationContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        if (asBoolean(ctx.methodDeclaration())) {
            ctx.methodDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitMethodDeclaration(ctx.methodDeclaration());
        } else if (asBoolean(ctx.fieldDeclaration())) {
            ctx.fieldDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitFieldDeclaration(ctx.fieldDeclaration());
        } else if (asBoolean(ctx.compactConstructorDeclaration())) {
            ctx.compactConstructorDeclaration().putNodeMetaData(COMPACT_CONSTRUCTOR_DECLARATION_MODIFIERS, this.visitModifiersOpt(ctx.modifiersOpt()));
            ctx.compactConstructorDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitCompactConstructorDeclaration(ctx.compactConstructorDeclaration());
        } else if (asBoolean(ctx.classDeclaration())) {
            ctx.classDeclaration().putNodeMetaData(TYPE_DECLARATION_MODIFIERS, this.visitModifiersOpt(ctx.modifiersOpt()));
            ctx.classDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
            this.visitClassDeclaration(ctx.classDeclaration());
        }

        return null;
    }

    @Override
    public GenericsType[] visitTypeParameters(final TypeParametersContext ctx) {
        if (!asBoolean(ctx)) {
            return null;
        }

        return ctx.typeParameter().stream()
                .map(this::visitTypeParameter)
                .toArray(GenericsType[]::new);
    }

    @Override
    public GenericsType visitTypeParameter(final TypeParameterContext ctx) {
        ClassNode baseType = configureAST(ClassHelper.make(this.visitClassName(ctx.className())), ctx);
        baseType.addTypeAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));
        GenericsType genericsType = new GenericsType(baseType, this.visitTypeBound(ctx.typeBound()), null);
        return configureAST(genericsType, ctx);
    }

    @Override
    public ClassNode[] visitTypeBound(final TypeBoundContext ctx) {
        if (!asBoolean(ctx)) {
            return null;
        }

        return ctx.type().stream()
                .map(this::visitType)
                .toArray(ClassNode[]::new);
    }

    @Override
    public Void visitFieldDeclaration(final FieldDeclarationContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        Objects.requireNonNull(classNode, "classNode should not be null");

        ctx.variableDeclaration().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, classNode);
        this.visitVariableDeclaration(ctx.variableDeclaration());

        return null;
    }

    private ConstructorCallExpression checkThisAndSuperConstructorCall(final Statement statement) {
        if (!(statement instanceof BlockStatement)) { // method code must be a BlockStatement
            return null;
        }

        BlockStatement blockStatement = (BlockStatement) statement;
        List<Statement> statementList = blockStatement.getStatements();

        for (int i = 0, n = statementList.size(); i < n; i += 1) {
            Statement s = statementList.get(i);
            if (s instanceof ExpressionStatement) {
                Expression expression = ((ExpressionStatement) s).getExpression();
                if ((expression instanceof ConstructorCallExpression) && 0 != i) {
                    return (ConstructorCallExpression) expression;
                }
            }
        }

        return null;
    }

    private ModifierManager createModifierManager(final MethodDeclarationContext ctx) {
        List<ModifierNode> modifierNodeList = Collections.emptyList();

        if (asBoolean(ctx.modifiersOpt())) {
            modifierNodeList = this.visitModifiersOpt(ctx.modifiersOpt());
        }

        return new ModifierManager(this, modifierNodeList);
    }

    private void validateParametersOfMethodDeclaration(final Parameter[] parameters, final ClassNode classNode) {
        if (!classNode.isInterface()) {
            return;
        }

        for (Parameter parameter : parameters) {
            if (parameter.hasInitialExpression()) {
                throw createParsingFailedException("Cannot specify default value for method parameter '" + parameter.getName() + " = " + parameter.getInitialExpression().getText() + "' inside an interface", parameter);
            }
        }
    }

    @Override
    public MethodNode visitCompactConstructorDeclaration(final CompactConstructorDeclarationContext ctx) {
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);

        if (classNode.getAnnotations().stream().noneMatch(a -> a.getClassNode().getName().equals(RECORD_TYPE_NAME))) {
            createParsingFailedException("Only record can have compact constructor", ctx);
        }

        if (new ModifierManager(this, ctx.getNodeMetaData(COMPACT_CONSTRUCTOR_DECLARATION_MODIFIERS)).containsAny(VAR)) {
            throw createParsingFailedException("var cannot be used for compact constructor declaration", ctx);
        }

        String methodName = this.visitMethodName(ctx.methodName());
        String className = classNode.getNodeMetaData(CLASS_NAME);
        if (!methodName.equals(className)) {
            createParsingFailedException("Compact constructor should have the same name as record: " + className, ctx.methodName());
        }

        Parameter[] header = classNode.getNodeMetaData(RECORD_HEADER);
        Statement code = this.visitMethodBody(ctx.methodBody());
        code.visit(new CodeVisitorSupport() {
            @Override
            public void visitPropertyExpression(final PropertyExpression expression) {
                String receiverText = expression.getObjectExpression().getText();
                String propertyName = expression.getPropertyAsString();
                if (THIS_STR.equals(receiverText) && Arrays.stream(header).anyMatch(p -> p.getName().equals(propertyName))) {
                    createParsingFailedException("Cannot assign a value to final variable '" + propertyName + "'", expression.getProperty());
                }
                super.visitPropertyExpression(expression);
            }
        });

        List<AnnotationNode> annos = classNode.getAnnotations(ClassHelper.make(TupleConstructor.class));
        AnnotationNode tupleConstructor = annos.isEmpty() ? makeAnnotationNode(TupleConstructor.class) : annos.get(0);
        tupleConstructor.setMember("pre", closureX(code));
        if (annos.isEmpty()) {
            classNode.addAnnotation(tupleConstructor);
        }

        return null;
    }

    @Override
    public MethodNode visitMethodDeclaration(final MethodDeclarationContext ctx) {
        ModifierManager modifierManager = createModifierManager(ctx);

        if (modifierManager.containsAny(VAR)) {
            throw createParsingFailedException("var cannot be used for method declarations", ctx);
        }

        String methodName = this.visitMethodName(ctx.methodName());
        ClassNode returnType = this.visitReturnType(ctx.returnType());
        Parameter[] parameters = this.visitFormalParameters(ctx.formalParameters());
        ClassNode[] exceptions = this.visitQualifiedClassNameList(ctx.qualifiedClassNameList());

        anonymousInnerClassesDefinedInMethodStack.push(new LinkedList<>());
        Statement code = this.visitMethodBody(ctx.methodBody());
        List<InnerClassNode> anonymousInnerClassList = anonymousInnerClassesDefinedInMethodStack.pop();

        MethodNode methodNode;
        // if classNode is not null, the method declaration is for class declaration
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);
        if (asBoolean(classNode)) {
            validateParametersOfMethodDeclaration(parameters, classNode);

            methodNode = createConstructorOrMethodNodeForClass(ctx, modifierManager, methodName, returnType, parameters, exceptions, code, classNode);
        } else { // script method declaration
            methodNode = createScriptMethodNode(modifierManager, methodName, returnType, parameters, exceptions, code);
        }
        anonymousInnerClassList.forEach(e -> e.setEnclosingMethod(methodNode));

        methodNode.setGenericsTypes(this.visitTypeParameters(ctx.typeParameters()));
        methodNode.setSyntheticPublic(
                this.isSyntheticPublic(
                        this.isAnnotationDeclaration(classNode),
                        classNode instanceof EnumConstantClassNode,
                        asBoolean(ctx.returnType()),
                        modifierManager));

        if (modifierManager.containsAny(STATIC)) {
            for (Parameter parameter : methodNode.getParameters()) {
                parameter.setInStaticContext(true);
            }

            methodNode.getVariableScope().setInStaticContext(true);
        }

        configureAST(methodNode, ctx);

        validateMethodDeclaration(ctx, methodNode, modifierManager, classNode);

        groovydocManager.handle(methodNode, ctx);

        return methodNode;
    }

    private void validateMethodDeclaration(final MethodDeclarationContext ctx, final MethodNode methodNode, final ModifierManager modifierManager, final ClassNode classNode) {
        if (1 == ctx.t || 2 == ctx.t || 3 == ctx.t) { // 1: normal method declaration; 2: abstract method declaration; 3: normal method declaration OR abstract method declaration
            if (!(asBoolean(ctx.modifiersOpt().modifiers()) || asBoolean(ctx.returnType()))) {
                throw createParsingFailedException("Modifiers or return type is required", ctx);
            }
        }

        if (1 == ctx.t) {
            if (!asBoolean(ctx.methodBody())) {
                throw createParsingFailedException("Method body is required", ctx);
            }
        }

        if (2 == ctx.t) {
            if (asBoolean(ctx.methodBody())) {
                throw createParsingFailedException("Abstract method should not have method body", ctx);
            }
        }

        boolean isAbstractMethod = methodNode.isAbstract();
        boolean hasMethodBody =
                asBoolean(methodNode.getCode())
                        && !(methodNode.getCode() instanceof ExpressionStatement);

        if (9 == ctx.ct) { // script
            if (isAbstractMethod || !hasMethodBody) { // method should not be declared abstract in the script
                throw createParsingFailedException("You cannot define " + (isAbstractMethod ? "an abstract" : "a") + " method[" + methodNode.getName() + "] " + (!hasMethodBody ? "without method body " : "") + "in the script. Try " + (isAbstractMethod ? "removing the 'abstract'" : "") + (isAbstractMethod && !hasMethodBody ? " and" : "") + (!hasMethodBody ? " adding a method body" : ""), methodNode);
            }
        } else {
            if (4 == ctx.ct) { // trait
                if (isAbstractMethod && hasMethodBody) {
                    throw createParsingFailedException("Abstract method should not have method body", ctx);
                }
            }

            if (3 == ctx.ct) { // annotation
                if (hasMethodBody) {
                    throw createParsingFailedException("Annotation type element should not have body", ctx);
                }
            }

            if (!isAbstractMethod && !hasMethodBody) { // non-abstract method without body in the non-script(e.g. class, enum, trait) is not allowed!
                throw createParsingFailedException("You defined a method[" + methodNode.getName() + "] without a body. Try adding a method body, or declare it abstract", methodNode);
            }

            boolean isInterfaceOrAbstractClass = asBoolean(classNode) && classNode.isAbstract() && !classNode.isAnnotationDefinition();
            if (isInterfaceOrAbstractClass && !modifierManager.containsAny(DEFAULT, PRIVATE) && isAbstractMethod && hasMethodBody) {
                throw createParsingFailedException("You defined an abstract method[" + methodNode.getName() + "] with a body. Try removing the method body" + (classNode.isInterface() ? ", or declare it default or private" : ""), methodNode);
            }
        }

        modifierManager.validate(methodNode);

        if (methodNode instanceof ConstructorNode) {
            modifierManager.validate((ConstructorNode) methodNode);
        }
    }

    private MethodNode createScriptMethodNode(final ModifierManager modifierManager, final String methodName, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        MethodNode methodNode = new MethodNode(
                methodName,
                modifierManager.containsAny(PRIVATE) ? Opcodes.ACC_PRIVATE : Opcodes.ACC_PUBLIC,
                returnType,
                parameters,
                exceptions,
                code
        );
        modifierManager.processMethodNode(methodNode);
        return methodNode;
    }

    private MethodNode createConstructorOrMethodNodeForClass(final MethodDeclarationContext ctx, final ModifierManager modifierManager, final String methodName, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code, final ClassNode classNode) {
        MethodNode methodNode;
        String className = classNode.getNodeMetaData(CLASS_NAME);
        int modifiers = modifierManager.getClassMemberModifiersOpValue();

        boolean hasReturnType = asBoolean(ctx.returnType());
        boolean hasMethodBody = asBoolean(ctx.methodBody());

        if (!hasReturnType && hasMethodBody && methodName.equals(className)) {
            methodNode = createConstructorNodeForClass(methodName, parameters, exceptions, code, classNode, modifiers);
        } else {
            if (!hasReturnType && hasMethodBody && (0 == modifierManager.getModifierCount())) {
                throw createParsingFailedException("Invalid method declaration: " + methodName, ctx);
            }
            methodNode = createMethodNodeForClass(ctx, modifierManager, methodName, returnType, parameters, exceptions, code, classNode, modifiers);
        }

        modifierManager.attachAnnotations(methodNode);
        return methodNode;
    }

    private MethodNode createMethodNodeForClass(final MethodDeclarationContext ctx, final ModifierManager modifierManager, final String methodName, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, Statement code, final ClassNode classNode, int modifiers) {
        if (asBoolean(ctx.elementValue())) { // the code of annotation method
            code = configureAST(
                    new ExpressionStatement(
                            this.visitElementValue(ctx.elementValue())),
                    ctx.elementValue());

        }

        modifiers |= !modifierManager.containsAny(STATIC) && classNode.isInterface() && !(isTrue(classNode, IS_INTERFACE_WITH_DEFAULT_METHODS) && modifierManager.containsAny(DEFAULT, PRIVATE)) ? Opcodes.ACC_ABSTRACT : 0;
        MethodNode methodNode = new MethodNode(methodName, modifiers, returnType, parameters, exceptions, code);
        classNode.addMethod(methodNode);

        methodNode.setAnnotationDefault(asBoolean(ctx.elementValue()));
        return methodNode;
    }

    private ConstructorNode createConstructorNodeForClass(final String methodName, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code, final ClassNode classNode, final int modifiers) {
        ConstructorCallExpression thisOrSuperConstructorCallExpression = this.checkThisAndSuperConstructorCall(code);
        if (asBoolean(thisOrSuperConstructorCallExpression)) {
            throw createParsingFailedException(thisOrSuperConstructorCallExpression.getText() + " should be the first statement in the constructor[" + methodName + "]", thisOrSuperConstructorCallExpression);
        }

        return classNode.addConstructor(
                modifiers,
                parameters,
                exceptions,
                code);
    }

    @Override
    public String visitMethodName(final MethodNameContext ctx) {
        if (asBoolean(ctx.identifier())) {
            return this.visitIdentifier(ctx.identifier());
        }

        if (asBoolean(ctx.stringLiteral())) {
            return this.visitStringLiteral(ctx.stringLiteral()).getText();
        }

        throw createParsingFailedException("Unsupported method name: " + ctx.getText(), ctx);
    }

    @Override
    public ClassNode visitReturnType(final ReturnTypeContext ctx) {
        if (!asBoolean(ctx)) {
            return ClassHelper.dynamicType();
        }

        if (asBoolean(ctx.type())) {
            return this.visitType(ctx.type());
        }

        if (asBoolean(ctx.VOID())) {
            if (ctx.ct == 3) { // annotation
                throw createParsingFailedException("annotation method cannot have void return type", ctx);
            }

            return configureAST(ClassHelper.VOID_TYPE.getPlainNodeReference(false), ctx.VOID());
        }

        throw createParsingFailedException("Unsupported return type: " + ctx.getText(), ctx);
    }

    @Override
    public Statement visitMethodBody(final MethodBodyContext ctx) {
        if (!asBoolean(ctx)) {
            return null;
        }

        return configureAST(this.visitBlock(ctx.block()), ctx);
    }

    @Override
    public DeclarationListStatement visitLocalVariableDeclaration(final LocalVariableDeclarationContext ctx) {
        return configureAST(this.visitVariableDeclaration(ctx.variableDeclaration()), ctx);
    }

    private DeclarationListStatement createMultiAssignmentDeclarationListStatement(final VariableDeclarationContext ctx, final ModifierManager modifierManager) {
        List<Expression> elist = this.visitTypeNamePairs(ctx.typeNamePairs());
        for (Expression e : elist)
            modifierManager.processVariableExpression((VariableExpression) e);

        DeclarationExpression de = new DeclarationExpression(
                configureAST(new TupleExpression(elist), ctx.typeNamePairs()),
                createGroovyTokenByType(ctx.ASSIGN().getSymbol(), Types.ASSIGN),
                visitVariableInitializer(ctx.variableInitializer())           );

        configureAST(modifierManager.attachAnnotations(de), ctx);
        return configureAST(new DeclarationListStatement(de), ctx);
    }

    @Override
    public DeclarationListStatement visitVariableDeclaration(final VariableDeclarationContext ctx) {
        ModifierManager modifierManager =
                new ModifierManager(
                        this,
                        asBoolean(ctx.modifiers()) ? this.visitModifiers(ctx.modifiers()) : Collections.emptyList()
                );

        if (asBoolean(ctx.typeNamePairs())) { // e.g. def (int a, int b) = [1, 2]
            return this.createMultiAssignmentDeclarationListStatement(ctx, modifierManager);
        }

        ClassNode variableType = this.visitType(ctx.type());
        ctx.variableDeclarators().putNodeMetaData(VARIABLE_DECLARATION_VARIABLE_TYPE, variableType);
        List<DeclarationExpression> declarationExpressionList = this.visitVariableDeclarators(ctx.variableDeclarators());

        // if classNode is not null, the variable declaration is for class declaration. In other words, it is a field declaration
        ClassNode classNode = ctx.getNodeMetaData(CLASS_DECLARATION_CLASS_NODE);

        if (asBoolean(classNode)) {
            return createFieldDeclarationListStatement(ctx, modifierManager, variableType, declarationExpressionList, classNode);
        }

        int size = declarationExpressionList.size();
        if (size > 0) {
            for (DeclarationExpression e : declarationExpressionList) {
                modifierManager.processVariableExpression(e.getVariableExpression());
                modifierManager.attachAnnotations(e);
            }

            DeclarationExpression declarationExpression = declarationExpressionList.get(0);
            if (size == 1) {
                configureAST(declarationExpression, ctx);
            } else { // adjust start of first declaration
                declarationExpression.setLineNumber(ctx.getStart().getLine());
                declarationExpression.setColumnNumber(ctx.getStart().getCharPositionInLine() + 1);
            }
        }

        return configureAST(new DeclarationListStatement(declarationExpressionList), ctx);
    }

    private DeclarationListStatement createFieldDeclarationListStatement(final VariableDeclarationContext ctx, final ModifierManager modifierManager, final ClassNode variableType, final List<DeclarationExpression> declarationExpressionList, final ClassNode classNode) {
        for (int i = 0, n = declarationExpressionList.size(); i < n; i += 1) {
            DeclarationExpression declarationExpression = declarationExpressionList.get(i);
            VariableExpression variableExpression = (VariableExpression) declarationExpression.getLeftExpression();

            String fieldName = variableExpression.getName();

            int modifiers = modifierManager.getClassMemberModifiersOpValue();

            Expression initialValue = declarationExpression.getRightExpression() instanceof EmptyExpression ? null : declarationExpression.getRightExpression();
            Object defaultValue = findDefaultValueByType(variableType);

            if (classNode.isInterface()) {
                if (!asBoolean(initialValue)) {
                    initialValue = !asBoolean(defaultValue) ? null : new ConstantExpression(defaultValue, true);
                }

                modifiers |= Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
            }

            if (isFieldDeclaration(modifierManager, classNode)) {
                declareField(ctx, modifierManager, variableType, classNode, i, variableExpression, fieldName, modifiers, initialValue);
            } else {
                declareProperty(ctx, modifierManager, variableType, classNode, i, variableExpression, fieldName, modifiers, initialValue);
            }
        }

        return null;
    }

    private static class PropertyExpander extends Verifier {
        private PropertyExpander(final ClassNode cNode) {
            setClassNode(cNode);
        }

        @Override
        protected Statement createSetterBlock(final PropertyNode propertyNode, final FieldNode field) {
            return stmt(assignX(varX(field), varX(VALUE_STR, field.getType())));
        }

        @Override
        protected Statement createGetterBlock(final PropertyNode propertyNode, final FieldNode field) {
            return stmt(varX(field));
        }
    }

    private PropertyNode declareProperty(final GroovyParserRuleContext ctx, final ModifierManager modifierManager, final ClassNode variableType, final ClassNode classNode, final int i, final ASTNode startNode, final String fieldName, final int modifiers, final Expression initialValue) {
        PropertyNode propertyNode;
        FieldNode fieldNode = classNode.getDeclaredField(fieldName);

        if (fieldNode != null && !classNode.hasProperty(fieldName)) {
            if (fieldNode.hasInitialExpression() && initialValue != null) {
                throw createParsingFailedException("The split property definition named '" + fieldName + "' must not have an initial value for both the field and the property", ctx);
            }
            if (!fieldNode.getType().equals(variableType)) {
                throw createParsingFailedException("The split property definition named '" + fieldName + "' must not have different types for the field and the property", ctx);
            }
            classNode.getFields().remove(fieldNode);

            propertyNode = new PropertyNode(fieldNode, modifiers | Opcodes.ACC_PUBLIC, null, null);
            classNode.addProperty(propertyNode);
            if (initialValue != null) {
                fieldNode.setInitialValueExpression(initialValue);
            }
            modifierManager.attachAnnotations(propertyNode);
            propertyNode.addAnnotation(makeAnnotationNode(CompileStatic.class));
            // expand properties early so AST transforms will be handled correctly
            PropertyExpander expander = new PropertyExpander(classNode);
            expander.visitProperty(propertyNode);
        } else {
            propertyNode = new PropertyNode(fieldName, modifiers | Opcodes.ACC_PUBLIC, variableType, classNode, initialValue, null, null);
            classNode.addProperty(propertyNode);

            fieldNode = propertyNode.getField();
            fieldNode.setModifiers(modifiers & ~Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE);
            fieldNode.setSynthetic(!classNode.isInterface());
            modifierManager.attachAnnotations(fieldNode);
            modifierManager.attachAnnotations(propertyNode);
            if (i == 0) {
                configureAST(fieldNode, ctx, initialValue);
            } else {
                configureAST(fieldNode, startNode, initialValue);
            }
        }

        groovydocManager.handle(fieldNode, ctx);
        groovydocManager.handle(propertyNode, ctx);

        if (i == 0) {
            configureAST(propertyNode, ctx, initialValue);
        } else {
            configureAST(propertyNode, startNode, initialValue);
        }
        return propertyNode;
    }

    private void declareField(final VariableDeclarationContext ctx, final ModifierManager modifierManager, final ClassNode variableType, final ClassNode classNode, final int i, final VariableExpression variableExpression, final String fieldName, final int modifiers, final Expression initialValue) {
        FieldNode fieldNode;
        PropertyNode propertyNode = classNode.getProperty(fieldName);

        if (propertyNode != null && propertyNode.getField().isSynthetic()) {
            if (propertyNode.hasInitialExpression() && initialValue != null) {
                throw createParsingFailedException("The split property definition named '" + fieldName + "' must not have an initial value for both the field and the property", ctx);
            }
            if (!propertyNode.getType().equals(variableType)) {
                throw createParsingFailedException("The split property definition named '" + fieldName + "' must not have different types for the field and the property", ctx);
            }
            classNode.getFields().remove(propertyNode.getField());
            fieldNode = new FieldNode(fieldName, modifiers, variableType, classNode.redirect(), propertyNode.hasInitialExpression() ? propertyNode.getInitialExpression() : initialValue);
            propertyNode.setField(fieldNode);
            propertyNode.addAnnotation(makeAnnotationNode(CompileStatic.class));
            classNode.addField(fieldNode);
            // expand properties early so AST transforms will be handled correctly
            PropertyExpander expander = new PropertyExpander(classNode);
            expander.visitProperty(propertyNode);
        } else {
            fieldNode =
                    classNode.addField(
                            fieldName,
                            modifiers,
                            variableType,
                            initialValue);
        }

        modifierManager.attachAnnotations(fieldNode);
        groovydocManager.handle(fieldNode, ctx);

        if (i == 0) {
            configureAST(fieldNode, ctx, initialValue);
        } else {
            configureAST(fieldNode, variableExpression, initialValue);
        }
    }

    private boolean isFieldDeclaration(final ModifierManager modifierManager, final ClassNode classNode) {
        return classNode.isInterface() || modifierManager.containsVisibilityModifier();
    }

    @Override
    public List<Expression> visitTypeNamePairs(final TypeNamePairsContext ctx) {
        return ctx.typeNamePair().stream().map(this::visitTypeNamePair).collect(Collectors.toList());
    }

    @Override
    public VariableExpression visitTypeNamePair(final TypeNamePairContext ctx) {
        return configureAST(
                new VariableExpression(
                        this.visitVariableDeclaratorId(ctx.variableDeclaratorId()).getName(),
                        this.visitType(ctx.type())),
                ctx);
    }

    @Override
    public List<DeclarationExpression> visitVariableDeclarators(final VariableDeclaratorsContext ctx) {
        ClassNode variableType = ctx.getNodeMetaData(VARIABLE_DECLARATION_VARIABLE_TYPE);
        Objects.requireNonNull(variableType, "variableType should not be null");

        return ctx.variableDeclarator().stream()
                .map(e -> {
                    e.putNodeMetaData(VARIABLE_DECLARATION_VARIABLE_TYPE, variableType);
                    return this.visitVariableDeclarator(e);
                })
                .collect(Collectors.toList());
    }

    @Override
    public DeclarationExpression visitVariableDeclarator(final VariableDeclaratorContext ctx) {
        ClassNode variableType = ctx.getNodeMetaData(VARIABLE_DECLARATION_VARIABLE_TYPE);
        Objects.requireNonNull(variableType, "variableType should not be null");

        org.codehaus.groovy.syntax.Token token;
        if (asBoolean(ctx.ASSIGN())) {
            token = createGroovyTokenByType(ctx.ASSIGN().getSymbol(), Types.ASSIGN);
        } else {
            token = new org.codehaus.groovy.syntax.Token(Types.ASSIGN, ASSIGN_STR, ctx.start.getLine(), 1);
        }

        return configureAST(
                new DeclarationExpression(
                        configureAST(
                                new VariableExpression(
                                        this.visitVariableDeclaratorId(ctx.variableDeclaratorId()).getName(),
                                        variableType
                                ),
                                ctx.variableDeclaratorId()),
                        token,
                        this.visitVariableInitializer(ctx.variableInitializer())),
                ctx);
    }

    @Override
    public Expression visitVariableInitializer(final VariableInitializerContext ctx) {
        if (!asBoolean(ctx)) {
            return EmptyExpression.INSTANCE;
        }

        return configureAST(
                this.visitEnhancedStatementExpression(ctx.enhancedStatementExpression()),
                ctx);
    }

    @Override
    public List<Expression> visitVariableInitializers(final VariableInitializersContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return ctx.variableInitializer().stream()
                        .map(this::visitVariableInitializer)
                        .collect(Collectors.toList());
    }

    @Override
    public List<Expression> visitArrayInitializer(final ArrayInitializerContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        try {
            visitingArrayInitializerCount += 1;
            return this.visitVariableInitializers(ctx.variableInitializers());
        } finally {
            visitingArrayInitializerCount -= 1;
        }
    }

    @Override
    public Statement visitBlock(final BlockContext ctx) {
        if (!asBoolean(ctx)) {
            return this.createBlockStatement();
        }

        return configureAST(
                this.visitBlockStatementsOpt(ctx.blockStatementsOpt()),
                ctx);
    }

    @Override
    public ExpressionStatement visitCommandExprAlt(final CommandExprAltContext ctx) {
        return configureAST(new ExpressionStatement(this.visitCommandExpression(ctx.commandExpression())), ctx);
    }

    @Override
    public Expression visitCommandExpression(final CommandExpressionContext ctx) {
        boolean hasArgumentList = asBoolean(ctx.enhancedArgumentListInPar());
        boolean hasCommandArgument = asBoolean(ctx.commandArgument());

        if ((hasArgumentList || hasCommandArgument) && visitingArrayInitializerCount > 0) {
            // To avoid ambiguities, command chain expression should not be used in array initializer
            // the old parser does not support either, so no breaking changes
            // SEE http://groovy.329449.n5.nabble.com/parrot-Command-expressions-in-array-initializer-tt5752273.html
            throw createParsingFailedException("Command chain expression can not be used in array initializer", ctx);
        }

        Expression baseExpr = (Expression) this.visit(ctx.expression());

        if ((hasArgumentList || hasCommandArgument) && !isInsideParentheses(baseExpr)
                && baseExpr instanceof BinaryExpression && !"[".equals(((BinaryExpression) baseExpr).getOperation().getText())) {
            throw createParsingFailedException("Unexpected input: '" + getOriginalText(ctx.expression()) + "'", ctx.expression());
        }

        MethodCallExpression methodCallExpression = null;

        if (hasArgumentList) {
            Expression arguments = this.visitEnhancedArgumentListInPar(ctx.enhancedArgumentListInPar());

            if (baseExpr instanceof PropertyExpression) { // e.g. obj.a 1, 2
                methodCallExpression = configureAST(this.createMethodCallExpression((PropertyExpression) baseExpr, arguments), ctx.expression(), arguments);

            } else if (baseExpr instanceof MethodCallExpression && !isInsideParentheses(baseExpr)) { // e.g. m {} a, b  OR  m(...) a, b
                if (asBoolean(arguments)) {
                    // The error should never be thrown.
                    throw new GroovyBugError("When baseExpr is a instance of MethodCallExpression, which should follow NO argumentList");
                }
                methodCallExpression = (MethodCallExpression) baseExpr;

            } else if (!isInsideParentheses(baseExpr)
                    && (baseExpr instanceof VariableExpression // e.g. m 1, 2
                        || baseExpr instanceof GStringExpression // e.g. "$m" 1, 2
                        || (baseExpr instanceof ConstantExpression && isTrue(baseExpr, IS_STRING)))) { // e.g. "m" 1, 2
                validateInvalidMethodDefinition(baseExpr, arguments);

                methodCallExpression = configureAST(this.createMethodCallExpression(baseExpr, arguments), ctx.expression(), arguments);
            } else { // e.g. a[x] b, new A() b, etc.
                methodCallExpression = configureAST(this.createCallMethodCallExpression(baseExpr, arguments), ctx.expression(), arguments);
            }

            methodCallExpression.putNodeMetaData(IS_COMMAND_EXPRESSION, Boolean.TRUE);

            if (!hasCommandArgument) {
                return methodCallExpression;
            }
        }

        if (hasCommandArgument) {
            baseExpr.putNodeMetaData(IS_COMMAND_EXPRESSION, Boolean.TRUE);
        }

        return configureAST(
                (Expression) ctx.commandArgument().stream()
                        .map(e -> (Object) e)
                        .reduce(methodCallExpression != null ? methodCallExpression : baseExpr,
                                (r, e) -> {
                                    CommandArgumentContext commandArgumentContext = (CommandArgumentContext) e;
                                    commandArgumentContext.putNodeMetaData(CMD_EXPRESSION_BASE_EXPR, r);
                                    return this.visitCommandArgument(commandArgumentContext);
                                }
                        ),
                ctx);
    }

    /* Validate the following invalid cases:
     *  1) void m() {}
     *  2) String m() {}
     *  Note: if the text of `VariableExpression` does not start with upper case character, e.g. task m() {}
     *        ,it may be a command expression
     */
    private void validateInvalidMethodDefinition(final Expression baseExpr, final Expression arguments) {
        if (baseExpr instanceof VariableExpression) {
            if (isBuiltInType(baseExpr) || Character.isUpperCase(baseExpr.getText().codePointAt(0))) {
                if (arguments instanceof ArgumentListExpression) {
                    List<Expression> expressionList = ((ArgumentListExpression) arguments).getExpressions();
                    if (1 == expressionList.size()) {
                        final Expression expression = expressionList.get(0);
                        if (expression instanceof MethodCallExpression) {
                            MethodCallExpression mce = (MethodCallExpression) expression;
                            final Expression methodCallArguments = mce.getArguments();

                            // check the method call tails with a closure
                            if (methodCallArguments instanceof ArgumentListExpression) {
                                List<Expression> methodCallArgumentExpressionList = ((ArgumentListExpression) methodCallArguments).getExpressions();
                                final int argumentCnt = methodCallArgumentExpressionList.size();
                                if (argumentCnt > 0) {
                                    final Expression lastArgumentExpression = methodCallArgumentExpressionList.get(argumentCnt - 1);
                                    if (lastArgumentExpression instanceof ClosureExpression) {
                                        if (ClosureUtils.hasImplicitParameter(((ClosureExpression) lastArgumentExpression))) {
                                            throw createParsingFailedException(
                                                    "Method definition not expected here",
                                                    tuple(baseExpr.getLineNumber(), baseExpr.getColumnNumber()),
                                                    tuple(expression.getLastLineNumber(), expression.getLastColumnNumber())
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Expression visitCommandArgument(final CommandArgumentContext ctx) {
        // e.g. x y a b     we call "x y" as the base expression
        Expression baseExpr = ctx.getNodeMetaData(CMD_EXPRESSION_BASE_EXPR);

        Expression primaryExpr = (Expression) this.visit(ctx.primary());

        if (asBoolean(ctx.enhancedArgumentListInPar())) { // e.g. x y a b
            if (baseExpr instanceof PropertyExpression) { // the branch should never reach, because a.b.c will be parsed as a path expression, not a method call
                throw createParsingFailedException("Unsupported command argument: " + ctx.getText(), ctx);
            }

            // the following code will process "a b" of "x y a b"
            MethodCallExpression methodCallExpression =
                    new MethodCallExpression(
                            baseExpr,
                            this.createConstantExpression(primaryExpr),
                            this.visitEnhancedArgumentListInPar(ctx.enhancedArgumentListInPar())
                    );
            methodCallExpression.setImplicitThis(false);

            return configureAST(methodCallExpression, ctx);
        } else if (asBoolean(ctx.pathElement())) { // e.g. x y a.b
            Expression pathExpression =
                    this.createPathExpression(
                            configureAST(
                                    new PropertyExpression(baseExpr, this.createConstantExpression(primaryExpr)),
                                    primaryExpr
                            ),
                            ctx.pathElement()
                    );

            return configureAST(pathExpression, ctx);
        }

        // e.g. x y a
        return configureAST(
                new PropertyExpression(
                        baseExpr,
                        primaryExpr instanceof VariableExpression
                                ? this.createConstantExpression(primaryExpr)
                                : primaryExpr
                ),
                primaryExpr
        );
    }

    // expression { ------------------------------------------------------------

    @Override
    public ClassNode visitCastParExpression(final CastParExpressionContext ctx) {
        return this.visitType(ctx.type());
    }

    @Override
    public Expression visitParExpression(final ParExpressionContext ctx) {
        Expression expression = this.visitExpressionInPar(ctx.expressionInPar());

        expression.getNodeMetaData(INSIDE_PARENTHESES_LEVEL,
                k -> new java.util.concurrent.atomic.AtomicInteger()).getAndAdd(1);

        return configureAST(expression, ctx);
    }

    @Override
    public Expression visitExpressionInPar(final ExpressionInParContext ctx) {
        return this.visitEnhancedStatementExpression(ctx.enhancedStatementExpression());
    }

    @Override
    public Expression visitEnhancedExpression(final EnhancedExpressionContext ctx) {
        Expression expression;

        if (asBoolean(ctx.expression())) {
            expression = (Expression) this.visit(ctx.expression());
        } else if (asBoolean(ctx.standardLambdaExpression())) {
            expression = this.visitStandardLambdaExpression(ctx.standardLambdaExpression());
        } else {
            throw createParsingFailedException("Unsupported enhanced expression: " + ctx.getText(), ctx);
        }

        return configureAST(expression, ctx);
    }

    @Override
    public Expression visitEnhancedStatementExpression(final EnhancedStatementExpressionContext ctx) {
        Expression expression;

        if (asBoolean(ctx.statementExpression())) {
            expression = ((ExpressionStatement) this.visit(ctx.statementExpression())).getExpression();
        } else if (asBoolean(ctx.standardLambdaExpression())) {
            expression = this.visitStandardLambdaExpression(ctx.standardLambdaExpression());
        } else {
            throw createParsingFailedException("Unsupported enhanced statement expression: " + ctx.getText(), ctx);
        }

        return configureAST(expression, ctx);
    }

    @Override
    public Expression visitPathExpression(final PathExpressionContext ctx) {
        final TerminalNode staticTerminalNode = ctx.STATIC();
        Expression primaryExpr;
        if (asBoolean(staticTerminalNode)) {
            primaryExpr = configureAST(new VariableExpression(staticTerminalNode.getText()), staticTerminalNode);
        } else {
            primaryExpr = (Expression) this.visit(ctx.primary());
        }

        return this.createPathExpression(primaryExpr, ctx.pathElement());
    }

    @Override
    public Expression visitPathElement(final PathElementContext ctx) {
        Expression baseExpr = ctx.getNodeMetaData(PATH_EXPRESSION_BASE_EXPR);
        Objects.requireNonNull(baseExpr, "baseExpr is required!");

        if (asBoolean(ctx.namePart())) {
            Expression namePartExpr = this.visitNamePart(ctx.namePart());
            GenericsType[] genericsTypes = this.visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments());

            if (asBoolean(ctx.DOT())) {
                boolean isSafeChain = isTrue(baseExpr, PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN);
                return this.createDotExpression(ctx, baseExpr, namePartExpr, genericsTypes, isSafeChain);
            } else if (asBoolean(ctx.SAFE_DOT())) {
                return this.createDotExpression(ctx, baseExpr, namePartExpr, genericsTypes, true);
            } else if (asBoolean(ctx.SAFE_CHAIN_DOT())) { // e.g. obj??.a  OR obj??.@a
                Expression expression = createDotExpression(ctx, baseExpr, namePartExpr, genericsTypes, true);
                expression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN, Boolean.TRUE);
                return expression;
            } else if (asBoolean(ctx.METHOD_POINTER())) { // e.g. obj.&m
                return configureAST(new MethodPointerExpression(baseExpr, namePartExpr), ctx);
            } else if (asBoolean(ctx.METHOD_REFERENCE())) { // e.g. obj::m
                return configureAST(new MethodReferenceExpression(baseExpr, namePartExpr), ctx);
            } else if (asBoolean(ctx.SPREAD_DOT())) {
                if (asBoolean(ctx.AT())) { // e.g. obj*.@a
                    AttributeExpression attributeExpression = new AttributeExpression(baseExpr, namePartExpr, true);
                    attributeExpression.setSpreadSafe(true);
                    return configureAST(attributeExpression, ctx);
                } else { // e.g. obj*.p
                    PropertyExpression propertyExpression = new PropertyExpression(baseExpr, namePartExpr, true);
                    propertyExpression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES, genericsTypes);
                    propertyExpression.setSpreadSafe(true);
                    return configureAST(propertyExpression, ctx);
                }
            }
        } else if (asBoolean(ctx.creator())) {
            CreatorContext creatorContext = ctx.creator();
            creatorContext.putNodeMetaData(ENCLOSING_INSTANCE_EXPRESSION, baseExpr);
            return configureAST(this.visitCreator(creatorContext), ctx);
        } else if (asBoolean(ctx.indexPropertyArgs())) { // e.g. list[1, 3, 5]
            Tuple2<Token, Expression> tuple = this.visitIndexPropertyArgs(ctx.indexPropertyArgs());
            boolean isSafeChain = isTrue(baseExpr, PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN);
            return configureAST(
                    new BinaryExpression(baseExpr, createGroovyToken(tuple.getV1()), tuple.getV2(), isSafeChain || asBoolean(ctx.indexPropertyArgs().SAFE_INDEX())),
                    ctx);
        } else if (asBoolean(ctx.namedPropertyArgs())) { // this is a special way to signify a cast, e.g. Person[name: 'Daniel.Sun', location: 'Shanghai']
            List<MapEntryExpression> mapEntryExpressionList = this.visitNamedPropertyArgs(ctx.namedPropertyArgs());

            Expression right;
            Expression firstKeyExpression;
            int mapEntryExpressionListSize = mapEntryExpressionList.size();
            if (mapEntryExpressionListSize == 0) {
                // expecting list of MapEntryExpressions later so use SpreadMap to smuggle empty MapExpression to later stages
                right = configureAST(
                        new SpreadMapExpression(configureAST(new MapExpression(), ctx.namedPropertyArgs())),
                        ctx.namedPropertyArgs());
            } else if (mapEntryExpressionListSize == 1 && (firstKeyExpression = mapEntryExpressionList.get(0).getKeyExpression()) instanceof SpreadMapExpression) {
                right = firstKeyExpression;
            } else {
                ListExpression listExpression =
                        configureAST(
                                new ListExpression(
                                        mapEntryExpressionList.stream()
                                                .map(e -> {
                                                    if (e.getKeyExpression() instanceof SpreadMapExpression) {
                                                        return e.getKeyExpression();
                                                    }
                                                    return e;
                                                })
                                                .collect(Collectors.toList())),
                                ctx.namedPropertyArgs()
                        );
                listExpression.setWrapped(true);
                right = listExpression;
            }

            NamedPropertyArgsContext namedPropertyArgsContext = ctx.namedPropertyArgs();
            Token token = (namedPropertyArgsContext.LBRACK() == null
                            ? namedPropertyArgsContext.SAFE_INDEX()
                            : namedPropertyArgsContext.LBRACK()).getSymbol();
            return configureAST(
                    new BinaryExpression(baseExpr, createGroovyToken(token), right),
                    ctx);
        } else if (asBoolean(ctx.arguments())) {
            Expression argumentsExpr = this.visitArguments(ctx.arguments());
            configureAST(argumentsExpr, ctx);

            if (isInsideParentheses(baseExpr)) { // e.g. (obj.x)(), (obj.@x)()
                return configureAST(createCallMethodCallExpression(baseExpr, argumentsExpr), ctx);
            }

            if (baseExpr instanceof AttributeExpression) { // e.g. obj.@a(1, 2)
                AttributeExpression attributeExpression = (AttributeExpression) baseExpr;
                attributeExpression.setSpreadSafe(false); // whether attributeExpression is spread safe or not, we must reset it as false
                return configureAST(createCallMethodCallExpression(attributeExpression, argumentsExpr, true), ctx);
            }

            if (baseExpr instanceof PropertyExpression) { // e.g. obj.a(1, 2)
                MethodCallExpression methodCallExpression = this.createMethodCallExpression((PropertyExpression) baseExpr, argumentsExpr);
                return configureAST(methodCallExpression, ctx);
            }

            if (baseExpr instanceof VariableExpression) { // void and primitive type AST node must be an instance of VariableExpression
                String baseExprText = baseExpr.getText();
                if (VOID_STR.equals(baseExprText)) { // e.g. void()
                    return configureAST(this.createCallMethodCallExpression(this.createConstantExpression(baseExpr), argumentsExpr), ctx);
                } else if (isPrimitiveType(baseExprText)) { // e.g. int(), long(), float(), etc.
                    throw this.createParsingFailedException("Primitive type literal: " + baseExprText + " cannot be used as a method name", ctx);
                }
            }

            if (baseExpr instanceof VariableExpression // e.g. m()
                    || baseExpr instanceof GStringExpression // e.g. "$m"()
                    || (baseExpr instanceof ConstantExpression && isTrue(baseExpr, IS_STRING))) { // e.g. "m"()
                String baseExprText = baseExpr.getText();
                if (THIS_STR.equals(baseExprText) || SUPER_STR.equals(baseExprText)) { // e.g. this(...), super(...)
                    // class declaration is not allowed in the closure,
                    // so if this and super is inside the closure, it will not be constructor call.
                    // e.g. src/test/org/codehaus/groovy/transform/MapConstructorTransformTest.groovy:
                    // @MapConstructor(pre={ super(args?.first, args?.last); args = args ?: [:] }, post = { first = first?.toUpperCase() })
                    if (visitingClosureCount > 0) {
                        return configureAST(
                                new MethodCallExpression(
                                        baseExpr,
                                        baseExprText,
                                        argumentsExpr
                                ),
                                ctx);
                    }

                    return configureAST(
                            new ConstructorCallExpression(
                                    SUPER_STR.equals(baseExprText)
                                            ? ClassNode.SUPER
                                            : ClassNode.THIS,
                                    argumentsExpr
                            ),
                            ctx);
                }

                MethodCallExpression methodCallExpression = this.createMethodCallExpression(baseExpr, argumentsExpr);
                return configureAST(methodCallExpression, ctx);
            }

            // e.g. 1(), 1.1(), ((int) 1 / 2)(1, 2), {a, b -> a + b }(1, 2), m()()
            return configureAST(this.createCallMethodCallExpression(baseExpr, argumentsExpr), ctx);

        } else if (asBoolean(ctx.closureOrLambdaExpression())) {
            ClosureExpression closureExpression = this.visitClosureOrLambdaExpression(ctx.closureOrLambdaExpression());

            if (baseExpr instanceof MethodCallExpression) {
                MethodCallExpression methodCallExpression = (MethodCallExpression) baseExpr;
                Expression argumentsExpression = methodCallExpression.getArguments();

                if (argumentsExpression instanceof ArgumentListExpression) { // normal arguments, e.g. 1, 2
                    ArgumentListExpression argumentListExpression = (ArgumentListExpression) argumentsExpression;
                    argumentListExpression.getExpressions().add(closureExpression);
                    return configureAST(methodCallExpression, ctx);
                }

                if (argumentsExpression instanceof TupleExpression) { // named arguments, e.g. x: 1, y: 2
                    TupleExpression tupleExpression = (TupleExpression) argumentsExpression;
                    NamedArgumentListExpression namedArgumentListExpression = (NamedArgumentListExpression) tupleExpression.getExpression(0);

                    if (asBoolean(tupleExpression.getExpressions())) {
                        methodCallExpression.setArguments(
                                configureAST(
                                        new ArgumentListExpression(
                                                configureAST(
                                                        new MapExpression(namedArgumentListExpression.getMapEntryExpressions()),
                                                        namedArgumentListExpression
                                                ),
                                                closureExpression
                                        ),
                                        tupleExpression
                                )
                        );
                    } else {
                        // the branch should never reach, because named arguments must not be empty
                        methodCallExpression.setArguments(
                                configureAST(
                                        new ArgumentListExpression(closureExpression),
                                        tupleExpression
                                )
                        );
                    }

                    return configureAST(methodCallExpression, ctx);
                }
            }

            if (baseExpr instanceof PropertyExpression) { // e.g. obj.m { }
                MethodCallExpression methodCallExpression =
                        this.createMethodCallExpression(
                                (PropertyExpression) baseExpr,
                                configureAST(
                                        new ArgumentListExpression(closureExpression),
                                        closureExpression
                                )
                        );

                return configureAST(methodCallExpression, ctx);
            }

            if (baseExpr instanceof VariableExpression // e.g. m { }
                    || baseExpr instanceof GStringExpression // e.g. "$m" { }
                    || (baseExpr instanceof ConstantExpression && isTrue(baseExpr, IS_STRING))) { // e.g. "m" { }
                MethodCallExpression methodCallExpression =
                        this.createMethodCallExpression(
                                baseExpr,
                                configureAST(
                                        new ArgumentListExpression(closureExpression),
                                        closureExpression
                                )
                        );

                return configureAST(methodCallExpression, ctx);
            }

            // e.g. 1 { }, 1.1 { }, (1 / 2) { }, m() { }, { -> ... } { }
            MethodCallExpression methodCallExpression =
                    this.createCallMethodCallExpression(
                        baseExpr,
                        configureAST(
                                new ArgumentListExpression(closureExpression),
                                closureExpression)
                    );

            return configureAST(methodCallExpression, ctx);
        }

        throw createParsingFailedException("Unsupported path element: " + ctx.getText(), ctx);
    }

    private Expression createDotExpression(final PathElementContext ctx, final Expression baseExpr, final Expression namePartExpr, final GenericsType[] genericsTypes, final boolean safe) {
        if (asBoolean(ctx.AT())) { // e.g. obj.@a  OR  obj?.@a
            return configureAST(new AttributeExpression(baseExpr, namePartExpr, safe), ctx);
        } else { // e.g. obj.p  OR  obj?.p
            PropertyExpression propertyExpression = new PropertyExpression(baseExpr, namePartExpr, safe);
            propertyExpression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES, genericsTypes);
            return configureAST(propertyExpression, ctx);
        }
    }

    private MethodCallExpression createCallMethodCallExpression(final Expression baseExpr, final Expression argumentsExpr) {
        return createCallMethodCallExpression(baseExpr, argumentsExpr, false);
    }

    private MethodCallExpression createCallMethodCallExpression(final Expression baseExpr, final Expression argumentsExpr, final boolean implicitThis) {
        MethodCallExpression methodCallExpression = new MethodCallExpression(baseExpr, CALL_STR, argumentsExpr);
        methodCallExpression.setImplicitThis(implicitThis);
        return methodCallExpression;
    }

    @Override
    public GenericsType[] visitNonWildcardTypeArguments(final NonWildcardTypeArgumentsContext ctx) {
        if (!asBoolean(ctx)) {
            return null;
        }

        return Arrays.stream(this.visitTypeList(ctx.typeList()))
                .map(this::createGenericsType)
                .toArray(GenericsType[]::new);
    }

    @Override
    public ClassNode[] visitTypeList(final TypeListContext ctx) {
        if (!asBoolean(ctx)) {
            return ClassNode.EMPTY_ARRAY;
        }

        return ctx.type().stream()
                .map(this::visitType)
                .toArray(ClassNode[]::new);
    }

    @Override
    public Expression visitArguments(final ArgumentsContext ctx) {
        if (asBoolean(ctx) && asBoolean(ctx.COMMA()) && !asBoolean(ctx.enhancedArgumentListInPar())) {
            throw createParsingFailedException("Expression expected", ctx.COMMA());
        }

        if (!asBoolean(ctx) || !asBoolean(ctx.enhancedArgumentListInPar())) {
            return new ArgumentListExpression();
        }

        return configureAST(this.visitEnhancedArgumentListInPar(ctx.enhancedArgumentListInPar()), ctx);
    }

    @Override
    public Expression visitEnhancedArgumentListInPar(final EnhancedArgumentListInParContext ctx) {
        if (!asBoolean(ctx)) {
            return null;
        }

        List<Expression> expressionList = new LinkedList<>();
        List<MapEntryExpression> mapEntryExpressionList = new LinkedList<>();

        ctx.enhancedArgumentListElement().stream()
                .map(this::visitEnhancedArgumentListElement)
                .forEach(e -> {

                    if (e instanceof MapEntryExpression) {
                        MapEntryExpression mapEntryExpression = (MapEntryExpression) e;
                        validateDuplicatedNamedParameter(mapEntryExpressionList, mapEntryExpression);

                        mapEntryExpressionList.add(mapEntryExpression);
                    } else {
                        expressionList.add(e);
                    }
                });

        if (!asBoolean(mapEntryExpressionList)) { // e.g. arguments like  1, 2 OR  someArg, e -> e
            return configureAST(
                    new ArgumentListExpression(expressionList),
                    ctx);
        }

        if (!asBoolean(expressionList)) { // e.g. arguments like  x: 1, y: 2
            return configureAST(
                    new TupleExpression(
                            configureAST(
                                    new NamedArgumentListExpression(mapEntryExpressionList),
                                    ctx)),
                    ctx);
        }

        if (asBoolean(mapEntryExpressionList) && asBoolean(expressionList)) { // e.g. arguments like x: 1, 'a', y: 2, 'b', z: 3
            ArgumentListExpression argumentListExpression = new ArgumentListExpression(expressionList);
            argumentListExpression.getExpressions().add(0, configureAST(new MapExpression(mapEntryExpressionList), ctx));
            return configureAST(argumentListExpression, ctx);
        }

        throw createParsingFailedException("Unsupported argument list: " + ctx.getText(), ctx);
    }

    private void validateDuplicatedNamedParameter(final List<MapEntryExpression> mapEntryExpressionList, final MapEntryExpression mapEntryExpression) {
        Expression keyExpression = mapEntryExpression.getKeyExpression();
        if (keyExpression == null || isInsideParentheses(keyExpression)) {
            return;
        }

        String parameterName = keyExpression.getText();
        boolean isDuplicatedNamedParameter = mapEntryExpressionList.stream()
                .anyMatch(m -> m.getKeyExpression().getText().equals(parameterName));
        if (!isDuplicatedNamedParameter) {
            return;
        }

        throw createParsingFailedException("Duplicated named parameter '" + parameterName + "' found", mapEntryExpression);
    }

    @Override
    public Expression visitEnhancedArgumentListElement(final EnhancedArgumentListElementContext ctx) {
        if (asBoolean(ctx.expressionListElement())) {
            return configureAST(this.visitExpressionListElement(ctx.expressionListElement()), ctx);
        }

        if (asBoolean(ctx.standardLambdaExpression())) {
            return configureAST(this.visitStandardLambdaExpression(ctx.standardLambdaExpression()), ctx);
        }

        if (asBoolean(ctx.mapEntry())) {
            return configureAST(this.visitMapEntry(ctx.mapEntry()), ctx);
        }

        throw createParsingFailedException("Unsupported enhanced argument list element: " + ctx.getText(), ctx);
    }

    @Override
    public ConstantExpression visitStringLiteral(final StringLiteralContext ctx) {
        String text = parseStringLiteral(ctx.StringLiteral().getText());

        ConstantExpression constantExpression = new ConstantExpression(text);
        constantExpression.putNodeMetaData(IS_STRING, Boolean.TRUE);
        return configureAST(constantExpression, ctx);
    }

    private String parseStringLiteral(String text) {
        int slashyType = getSlashyType(text);
        boolean startsWithSlash = false;

        if (text.startsWith(TSQ_STR) || text.startsWith(TDQ_STR)) {
            text = StringUtils.removeCR(text); // remove CR in the multiline string

            text = StringUtils.trimQuotations(text, 3);
        } else if (text.startsWith(SQ_STR) || text.startsWith(DQ_STR) || (startsWithSlash = text.startsWith(SLASH_STR))) {
            if (startsWithSlash) { // the slashy string can span rows, so we have to remove CR for it
                text = StringUtils.removeCR(text); // remove CR in the multiline string
            }

            text = StringUtils.trimQuotations(text, 1);
        } else if (text.startsWith(DOLLAR_SLASH_STR)) {
            text = StringUtils.removeCR(text);

            text = StringUtils.trimQuotations(text, 2);
        }

        //handle escapes.
        return StringUtils.replaceEscapes(text, slashyType);
    }

    private int getSlashyType(final String text) {
        return text.startsWith(SLASH_STR) ? StringUtils.SLASHY :
                    text.startsWith(DOLLAR_SLASH_STR) ? StringUtils.DOLLAR_SLASHY : StringUtils.NONE_SLASHY;
    }

    @Override
    public Tuple2<Token, Expression> visitIndexPropertyArgs(final IndexPropertyArgsContext ctx) {
        List<Expression> expressionList = this.visitExpressionList(ctx.expressionList());
        Token token = (ctx.LBRACK() == null
                            ? ctx.SAFE_INDEX()
                            : ctx.LBRACK()).getSymbol();

        if (expressionList.size() == 1) {
            Expression expr = expressionList.get(0);

            Expression indexExpr;
            if (expr instanceof SpreadExpression) { // e.g. a[*[1, 2]]
                ListExpression listExpression = new ListExpression(expressionList);
                listExpression.setWrapped(false);

                indexExpr = listExpression;
            } else { // e.g. a[1]
                indexExpr = expr;
            }

            return tuple(token, indexExpr);
        }

        // e.g. a[1, 2]
        ListExpression listExpression = new ListExpression(expressionList);
        listExpression.setWrapped(true);

        return tuple(token, configureAST(listExpression, ctx));
    }

    @Override
    public List<MapEntryExpression> visitNamedPropertyArgs(final NamedPropertyArgsContext ctx) {
        return this.visitMapEntryList(ctx.mapEntryList());
    }

    @Override
    public Expression visitNamePart(final NamePartContext ctx) {
        if (asBoolean(ctx.identifier())) {
            return configureAST(new ConstantExpression(this.visitIdentifier(ctx.identifier())), ctx);
        } else if (asBoolean(ctx.stringLiteral())) {
            return configureAST(this.visitStringLiteral(ctx.stringLiteral()), ctx);
        } else if (asBoolean(ctx.dynamicMemberName())) {
            return configureAST(this.visitDynamicMemberName(ctx.dynamicMemberName()), ctx);
        } else if (asBoolean(ctx.keywords())) {
            return configureAST(new ConstantExpression(ctx.keywords().getText()), ctx);
        }

        throw createParsingFailedException("Unsupported name part: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitDynamicMemberName(final DynamicMemberNameContext ctx) {
        if (asBoolean(ctx.parExpression())) {
            return configureAST(this.visitParExpression(ctx.parExpression()), ctx);
        } else if (asBoolean(ctx.gstring())) {
            return configureAST(this.visitGstring(ctx.gstring()), ctx);
        }

        throw createParsingFailedException("Unsupported dynamic member name: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitPostfixExpression(final PostfixExpressionContext ctx) {
        Expression pathExpr = this.visitPathExpression(ctx.pathExpression());

        if (asBoolean(ctx.op)) {
            PostfixExpression postfixExpression = new PostfixExpression(pathExpr, createGroovyToken(ctx.op));

            if (visitingAssertStatementCount > 0) {
                // powerassert requires different column for values, so we have to copy the location of op
                return configureAST(postfixExpression, ctx.op);
            } else {
                return configureAST(postfixExpression, ctx);
            }
        }

        return configureAST(pathExpr, ctx);
    }

    @Override
    public Expression visitUnaryNotExprAlt(final UnaryNotExprAltContext ctx) {
        if (asBoolean(ctx.NOT())) {
            return configureAST(
                    new NotExpression((Expression) this.visit(ctx.expression())),
                    ctx);
        }

        if (asBoolean(ctx.BITNOT())) {
            return configureAST(
                    new BitwiseNegationExpression((Expression) this.visit(ctx.expression())),
                    ctx);
        }

        throw createParsingFailedException("Unsupported unary expression: " + ctx.getText(), ctx);
    }

    @Override
    public CastExpression visitCastExprAlt(final CastExprAltContext ctx) {
        Expression expr = (Expression) this.visit(ctx.expression());
        if (expr instanceof VariableExpression && ((VariableExpression) expr).isSuperExpression()) {
            this.createParsingFailedException("Cannot cast or coerce `super`", ctx); // GROOVY-9391
        }
        CastExpression cast = new CastExpression(this.visitCastParExpression(ctx.castParExpression()), expr);
        return configureAST(cast, ctx);
    }

    @Override
    public BinaryExpression visitPowerExprAlt(final PowerExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public Expression visitUnaryAddExprAlt(final UnaryAddExprAltContext ctx) {
        Expression expression = (Expression) this.visit(ctx.expression());
        switch (ctx.op.getType()) {
          case ADD:
            if (this.isNonStringConstantOutsideParentheses(expression)) {
                return configureAST(expression, ctx);
            }
            return configureAST(new UnaryPlusExpression(expression), ctx);

          case SUB:
            if (this.isNonStringConstantOutsideParentheses(expression)) {
                ConstantExpression constantExpression = (ConstantExpression) expression;
                try {
                    String integerLiteralText = constantExpression.getNodeMetaData(INTEGER_LITERAL_TEXT);
                    if (integerLiteralText != null) {
                        ConstantExpression result = new ConstantExpression(Numbers.parseInteger(SUB_STR + integerLiteralText), true);
                        this.numberFormatError = null; // reset
                        return configureAST(result, ctx);
                    }

                    String floatingPointLiteralText = constantExpression.getNodeMetaData(FLOATING_POINT_LITERAL_TEXT);
                    if (floatingPointLiteralText != null) {
                        ConstantExpression result = new ConstantExpression(Numbers.parseDecimal(SUB_STR + floatingPointLiteralText), true);
                        this.numberFormatError = null; // reset
                        return configureAST(result, ctx);
                    }
                } catch (Exception e) {
                    throw this.createParsingFailedException(e.getMessage(), ctx);
                }
                throw new GroovyBugError("Failed to find the original number literal text: " + constantExpression.getText());
            }
            return configureAST(new UnaryMinusExpression(expression), ctx);

          case INC:
          case DEC:
            return configureAST(new PrefixExpression(this.createGroovyToken(ctx.op), expression), ctx);

          default:
            throw this.createParsingFailedException("Unsupported unary operation: " + ctx.getText(), ctx);
        }
    }

    private boolean isNonStringConstantOutsideParentheses(final Expression expression) {
        return expression instanceof ConstantExpression
                && !(((ConstantExpression) expression).getValue() instanceof String)
                && !isInsideParentheses(expression);
    }

    @Override
    public BinaryExpression visitMultiplicativeExprAlt(final MultiplicativeExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public BinaryExpression visitAdditiveExprAlt(final AdditiveExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public Expression visitShiftExprAlt(final ShiftExprAltContext ctx) {
        Expression left = (Expression) this.visit(ctx.left);
        Expression right = (Expression) this.visit(ctx.right);

        if (asBoolean(ctx.rangeOp)) {
            return configureAST(new RangeExpression(left, right, ctx.rangeOp.getText().startsWith("<"), ctx.rangeOp.getText().endsWith("<")), ctx);
        }

        org.codehaus.groovy.syntax.Token op;
        Token antlrToken;

        if (asBoolean(ctx.dlOp)) {
            op = this.createGroovyToken(ctx.dlOp, 2);
            antlrToken = ctx.dlOp;
        } else if (asBoolean(ctx.dgOp)) {
            op = this.createGroovyToken(ctx.dgOp, 2);
            antlrToken = ctx.dgOp;
        } else if (asBoolean(ctx.tgOp)) {
            op = this.createGroovyToken(ctx.tgOp, 3);
            antlrToken = ctx.tgOp;
        } else {
            throw createParsingFailedException("Unsupported shift expression: " + ctx.getText(), ctx);
        }

        BinaryExpression binaryExpression = new BinaryExpression(left, op, right);
        if (isTrue(ctx, IS_INSIDE_CONDITIONAL_EXPRESSION)) {
            return configureAST(binaryExpression, antlrToken);
        }

        return configureAST(binaryExpression, ctx);
    }

    @Override
    public Expression visitRelationalExprAlt(final RelationalExprAltContext ctx) {
        switch (ctx.op.getType()) {
          case AS:
            Expression expr = (Expression) this.visit(ctx.left);
            if (expr instanceof VariableExpression && ((VariableExpression) expr).isSuperExpression()) {
                this.createParsingFailedException("Cannot cast or coerce `super`", ctx); // GROOVY-9391
            }
            CastExpression cast = CastExpression.asExpression(this.visitType(ctx.type()), expr);
            return configureAST(cast, ctx);

          case INSTANCEOF:
          case NOT_INSTANCEOF:
            ctx.type().putNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR, Boolean.TRUE);
            return configureAST(
                    new BinaryExpression(
                            (Expression) this.visit(ctx.left),
                            this.createGroovyToken(ctx.op),
                            configureAST(new ClassExpression(this.visitType(ctx.type())), ctx.type())),
                    ctx);

          case GT:
          case GE:
          case LT:
          case LE:
          case IN:
          case NOT_IN:
            return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);

          default:
            throw this.createParsingFailedException("Unsupported relational expression: " + ctx.getText(), ctx);
        }
    }

    @Override
    public BinaryExpression visitEqualityExprAlt(final EqualityExprAltContext ctx) {
        return configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitRegexExprAlt(final RegexExprAltContext ctx) {
        return configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitAndExprAlt(final AndExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public BinaryExpression visitExclusiveOrExprAlt(final ExclusiveOrExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public BinaryExpression visitInclusiveOrExprAlt(final InclusiveOrExprAltContext ctx) {
        return this.createBinaryExpression(ctx.left, ctx.op, ctx.right, ctx);
    }

    @Override
    public BinaryExpression visitLogicalAndExprAlt(final LogicalAndExprAltContext ctx) {
        return configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitLogicalOrExprAlt(final LogicalOrExprAltContext ctx) {
        return configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public BinaryExpression visitImplicationExprAlt(final ImplicationExprAltContext ctx) {
        return configureAST(
                this.createBinaryExpression(ctx.left, ctx.op, ctx.right),
                ctx);
    }

    @Override
    public Expression visitConditionalExprAlt(final ConditionalExprAltContext ctx) {
        ctx.fb.putNodeMetaData(IS_INSIDE_CONDITIONAL_EXPRESSION, Boolean.TRUE);

        if (asBoolean(ctx.ELVIS())) { // e.g. a == 6 ?: 0
            return configureAST(
                    new ElvisOperatorExpression((Expression) this.visit(ctx.con), (Expression) this.visit(ctx.fb)),
                    ctx);
        }

        ctx.tb.putNodeMetaData(IS_INSIDE_CONDITIONAL_EXPRESSION, Boolean.TRUE);

        return configureAST(
                new TernaryExpression(
                        configureAST(new BooleanExpression((Expression) this.visit(ctx.con)),
                                ctx.con),
                        (Expression) this.visit(ctx.tb),
                        (Expression) this.visit(ctx.fb)),
                ctx);
    }

    @Override
    public BinaryExpression visitMultipleAssignmentExprAlt(final MultipleAssignmentExprAltContext ctx) {
        return configureAST(
                new BinaryExpression(
                        this.visitVariableNames(ctx.left),
                        this.createGroovyToken(ctx.op),
                        ((ExpressionStatement) this.visit(ctx.right)).getExpression()),
                ctx);
    }

    @Override
    public BinaryExpression visitAssignmentExprAlt(final AssignmentExprAltContext ctx) {
        Expression leftExpr = (Expression) this.visit(ctx.left);

        if (leftExpr instanceof VariableExpression
                && isInsideParentheses(leftExpr)) { // it is a special multiple assignment whose variable count is only one, e.g. (a) = [1]

            if (leftExpr.<Number>getNodeMetaData(INSIDE_PARENTHESES_LEVEL).intValue() > 1) {
                throw createParsingFailedException("Nested parenthesis is not allowed in multiple assignment, e.g. ((a)) = b", ctx);
            }

            return configureAST(
                    new BinaryExpression(
                            configureAST(new TupleExpression(leftExpr), ctx.left),
                            this.createGroovyToken(ctx.op),
                            (Expression) this.visit(ctx.right)),
                    ctx);
        }

        // the LHS expression should be a variable which is not inside any parentheses
        if (
                !(
                        (leftExpr instanceof VariableExpression
//                                && !(THIS_STR.equals(leftExpr.getText()) || SUPER_STR.equals(leftExpr.getText()))     // commented, e.g. this = value // this will be transformed to $this
                                && !isInsideParentheses(leftExpr)) // e.g. p = 123

                                || leftExpr instanceof PropertyExpression // e.g. obj.p = 123

                                || (leftExpr instanceof BinaryExpression
//                                && !(((BinaryExpression) leftExpr).getRightExpression() instanceof ListExpression)    // commented, e.g. list[1, 2] = [11, 12]
                                && Types.LEFT_SQUARE_BRACKET == ((BinaryExpression) leftExpr).getOperation().getType()) // e.g. map[a] = 123 OR map['a'] = 123 OR map["$a"] = 123
                )

            ) {

            throw createParsingFailedException("The LHS of an assignment should be a variable or a field accessing expression", ctx);
        }

        return configureAST(
                new BinaryExpression(
                        leftExpr,
                        this.createGroovyToken(ctx.op),
                        (Expression) this.visit(ctx.right)),
                ctx);
    }

    // } expression ------------------------------------------------------------

    // primary { ---------------------------------------------------------------

    @Override
    public Expression visitIdentifierPrmrAlt(final IdentifierPrmrAltContext ctx) {
        if (asBoolean(ctx.typeArguments())) {
            ClassNode classNode = ClassHelper.make(ctx.identifier().getText());

            classNode.setGenericsTypes(
                    this.visitTypeArguments(ctx.typeArguments()));

            return configureAST(new ClassExpression(classNode), ctx);
        }

        return configureAST(new VariableExpression(this.visitIdentifier(ctx.identifier())), ctx);
    }

    @Override
    public Expression visitNewPrmrAlt(final NewPrmrAltContext ctx) {
        return configureAST(this.visitCreator(ctx.creator()), ctx);
    }

    @Override
    public VariableExpression visitThisPrmrAlt(final ThisPrmrAltContext ctx) {
        return configureAST(new VariableExpression(ctx.THIS().getText()), ctx);
    }

    @Override
    public VariableExpression visitSuperPrmrAlt(final SuperPrmrAltContext ctx) {
        return configureAST(new VariableExpression(ctx.SUPER().getText()), ctx);
    }

    // } primary ---------------------------------------------------------------

    @Override
    public Expression visitCreator(final CreatorContext ctx) {
        ClassNode classNode = this.visitCreatedName(ctx.createdName());

        if (asBoolean(ctx.arguments())) { // create instance of class
            Expression arguments = this.visitArguments(ctx.arguments());
            Expression enclosingInstanceExpression = ctx.getNodeMetaData(ENCLOSING_INSTANCE_EXPRESSION);

            if (enclosingInstanceExpression != null) {
                if (arguments instanceof ArgumentListExpression) {
                    ((ArgumentListExpression) arguments).getExpressions().add(0, enclosingInstanceExpression);
                } else if (arguments instanceof TupleExpression) {
                    throw createParsingFailedException("Creating instance of non-static class does not support named parameters", arguments);
                } else if (arguments instanceof NamedArgumentListExpression) {
                    throw createParsingFailedException("Unexpected arguments", arguments);
                } else {
                    throw createParsingFailedException("Unsupported arguments", arguments); // should never reach here
                }
                if (enclosingInstanceExpression instanceof ConstructorCallExpression && classNode.getName().indexOf('.') < 0) {
                    classNode.setName(enclosingInstanceExpression.getType().getName() + '.' + classNode.getName()); // GROOVY-8947
                }
            }

            if (asBoolean(ctx.anonymousInnerClassDeclaration())) {
                ctx.anonymousInnerClassDeclaration().putNodeMetaData(ANONYMOUS_INNER_CLASS_SUPER_CLASS, classNode);
                InnerClassNode anonymousInnerClassNode = this.visitAnonymousInnerClassDeclaration(ctx.anonymousInnerClassDeclaration());

                List<InnerClassNode> anonymousInnerClassList = anonymousInnerClassesDefinedInMethodStack.peek();
                if (anonymousInnerClassList != null) { // if the anonymous class is created in a script, no anonymousInnerClassList is available.
                    anonymousInnerClassList.add(anonymousInnerClassNode);
                }

                ConstructorCallExpression constructorCallExpression = new ConstructorCallExpression(anonymousInnerClassNode, arguments);
                constructorCallExpression.setUsingAnonymousInnerClass(true);

                return configureAST(constructorCallExpression, ctx);
            }

            ConstructorCallExpression constructorCallExpression = new ConstructorCallExpression(classNode, arguments);
            return configureAST(constructorCallExpression, ctx);
        }

        if (asBoolean(ctx.dim())) { // create array
            ArrayExpression arrayExpression;

            List<Tuple3<Expression, List<AnnotationNode>, TerminalNode>> dimList =
                    ctx.dim().stream()
                            .map(this::visitDim)
                            .collect(Collectors.toList());

            TerminalNode invalidDimLBrack = null;
            Boolean exprEmpty = null;
            List<Tuple3<Expression, List<AnnotationNode>, TerminalNode>> emptyDimList = new LinkedList<>();
            List<Tuple3<Expression, List<AnnotationNode>, TerminalNode>> dimWithExprList = new LinkedList<>();
            Tuple3<Expression, List<AnnotationNode>, TerminalNode> latestDim = null;
            for (Tuple3<Expression, List<AnnotationNode>, TerminalNode> dim : dimList) {
                if (null == dim.getV1()) {
                    emptyDimList.add(dim);
                    exprEmpty = Boolean.TRUE;
                } else {
                    if (Boolean.TRUE.equals(exprEmpty)) {
                        invalidDimLBrack = latestDim.getV3();
                    }

                    dimWithExprList.add(dim);
                    exprEmpty = Boolean.FALSE;
                }

                latestDim = dim;
            }

            if (asBoolean(ctx.arrayInitializer())) {
                if (!dimWithExprList.isEmpty()) {
                    throw createParsingFailedException("dimension should be empty", dimWithExprList.get(0).getV3());
                }

                ClassNode elementType = classNode;
                for (int i = 0, n = emptyDimList.size() - 1; i < n; i += 1) {
                    elementType = this.createArrayType(elementType);
                }

                arrayExpression =
                        new ArrayExpression(
                                elementType,
                                this.visitArrayInitializer(ctx.arrayInitializer()));

            } else {
                if (null != invalidDimLBrack) {
                    throw createParsingFailedException("dimension cannot be empty", invalidDimLBrack);
                }

                if (dimWithExprList.isEmpty() && !emptyDimList.isEmpty()) {
                    throw createParsingFailedException("dimensions cannot be all empty", emptyDimList.get(0).getV3());
                }

                Expression[] empties;
                if (asBoolean(emptyDimList)) {
                    empties = new Expression[emptyDimList.size()];
                    Arrays.fill(empties, ConstantExpression.EMPTY_EXPRESSION);
                } else {
                    empties = Expression.EMPTY_ARRAY;
                }

                arrayExpression =
                        new ArrayExpression(
                                classNode,
                                null,
                                Stream.concat(
                                        dimWithExprList.stream().map(Tuple3::getV1),
                                        Arrays.stream(empties)
                                ).collect(Collectors.toList()));
            }

            arrayExpression.setType(
                    this.createArrayType(
                            classNode,
                            dimList.stream().map(Tuple3::getV2).collect(Collectors.toList())
                    )
            );

            return configureAST(arrayExpression, ctx);
        }

        throw createParsingFailedException("Unsupported creator: " + ctx.getText(), ctx);
    }

    @Override
    public Tuple3<Expression, List<AnnotationNode>, TerminalNode> visitDim(final DimContext ctx) {
        return tuple((Expression) this.visit(ctx.expression()), this.visitAnnotationsOpt(ctx.annotationsOpt()), ctx.LBRACK());
    }

    private static String nextAnonymousClassName(final ClassNode outerClass) {
        int anonymousClassCount = 0;
        for (Iterator<InnerClassNode> it = outerClass.getInnerClasses(); it.hasNext();) {
            InnerClassNode innerClass = it.next();
            if (innerClass.isAnonymous()) {
                anonymousClassCount += 1;
            }
        }

        return outerClass.getName() + "$" + (anonymousClassCount + 1);
    }

    @Override
    public InnerClassNode visitAnonymousInnerClassDeclaration(final AnonymousInnerClassDeclarationContext ctx) {
        ClassNode superClass = Objects.requireNonNull(ctx.getNodeMetaData(ANONYMOUS_INNER_CLASS_SUPER_CLASS), "superClass should not be null");
        ClassNode outerClass = Optional.ofNullable(this.classNodeStack.peek()).orElse(this.moduleNode.getScriptClassDummy());
        String innerClassName = nextAnonymousClassName(outerClass);

        InnerClassNode anonymousInnerClass;
        if (ctx.t == 1) {
            anonymousInnerClass = new EnumConstantClassNode(outerClass, innerClassName, superClass.getPlainNodeReference());
            // and remove the final modifier from superClass to allow the sub class
            superClass.setModifiers(superClass.getModifiers() & ~Opcodes.ACC_FINAL);
        } else {
            anonymousInnerClass = new InnerClassNode(outerClass, innerClassName, Opcodes.ACC_PUBLIC, superClass);
        }

        anonymousInnerClass.setAnonymous(true);
        anonymousInnerClass.setUsingGenerics(false);
        anonymousInnerClass.putNodeMetaData(CLASS_NAME, innerClassName);
        configureAST(anonymousInnerClass, ctx);

        this.classNodeStack.push(anonymousInnerClass);
        ctx.classBody().putNodeMetaData(CLASS_DECLARATION_CLASS_NODE, anonymousInnerClass);
        this.visitClassBody(ctx.classBody());
        this.classNodeStack.pop();

        if (this.classNodeStack.isEmpty())
            this.addToClassNodeList(anonymousInnerClass);

        return anonymousInnerClass;
    }

    @Override
    public ClassNode visitCreatedName(final CreatedNameContext ctx) {
        ClassNode classNode = null;
        if (asBoolean(ctx.qualifiedClassName())) {
            classNode = this.visitQualifiedClassName(ctx.qualifiedClassName());
            if (asBoolean(ctx.typeArgumentsOrDiamond())) {
                classNode.setGenericsTypes(
                        this.visitTypeArgumentsOrDiamond(ctx.typeArgumentsOrDiamond()));
                configureAST(classNode, ctx);
            }
        } else if (asBoolean(ctx.primitiveType())) {
            classNode = configureAST(this.visitPrimitiveType(ctx.primitiveType()), ctx);
        }
        if (classNode == null) {
            throw createParsingFailedException("Unsupported created name: " + ctx.getText(), ctx);
        }
        classNode.addTypeAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt())); // GROOVY-11178

        return classNode;
    }

    @Override
    public MapExpression visitMap(final MapContext ctx) {
        return configureAST(
                new MapExpression(this.visitMapEntryList(ctx.mapEntryList())),
                ctx);
    }

    @Override
    public List<MapEntryExpression> visitMapEntryList(final MapEntryListContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return this.createMapEntryList(ctx.mapEntry());
    }

    private List<MapEntryExpression> createMapEntryList(final List<? extends MapEntryContext> mapEntryContextList) {
        if (!asBoolean(mapEntryContextList)) {
            return Collections.emptyList();
        }

        return mapEntryContextList.stream()
                .map(this::visitMapEntry)
                .collect(Collectors.toList());
    }

    @Override
    public MapEntryExpression visitMapEntry(final MapEntryContext ctx) {
        Expression keyExpr;
        Expression valueExpr = this.visitEnhancedExpression(ctx.enhancedExpression());

        if (asBoolean(ctx.MUL())) {
            keyExpr = configureAST(new SpreadMapExpression(valueExpr), ctx);
        } else if (asBoolean(ctx.mapEntryLabel())) {
            keyExpr = this.visitMapEntryLabel(ctx.mapEntryLabel());
        } else {
            throw createParsingFailedException("Unsupported map entry: " + ctx.getText(), ctx);
        }

        return configureAST(
                new MapEntryExpression(keyExpr, valueExpr),
                ctx);
    }

    @Override
    public Expression visitMapEntryLabel(final MapEntryLabelContext ctx) {
        if (asBoolean(ctx.keywords())) {
            return configureAST(this.visitKeywords(ctx.keywords()), ctx);
        } else if (asBoolean(ctx.primary())) {
            Expression expression = (Expression) this.visit(ctx.primary());

            // if the key is variable and not inside parentheses, convert it to a constant, e.g. [a:1, b:2]
            if (expression instanceof VariableExpression && !isInsideParentheses(expression)) {
                expression =
                        configureAST(
                                new ConstantExpression(((VariableExpression) expression).getName()),
                                expression);
            }

            return configureAST(expression, ctx);
        }

        throw createParsingFailedException("Unsupported map entry label: " + ctx.getText(), ctx);
    }

    @Override
    public ConstantExpression visitKeywords(final KeywordsContext ctx) {
        return configureAST(new ConstantExpression(ctx.getText()), ctx);
    }

    @Override
    public VariableExpression visitBuiltInType(final BuiltInTypeContext ctx) {
        String text;
        if (asBoolean(ctx.VOID())) {
            text = ctx.VOID().getText();
        } else if (asBoolean(ctx.BuiltInPrimitiveType())) {
            text = ctx.BuiltInPrimitiveType().getText();
        } else {
            throw createParsingFailedException("Unsupported built-in type: " + ctx, ctx);
        }

        final VariableExpression variableExpression = new VariableExpression(text);
        variableExpression.setNodeMetaData(IS_BUILT_IN_TYPE, Boolean.TRUE);
        return configureAST(variableExpression, ctx);
    }

    @Override
    public ListExpression visitList(final ListContext ctx) {
        if (asBoolean(ctx.COMMA()) && !asBoolean(ctx.expressionList())) {
            throw createParsingFailedException("Empty list constructor should not contain any comma(,)", ctx.COMMA());
        }

        return configureAST(
                new ListExpression(
                        this.visitExpressionList(ctx.expressionList())),
                ctx);
    }

    @Override
    public List<Expression> visitExpressionList(final ExpressionListContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return this.createExpressionList(ctx.expressionListElement());
    }

    private List<Expression> createExpressionList(final List<? extends ExpressionListElementContext> expressionListElementContextList) {
        if (!asBoolean(expressionListElementContextList)) {
            return Collections.emptyList();
        }

        return expressionListElementContextList.stream()
                .map(this::visitExpressionListElement)
                .collect(Collectors.toList());
    }

    @Override
    public Expression visitExpressionListElement(final ExpressionListElementContext ctx) {
        Expression expression = (Expression) this.visit(ctx.expression());

        validateExpressionListElement(ctx, expression);

        if (asBoolean(ctx.MUL())) {
            if (!ctx.canSpread) {
                throw createParsingFailedException("spread operator is not allowed here", ctx.MUL());
            }

            return configureAST(new SpreadExpression(expression), ctx);
        }

        return configureAST(expression, ctx);
    }

    private void validateExpressionListElement(final ExpressionListElementContext ctx, final Expression expression) {
        if (expression instanceof MethodCallExpression && isTrue(expression, IS_COMMAND_EXPRESSION)) {
            // statements like `foo(String a)` is invalid
            MethodCallExpression methodCallExpression = (MethodCallExpression) expression;
            String methodName = methodCallExpression.getMethodAsString();
            if (methodCallExpression.isImplicitThis() && Character.isUpperCase(methodName.codePointAt(0)) || isPrimitiveType(methodName)) {
                throw createParsingFailedException("Invalid method declaration", ctx);
            }
        }
    }

    // literal { ---------------------------------------------------------------

    @Override
    public ConstantExpression visitIntegerLiteralAlt(final IntegerLiteralAltContext ctx) {
        String text = ctx.IntegerLiteral().getText();
        Number num = null;
        try {
            num = Numbers.parseInteger(text);
        } catch (Exception e) {
            this.numberFormatError = tuple(ctx, e);
        }

        ConstantExpression constantExpression = new ConstantExpression(num, true);
        constantExpression.putNodeMetaData(INTEGER_LITERAL_TEXT, text);
        constantExpression.putNodeMetaData(IS_NUMERIC, Boolean.TRUE);
        return configureAST(constantExpression, ctx);
    }

    @Override
    public ConstantExpression visitFloatingPointLiteralAlt(final FloatingPointLiteralAltContext ctx) {
        String text = ctx.FloatingPointLiteral().getText();
        Number num = null;
        try {
            num = Numbers.parseDecimal(text);
        } catch (Exception e) {
            this.numberFormatError = tuple(ctx, e);
        }

        ConstantExpression constantExpression = new ConstantExpression(num, true);
        constantExpression.putNodeMetaData(FLOATING_POINT_LITERAL_TEXT, text);
        constantExpression.putNodeMetaData(IS_NUMERIC, Boolean.TRUE);
        return configureAST(constantExpression, ctx);
    }

    @Override
    public ConstantExpression visitBooleanLiteralAlt(final BooleanLiteralAltContext ctx) {
        return configureAST(new ConstantExpression("true".equals(ctx.BooleanLiteral().getText()), true), ctx);
    }

    @Override
    public ConstantExpression visitNullLiteralAlt(final NullLiteralAltContext ctx) {
        return configureAST(new ConstantExpression(null), ctx);
    }

    // } literal ---------------------------------------------------------------

    // gstring { ---------------------------------------------------------------

    @Override
    public GStringExpression visitGstring(final GstringContext ctx) {
        final List<ConstantExpression> stringLiteralList = new LinkedList<>();
        final String begin = ctx.GStringBegin().getText();
        final String beginQuotation = beginQuotation(begin);
        stringLiteralList.add(configureAST(new ConstantExpression(parseGStringBegin(ctx, beginQuotation)), ctx.GStringBegin()));

        List<ConstantExpression> partStrings =
                ctx.GStringPart().stream()
                        .map(e -> configureAST(new ConstantExpression(parseGStringPart(e, beginQuotation)), e))
                        .collect(Collectors.toList());
        stringLiteralList.addAll(partStrings);

        stringLiteralList.add(configureAST(new ConstantExpression(parseGStringEnd(ctx, beginQuotation)), ctx.GStringEnd()));

        List<Expression> values = ctx.gstringValue().stream()
                .map(this::visitGstringValue)
                .collect(Collectors.toList());

        StringBuilder verbatimText = new StringBuilder(ctx.getText().length());
        for (int i = 0, n = stringLiteralList.size(), s = values.size(); i < n; i += 1) {
            verbatimText.append(stringLiteralList.get(i).getValue());

            if (i == s) {
                continue;
            }

            Expression value = values.get(i);
            if (!asBoolean(value)) {
                continue;
            }

            boolean isVariableExpression = value instanceof VariableExpression;
            verbatimText.append(DOLLAR_STR);
            if (!isVariableExpression) verbatimText.append('{');
            verbatimText.append(value.getText());
            if (!isVariableExpression) verbatimText.append('}');
        }

        return configureAST(new GStringExpression(verbatimText.toString(), stringLiteralList, values), ctx);
    }

    private static boolean hasArrow(final GstringValueContext e) {
        return asBoolean(e.closure().ARROW());
    }

    private String parseGStringEnd(final GstringContext ctx, final String beginQuotation) {
        StringBuilder text = new StringBuilder(ctx.GStringEnd().getText());
        text.insert(0, beginQuotation);

        return this.parseStringLiteral(text.toString());
    }

    private String parseGStringPart(final TerminalNode e, final String beginQuotation) {
        StringBuilder text = new StringBuilder(e.getText());
        text.deleteCharAt(text.length() - 1);  // remove the tailing $
        text.insert(0, beginQuotation).append(QUOTATION_MAP.get(beginQuotation));

        return this.parseStringLiteral(text.toString());
    }

    private String parseGStringBegin(final GstringContext ctx, final String beginQuotation) {
        StringBuilder text = new StringBuilder(ctx.GStringBegin().getText());
        text.deleteCharAt(text.length() - 1);  // remove the tailing $
        text.append(QUOTATION_MAP.get(beginQuotation));

        return this.parseStringLiteral(text.toString());
    }

    private static String beginQuotation(final String text) {
        if (text.startsWith(TDQ_STR)) {
            return TDQ_STR;
        } else if (text.startsWith(DQ_STR)) {
            return DQ_STR;
        } else if (text.startsWith(SLASH_STR)) {
            return SLASH_STR;
        } else if (text.startsWith(DOLLAR_SLASH_STR)) {
            return DOLLAR_SLASH_STR;
        } else {
            return String.valueOf(text.charAt(0));
        }
    }

    @Override
    public Expression visitGstringValue(final GstringValueContext ctx) {
        if (asBoolean(ctx.gstringPath())) {
            return configureAST(this.visitGstringPath(ctx.gstringPath()), ctx);
        }

        if (asBoolean(ctx.closure())) {
            ClosureExpression closureExpression = this.visitClosure(ctx.closure());
            if (!hasArrow(ctx)) {
                List<Statement> statementList = ((BlockStatement) closureExpression.getCode()).getStatements();
                int size = statementList.size();
                if (1 == size) {
                    Statement statement = statementList.get(0);
                    if (statement instanceof ExpressionStatement) {
                        Expression expression = ((ExpressionStatement) statement).getExpression();
                        if (!(expression instanceof DeclarationExpression)) {
                            return expression;
                        }
                    }
                } else if (0 == size) { // e.g. "${}"
                    return configureAST(new ConstantExpression(null), ctx);
                }

                return configureAST(this.createCallMethodCallExpression(closureExpression, new ArgumentListExpression(), true), ctx);
            }

            return configureAST(closureExpression, ctx);
        }

        throw createParsingFailedException("Unsupported gstring value: " + ctx.getText(), ctx);
    }

    @Override
    public Expression visitGstringPath(final GstringPathContext ctx) {
        VariableExpression variableExpression = new VariableExpression(this.visitIdentifier(ctx.identifier()));

        if (asBoolean(ctx.GStringPathPart())) {
            Expression propertyExpression = ctx.GStringPathPart().stream()
                    .map(e -> configureAST((Expression) new ConstantExpression(e.getText().substring(1)), e))
                    .reduce(configureAST(variableExpression, ctx.identifier()), (r, e) -> configureAST(new PropertyExpression(r, e), e));

            return configureAST(propertyExpression, ctx);
        }

        return configureAST(variableExpression, ctx);
    }

    // } gstring ---------------------------------------------------------------

    @Override
    public LambdaExpression visitStandardLambdaExpression(final StandardLambdaExpressionContext ctx) {
        switchExpressionRuleContextStack.push(ctx);
        try {
            return configureAST(this.createLambda(ctx.standardLambdaParameters(), ctx.lambdaBody()), ctx);
        } finally {
            switchExpressionRuleContextStack.pop();
        }
    }

    private LambdaExpression createLambda(final StandardLambdaParametersContext standardLambdaParametersContext, final LambdaBodyContext lambdaBodyContext) {
        return new LambdaExpression(
                this.visitStandardLambdaParameters(standardLambdaParametersContext),
                this.visitLambdaBody(lambdaBodyContext));
    }

    @Override
    public Parameter[] visitStandardLambdaParameters(final StandardLambdaParametersContext ctx) {
        if (asBoolean(ctx.variableDeclaratorId())) {
            VariableExpression variable = this.visitVariableDeclaratorId(ctx.variableDeclaratorId());
            Parameter parameter = new Parameter(ClassHelper.dynamicType(), variable.getName());
            configureAST(parameter, variable);
            return new Parameter[]{parameter};
        }

        Parameter[] parameters = this.visitFormalParameters(ctx.formalParameters());
        return (parameters.length > 0 ? parameters : null);
    }

    @Override
    public Statement visitLambdaBody(final LambdaBodyContext ctx) {
        if (asBoolean(ctx.block())) {
            return configureAST(this.visitBlock(ctx.block()), ctx);
        }
        return configureAST((Statement) this.visit(ctx.statementExpression()), ctx);
    }

    @Override
    public ClosureExpression visitClosure(final ClosureContext ctx) {
        switchExpressionRuleContextStack.push(ctx);
        visitingClosureCount += 1;
        try {
            Parameter[] parameters = asBoolean(ctx.formalParameterList())
                    ? this.visitFormalParameterList(ctx.formalParameterList())
                    : null;

            BlockStatement code = this.visitBlockStatementsOpt(ctx.blockStatementsOpt());
            if (!asBoolean(ctx.ARROW())) {
                parameters = Parameter.EMPTY_ARRAY;

                if (code.isEmpty()) {
                    configureAST(code, ctx);
                }
            }

            return configureAST(new ClosureExpression(parameters, code), ctx);
        } finally {
            switchExpressionRuleContextStack.pop();
            visitingClosureCount -= 1;
        }
    }

    @Override
    public Parameter[] visitFormalParameters(final FormalParametersContext ctx) {
        if (!asBoolean(ctx)) {
            return Parameter.EMPTY_ARRAY;
        }

        return this.visitFormalParameterList(ctx.formalParameterList());
    }

    @Override
    public Parameter[] visitFormalParameterList(final FormalParameterListContext ctx) {
        if (!asBoolean(ctx)) {
            return Parameter.EMPTY_ARRAY;
        }

        List<Parameter> parameterList = new LinkedList<>();

        if (asBoolean(ctx.thisFormalParameter())) {
            parameterList.add(this.visitThisFormalParameter(ctx.thisFormalParameter()));
        }

        List<? extends FormalParameterContext> formalParameterList = ctx.formalParameter();
        if (asBoolean(formalParameterList)) {
            validateVarArgParameter(formalParameterList);

            parameterList.addAll(
                    formalParameterList.stream()
                            .map(this::visitFormalParameter)
                            .collect(Collectors.toList()));
        }

        validateParameterList(parameterList);

        return parameterList.toArray(Parameter.EMPTY_ARRAY);
    }

    private void validateVarArgParameter(final List<? extends FormalParameterContext> formalParameterList) {
        for (int i = 0, n = formalParameterList.size(); i < n - 1; i += 1) {
            FormalParameterContext formalParameterContext = formalParameterList.get(i);
            if (asBoolean(formalParameterContext.ELLIPSIS())) {
                throw createParsingFailedException("The var-arg parameter strs must be the last parameter", formalParameterContext);
            }
        }
    }

    private void validateParameterList(final List<Parameter> parameterList) {
        for (int n = parameterList.size(), i = n - 1; i >= 0; i -= 1) {
            Parameter parameter = parameterList.get(i);
            String name = parameter.getName();
            if ("_".equals(name)) {
                continue; // check this later
            }
            for (Parameter otherParameter : parameterList) {
                if (otherParameter == parameter) {
                    continue;
                }
                if (otherParameter.getName().equals(name)) {
                    throw createParsingFailedException("Duplicated parameter '" + name + "' found.", parameter);
                }
            }
        }
    }

    @Override
    public Parameter visitFormalParameter(final FormalParameterContext ctx) {
        return this.processFormalParameter(ctx, ctx.variableModifiersOpt(), ctx.type(), ctx.ELLIPSIS(), ctx.variableDeclaratorId(), ctx.expression());
    }

    @Override
    public Parameter visitThisFormalParameter(final ThisFormalParameterContext ctx) {
        return configureAST(new Parameter(this.visitType(ctx.type()), THIS_STR), ctx);
    }

    @Override
    public List<ModifierNode> visitClassOrInterfaceModifiersOpt(final ClassOrInterfaceModifiersOptContext ctx) {
        if (asBoolean(ctx.classOrInterfaceModifiers())) {
            return this.visitClassOrInterfaceModifiers(ctx.classOrInterfaceModifiers());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ModifierNode> visitClassOrInterfaceModifiers(final ClassOrInterfaceModifiersContext ctx) {
        return ctx.classOrInterfaceModifier().stream()
                .map(this::visitClassOrInterfaceModifier)
                .collect(Collectors.toList());
    }

    @Override
    public ModifierNode visitClassOrInterfaceModifier(final ClassOrInterfaceModifierContext ctx) {
        if (asBoolean(ctx.annotation())) {
            return configureAST(new ModifierNode(this.visitAnnotation(ctx.annotation()), ctx.getText()), ctx);
        }

        if (asBoolean(ctx.m)) {
            return configureAST(new ModifierNode(ctx.m.getType(), ctx.getText()), ctx);
        }

        throw createParsingFailedException("Unsupported class or interface modifier: " + ctx.getText(), ctx);
    }

    @Override
    public ModifierNode visitModifier(final ModifierContext ctx) {
        if (asBoolean(ctx.classOrInterfaceModifier())) {
            return configureAST(this.visitClassOrInterfaceModifier(ctx.classOrInterfaceModifier()), ctx);
        }

        if (asBoolean(ctx.m)) {
            return configureAST(new ModifierNode(ctx.m.getType(), ctx.getText()), ctx);
        }

        throw createParsingFailedException("Unsupported modifier: " + ctx.getText(), ctx);
    }

    @Override
    public List<ModifierNode> visitModifiers(final ModifiersContext ctx) {
        return ctx.modifier().stream()
                .map(this::visitModifier)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModifierNode> visitModifiersOpt(final ModifiersOptContext ctx) {
        if (asBoolean(ctx.modifiers())) {
            return this.visitModifiers(ctx.modifiers());
        }

        return Collections.emptyList();
    }

    @Override
    public ModifierNode visitVariableModifier(final VariableModifierContext ctx) {
        if (asBoolean(ctx.annotation())) {
            return configureAST(new ModifierNode(this.visitAnnotation(ctx.annotation()), ctx.getText()), ctx);
        }

        if (asBoolean(ctx.m)) {
            return configureAST(new ModifierNode(ctx.m.getType(), ctx.getText()), ctx);
        }

        throw createParsingFailedException("Unsupported variable modifier", ctx);
    }

    @Override
    public List<ModifierNode> visitVariableModifiersOpt(final VariableModifiersOptContext ctx) {
        if (asBoolean(ctx.variableModifiers())) {
            return this.visitVariableModifiers(ctx.variableModifiers());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ModifierNode> visitVariableModifiers(final VariableModifiersContext ctx) {
        return ctx.variableModifier().stream()
                .map(this::visitVariableModifier)
                .collect(Collectors.toList());
    }

    @Override
    public List<List<AnnotationNode>> visitEmptyDims(final EmptyDimsContext ctx) {
        List<List<AnnotationNode>> dimList =
                ctx.annotationsOpt().stream()
                        .map(this::visitAnnotationsOpt)
                        .collect(Collectors.toList());

        Collections.reverse(dimList);

        return dimList;
    }

    @Override
    public List<List<AnnotationNode>> visitEmptyDimsOpt(final EmptyDimsOptContext ctx) {
        if (!asBoolean(ctx.emptyDims())) {
            return Collections.emptyList();
        }

        return this.visitEmptyDims(ctx.emptyDims());
    }

    // type { ------------------------------------------------------------------

    @Override
    public ClassNode visitType(final TypeContext ctx) {
        if (!asBoolean(ctx)) {
            return ClassHelper.dynamicType();
        }

        ClassNode classNode = null;

        if (asBoolean(ctx.classOrInterfaceType())) {
            if (isTrue(ctx, IS_INSIDE_INSTANCEOF_EXPR))
                ctx.classOrInterfaceType().putNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR, Boolean.TRUE);
            classNode = this.visitClassOrInterfaceType(ctx.classOrInterfaceType());
        } else if (asBoolean(ctx.primitiveType())) {
            classNode = this.visitPrimitiveType(ctx.primitiveType());
        }

        if (!asBoolean(classNode)) {
            if (VOID_STR.equals(ctx.getText())) {
                throw createParsingFailedException("void is not allowed here", ctx);
            }
            throw createParsingFailedException("Unsupported type: " + ctx.getText(), ctx);
        }

        classNode.addTypeAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

        List<List<AnnotationNode>> dimList = this.visitEmptyDimsOpt(ctx.emptyDimsOpt());
        if (asBoolean(dimList)) {
            classNode = this.createArrayType(classNode, dimList);
        }

        return configureAST(classNode, ctx);
    }

    @Override
    public ClassNode visitClassOrInterfaceType(final ClassOrInterfaceTypeContext ctx) {
        ClassNode classNode;
        if (asBoolean(ctx.qualifiedClassName())) {
            if (isTrue(ctx, IS_INSIDE_INSTANCEOF_EXPR))
                ctx.qualifiedClassName().putNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR, Boolean.TRUE);
            classNode = this.visitQualifiedClassName(ctx.qualifiedClassName());
        } else {
            if (isTrue(ctx, IS_INSIDE_INSTANCEOF_EXPR))
                ctx.qualifiedStandardClassName().putNodeMetaData(IS_INSIDE_INSTANCEOF_EXPR, Boolean.TRUE);
            classNode = this.visitQualifiedStandardClassName(ctx.qualifiedStandardClassName());
        }

        if (asBoolean(ctx.typeArguments())) {
            classNode.setGenericsTypes(
                    this.visitTypeArguments(ctx.typeArguments()));
        }

        return configureAST(classNode, ctx);
    }

    @Override
    public GenericsType[] visitTypeArgumentsOrDiamond(final TypeArgumentsOrDiamondContext ctx) {
        if (asBoolean(ctx.typeArguments())) {
            return this.visitTypeArguments(ctx.typeArguments());
        }

        if (asBoolean(ctx.LT())) { // e.g. <>
            return GenericsType.EMPTY_ARRAY;
        }

        throw createParsingFailedException("Unsupported type arguments or diamond: " + ctx.getText(), ctx);
    }

    @Override
    public GenericsType[] visitTypeArguments(final TypeArgumentsContext ctx) {
        return ctx.typeArgument().stream().map(this::visitTypeArgument).toArray(GenericsType[]::new);
    }

    @Override
    public GenericsType visitTypeArgument(final TypeArgumentContext ctx) {
        if (asBoolean(ctx.QUESTION())) {
            ClassNode baseType = configureAST(ClassHelper.makeWithoutCaching(QUESTION_STR), ctx.QUESTION());
            baseType.addTypeAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

            if (!asBoolean(ctx.type())) {
                GenericsType genericsType = new GenericsType(baseType);
                genericsType.setWildcard(true);

                return configureAST(genericsType, ctx);
            }

            ClassNode[] upperBounds = null;
            ClassNode lowerBound = null;

            ClassNode classNode = this.visitType(ctx.type());
            if (asBoolean(ctx.EXTENDS())) {
                upperBounds = new ClassNode[]{classNode};
            } else if (asBoolean(ctx.SUPER())) {
                lowerBound = classNode;
            }

            GenericsType genericsType = new GenericsType(baseType, upperBounds, lowerBound);
            genericsType.setWildcard(true);

            return configureAST(genericsType, ctx);
        } else if (asBoolean(ctx.type())) {
            ClassNode baseType = this.visitType(ctx.type());
            return configureAST(this.createGenericsType(baseType), ctx);
        }

        throw createParsingFailedException("Unsupported type argument: " + ctx.getText(), ctx);
    }

    @Override
    public ClassNode visitPrimitiveType(final PrimitiveTypeContext ctx) {
        return configureAST(ClassHelper.make(ctx.getText()).getPlainNodeReference(false), ctx);
    }

    // } type ------------------------------------------------------------------

    @Override
    public VariableExpression visitVariableDeclaratorId(final VariableDeclaratorIdContext ctx) {
        return configureAST(new VariableExpression(this.visitIdentifier(ctx.identifier())), ctx);
    }

    @Override
    public TupleExpression visitVariableNames(final VariableNamesContext ctx) {
        return configureAST(
                new TupleExpression(
                        ctx.variableDeclaratorId().stream()
                                .map(this::visitVariableDeclaratorId)
                                .collect(Collectors.toList())
                ),
                ctx);
    }

    @Override
    public ClosureExpression visitClosureOrLambdaExpression(final ClosureOrLambdaExpressionContext ctx) {
        // GROOVY-8991: Difference in behaviour with closure and lambda
        if (asBoolean(ctx.closure())) {
            return configureAST(this.visitClosure(ctx.closure()), ctx);
        } else if (asBoolean(ctx.standardLambdaExpression())) {
            return configureAST(this.visitStandardLambdaExpression(ctx.standardLambdaExpression()), ctx);
        }

        // should never reach here
        throw createParsingFailedException("The node is not expected here" + ctx.getText(), ctx);
    }

    @Override
    public BlockStatement visitBlockStatementsOpt(final BlockStatementsOptContext ctx) {
        if (asBoolean(ctx.blockStatements())) {
            return configureAST(this.visitBlockStatements(ctx.blockStatements()), ctx);
        }

        return configureAST(this.createBlockStatement(), ctx);
    }

    @Override
    public BlockStatement visitBlockStatements(final BlockStatementsContext ctx) {
        return configureAST(
                this.createBlockStatement(
                        ctx.blockStatement().stream()
                                .map(this::visitBlockStatement)
                                .filter(DefaultGroovyMethods::asBoolean)
                                .collect(Collectors.toList())),
                ctx);
    }

    @Override
    public Statement visitBlockStatement(final BlockStatementContext ctx) {
        if (asBoolean(ctx.localVariableDeclaration())) {
            return configureAST(this.visitLocalVariableDeclaration(ctx.localVariableDeclaration()), ctx);
        }

        if (asBoolean(ctx.statement())) {
            Object astNode = this.visit(ctx.statement()); //this.configureAST((Statement) this.visit(ctx.statement()), ctx);

            if (null == astNode) {
                return null;
            }

            if (astNode instanceof Statement) {
                return (Statement) astNode;
            } else if (astNode instanceof MethodNode) {
                throw createParsingFailedException("Method definition not expected here", ctx);
            } else if (astNode instanceof ImportNode) {
                throw createParsingFailedException("Import statement not expected here", ctx);
            } else {
                throw createParsingFailedException("The statement(" + astNode.getClass() + ") not expected here", ctx);
            }
        }

        throw createParsingFailedException("Unsupported block statement: " + ctx.getText(), ctx);
    }

    @Override
    public List<AnnotationNode> visitAnnotationsOpt(final AnnotationsOptContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        return ctx.annotation().stream()
                .map(this::visitAnnotation)
                .collect(Collectors.toList());
    }

    @Override
    public AnnotationNode visitAnnotation(final AnnotationContext ctx) {
        String annotationName = this.visitAnnotationName(ctx.annotationName());
        AnnotationNode annotationNode = new AnnotationNode(makeClassNode(annotationName));
        List<Tuple2<String, Expression>> annotationElementValues = this.visitElementValues(ctx.elementValues());

        annotationElementValues.forEach(e -> annotationNode.addMember(e.getV1(), e.getV2()));
        configureAST(annotationNode.getClassNode(), ctx.annotationName());
        return configureAST(annotationNode, ctx);
    }

    @Override
    public List<Tuple2<String, Expression>> visitElementValues(final ElementValuesContext ctx) {
        if (!asBoolean(ctx)) {
            return Collections.emptyList();
        }

        List<Tuple2<String, Expression>> annotationElementValues = new LinkedList<>();

        if (asBoolean(ctx.elementValuePairs())) {
            this.visitElementValuePairs(ctx.elementValuePairs()).forEach((key, value) -> annotationElementValues.add(tuple(key, value)));
        } else if (asBoolean(ctx.elementValue())) {
            annotationElementValues.add(tuple(VALUE_STR, this.visitElementValue(ctx.elementValue())));
        }

        return annotationElementValues;
    }

    @Override
    public String visitAnnotationName(final AnnotationNameContext ctx) {
        return this.visitQualifiedClassName(ctx.qualifiedClassName()).getName();
    }

    @Override
    public Map<String, Expression> visitElementValuePairs(final ElementValuePairsContext ctx) {
        return ctx.elementValuePair().stream()
                .map(this::visitElementValuePair)
                .collect(Collectors.toMap(
                        Tuple2::getV1,
                        Tuple2::getV2,
                        (k, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", k));
                        },
                        LinkedHashMap::new
                ));
    }

    @Override
    public Tuple2<String, Expression> visitElementValuePair(final ElementValuePairContext ctx) {
        return tuple(ctx.elementValuePairName().getText(), this.visitElementValue(ctx.elementValue()));
    }

    @Override
    public Expression visitElementValue(final ElementValueContext ctx) {
        if (asBoolean(ctx.expression())) {
            return configureAST((Expression) this.visit(ctx.expression()), ctx);
        }

        if (asBoolean(ctx.annotation())) {
            return configureAST(new AnnotationConstantExpression(this.visitAnnotation(ctx.annotation())), ctx);
        }

        if (asBoolean(ctx.elementValueArrayInitializer())) {
            return configureAST(this.visitElementValueArrayInitializer(ctx.elementValueArrayInitializer()), ctx);
        }

        throw createParsingFailedException("Unsupported element value: " + ctx.getText(), ctx);
    }

    @Override
    public ListExpression visitElementValueArrayInitializer(final ElementValueArrayInitializerContext ctx) {
        return configureAST(new ListExpression(ctx.elementValue().stream().map(this::visitElementValue).collect(Collectors.toList())), ctx);
    }

    @Override
    public String visitClassName(final ClassNameContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitIdentifier(final IdentifierContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitQualifiedName(final QualifiedNameContext ctx) {
        return ctx.qualifiedNameElement().stream()
                .map(ParseTree::getText)
                .collect(Collectors.joining(DOT_STR));
    }

    @Override
    public ClassNode visitAnnotatedQualifiedClassName(final AnnotatedQualifiedClassNameContext ctx) {
        ClassNode classNode = this.visitQualifiedClassName(ctx.qualifiedClassName());

        classNode.addTypeAnnotations(this.visitAnnotationsOpt(ctx.annotationsOpt()));

        return classNode;
    }

    @Override
    public ClassNode[] visitQualifiedClassNameList(final QualifiedClassNameListContext ctx) {
        if (!asBoolean(ctx)) {
            return ClassNode.EMPTY_ARRAY;
        }

        return ctx.annotatedQualifiedClassName().stream()
                .map(this::visitAnnotatedQualifiedClassName)
                .toArray(ClassNode[]::new);
    }

    @Override
    public ClassNode visitQualifiedClassName(final QualifiedClassNameContext ctx) {
        return this.createClassNode(ctx);
    }

    @Override
    public ClassNode visitQualifiedStandardClassName(final QualifiedStandardClassNameContext ctx) {
        return this.createClassNode(ctx);
    }

    private ClassNode createArrayType(final ClassNode elementType, List<List<AnnotationNode>> dimAnnotationsList) {
        ClassNode arrayType = elementType;
        for (int i = dimAnnotationsList.size() - 1; i >= 0; i -= 1) {
            arrayType = this.createArrayType(arrayType);
            arrayType.addAnnotations(dimAnnotationsList.get(i));
        }
        return arrayType;
    }

    private ClassNode createArrayType(final ClassNode elementType) {
        if (ClassHelper.isPrimitiveVoid(elementType)) {
            throw this.createParsingFailedException("void[] is an invalid type", elementType);
        }
        return elementType.makeArray();
    }

    private ClassNode createClassNode(final GroovyParserRuleContext ctx) {
        ClassNode result = makeClassNode(ctx.getText());
        if (isTrue(ctx, IS_INSIDE_INSTANCEOF_EXPR)) {
            // type in the "instanceof" expression shouldn't have redirect
        } else {
            result = this.proxyClassNode(result);
        }
        return configureAST(result, ctx);
    }

    private ClassNode proxyClassNode(final ClassNode classNode) {
        if (!classNode.isUsingGenerics()) {
            return classNode;
        }

        ClassNode cn = ClassHelper.makeWithoutCaching(classNode.getName());
        cn.setRedirect(classNode);
        return cn;
    }

    /**
     * Visit tree safely, no NPE occurred when the tree is null.
     *
     * @param tree an AST node
     * @return the visiting result
     */
    @Override
    public Object visit(final ParseTree tree) {
        if (!asBoolean(tree)) {
            return null;
        }

        return super.visit(tree);
    }

    // e.g. obj.a(1, 2) or obj.a 1, 2
    private MethodCallExpression createMethodCallExpression(final PropertyExpression propertyExpression, final Expression arguments) {
        MethodCallExpression methodCallExpression =
                new MethodCallExpression(
                        propertyExpression.getObjectExpression(),
                        propertyExpression.getProperty(),
                        arguments
                );

        methodCallExpression.setImplicitThis(false);
        methodCallExpression.setSafe(propertyExpression.isSafe());
        methodCallExpression.setSpreadSafe(propertyExpression.isSpreadSafe());

        // method call obj*.m(): "safe"(false) and "spreadSafe"(true)
        // property access obj*.p: "safe"(true) and "spreadSafe"(true)
        // so we have to reset safe here.
        if (propertyExpression.isSpreadSafe()) {
            methodCallExpression.setSafe(false);
        }

        // if the generics types metadata is not empty, it is a generic method call, e.g. obj.<Integer>a(1, 2)
        methodCallExpression.setGenericsTypes(
                propertyExpression.getNodeMetaData(PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES));

        return methodCallExpression;
    }

    // e.g. m(1, 2) or m 1, 2
    private MethodCallExpression createMethodCallExpression(final Expression baseExpr, final Expression arguments) {
        Expression thisExpr = new VariableExpression("this");
        thisExpr.setColumnNumber(baseExpr.getColumnNumber());
        thisExpr.setLineNumber(baseExpr.getLineNumber());

        return new MethodCallExpression(
                thisExpr,

                (baseExpr instanceof VariableExpression)
                        ? this.createConstantExpression(baseExpr)
                        : baseExpr,

                arguments
        );
    }

    private Parameter processFormalParameter(final GroovyParserRuleContext ctx, final VariableModifiersOptContext variableModifiersOptContext, final TypeContext typeContext, final TerminalNode ellipsis, final VariableDeclaratorIdContext variableDeclaratorIdContext, final ExpressionContext expressionContext) {
        ClassNode classNode = this.visitType(typeContext);

        if (asBoolean(ellipsis)) {
            classNode = this.createArrayType(classNode);
            if (!asBoolean(typeContext)) {
                configureAST(classNode, ellipsis);
            } else {
                configureAST(classNode, typeContext, configureAST(new ConstantExpression("..."), ellipsis));
            }
        }

        ModifierManager modifierManager = new ModifierManager(this, this.visitVariableModifiersOpt(variableModifiersOptContext));
        Parameter parameter =
                modifierManager
                        .processParameter(
                                configureAST(
                                        new Parameter(
                                                classNode,
                                                this.visitVariableDeclaratorId(variableDeclaratorIdContext).getName()
                                        ),
                                        ctx
                                )
                        );
        parameter.putNodeMetaData(PARAMETER_MODIFIER_MANAGER, modifierManager);
        parameter.putNodeMetaData(PARAMETER_CONTEXT, ctx);

        if (asBoolean(expressionContext)) {
            parameter.setInitialExpression((Expression) this.visit(expressionContext));
        }

        return parameter;
    }

    private Expression createPathExpression(final Expression primaryExpr, final List<? extends PathElementContext> pathElementContextList) {
        return (Expression) pathElementContextList.stream()
                .map(e -> (Object) e)
                .reduce(primaryExpr,
                        (r, e) -> {
                            PathElementContext pathElementContext = (PathElementContext) e;
                            pathElementContext.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR, r);
                            Expression expression = this.visitPathElement(pathElementContext);
                            if (isTrue((Expression) r, PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN)) {
                                expression.putNodeMetaData(PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN, Boolean.TRUE);
                            }
                            return expression;
                        }
                );
    }

    private GenericsType createGenericsType(final ClassNode classNode) {
        return configureAST(new GenericsType(classNode), classNode);
    }

    private ConstantExpression createConstantExpression(final Expression expression) {
        if (expression instanceof ConstantExpression) {
            return (ConstantExpression) expression;
        }

        return configureAST(new ConstantExpression(expression.getText()), expression);
    }

    private BinaryExpression createBinaryExpression(final ExpressionContext left, final Token op, final ExpressionContext right) {
        return new BinaryExpression((Expression) this.visit(left), this.createGroovyToken(op), (Expression) this.visit(right));
    }

    private BinaryExpression createBinaryExpression(final ExpressionContext left, final Token op, final ExpressionContext right, final ExpressionContext ctx) {
        BinaryExpression binaryExpression = this.createBinaryExpression(left, op, right);

        if (isTrue(ctx, IS_INSIDE_CONDITIONAL_EXPRESSION)) {
            return configureAST(binaryExpression, op);
        }

        return configureAST(binaryExpression, ctx);
    }

    private Statement unpackStatement(final Statement statement) {
        if (statement instanceof DeclarationListStatement) {
            List<ExpressionStatement> expressionStatementList = ((DeclarationListStatement) statement).getDeclarationStatements();

            if (1 == expressionStatementList.size()) {
                return expressionStatementList.get(0);
            }

            return configureAST(this.createBlockStatement(statement), statement); // if DeclarationListStatement contains more than 1 declarations, maybe it's better to create a block to hold them
        }

        return statement;
    }

    BlockStatement createBlockStatement(final Statement... statements) {
        return this.createBlockStatement(Arrays.asList(statements));
    }

    private BlockStatement createBlockStatement(final List<Statement> statementList) {
        return this.appendStatementsToBlockStatement(new BlockStatement(), statementList);
    }

    public BlockStatement appendStatementsToBlockStatement(final BlockStatement bs, final Statement... statements) {
        return this.appendStatementsToBlockStatement(bs, Arrays.asList(statements));
    }

    private BlockStatement appendStatementsToBlockStatement(final BlockStatement bs, final List<Statement> statementList) {
        return (BlockStatement) statementList.stream()
                .reduce(bs, (r, e) -> {
                    BlockStatement blockStatement = (BlockStatement) r;

                    if (e instanceof DeclarationListStatement) {
                        ((DeclarationListStatement) e).getDeclarationStatements().forEach(blockStatement::addStatement);
                    } else {
                        blockStatement.addStatement(e);
                    }

                    return blockStatement;
                });
    }

    private boolean isAnnotationDeclaration(final ClassNode classNode) {
        return asBoolean(classNode) && classNode.isAnnotationDefinition();
    }

    private boolean isSyntheticPublic(final boolean isAnnotationDeclaration, final boolean isAnonymousInnerEnumDeclaration, final boolean hasReturnType, final ModifierManager modifierManager) {
        if (modifierManager.containsVisibilityModifier()) {
            return false;
        }

        if (isAnnotationDeclaration) {
            return true;
        }

        if (hasReturnType && (modifierManager.containsAny(DEF, VAR))) {
            return true;
        }

        if (!hasReturnType || modifierManager.containsNonVisibilityModifier() || modifierManager.containsAnnotations()) {
            return true;
        }

        return isAnonymousInnerEnumDeclaration;
    }

    // the mixins of interface and annotation should be null
    private void hackMixins(final ClassNode classNode) {
        classNode.setMixins(null);
    }

    private static final Map<ClassNode, Object> TYPE_DEFAULT_VALUE_MAP = Maps.of(
            ClassHelper.int_TYPE, 0,
            ClassHelper.long_TYPE, 0L,
            ClassHelper.double_TYPE, 0.0D,
            ClassHelper.float_TYPE, 0.0F,
            ClassHelper.short_TYPE, (short) 0,
            ClassHelper.byte_TYPE, (byte) 0,
            ClassHelper.char_TYPE, (char) 0,
            ClassHelper.boolean_TYPE, Boolean.FALSE
    );

    private Object findDefaultValueByType(final ClassNode type) {
        return TYPE_DEFAULT_VALUE_MAP.get(type);
    }

    private boolean isPackageInfoDeclaration() {
        String name = this.sourceUnit.getName();
        return name != null && name.endsWith(PACKAGE_INFO_FILE_NAME);
    }

    private boolean isBlankScript() {
        return moduleNode.getStatementBlock().isEmpty() && moduleNode.getMethods().isEmpty() && moduleNode.getClasses().isEmpty();
    }

    private boolean isInsideParentheses(final NodeMetaDataHandler nodeMetaDataHandler) {
        Number insideParenLevel = nodeMetaDataHandler.getNodeMetaData(INSIDE_PARENTHESES_LEVEL);
        return insideParenLevel != null && insideParenLevel.intValue() > 0;
    }

    private boolean isBuiltInType(final Expression expression) {
        return (expression instanceof VariableExpression && isTrue(expression, IS_BUILT_IN_TYPE));
    }

    private org.codehaus.groovy.syntax.Token createGroovyTokenByType(final Token token, final int type) {
        if (token == null) {
            throw new IllegalArgumentException("token should not be null");
        }
        return new org.codehaus.groovy.syntax.Token(type, token.getText(), token.getLine(), token.getCharPositionInLine());
    }

    private org.codehaus.groovy.syntax.Token createGroovyToken(final Token token) {
        return this.createGroovyToken(token, 1);
    }

    private org.codehaus.groovy.syntax.Token createGroovyToken(final Token token, final int cardinality) {
        String tokenText = token.getText();
        int tokenType = token.getType();
        String text = 1 == cardinality ? tokenText : StringGroovyMethods.multiply(tokenText, cardinality);
        return new org.codehaus.groovy.syntax.Token(
                RANGE_EXCLUSIVE_FULL == tokenType || RANGE_EXCLUSIVE_LEFT == tokenType || RANGE_EXCLUSIVE_RIGHT == tokenType || RANGE_INCLUSIVE == tokenType
                        ? Types.RANGE_OPERATOR
                        : SAFE_INDEX == tokenType
                        ? Types.LEFT_SQUARE_BRACKET
                        : Types.lookup(text, Types.ANY),
                text,
                token.getLine(),
                token.getCharPositionInLine() + 1
        );
    }

    /**
     * Sets the script source position.
     */
    private void configureScriptClassNode() {
        var scriptClassNode = moduleNode.getScriptClassDummy();
        if (scriptClassNode != null) {
            List<Statement> statements = moduleNode.getStatementBlock().getStatements();
            if (!statements.isEmpty()) {
                Statement firstStatement = statements.get(0);
                scriptClassNode.setSourcePosition(firstStatement);
                Statement lastStatement  = statements.get(statements.size() - 1);
                scriptClassNode.setLastLineNumber(lastStatement.getLastLineNumber());
                scriptClassNode.setLastColumnNumber(lastStatement.getLastColumnNumber());
            }
        }
    }

    private String getOriginalText(final ParserRuleContext context) {
        return lexer.getInputStream().getText(Interval.of(context.getStart().getStartIndex(), context.getStop().getStopIndex()));
    }

    private static boolean isTrue(final NodeMetaDataHandler obj, final String key) {
        return Boolean.TRUE.equals(obj.getNodeMetaData(key));
    }

    private CompilationFailedException createParsingFailedException(final String msg, final GroovyParserRuleContext ctx) {
        return createParsingFailedException(
                new SyntaxException(msg,
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine() + 1,
                        ctx.stop.getLine(),
                        ctx.stop.getCharPositionInLine() + 1 + ctx.stop.getText().length()));
    }

    CompilationFailedException createParsingFailedException(final String msg, final Tuple2<Integer, Integer> start, final Tuple2<Integer, Integer> end) {
        return createParsingFailedException(
                new SyntaxException(msg,
                        start.getV1(),
                        start.getV2(),
                        end.getV1(),
                        end.getV2()));
    }

    CompilationFailedException createParsingFailedException(final String msg, final ASTNode node) {
        Objects.requireNonNull(node, "node passed into createParsingFailedException should not be null");

        return createParsingFailedException(
                new SyntaxException(msg,
                        node.getLineNumber(),
                        node.getColumnNumber(),
                        node.getLastLineNumber(),
                        node.getLastColumnNumber()));
    }

    private CompilationFailedException createParsingFailedException(final String msg, final TerminalNode node) {
        return createParsingFailedException(msg, node.getSymbol());
    }

    private CompilationFailedException createParsingFailedException(final String msg, final Token token) {
        return createParsingFailedException(
                new SyntaxException(msg,
                        token.getLine(),
                        token.getCharPositionInLine() + 1,
                        token.getLine(),
                        token.getCharPositionInLine() + 1 + token.getText().length()));
    }

    private CompilationFailedException createParsingFailedException(final Throwable t) {
        if (t instanceof SyntaxException) {
            this.collectSyntaxError((SyntaxException) t);
        } else if (t instanceof GroovySyntaxError) {
            GroovySyntaxError groovySyntaxError = (GroovySyntaxError) t;

            this.collectSyntaxError(
                    new SyntaxException(
                            groovySyntaxError.getMessage(),
                            groovySyntaxError,
                            groovySyntaxError.getLine(),
                            groovySyntaxError.getColumn()));
        } else if (t instanceof Exception) {
            this.collectException((Exception) t);
        }

        return new CompilationFailedException(
                CompilePhase.PARSING.getPhaseNumber(),
                this.sourceUnit,
                t);
    }

    private void collectSyntaxError(final SyntaxException e) {
        sourceUnit.getErrorCollector().addFatalError(new SyntaxErrorMessage(e, sourceUnit));
    }

    private void collectException(final Exception e) {
        sourceUnit.getErrorCollector().addException(e, this.sourceUnit);
    }

    private ANTLRErrorListener createANTLRErrorListener() {
        return new ANTLRErrorListener() {
            @Override
            public void syntaxError(final Recognizer recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
                collectSyntaxError(new SyntaxException(msg, line, charPositionInLine + 1));
            }
        };
    }

    private void removeErrorListeners() {
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
    }

    private void addErrorListeners() {
        lexer.removeErrorListeners();
        lexer.addErrorListener(this.createANTLRErrorListener());

        parser.removeErrorListeners();
        parser.addErrorListener(this.createANTLRErrorListener());
    }

    //--------------------------------------------------------------------------

    private static class DeclarationListStatement extends Statement {

        private final List<ExpressionStatement> declarationStatements;

        public DeclarationListStatement(DeclarationExpression... declarations) {
            this(Arrays.asList(declarations));
        }

        public DeclarationListStatement(List<DeclarationExpression> declarations) {
            this.declarationStatements =
                    declarations.stream()
                            .map(e -> configureAST(new ExpressionStatement(e), e))
                            .collect(Collectors.toList());
        }

        public List<ExpressionStatement> getDeclarationStatements() {
            List<String> declarationListStatementLabels = this.getStatementLabels();

            this.declarationStatements.forEach(e -> {
                if (null != declarationListStatementLabels) {
                    // clear existing statement labels before setting labels
                    if (null != e.getStatementLabels()) {
                        e.getStatementLabels().clear();
                    }

                    declarationListStatementLabels.forEach(e::addStatementLabel);
                }
            });

            return this.declarationStatements;
        }

        public List<DeclarationExpression> getDeclarationExpressions() {
            return this.declarationStatements.stream()
                    .map(e -> (DeclarationExpression) e.getExpression())
                    .collect(Collectors.toList());
        }
    }

    private final ModuleNode moduleNode;
    private final SourceUnit sourceUnit;
    private final GroovyLangLexer lexer;
    private final GroovyLangParser parser;
    private final GroovydocManager groovydocManager;
    private final TryWithResourcesASTTransformation tryWithResourcesASTTransformation;

    private final List<ClassNode> classNodeList = new ArrayList<>();
    private final Deque<ClassNode> classNodeStack = new ArrayDeque<>();
    private final Deque<List<InnerClassNode>> anonymousInnerClassesDefinedInMethodStack = new ArrayDeque<>();
    private final Deque<GroovyParserRuleContext> switchExpressionRuleContextStack = new ArrayDeque<>();

    private Tuple2<GroovyParserRuleContext, Exception> numberFormatError;

    private int visitingClosureCount;
    private int visitingLoopStatementCount;
    private int visitingSwitchStatementCount;
    private int visitingAssertStatementCount;
    private int visitingArrayInitializerCount;

    private static final int SLL_THRESHOLD = SystemUtil.getIntegerSafe("groovy.antlr4.sll.threshold", -1);

    private static final String QUESTION_STR = "?";
    private static final String DOT_STR = ".";
    private static final String SUB_STR = "-";
    private static final String ASSIGN_STR = "=";
    private static final String VALUE_STR = "value";
    private static final String DOLLAR_STR = "$";
    private static final String CALL_STR = "call";
    private static final String THIS_STR = "this";
    private static final String SUPER_STR = "super";
    private static final String VOID_STR = "void";
    private static final String SLASH_STR = "/";
    private static final String SLASH_DOLLAR_STR = "/$";
    private static final String TDQ_STR = "\"\"\"";
    private static final String TSQ_STR = "'''";
    private static final String SQ_STR = "'";
    private static final String DQ_STR = "\"";
    private static final String DOLLAR_SLASH_STR = "$/";

    private static final Map<String, String> QUOTATION_MAP = Maps.of(
            DQ_STR, DQ_STR,
            SQ_STR, SQ_STR,
            TDQ_STR, TDQ_STR,
            TSQ_STR, TSQ_STR,
            SLASH_STR, SLASH_STR,
            DOLLAR_SLASH_STR, SLASH_DOLLAR_STR
    );

    private static final String PACKAGE_INFO = "package-info";
    private static final String PACKAGE_INFO_FILE_NAME = PACKAGE_INFO + ".groovy";

    private static final String CLASS_NAME = "_CLASS_NAME";
    private static final String INSIDE_PARENTHESES_LEVEL = "_INSIDE_PARENTHESES_LEVEL";
    private static final String IS_INSIDE_INSTANCEOF_EXPR = "_IS_INSIDE_INSTANCEOF_EXPR";
    private static final String IS_SWITCH_DEFAULT = "_IS_SWITCH_DEFAULT";
    private static final String IS_NUMERIC = "_IS_NUMERIC";
    private static final String IS_STRING = "_IS_STRING";
    private static final String IS_INTERFACE_WITH_DEFAULT_METHODS = "_IS_INTERFACE_WITH_DEFAULT_METHODS";
    private static final String IS_INSIDE_CONDITIONAL_EXPRESSION = "_IS_INSIDE_CONDITIONAL_EXPRESSION";
    private static final String IS_COMMAND_EXPRESSION = "_IS_COMMAND_EXPRESSION";
    private static final String IS_BUILT_IN_TYPE = "_IS_BUILT_IN_TYPE";
    private static final String PATH_EXPRESSION_BASE_EXPR = "_PATH_EXPRESSION_BASE_EXPR";
    private static final String PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES = "_PATH_EXPRESSION_BASE_EXPR_GENERICS_TYPES";
    private static final String PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN = "_PATH_EXPRESSION_BASE_EXPR_SAFE_CHAIN";
    private static final String CMD_EXPRESSION_BASE_EXPR = "_CMD_EXPRESSION_BASE_EXPR";
    private static final String TYPE_DECLARATION_MODIFIERS = "_TYPE_DECLARATION_MODIFIERS";
    private static final String COMPACT_CONSTRUCTOR_DECLARATION_MODIFIERS = "_COMPACT_CONSTRUCTOR_DECLARATION_MODIFIERS";
    private static final String CLASS_DECLARATION_CLASS_NODE = "_CLASS_DECLARATION_CLASS_NODE";
    private static final String VARIABLE_DECLARATION_VARIABLE_TYPE = "_VARIABLE_DECLARATION_VARIABLE_TYPE";
    private static final String ANONYMOUS_INNER_CLASS_SUPER_CLASS = "_ANONYMOUS_INNER_CLASS_SUPER_CLASS";
    private static final String INTEGER_LITERAL_TEXT = "_INTEGER_LITERAL_TEXT";
    private static final String FLOATING_POINT_LITERAL_TEXT = "_FLOATING_POINT_LITERAL_TEXT";
    private static final String ENCLOSING_INSTANCE_EXPRESSION = "_ENCLOSING_INSTANCE_EXPRESSION";
    private static final String IS_YIELD_STATEMENT = "_IS_YIELD_STATEMENT";
    private static final String PARAMETER_MODIFIER_MANAGER = "_PARAMETER_MODIFIER_MANAGER";
    private static final String PARAMETER_CONTEXT = "_PARAMETER_CONTEXT";
    private static final String IS_RECORD_GENERATED = "_IS_RECORD_GENERATED";
    private static final String RECORD_HEADER = "_RECORD_HEADER";
    private static final String RECORD_TYPE_NAME = "groovy.transform.RecordType";
}
